/*
 * Copyright 2000-2007 JetBrains s.r.o.
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
package org.jetbrains.plugins.ruby.ruby.lang.psi.dataFlow.types;

import consulo.application.progress.ProgressManager;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.codeInsight.references.RPsiPolyvariantReference;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.CallSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.SymbolUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.CoreTypes;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RTypeUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.impl.MessageImpl;
import org.jetbrains.plugins.ruby.ruby.codeInsight.usages.*;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlFlow.Instruction;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlFlow.ReadWriteInstruction;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.ArgumentInfo;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RArgument;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RPredefinedArgument;
import org.jetbrains.plugins.ruby.ruby.lang.psi.dataFlow.DfaInstance;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RConstant;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RIdentifier;

import java.util.HashMap;
import java.util.Map;

/**
 * @author oleg
 */
public class TypeDfaInstance implements DfaInstance<Map<String, RType>>
{
	public final Map<RIdentifier, RType> localVariablesMap;
	protected FileSymbol myFileSymbol;

	public TypeDfaInstance(final FileSymbol fileSymbol, final Map<RIdentifier, RType> map)
	{
		myFileSymbol = fileSymbol;
		localVariablesMap = map;
	}

	@Override
	public void fun(final Map<String, RType> map, final Instruction instruction)
	{
		ProgressManager.getInstance().checkCanceled();

		// we process only instructions with our local variables
		if(instruction instanceof ReadWriteInstruction)
		{
			final Access access = ((ReadWriteInstruction) instruction).getAccess();
			final RIdentifier id = (RIdentifier) access.getElement();

			final String name = id.getName();
			RType oldType = map.get(name);
			if(oldType == null)
			{
				oldType = RType.NOT_TYPED;
			}
			if(access instanceof MethodParameterAccess)
			{
				final RArgument argument = ((MethodParameterAccess) access).getArgument();
				final ArgumentInfo.Type argType = argument.getType();
				if(argType == ArgumentInfo.Type.ARRAY)
				{
					final RType type = RTypeUtil.createTypeBySymbol(myFileSymbol, SymbolUtil.getTopLevelClassByName(myFileSymbol, CoreTypes.Array), Context.INSTANCE, true);
					localVariablesMap.put(id, type);
				}
				if(argType == ArgumentInfo.Type.BLOCK)
				{
					final RType type = RTypeUtil.createTypeBySymbol(myFileSymbol, SymbolUtil.getTopLevelClassByName(myFileSymbol, CoreTypes.Proc), Context.INSTANCE, true);
					localVariablesMap.put(id, type);
				}
				if(argType == ArgumentInfo.Type.PREDEFINED)
				{
					// if it alredy contains value, we ignore predefined value
					if(!localVariablesMap.containsKey(id))
					{
						final RPsiElement value = ((RPredefinedArgument) argument).getValue();
						final RType type = value instanceof RExpression ? ((RExpression) value).getType(myFileSymbol) : RType.NOT_TYPED;
						localVariablesMap.put(id, type);
					}
				}

				// And here we just take type from localVariablesMap
				final RType type = localVariablesMap.get(id);
				if(type != null)
				{
					map.put(name, type);
				}
			}

			// a ||= smth
			else if(access instanceof SelfAssignAccess)
			{
				final RPsiElement value = ((SelfAssignAccess) access).getValue();
				if(value != null)
				{
					if(value instanceof RExpression)
					{
						final RType type = ((RExpression) value).getType(myFileSymbol);
						// here we join types!!!
						map.put(name, RTypeUtil.joinOr(oldType, type));
					}
				}
			}
			// a = smth
			else if(access instanceof AssignAccess)
			{
				final RPsiElement value = ((AssignAccess) access).getValue();
				if(value != null)
				{
					if(value instanceof RExpression)
					{
						final RType type = ((RExpression) value).getType(myFileSymbol);
						map.put(name, type);
					}
					else
					{
						map.put(name, RType.NOT_TYPED);
					}
				}
			}

			// rescue Something here => usage
			else if(access instanceof RescueBlockAccess)
			{
				final RPsiElement exception = ((RescueBlockAccess) access).getTypeElement();
				if(exception != null)
				{
					if(exception instanceof RExpression)
					{
						final RType type = ((RExpression) exception).getType(myFileSymbol);
						map.put(name, type);
					}
				}
			}

			// a.variable = smth
			else if(access instanceof FieldWriteAccess)
			{
				final RPsiElement ref = ((FieldWriteAccess) access).getFullReference();
				final RPsiElement field = ((FieldWriteAccess) access).getField();
				if(field.getCopyableUserData(RPsiPolyvariantReference.REFERENCE_BEING_COMPLETED) == null)
				{
					final String text = field.getText() + "=";
					// if such a field writer can be resolved, we don`t add it manually
					if(oldType.getMessagesForName(text).isEmpty())
					{
						final RType type = oldType.addMessage(new MessageImpl(text, 0, true, new CallSymbol(text, Type.FIELD_WRITE_ACCESS, ref)));
						map.put(name, type);
					}
				}
			}

			// a.CONSTANT
			else if(access instanceof ConstantAccess)
			{
				final RPsiElement ref = ((ConstantAccess) access).getFullReference();
				final RConstant constant = ((ConstantAccess) access).getConstant();
				if(constant.getCopyableUserData(RPsiPolyvariantReference.REFERENCE_BEING_COMPLETED) == null)
				{
					final String text = constant.getText();
					// if such a constant can be resolved, we don`t add it manually
					if(oldType.getMessagesForName(text).isEmpty())
					{
						final RType type = oldType.addMessage(new MessageImpl(text, 0, true, new CallSymbol(text, Type.CONSTANT_ACCESS, ref)));
						map.put(name, type);
					}
				}
			}

			// a.foo
			else if(access instanceof CallAccess)
			{
				final RPsiElement ref = ((CallAccess) access).getFullReference();
				final CallAccess callAccess = (CallAccess) access;
				final RPsiElement call = callAccess.getCall();
				if(call.getCopyableUserData(RPsiPolyvariantReference.REFERENCE_BEING_COMPLETED) == null)
				{
					final String text = call.getText();
					// if such a method call can be resolved, we don`t add it manually
					if(oldType.getMessagesForName(text).isEmpty())
					{
						final RType type = oldType.addMessage(new MessageImpl(text, callAccess.getNumberOfArgs(), true, new CallSymbol(text, Type.CALL_ACCESS, ref)));
						map.put(name, type);
					}
				}
			}

			// Add type to localVariablesMap
			final RType type = map.get(id.getName());
			if(type != null)
			{
				localVariablesMap.put(id, type);
			}
		}
	}

	@Override
	@Nonnull
	public Map<String, RType> initial()
	{
		return new HashMap<String, RType>();
	}

	@Override
	public boolean isForward()
	{
		return true;
	}
}
