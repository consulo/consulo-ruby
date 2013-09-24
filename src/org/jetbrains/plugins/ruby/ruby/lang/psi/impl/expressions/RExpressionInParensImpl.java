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

package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.expressions;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RExpressionInParens;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.RPsiElementBase;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 07.06.2006
 */
public class RExpressionInParensImpl extends RPsiElementBase implements RExpressionInParens {
    public RExpressionInParensImpl(ASTNode astNode) {
        super(astNode);
    }

    @Nullable
    public RPsiElement getExpression() {
        return getChildByType(RPsiElement.class, 0);
    }

    @NotNull
    public RType getType(@Nullable final FileSymbol fileSymbol) {
        final RPsiElement expression = getExpression();
        return expression instanceof RExpression ? ((RExpression) expression).getType(fileSymbol) : RType.NOT_TYPED;
    }
}
