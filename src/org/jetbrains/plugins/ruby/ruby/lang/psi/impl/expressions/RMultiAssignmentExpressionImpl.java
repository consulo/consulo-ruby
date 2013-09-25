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
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RListOfExpressions;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RMultiAssignmentExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.visitors.RubyElementVisitor;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 03.07.2006
 */
public class RMultiAssignmentExpressionImpl extends RAssignmentExpressionImpl implements RMultiAssignmentExpression {

    public RMultiAssignmentExpressionImpl(ASTNode astNode) {
        super(astNode);
    }

    @Override
	public boolean isMultiObject() {
        return getObject() instanceof RListOfExpressions;
    }

    @Override
	public boolean isMultiValue() {
        return getValue() instanceof RListOfExpressions;
    }

    @Override
	public void accept(@NotNull final PsiElementVisitor visitor){
        if (visitor instanceof RubyElementVisitor){
            ((RubyElementVisitor) visitor).visitRMultiAssignmentExpression(this);
            return;
        }
        super.accept(visitor);
    }

    @Override
	@NotNull
    public RType getType(@Nullable final FileSymbol fileSymbol) {
        return isMultiValue() ? RType.NOT_TYPED : super.getType(fileSymbol);
    }
}
