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

package org.jetbrains.plugins.ruby.jruby.inspections;

import java.util.List;

import consulo.application.progress.ProgressManager;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.LastSymbolStorage;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.inspections.RubyInspectionVisitor;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.classes.RClass;
import consulo.language.editor.inspection.ProblemsHolder;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.RubyOverrideImplementUtil;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Feb 29, 2008
 */
public class JRubyImplementInterfaceVisitor extends RubyInspectionVisitor
{

	public JRubyImplementInterfaceVisitor(final ProblemsHolder holder)
	{
		super(holder);
	}

	@Override
	public void visitRClass(final RClass rClass)
	{
		// It`s often operation
		ProgressManager.getInstance().checkCanceled();

		final FileSymbol fileSymbol = LastSymbolStorage.getInstance(rClass.getProject()).getSymbol();
		final Symbol symbol = SymbolUtil.getSymbolByContainer(fileSymbol, rClass);
		if(symbol != null)
		{
			final List<Symbol> methods = RubyOverrideImplementUtil.getMethodsToImplement(fileSymbol, symbol);
			if(!methods.isEmpty())
			{
				final String message = RBundle.message("inspection.implement.interface.class.should.implement.method") + methods.get(0).getName();
				//noinspection ConstantConditions
				registerProblem(rClass.getClassName(), message, new JRubyImplementInterfaceFix(rClass.getLastChild()));
			}
		}
	}
}
