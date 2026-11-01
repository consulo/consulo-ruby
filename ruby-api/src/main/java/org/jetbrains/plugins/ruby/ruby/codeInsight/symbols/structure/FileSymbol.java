/*
 * Copyright 2000-2008 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure;

import consulo.application.ReadAction;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.FileSymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.InterpretationMode;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.data.Children;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.data.LoadPath;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.data.Prototypes;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Oct 15, 2007
 */
public class FileSymbol {

    private final Project myProject;

    private final HashMap<Symbol, Children> myChildrenMap = new HashMap<Symbol, Children>();
    private final HashMap<Symbol, Prototypes> myPrototypesMap = new HashMap<Symbol, Prototypes>();

    private final LoadPath myLoadPath;

    private final Symbol myRootSymbol;
    private FileSymbol myBaseFileSymbol;

    @NonNls
    private static final String INDENT_STRING = "    ";
    @NonNls
    private static final String NEW_LINE = "\n";
    @NonNls
    private int listNumber;
    private boolean isInteropEnabled;

    public FileSymbol(@Nullable final FileSymbol baseFileSymbol, @Nonnull final Project project, final boolean interopEnabled) {
        myBaseFileSymbol = baseFileSymbol;
        myProject = project;
        isInteropEnabled = interopEnabled;
        if (myBaseFileSymbol != null) {
            myRootSymbol = myBaseFileSymbol.getRootSymbol();
            myLoadPath = new LoadPath(myBaseFileSymbol.getLoadPath());
        }
        else {
            // Create new rootSymbol and set it as rootSymbol
            myRootSymbol = new Symbol(this, null, Type.FILE, null, null);
            myRootSymbol.setRootSymbol(myRootSymbol);
            myLoadPath = new LoadPath(null);
        }
    }

