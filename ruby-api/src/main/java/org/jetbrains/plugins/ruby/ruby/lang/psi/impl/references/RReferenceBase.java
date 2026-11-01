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

package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.references;

import java.util.Collections;
import java.util.List;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElementVisitor;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;

import org.jetbrains.plugins.ruby.ruby.codeInsight.references.ForeignClassReference;
import org.jetbrains.plugins.ruby.ruby.codeInsight.references.NewReference;
import org.jetbrains.plugins.ruby.ruby.codeInsight.references.RQualifiedReference;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.ResolveUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Types;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RTypeUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.TypeInferenceHelper;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.methods.RMethod;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.RPsiElementBase;
import org.jetbrains.plugins.ruby.ruby.lang.psi.references.RReference;
import org.jetbrains.plugins.ruby.ruby.lang.psi.visitors.RubyElementVisitor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: 08.05.2007
 */
public abstract class RReferenceBase extends RPsiElementBase implements RReference
{
	@NonNls
	private static final String GET_INSTANCE = "getInstance";
	@NonNls
	private static final String INSTANCE = "instance";

	public RReferenceBase(ASTNode astNode)
	{
		super(astNode);
	}

	@Override
	public void accept(@Nonnull PsiElementVisitor visitor)
	{
		if(visitor instanceof RubyElementVisitor)
		{
			((RubyElementVisitor) visitor).visitRReference(this);
			return;
		}
		super.accept(visitor);
	}

	@Override
	public RPsiElement getValue()
	{
		return PsiTreeUtil.getNextSiblingOfType(getDelimiter(), RPsiElement.class);
	}

	@Override
	public RPsiElement getReciever()
	{
		return PsiTreeUtil.getPrevSiblingOfType(getDelimiter(), RPsiElement.class);
	}

	@Nonnull
	public abstract PsiElement getDelimiter();

	@Override
	@Nullable
	public RQualifiedReference getReference()
	{
		final RPsiElement reciever = getReciever();
		final RPsiElement value = getValue();
		if(reciever == null || value == null)
		{
			return null;
		}
		if(RMethod.NEW.equals(value.getText()))
		{
			return new NewReference(getProject(), this, reciever, value);
		}
		if(isJavaClassCall())
		{
			return new ForeignClassReference(getProject(), this, reciever, value);
		}
		return new RQualifiedReference(getProject(), this, reciever, value, getType());
	}

	@Override
	@Nonnull
	public RType getType(@Nullable final FileSymbol fileSymbol)
	{
		if(isConstructorLike())
		{
			final RPsiElement reciever = getReciever();
			final List<Symbol> symbols = ResolveUtil.resolveToSymbols(reciever);
			// If it`s constructor like, we should return instance type
			if(symbols.size() == 1)
			{
				return RTypeUtil.createTypeBySymbol(fileSymbol, symbols.get(0), Context.INSTANCE, true);
			}
			else
			{
				return RType.NOT_TYPED;
			}
		}

		// If it`s constructor like, we should return instance type
		final Symbol symbol = ResolveUtil.resolveToSymbol(fileSymbol, getReference());
		if(symbol == null)
		{
			return RType.NOT_TYPED;
		}
		final org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type type = symbol.getType();
		if(Types.METHODS.contains(type))
		{
			final TypeInferenceHelper helper = TypeInferenceHelper.getInstance(getProject());
			helper.testAndSet(fileSymbol);
			return helper.inferCallTypeBySymbol(symbol, Collections.<RPsiElement>emptyList());
		}

		// If we can resolve to module, class, java_class or java_proxy_class we expect class context
		final Context context = Types.MODULE_OR_CLASS.contains(type) ? Context.CLASS : Context.INSTANCE;
		return RTypeUtil.createTypeBySymbol(fileSymbol, symbol, context, true);
	}

	public boolean isConstructorLike()
	{
		final RPsiElement value = getValue();
		if(value != null)
		{
			final String text = value.getText();
			return RMethod.NEW.equals(text) || GET_INSTANCE.equals(text) || INSTANCE.equals(text);
		}
		return false;
	}

	public boolean isJavaClassCall()
	{
		final RPsiElement value = getValue();
		if(value != null)
		{
			final String text = value.getText();
			return RMethod.JAVA_CLASS.equals(text);
		}
		return false;
	}
}