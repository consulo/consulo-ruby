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
package org.jetbrains.plugins.ruby.ruby.lang.findUsages;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.Language;
import jakarta.annotation.Nonnull;

import consulo.language.findUsage.FindUsagesProvider;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.lang.RubyLanguage;
import org.jetbrains.plugins.ruby.ruby.lang.RubyWordsScanner;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RObjectClass;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RConstant;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RIdentifier;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.fields.RField;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.global.RGlobalVariable;
import consulo.language.cacheBuilder.WordsScanner;
import consulo.language.psi.PsiElement;

/**
 * @author oleg
 * @since 2007-03-09
 */
@ExtensionImpl
public class RubyFindUsagesProvider implements FindUsagesProvider {
    private RubyWordsScanner rubyWordsScanner;

    @Override
    @Nullable
    public WordsScanner getWordsScanner() {
        if (rubyWordsScanner == null) {
            rubyWordsScanner = new RubyWordsScanner();
        }
        return rubyWordsScanner;
    }

    @Override
    public boolean canFindUsagesFor(@Nonnull PsiElement psiElement) {
        // we can find containers except RObjectClass
        if (psiElement instanceof RContainer && !(psiElement instanceof RObjectClass)) {
            return true;
        }

        if (psiElement instanceof RField) {
            return true;
        }
        if (psiElement instanceof RConstant constant) {
            return constant.isInDefinition();
        }
        if (psiElement instanceof RGlobalVariable) {
            return true;
        }
        if (psiElement instanceof RIdentifier id) {
            return id.isParameter() || id.isLocalVariable();
        }
        return false;
    }

    @Override
    @Nullable
    public String getHelpId(@Nonnull PsiElement psiElement) {
        return null;
    }

    @Override
    @Nonnull
    public String getType(@Nonnull PsiElement psiElement) {
        if (psiElement instanceof RIdentifier id) {
            if (id.isParameter()) {
                return RBundle.message("parameter");
            }
            if (id.isLocalVariable()) {
                return RBundle.message("local.variable");
            }
        }
        return psiElement.toString();
    }

    @Override
    @Nonnull
    @RequiredReadAction
    public String getDescriptiveName(@Nonnull PsiElement element) {
        return RubyPsiUtil.getPresentableName(element).get();
    }

    @Override
    @Nonnull
    @RequiredReadAction
    public String getNodeText(@Nonnull PsiElement element, boolean useFullName) {
        return RubyPsiUtil.getPresentableName(element).get();
    }

    @Nonnull
    @Override
    public Language getLanguage() {
        return RubyLanguage.INSTANCE;
    }
}
