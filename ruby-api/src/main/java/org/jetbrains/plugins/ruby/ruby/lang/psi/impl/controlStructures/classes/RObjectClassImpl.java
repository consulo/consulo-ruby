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
package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.classes;

import consulo.annotation.access.RequiredReadAction;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.stub.IStubElementType;
import consulo.localize.LocalizeValue;
import consulo.navigation.ItemPresentation;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.cache.psi.StructureType;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RObjectClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RClassObject;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders.RFieldConstantContainerImpl;
import org.jetbrains.plugins.ruby.ruby.lang.psi.stubs.RubyObjectClassStub;
import org.jetbrains.plugins.ruby.ruby.lang.psi.visitors.RubyElementVisitor;
import org.jetbrains.plugins.ruby.ruby.presentation.RObjectClassPresentationUtil;

/**
 * @author oleg
 * @since 2006-06-11
 */
public class RObjectClassImpl extends RFieldConstantContainerImpl<RubyObjectClassStub> implements RObjectClass {
    public RObjectClassImpl(ASTNode astNode) {
        super(astNode);
    }

    public RObjectClassImpl(@Nonnull RubyObjectClassStub stub, @Nonnull IStubElementType nodeType) {
        super(stub, nodeType);
    }

    @Override
    @Nonnull
    public ItemPresentation getPresentation() {
        return RObjectClassPresentationUtil.getPresentation(this);
    }

    public Image getIcon(int flags) {
        return RObjectClassPresentationUtil.getIcon();
    }

    @Override
    @Nonnull
    public LocalizeValue getPresentableName() {
        return LocalizeValue.of("<<" + getFullName());
    }

    @Override
    @Nullable
    public RClassObject getObject() {
        PsiElement result = RubyPsiUtil.getChildByFilter(this, RubyElementTypes.CLASS_OBJECT, 0);
        return result != null ? (RClassObjectImpl) result : null;
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof RubyElementVisitor elementVisitor) {
            elementVisitor.visitRObjectClass(this);
            return;
        }
        super.accept(visitor);
    }

    @Override
    @RequiredReadAction
    public StructureType getType() {
        return StructureType.OBJECT_CLASS;
    }

    @Override
    protected RPsiElement getNameElement() {
        return getObject();
    }
}
