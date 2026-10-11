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
package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures;

import consulo.annotation.access.RequiredReadAction;
import consulo.localize.LocalizeValue;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RStructuralElement;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElementVisitor;
import consulo.navigation.ItemPresentation;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.jetbrains.plugins.ruby.ruby.cache.psi.StructureType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.RSymbol;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.RAliasStatement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.RPsiElementBase;
import org.jetbrains.plugins.ruby.ruby.lang.psi.visitors.RubyElementVisitor;
import org.jetbrains.plugins.ruby.ruby.presentation.RAliasPresentationUtil;
import consulo.language.psi.stub.StubElement;

/**
 * @author oleg
 * @since 2006-06-18
 */
public class RAliasStatementImpl extends RPsiElementBase<StubElement> implements RAliasStatement {
    public RAliasStatementImpl(ASTNode astNode) {
        super(astNode);
    }

    @Override
    public void accept(@Nonnull PsiElementVisitor visitor) {
        if (visitor instanceof RubyElementVisitor elemVisitor) {
            elemVisitor.visitRAliasStatement(this);
            return;
        }
        super.accept(visitor);
    }

    @Override
    @Nullable
    public RPsiElement getPsiOldName() {
        return getChildByType(RPsiElement.class, 1);
    }

    @Override
    @Nullable
    public RPsiElement getPsiNewName() {
        return getChildByType(RPsiElement.class, 0);
    }

    @Override
    @Nonnull
    @RequiredReadAction
    public String getOldName() {
        return getName(getPsiOldName());
    }

    @Override
    @Nonnull
    @RequiredReadAction
    public String getNewName() {
        return getName(getPsiNewName());
    }

    @Override
    @RequiredReadAction
    public StructureType getType() {
        return StructureType.ALIAS;
    }

    @Override
    @Nonnull
    @RequiredReadAction
    public LocalizeValue getPresentableText() {
        return LocalizeValue.of(getText());
    }

    @Nonnull
    @RequiredReadAction
    private static String getName(RPsiElement element) {
        if (element != null) {
            if (element instanceof RSymbol symbol) {
                return symbol.getObject().getText();
            }
            return element.getText();
        }
        return "";
    }

    @Override
    @RequiredReadAction
    public boolean equalsToVirtual(@Nonnull RStructuralElement element) {
        return element instanceof RAliasStatement alias
            && getNewName().equals(alias.getNewName())
            && getOldName().equals(alias.getOldName());
    }

    @Override
    public ItemPresentation getPresentation() {
        return RAliasPresentationUtil.getPresentation(this);
    }
}