    @Nonnull
    public Project getProject() {
        return myProject;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////// Symbols management
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void addChild(@Nonnull final Symbol parent, @Nonnull final Symbol child) {
        Children children = myChildrenMap.get(parent);
        if (children == null) {
            children = new Children(myBaseFileSymbol != null ? myBaseFileSymbol.getChildren(parent) : null);
            myChildrenMap.put(parent, children);
        }
        children.addSymbol(child);
    }

    public void setEmptyChildren(@Nonnull final Symbol parent) {
        myChildrenMap.put(parent, new Children(null));
    }

    public void addPrototype(@Nonnull final Symbol symbol, @Nonnull final RPsiElement prototype) {
        Prototypes prototypes = myPrototypesMap.get(symbol);
        if (prototypes == null) {
            prototypes = new Prototypes(myBaseFileSymbol != null ? myBaseFileSymbol.getVirtualPrototypes(symbol) : null);
            myPrototypesMap.put(symbol, prototypes);
        }
        prototypes.add(prototype);
    }

    /**
     * Returns all the children of given symbol
     *
     * @param symbol Symbol to get list of children for
     * @return list of children
     */
    @Nonnull
    Children getChildren(@Nonnull final Symbol symbol) {
        final Children myChildren = myChildrenMap.get(symbol);
        return myChildren != null ? myChildren : myBaseFileSymbol != null ? myBaseFileSymbol.getChildren(symbol) : Children.EMPTY_CHILDREN;
    }

    @Nonnull
    Prototypes getVirtualPrototypes(@Nonnull final Symbol symbol) {
        final Prototypes myPrototypes = myPrototypesMap.get(symbol);
        return myPrototypes != null ? myPrototypes : myBaseFileSymbol != null ? myBaseFileSymbol.getVirtualPrototypes(symbol) : Prototypes.EMPTY_PROTOTYPES;
    }

    @Nullable
    RPsiElement getLastVirualPrototype(@Nonnull final Symbol symbol) {
        final Prototypes myPrototypes = myPrototypesMap.get(symbol);
        return myPrototypes != null ? myPrototypes.getLast() : myBaseFileSymbol != null ? myBaseFileSymbol.getLastVirualPrototype(symbol) : null;
    }

    public Symbol getRootSymbol() {
        return myRootSymbol;
    }

    public boolean isInteropEnabled() {
        return isInteropEnabled;
    }

    public void process(@Nonnull final String url, @Nonnull final InterpretationMode mode, final boolean forceAdd) {
        final VirtualFile vFile = VirtualFileManager.getInstance().findFileByUrl(url);
        if (vFile == null) {
            return;
        }
        final PsiFile psiFile = ReadAction.compute(() -> PsiManager.getInstance(myProject).findFile(vFile));
        if (!(psiFile instanceof RFile)) {
            return;
        }
        final RFile file = (RFile) psiFile;

        if (!forceAdd && containsUrl(url)) {
            return;
        }
        // we shouldn`t add url if we`re in external mode
        if (mode != InterpretationMode.EXTERNAL) {
            synchronized (URLS_LOCK) {
                myUrls.add(url);
            }
        }
        // processing this url
        new SymbolBuilder(this, file, mode).process();
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////// Contained Urls
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Set<String> myUrls = new HashSet<String>();
    private final Object URLS_LOCK = new Object();


    private boolean containsUrl(@Nonnull final String url) {
        synchronized (URLS_LOCK) {
            return myUrls.contains(url) || myBaseFileSymbol != null && myBaseFileSymbol.containsUrl(url);
        }
    }

    @Nonnull
    public Set<String> getUrls() {
        final HashSet<String> all = new HashSet<String>();
        if (myBaseFileSymbol != null) {
            all.addAll(FileSymbolUtil.getUrls(myBaseFileSymbol));
        }
        synchronized (URLS_LOCK) {
            all.addAll(myUrls);
        }
        return all;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////// Prototype to Symbols
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final HashMap<RContainer, Symbol> myContainer2SymbolsMap = new HashMap<RContainer, Symbol>();

    public void registerContainerSymbol(@Nonnull final RContainer container, @Nonnull final Symbol symbol) {
        myContainer2SymbolsMap.put(container, symbol);
    }

    @Nullable
    public Symbol getSymbolForContainer(@Nullable final RContainer container) {
        final Symbol symbol = myContainer2SymbolsMap.get(container);
        return symbol != null ? symbol : myBaseFileSymbol != null ? myBaseFileSymbol.getSymbolForContainer(container) : null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////// List of visibleSymbols, allSymbols
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Children myAllSymbols;

    @Nullable
    public Children getAllSymbols() {
        return myAllSymbols;
    }

    public void setAllSymbols(@Nonnull final Children children) {
        myAllSymbols = children;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////// LOAD_PATH
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public LoadPath getLoadPath() {
        return myLoadPath;
    }

    public Set<VirtualFile> getLoadPathFiles() {
        return myLoadPath.getLoadPathFiles();
    }

    public void addLoadPathUrl(@Nonnull final String loadPathUrl) {
        myLoadPath.addLoadPathUrl(loadPathUrl);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////// Dump
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String dump() {
        final StringBuilder buffer = new StringBuilder();
        dump(getRootSymbol(), buffer, 0);
        return buffer.toString();
    }

    public void dump(@Nonnull final Symbol symbol, @Nonnull final StringBuilder buffer, final int indent) {
        for (int i = 0; i < indent; i++) {
            buffer.append(INDENT_STRING);
        }
        buffer.append(symbol.toString(this, false));
        buffer.append(NEW_LINE);
        for (Symbol child : getChildren(symbol).getAll()) {
            dump(child, buffer, indent + 1);
        }
        listNumber = 0;
    }

    public String dumpHTML() {
        final StringBuilder buffer = new StringBuilder();
        dumpHTML(getRootSymbol(), buffer);
        return "<html><body>" +
            "<script type=\"text/javascript\">\n" +
            "function hide(id) {\n" +
            " var x=document.getElementById(id);\n" +
            " if (x.style.visibility == \"visible\") {\n" +
            "  x.style.visibility = \"hidden\"\n" +
            " } else {\n" +
            "  x.style.visibility = \"visible\"\n" +
            " }\n" +
            "}\n" +
            "</script>" +
            buffer.toString() + "</html></body>";
    }

    @SuppressWarnings({"StringConcatenationInsideStringBufferAppend"})
    public void dumpHTML(@Nonnull final Symbol symbol, @Nonnull final StringBuilder buffer) {
        buffer.append(processSymbol(symbol));

        final Symbol parent = symbol.getParentSymbol();
        if (parent != null) {
            final long id = parent.getId();
            buffer.append(" <em><a href =\"#s" + id + "\">p:" + id + "</a></em> ");
        }

        final ArrayList<Symbol> children = new ArrayList<Symbol>();
        children.addAll(getChildren(symbol).getAll());
        if (!children.isEmpty()) {
            listNumber++;
            buffer.append("<a href=\"#\" onclick=\"javascript:hide('list" + listNumber + "');\">+-</a>");
            buffer.append("<div id=\"list" + listNumber + "\">");
        }

        buffer.append("<ul>");
        for (Symbol child : children) {
            buffer.append("<li>");
            dumpHTML(child, buffer);
            buffer.append("</li>");
        }
        buffer.append("</ul>");
    }

    private String processSymbol(Symbol symbol) {
        final String text = symbol.toString(this, true);
        final StringBuffer buffer = new StringBuffer();
        final Pattern SYMBOL_ID = Pattern.compile("\\[\\d+\\]");
        final Matcher matcher = SYMBOL_ID.matcher(text);
        boolean seen = false;
        while (matcher.find()) {
            final String matched = matcher.group(0);
            final String id = matched.substring(1, matched.length() - 1);
            if (!seen) {
                matcher.appendReplacement(buffer, "<em><a name =\"s" + id + "\">" + id + "</a></em>");
                seen = true;
            }
            else {
                matcher.appendReplacement(buffer, "<em><a href =\"#s" + id + "\">" + id + "</a></em>");
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
