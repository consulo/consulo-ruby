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

package org.jetbrains.plugins.ruby.ruby.lang.psi.impl;

import consulo.content.bundle.Sdk;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.file.FileViewProvider;
import consulo.language.impl.psi.PsiFileImpl;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiFile;
import consulo.language.psi.stub.StubElement;
import consulo.module.Module;
import consulo.navigation.ItemPresentation;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import consulo.ruby.interop.RubyInterop;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RRequire;
import org.jetbrains.plugins.ruby.ruby.cache.psi.StructureType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.scope.RootScope;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.scope.ScopeBuilder;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.FileSymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.TypeInferenceContext;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.TypeInferenceHelper;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RStructuralElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RVirtualPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.stubs.RubyFileStub;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlFlow.Instruction;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlFlow.impl.RControlFlowBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.AccessModifier;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.blocks.RCompoundStatement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.ConstantDefinitions;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.FieldDefinition;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.GlobalVarDefinition;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders.utils.*;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RConstant;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.fields.RField;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.global.RGlobalVariable;
import org.jetbrains.plugins.ruby.ruby.lang.psi.visitors.RubyElementVisitor;
import org.jetbrains.plugins.ruby.ruby.presentation.RFilePresentationUtil;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik, oleg
 * @date: 02.04.2007
 */

public abstract class RPsiFileBase extends PsiFileImpl implements RFile {

    protected List<RStructuralElement> myStructureElements;
    private Instruction[] myControlFlow;

    protected RPsiFileBase(IElementType elementType, IElementType contentElementType, FileViewProvider provider) {
        super(elementType, contentElementType, provider);
    }

    public RPsiFileBase(final FileViewProvider viewProvider) {
        super(viewProvider);
    }

    @Override
    @Nonnull
    public String getContainingFileUrl() {
        // a completion or injection copy has no virtual file of its own: it stands for the original one
        VirtualFile file = getOriginalFile().getVirtualFile();
        if (file == null) {
            file = getViewProvider().getVirtualFile();
        }
        return file.getUrl();
    }

    @Override
    @Nonnull
    public AccessModifier getAccessModifier() {
        return AccessModifier.PUBLIC;
    }

    @Override
    @Nullable
    public RContainer getParentContainer() {
        return null;
    }

    @Override
    @Nullable
    public RContainer getVirtualParentContainer() {
        return getParentContainer();
    }

    @Override
    @Nonnull
    public AccessModifier getDefaultChildAccessModifier() {
        return AccessModifier.PUBLIC;
    }


    @Override
    public synchronized void subtreeChanged() {
        clearMyCaches();
        super.subtreeChanged();
    }

    @Override
    @Nonnull
    public FileType getFileType() {
        return getViewProvider().getVirtualFile().getFileType();
    }

