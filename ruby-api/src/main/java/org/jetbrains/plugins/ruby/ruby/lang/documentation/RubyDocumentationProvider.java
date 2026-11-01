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

package org.jetbrains.plugins.ruby.ruby.lang.documentation;

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import consulo.language.editor.documentation.LanguageDocumentationProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiManager;
import consulo.project.Project;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.ResolveUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.LastSymbolStorage;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.RubyOverrideImplementUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Types;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.ForeignSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.lang.RubyLanguage;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RVirtualPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Apr 22, 2007
 */
@ExtensionImpl
public class RubyDocumentationProvider implements LanguageDocumentationProvider {
    @Nonnull
    @Override
    public Language getLanguage() {
        return RubyLanguage.INSTANCE;
    }

    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement element2) {
        return RubyHelpUtil.getShortDescription(element, false);
    }

    @Override
    @Nullable
    public java.util.List<java.lang.String> getUrlFor(PsiElement element, PsiElement originalElement) {
        return Collections.emptyList();
    }

    @Override
    @Nullable
    public String generateDoc(@Nullable final PsiElement element, @Nullable final PsiElement originalElement) {
        return element instanceof RPsiElement ? RubyHelpUtil.getHelpByElement((RPsiElement) element) : null;
    }

    @Override
    @Nullable
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        return null;
    }

    @Override
    @SuppressWarnings({"ConstantConditions"})
    @Nullable
    public PsiElement getDocumentationElementForLink(@Nonnull final PsiManager psiManager, @Nonnull final String link, @Nonnull final PsiElement element) {
        final Project project = psiManager.getProject();
        final FileSymbol fileSymbol = LastSymbolStorage.getInstance(project).getSymbol();
        try {
            if (link.startsWith(RubyHelpUtil.SYMBOL)) {
                final Integer index = Integer.valueOf(link.substring(RubyHelpUtil.SYMBOL.length()));
                final List<Symbol> symbols = ResolveUtil.resolveToSymbols(element);
                final Symbol symbol = symbols.get(index);
                // Java symbols handling
                if (Types.FOREIGN.contains(symbol.getType())) {
                    return ((ForeignSymbol) symbol).getPsiElement();
                }
                final RPsiElement prototype = symbol.getLastVirtualPrototype(fileSymbol);
                return prototype != null ? RVirtualPsiUtil.findPsiByVirtualElement(prototype, project) : null;
            }

            if (link.startsWith(RubyHelpUtil.ELEMENT)) {
                final Integer index = Integer.valueOf(link.substring(RubyHelpUtil.ELEMENT.length()));
                final Symbol symbol = element instanceof RContainer ? SymbolUtil.getSymbolByContainer(fileSymbol, (RContainer) element) : ResolveUtil.resolveToSymbols(element).get(0);
                final Object overridenElem = RubyOverrideImplementUtil.getOverridenElements(fileSymbol, symbol, null).get(index);
                return overridenElem instanceof RPsiElement ? RVirtualPsiUtil.findPsiByVirtualElement((RPsiElement) overridenElem, project) : null;
            }
        }
        catch (Exception e) {
            return null;
        }
        return null;
    }
}
