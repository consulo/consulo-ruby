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
package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.methods;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.language.ast.ASTNode;
import consulo.language.ast.TokenSet;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.NamedStub;
import consulo.language.util.IncorrectOperationException;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.cache.psi.StructureType;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RStructuralElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RVirtualPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.AccessModifier;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.blocks.RBodyStatement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.blocks.RCompoundStatement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.ArgumentInfo;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RArgumentList;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.names.RMethodName;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders.RContainerBase;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders.utils.RContainerUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.visitors.RubyElementVisitor;
import org.jetbrains.plugins.ruby.ruby.presentation.RMethodPresentationUtil;
import org.jetbrains.plugins.ruby.ruby.presentation.RPresentationConstants;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * @author oleg
 * @since 2006-06-11
 */
public class RMethodImpl<T extends NamedStub<? extends RMethod>> extends RContainerBase<T> implements RMethod {
    private static final TokenSet TS_ARGUMENT_LISTS = TokenSet.create(
        RubyElementTypes.FUNCTION_ARGUMENT_LIST,
        RubyElementTypes.COMMAND_ARGUMENT_LIST
    );
    private boolean isClassConstructor;

    public RMethodImpl(ASTNode astNode) {
        super(astNode);
        updateIfIsConstructor();
    }

    public RMethodImpl(@Nonnull T stub, @Nonnull IStubElementType nodeType) {
        super(stub, nodeType);
        updateIfIsConstructor();
    }

    @Override
    public @Nullable String getName() {
        T stub = getGreenStub();
        if (stub != null) {
            return stub.getName();
        }
        return super.getName();
    }

    private void updateIfIsConstructor() {
        if (INITIALIZE.equals(getName())) {
            if (isClassConstructor) {
                return;
            }
            if (isClassConstructor = RContainerUtil.belongsToRContainer(this, StructureType.CLASS)) {
                super.setAccessModifier(AccessModifier.PRIVATE);
            }
        }
        else {
            isClassConstructor = false;
        }
    }

    @Override
    @Nonnull
    public AccessModifier getAccessModifier() {
        updateIfIsConstructor();
        if (isClassConstructor) {
            return AccessModifier.PRIVATE;
        }
        return super.getAccessModifier();
    }


    @Override
    public void setAccessModifier(AccessModifier modifier) {
        updateIfIsConstructor();
        if (isClassConstructor) {
            return;
        }
        super.setAccessModifier(modifier);
    }

    @Override
    @Nullable
    public RArgumentList getArgumentList() {
        PsiElement argList = getChildByFilter(TS_ARGUMENT_LISTS, 0);
        return argList != null ? (RArgumentList) argList : null;
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof RubyElementVisitor) {
            ((RubyElementVisitor) visitor).visitRMethod(this);
            return;
        }
        super.accept(visitor);
    }

    @Override
    @Nonnull
    public ItemPresentation getPresentation() {
        return RMethodPresentationUtil.getPresentation(this);
    }

    @Nullable
    public Image getIcon(int flags) {
        return RMethodPresentationUtil.getIcon(this, flags);
    }

    @Override
    @Nullable
    public RMethodName getMethodName() {
        return getChildByType(RMethodName.class, 0);
    }

    @Override
    @Nonnull
    public List<ArgumentInfo> getArgumentInfos() {
        RArgumentList argumentList = getArgumentList();
        if (argumentList == null) {
            return Collections.emptyList();
        }
        return argumentList.getArgumentInfos();
    }

    @Override
    public boolean isConstructor() {
        updateIfIsConstructor();
        return isClassConstructor;
    }

    @Override
    public boolean equalsToVirtual(@Nonnull RStructuralElement element) {
        return element instanceof RMethod &&
            getArgumentInfos().equals(((RMethod) element).getArgumentInfos()) &&
            super.equalsToVirtual(element);
    }

    /**
     * Method assumes that both method have equal parent container
     *
     * @param otherMethod other method (virtual or psi)
     * @return true if methods equals.
     */
    @Override
    public boolean equalsToMethod(@Nonnull RMethod otherMethod) {
        return RVirtualPsiUtil.areMethodsEqual(this, otherMethod);
    }

    @Override
    @Nonnull
    public LocalizeValue getPresentableName() {
        int options = RPresentationConstants.SHOW_NAME | RPresentationConstants.SHOW_PARAMETERS;
        return LocalizeValue.of(RMethodPresentationUtil.formatName(this, options));
    }

    @Override
    @Nonnull
    public String getPresentableName(boolean includeDefaultArgs) {
        int options = RPresentationConstants.SHOW_NAME | RPresentationConstants.SHOW_PARAMETERS;
        if (includeDefaultArgs) {
            options |= RPresentationConstants.SHOW_INITIALIZER;
        }
        return RMethodPresentationUtil.formatName(this, options);
    }

    @Override
    @RequiredReadAction
    public int getTextOffset() {
        RMethodName methodName = getMethodName();
        return methodName != null ? methodName.getTextOffset() : super.getTextOffset();
    }

    @Override
    @RequiredWriteAction
    public PsiElement setName(@Nonnull String name) throws IncorrectOperationException {
        return null;
    }

    @Override
    @RequiredReadAction
    public StructureType getType() {
        return StructureType.METHOD;
    }

    @Override
    protected RPsiElement getNameElement() {
        return getMethodName();
    }

    @Override
    @Nonnull
    @RequiredReadAction
    public RCompoundStatement getCompoundStatement() {
        RBodyStatement body = RubyPsiUtil.getChildByType(this, RBodyStatement.class, 0);
        assert body != null;
        //noinspection ConstantConditions
        return RubyPsiUtil.getChildByType(body, RCompoundStatement.class, 0);
    }
}