    private void clearMyCaches() {
        myStructureElements = null;
        myConstantDefinitions = null;
        myGlobalVarDefinitions = null;
        myFieldDefinitions = null;
        myScope = null;

        // Clear control flow and inference info
        final TypeInferenceContext context = TypeInferenceHelper.getInstance(getProject()).getContext();
        if (context != null) {
            context.localVariablesTypesCache.remove(this);
        }
        myControlFlow = null;
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof RubyElementVisitor) {
            ((RubyElementVisitor) visitor).visitRFile(this);
            return;
        }
        visitor.visitFile(this);
    }


    @Override
    @Nonnull
    public RCompoundStatement getCompoundStatement() {
        //noinspection ConstantConditions
        return getChildByType(RCompoundStatement.class, 0);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //// RPsiElement methods implementation
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    @Nonnull
    public List<PsiElement> getChildrenByFilter(IElementType filter) {
        return RubyPsiUtil.getChildrenByFilter(this, filter);
    }

    @Override
    @Nullable
    public PsiElement getChildByFilter(TokenSet filter, int number) {
        return RubyPsiUtil.getChildByFilter(this, filter, number);
    }

    @Override
    @Nullable
    public PsiElement getChildByFilter(IElementType filter, int number) {
        return RubyPsiUtil.getChildByFilter(this, filter, number);
    }

    @Override
    @Nonnull
    public <T extends PsiElement> List<T> getChildrenByType(Class<T> c) {
        return RubyPsiUtil.getChildrenByType(this, c);
    }

    @Override
    @Nullable
    public <T extends PsiElement> T getChildByType(Class<T> c, int number) {
        return RubyPsiUtil.getChildByType(this, c, number);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //// RContainer methods implementation
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    @Nonnull
    public final List<RStructuralElement> getStructureElements() {
        if (myStructureElements == null) {
            myStructureElements = RContainerUtil.getStructureElements(this);
        }
        return myStructureElements;
    }

    @Override
    @Nonnull
    public List<RConstant> getVirtualConstants() {
        final List<RConstant> result = new ArrayList<RConstant>();
        for (ConstantDefinitions def : getConstantDefinitions()) {
            result.add(def.getFirstDefinition());
        }
        return result;
    }

    @Override
    @Nonnull
    public List<RField> getVirtualFields() {
        final List<RField> result = new ArrayList<RField>();
        for (FieldDefinition def : getFieldsDefinitions()) {
            result.add(def.getFirstUsage());
        }
        return result;
    }

    @Override
    @Nonnull
    public List<RGlobalVariable> getVirtualGlobalVars() {
        final List<RGlobalVariable> result = new ArrayList<RGlobalVariable>();
        for (GlobalVarDefinition def : getGlobalVarDefinitions()) {
            result.add(def.getFirstDefinition());
        }
        return result;
    }

    @Override
    @Nonnull
    public List<RRequire> getRequires() {
        final List<RRequire> requires = new ArrayList<RRequire>();
        collectRequires(this, requires);
        return requires;
    }

    @Override
    @Nonnull
    public List<String> getRequiredUrls() {
        final StubElement<?> stub = getGreenStub();
        if (stub instanceof RubyFileStub) {
            return ((RubyFileStub) stub).getRequiredUrls();
        }
        final List<String> urls = new ArrayList<String>();
        for (RRequire require : getRequires()) {
            urls.addAll(require.getNames());
        }
        return urls;
    }

    private static void collectRequires(final PsiElement element, final List<RRequire> sink) {
        for (PsiElement child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof RRequire && ((RRequire) child).getType() == StructureType.CALL_REQUIRE) {
                sink.add((RRequire) child);
                continue;
            }
            collectRequires(child, sink);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //// Fields, constantHolder, globalVarHolder
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Cached information about constant usages
    private List<ConstantDefinitions> myConstantDefinitions;
    // Cached information about globalVariables
    private List<GlobalVarDefinition> myGlobalVarDefinitions;
    // Cached information for FieldUsages
    private List<FieldDefinition> myFieldDefinitions;

    @Override
    @Nonnull
    public List<FieldDefinition> getFieldsDefinitions() {
        if (myFieldDefinitions == null) {
            myFieldDefinitions = RFieldHolderUtil.gatherFieldDescriptions(this);
        }
        return myFieldDefinitions;
    }

    @Override
    @Nullable
    public FieldDefinition getDefinition(@Nonnull final RField field) {
        return RFieldHolderUtil.getDefinition(this, field);
    }

    @Override
    @Nonnull
    public List<ConstantDefinitions> getConstantDefinitions() {
        if (myConstantDefinitions == null) {
            myConstantDefinitions = RConstantHolderUtil.gatherConstantDefinitions(this);
        }
        return myConstantDefinitions;
    }

    @Override
    @Nullable
    public ConstantDefinitions getDefinition(@Nonnull final RConstant constant) {
        return RConstantHolderUtil.getDefinition(this, constant);
    }

    @Override
    @Nonnull
    public List<GlobalVarDefinition> getGlobalVarDefinitions() {
        if (myGlobalVarDefinitions == null) {
            myGlobalVarDefinitions = RGlobalVarHolderUtil.gatherGlobalVarDefinitions(this);
        }
        return myGlobalVarDefinitions;
    }

    @Override
    @Nullable
    public GlobalVarDefinition getDefinition(@Nonnull RGlobalVariable globalVar) {
        return RGlobalVarHolderUtil.getDefinition(this, globalVar);
    }

    @Override
    @Nullable
    public String getPresentableLocation() {
        final VirtualFile file = getVirtualFile();
        assert file != null;
        return RubySdkUtil.getPresentableLocation(getSdk(), file.getUrl());
    }

    @Override
    public StructureType getType() {
        return StructureType.FILE;
    }

    @Override
    @Nonnull
    public List<RStructuralElement> getVirtualStructureElements() {
        // I do really hate JAVA Type system with covariant typing!!!
        final ArrayList<RStructuralElement> elements = new ArrayList<RStructuralElement>();
        for (RStructuralElement element : getStructureElements()) {
            elements.add(element);
        }
        return elements;
    }

    @Override
    @Nonnull
    public List<String> getFullPath() {
        return Collections.emptyList();
    }

    @Override
    @Nonnull
    public String getFullName() {
        return "foo";
    }

    @Override
    public boolean isGlobal() {
        return false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //// RPsiFile methods implementation
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    @Nonnull
    public ItemPresentation getPresentation() {
        return RFilePresentationUtil.getPresentation(this);
    }

    @Override
    @Nullable
    public Module getModule() {
        // a completion copy has no file of its own: the module is the original file's
        final VirtualFile file = getOriginalFile().getVirtualFile();
        return file != null ? RFileUtil.getModule(getProject(), file) : null;
    }

    @Override
    public boolean isInteropEnabled() {
        final Module module = getModule();
        return module != null && RubyInterop.isEnabled(module);
    }

    @Override
    @Nullable
    public Sdk getSdk() {
        final VirtualFile file = getVirtualFile();
        return file != null ? RFileUtil.getSdk(getProject(), file) : null;
    }


    @Override
    public int getIndexOf(@Nonnull RStructuralElement element) {
        final List<RStructuralElement> structuralElements = getStructureElements();
        for (int i = 0; i < structuralElements.size(); i++) {
            if (element == structuralElements.get(i)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    @Nullable
    public FileSymbol getFileSymbol() {
        return FileSymbolUtil.getFileSymbol(this);
    }

    @Override
    public boolean equalsToVirtual(@Nonnull final RStructuralElement element) {
        // TODO: to be honest, we must add another 2 check!
        // RVPsiUtuils.areConstantHoldersEqual and RVPsiUtuils.areFieldHoldersEqual
        return element instanceof RFile && RVirtualPsiUtil.areSubStructureEqual(this, (RFile) element);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// ScopeHolder
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // scope for scopeHolder
    private RootScope myScope;

    @Override
    @Nonnull
    public synchronized RootScope getScope() {
        if (myScope == null) {
            myScope = ScopeBuilder.buildScope(this);
        }
        return myScope;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///// ControlFlowOwner
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public Instruction[] getControlFlow() {
        if (myControlFlow == null) {
            myControlFlow = new RControlFlowBuilder().buildControlFlow(null, this, null, null);
        }
        return myControlFlow;
    }
}