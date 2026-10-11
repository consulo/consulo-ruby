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
package org.jetbrains.plugins.ruby.ruby.lang.structure;

import java.util.ArrayList;
import java.util.List;

import consulo.annotation.access.RequiredReadAction;
import consulo.localize.LocalizeValue;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.RAliasStatement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.ConstantDefinitions;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.FieldDefinition;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.GlobalVarDefinition;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RConstantHolder;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RFieldHolder;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RGlobalVarHolder;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.visitors.RubyStructureVisitor;
import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.navigation.ItemPresentation;
import consulo.navigation.NavigationItem;
import consulo.ui.image.Image;
import org.jspecify.annotations.Nullable;

class RubyStructureViewElement implements StructureViewTreeElement {
    private RPsiElement myElement;

    public RubyStructureViewElement(RPsiElement element) {
        myElement = element;
    }

    @Override
    public StructureViewTreeElement[] getChildren() {
        List<RPsiElement> childrenElements = new ArrayList<>();

        RubyStructureVisitor myVisitor = new RubyStructureVisitor() {
            @Override
            public void visitRCall(RCall rCall) {
                // do nothing
            }

            @Override
            public void visitRAliasStatement(RAliasStatement rAliasStatement) {
                // do nothing
            }

            @Override
            public void visitContainer(RContainer rContainer) {
                childrenElements.addAll(rContainer.getStructureElements());


                // fields
                if (rContainer instanceof RFieldHolder) {
                    List<FieldDefinition> definitions = ((RFieldHolder) rContainer).getFieldsDefinitions();
                    for (FieldDefinition definition : definitions) {
                        childrenElements.add(definition.getFirstUsage());
                    }
                }

                // constants
                if (rContainer instanceof RConstantHolder) {
                    List<ConstantDefinitions> definitions = ((RConstantHolder) rContainer).getConstantDefinitions();
                    for (ConstantDefinitions definition : definitions) {
                        childrenElements.add(definition.getFirstDefinition());
                    }
                }

                // global variables
                if (rContainer instanceof RGlobalVarHolder) {
                    List<GlobalVarDefinition> definitions = ((RGlobalVarHolder) rContainer).getGlobalVarDefinitions();
                    for (GlobalVarDefinition definition : definitions) {
                        childrenElements.add(definition.getFirstDefinition());
                    }
                }

            }
        };

        myElement.accept(myVisitor);
        StructureViewTreeElement[] children = new StructureViewTreeElement[childrenElements.size()];
        for (int i = 0; i < children.length; i++) {
            children[i] = new RubyStructureViewElement(childrenElements.get(i));
        }

        return children;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override
            @RequiredReadAction
            public LocalizeValue getPresentableText() {
                return RubyPsiUtil.getPresentableName(myElement);
            }

            @Override
            @RequiredReadAction
            public @Nullable Image getIcon() {
                return RubyPsiUtil.getIcon(myElement);
            }

            @Override
            public @Nullable String getLocationString() {
                return null;
            }
        };
    }

    @Override
    public RPsiElement getValue() {
        return myElement;
    }

    @Override
    @RequiredReadAction
    public void navigate(boolean requestFocus) {
        ((NavigationItem) myElement).navigate(requestFocus);
    }

    @Override
    @RequiredReadAction
    public boolean canNavigate() {
        return ((NavigationItem) myElement).canNavigate();
    }

    @Override
    @RequiredReadAction
    public boolean canNavigateToSource() {
        return ((NavigationItem) myElement).canNavigateToSource();
    }
}

