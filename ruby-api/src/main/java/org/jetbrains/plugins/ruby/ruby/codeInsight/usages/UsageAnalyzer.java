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

package org.jetbrains.plugins.ruby.ruby.codeInsight.usages;

import consulo.ruby.interop.RubyInterop;
import consulo.ruby.interop.RubyInteropProvider;
import consulo.language.psi.PsiElement;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.LastSymbolStorage;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.RubyOverrideImplementUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.usages.impl.*;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.blocks.RRescueBlock;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.ArgumentInfo;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RArgument;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RArgumentList;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RAssignmentExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RSelfAssignmentExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.blocks.RRescueBlockNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.methods.RArgumentListNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.methods.arguments.RArgumentNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.expressions.RAssignmentExpressionNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.expressions.RSelfAssingmentExpressionNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.iterators.RBlockVariableNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.methodCall.RCallNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.references.RReferenceNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.iterators.RBlockVariables;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.references.RReference;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RConstant;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RFid;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: 06.05.2007
 */
public class UsageAnalyzer
{

	public static Access createUsageAccess(@Nonnull final RPsiElement usage)
	{
		// Rescue block
		if(usage instanceof RIdentifier)
		{
			final RRescueBlock rescueBlock = RRescueBlockNavigator.getByParameter((RIdentifier) usage);
			if(rescueBlock != null)
			{
				return new RescueBlockAccessImpl(rescueBlock, usage);
			}
		}

		// Overriden method argument
		if(usage instanceof RIdentifier && ((RIdentifier) usage).isMethodParameter())
		{
			final RContainer container = usage.getParentContainer();
			if(container instanceof RMethod)
			{
				final RArgument argument = RArgumentNavigator.getByRIdentifier((RIdentifier) usage);
				assert argument != null;
				if(argument.getType() == ArgumentInfo.Type.SIMPLE)
				{
					final RArgumentList list = RArgumentListNavigator.getByArgument(argument);
					if(list != null)
					{
						final int number = list.getArgNumber(argument);
						if(number != -1)
						{
							// We can say precisely only if all prev arguments are simple arguments
							boolean nonSimpleArgSeen = false;
							for(ArgumentInfo argumentInfo : list.getArgumentInfos().subList(0, number))
							{
								if(argumentInfo.getType() != ArgumentInfo.Type.SIMPLE)
								{
									nonSimpleArgSeen = true;
								}
							}
							if(!nonSimpleArgSeen)
							{
								final FileSymbol fileSymbol = LastSymbolStorage.getInstance(usage.getProject()).getSymbol();
								final Symbol methodSymbol = SymbolUtil.getSymbolByContainer(fileSymbol, container);
								if(methodSymbol != null)
								{
									final List<Symbol> overridenSymbols = RubyOverrideImplementUtil.getOverridenSymbols(fileSymbol, methodSymbol);
									// At first we look for implemented methods, then for overriden foreign methods
									final List<Object> foreignMethods = new ArrayList<Object>(RubyOverrideImplementUtil.getImplementedForeignMethods(overridenSymbols));
									foreignMethods.addAll(RubyOverrideImplementUtil.getOverridenElements(fileSymbol, methodSymbol, null, overridenSymbols));
									for(Object element : foreignMethods)
									{
										final RubyInteropProvider provider = element instanceof PsiElement foreignElement ? RubyInterop.forElement(foreignElement) : null;
										if(provider != null && number < provider.getParameterCount((PsiElement) element))
										{
											return null;
										}
									}
								}
							}
						}
					}
				}
			}
		}

		if(usage instanceof RIdentifier)
		{
			final RIdentifier identifier = (RIdentifier) usage;
			// Block Parameter
			if(identifier.isBlockParameter())
			{
				return new BlockParameterAccess()
				{
					@Override
					@Nonnull
					public RPsiElement getElement()
					{
						return identifier;
					}

					@Override
					@Nonnull
					public RBlockVariables getBlockVariables()
					{
						return RBlockVariableNavigator.getByIdentifier(identifier);
					}
				};
			}

			// Method Parameter
			if(identifier.isMethodParameter())
			{
				return new MethodParameterAccess()
				{
					@Override
					@Nonnull
					public RPsiElement getElement()
					{
						return identifier;
					}

					@Override
					@Nonnull
					public RArgument getArgument()
					{
						//noinspection ConstantConditions
						return RArgumentNavigator.getByRIdentifier(identifier);
					}
				};
			}
		}

		// Assignment
		final RAssignmentExpression assignment = RAssignmentExpressionNavigator.getAssignmentByLeftPart(usage);
		if(assignment != null)
		{
			return new AssignAccessImpl(assignment, usage);
		}

		// Self assignment
		final RSelfAssignmentExpression selfAssignment = RSelfAssingmentExpressionNavigator.getSelfAssignmentByLeftPart(usage);
		if(selfAssignment != null)
		{
			return new SelfAssignAccessImpl(selfAssignment, usage);
		}

		// References
		final RReference reference = RReferenceNavigator.getReferenceByLeftPart(usage);
		if(reference != null)
		{
			final RPsiElement value = reference.getValue();

			// Field write access
			if(value instanceof RConstant || value instanceof RIdentifier)
			{
				final RAssignmentExpression refAssign = RAssignmentExpressionNavigator.getAssignmentByLeftPart(reference);
				if(refAssign != null)
				{
					return new FieldWriteAccessImpl(value, reference, usage);
				}
			}

			// Constant, i.e. variable.CONSTANT
			if(value instanceof RConstant)
			{
				return new ConstantAccessImpl(value, reference, usage);

			}

			// method_call, i.e. variable.methodName
			if(value instanceof RIdentifier || value instanceof RFid)
			{
				final RCall commandCall = RCallNavigator.getByCommand(reference);
				return new CallAccessImpl(value, commandCall, reference, usage);
			}

		}

		return new ReadAccess()
		{
			@Override
			@Nonnull
			public RPsiElement getElement()
			{
				return usage;
			}
		};
	}

}
