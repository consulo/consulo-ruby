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

package consulo.jruby.lang.searcher;

import com.intellij.java.language.psi.PsiMethod;
import java.util.function.Predicate;
import consulo.language.psi.PsiElement;
import consulo.language.psi.search.TextOccurenceProcessor;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.RubyOverrideImplementUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.ForeignSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RVirtualPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.names.RMethodName;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.names.RNameNavigator;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Mar 18, 2008
 */
public class JRubyImplementingMethodsProcessor implements TextOccurenceProcessor
{
	protected PsiMethod myMethod;
	protected String myName;
	protected Predicate<? super PsiElement> myConsumer;

	public JRubyImplementingMethodsProcessor(final PsiMethod method, final String name, final Predicate<? super PsiElement> consumer)
	{
		myMethod = method;
		myName = name;
		myConsumer = consumer;
	}

	@Override
	public boolean execute(final PsiElement element, final int offsetInElement)
	{
		if(element instanceof RPsiElement && RNameNavigator.getRName(element) instanceof RMethodName)
		{
			final RFile rFile = RubyPsiUtil.getRFile(element);
			if(rFile == null)
			{
				return true;
			}
			final RContainer container = ((RPsiElement) element).getParentContainer();
			if(container == null)
			{
				return true;
			}
			final RContainer virtualContainer = RVirtualPsiUtil.findVirtualContainer(container);
			if(virtualContainer == null)
			{
				return true;
			}
			final FileSymbol fileSymbol = rFile.getFileSymbol();
			if(fileSymbol == null)
			{
				return true;
			}
			final Symbol symbol = SymbolUtil.getSymbolByContainer(fileSymbol, virtualContainer);
			if(symbol == null)
			{
				return true;
			}
			final List elements = RubyOverrideImplementUtil.getImplementedForeignMethods(RubyOverrideImplementUtil.getOverridenSymbols(fileSymbol, symbol));
			for(Object o : elements)
			{
				if(o instanceof Symbol && ((Symbol) o).getType() == Type.FOREIGN_METHOD)
				{
					if(myMethod == ((ForeignSymbol) o).getPsiElement())
					{
						myConsumer.test(container);
						break;
					}
				}
			}
		}
		return true;
	}
}