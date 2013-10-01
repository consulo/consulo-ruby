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

package org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.stringLiterals;

import java.util.List;

import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RLiteral;
import com.intellij.psi.PsiElement;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 14.08.2006
 */
public interface RStringLiteral extends RLiteral
{
	/**
	 * @return string content
	 */
	public String getContent();

	/**
	 * @return true if there are at least one expression subtitution
	 */
	public boolean hasExpressionSubstitutions();

	/**
	 * @return List of all expression subtitutions
	 */
	public List<RExpressionSubstitution> getExpressionSubstitutions();

	public List<PsiElement> getPsiContent();
}
