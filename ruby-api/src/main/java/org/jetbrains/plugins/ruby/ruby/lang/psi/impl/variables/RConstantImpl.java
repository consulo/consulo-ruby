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

package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.variables;

import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.psi.PsiReference;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.language.util.IncorrectOperationException;
import consulo.navigation.ItemPresentation;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamContext;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamDef;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamDefReference;
import org.jetbrains.plugins.ruby.ruby.codeInsight.paramDefs.ParamDefUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.references.psi.RConstantReference;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.ResolveUtil;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.Type;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.Symbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.Context;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RTypeUtil;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.ConstantDefinitions;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RConstantHolder;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.expressions.RAssignmentExpressionNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.references.RReferenceNavigator;
import org.jetbrains.plugins.ruby.ruby.lang.psi.references.RReference;
import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RConstant;
import org.jetbrains.plugins.ruby.ruby.lang.psi.visitors.RubyElementVisitor;
import org.jetbrains.plugins.ruby.ruby.presentation.RConstantPresentationUtil;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 05.09.2006
 */
public class RConstantImpl extends RNamedElementBase implements RConstant
{
	private RConstantHolder myHolder;
	private static final String INTELLIJ_IDEA_RULEZZZ = "IntellijIdeaRulezzz";

	public RConstantImpl(ASTNode astNode)
	{
		super(astNode);
	}

	@Override
	public boolean isInDefinition()
	{
		return RAssignmentExpressionNavigator.getAssignmentByLeftPart(this) != null;
	}

	@Override
	@Nullable
	public ConstantDefinitions getConstantDefinitions()
	{
		return getHolder().getDefinition(this);
	}


	@Override
	public void accept(@Nonnull PsiElementVisitor visitor)
	{
		if(visitor instanceof RubyElementVisitor)
		{
			((RubyElementVisitor) visitor).visitRConstant(this);
			return;
		}
		super.accept(visitor);
	}

	@Override
	@Nonnull
	public RConstantHolder getHolder()
	{
		if(myHolder == null)
		{
			myHolder = PsiTreeUtil.getParentOfType(this, RConstantHolder.class);
		}
		assert myHolder != null;
		return myHolder;
	}

	@Nullable
	public Image getIcon(final int flags)
	{
		return RConstantPresentationUtil.getIcon();
	}

	@Override
	@Nullable
	public ItemPresentation getPresentation()
	{
		return RConstantPresentationUtil.getPresentation(this);
	}

	@Override
	protected PsiReference createReference()
	{
		String s = getText();
		if(s.equals(INTELLIJ_IDEA_RULEZZZ))
		{
			ParamContext paramContext = ParamDefUtil.getParamContext(this);
			if(paramContext != null)
			{
				ParamDef paramDef = ParamDefUtil.getParamDef(paramContext);
				if(paramDef != null)
				{
					return new ParamDefReference(this, paramDef, paramContext);
				}
			}
		}
		return new RConstantReference(this);
	}

	@Override
	public boolean isRealConstant()
	{
		if(isInDefinition())
		{
			return true;
		}
		final RReference ref = RReferenceNavigator.getReferenceByRightPart(this);
		final PsiReference psiRef = ref != null ? ref.getReference() : new RConstantReference(this);
		final Symbol symbol = ResolveUtil.resolveToSymbol(forceFileSymbolUpdate(), psiRef);
		return symbol != null && (symbol.getType() == Type.CONSTANT || symbol.getType() == Type.FOREIGN_FIELD);
	}

	@Override
	@Nullable
	protected String getPrefix()
	{
		return null;
	}

	@Override
	protected void checkName(@NonNls @Nonnull final String newName) throws IncorrectOperationException
	{
		if(!Character.isUpperCase(newName.charAt(0)) || !TextUtil.isCID(newName))
		{
			throw new IncorrectOperationException(RBundle.message("rename.incorrect.name"));
		}
	}

	@Override
	@Nonnull
	public RType getType(@Nullable final FileSymbol fileSymbol)
	{
		return RTypeUtil.createTypeBySymbol(fileSymbol, ResolveUtil.resolveToSymbol(fileSymbol, getReference()), Context.CLASS, true);
	}
}
