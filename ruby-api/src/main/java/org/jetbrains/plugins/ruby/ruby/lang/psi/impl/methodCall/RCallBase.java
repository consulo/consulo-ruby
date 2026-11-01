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

package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.methodCall;

import org.jetbrains.plugins.ruby.ruby.lang.psi.RStructuralElement;

import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.navigation.ItemPresentation;
import consulo.virtualFileSystem.VirtualFile;
import consulo.ruby.interop.RubyInterop;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RExtend;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RImportJavaClass;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RInclude;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RIncludeJavaClass;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RIncludeJavaPackage;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RLoad;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RRequire;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RVirtualName;
import org.jetbrains.plugins.ruby.ruby.cache.psi.StructureType;
import org.jetbrains.plugins.ruby.ruby.cache.psi.variables.FieldAttrType;
import org.jetbrains.plugins.ruby.ruby.cache.psi.variables.RFieldAttr;
import org.jetbrains.plugins.ruby.ruby.codeInsight.references.GemReference;
import org.jetbrains.plugins.ruby.ruby.codeInsight.references.RFieldAttrReference;
import org.jetbrains.plugins.ruby.ruby.codeInsight.references.RFileReference;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.RType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.TypeInferenceHelper;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.basicTypes.stringLiterals.RBaseString;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RBinaryExpression;
import org.jetbrains.plugins.ruby.ruby.lang.psi.expressions.RListOfExpressions;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RFieldHolder;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.RPsiElementBase;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders.utils.RFileUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RubyCallType;
import org.jetbrains.plugins.ruby.ruby.presentation.ForeignImportPresentationUtil;
import org.jetbrains.plugins.ruby.ruby.presentation.RFieldAttrPresentationUtil;
import consulo.language.ast.ASTNode;
import consulo.language.psi.stub.StubElement;
import consulo.util.lang.Comparing;
import consulo.language.psi.PsiReference;
import consulo.ui.image.Image;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 14.08.2006
 */
public abstract class RCallBase extends RPsiElementBase<StubElement> implements RPsiElement, RCall, RRequire, RLoad, RInclude, RExtend, RFieldAttr, RImportJavaClass, RIncludeJavaPackage
{
	public RCallBase(ASTNode astNode)
	{
		super(astNode);
	}


	@Override
	@Nonnull
	public RubyCallType getCallType()
	{
		return RCallBaseUtil.getCallType(this);
	}

	@Override
	@Nonnull
	public PsiElement getPsiCommand()
	{
		final PsiElement command = getFirstChild();
		assert command != null : "Cannot find command";
		return command;
	}

	@Override
	@Nonnull
	public PsiReference[] getReferences()
	{
		final RubyCallType callType = getCallType();

		// Adding required or loaded elements references
		final List<RPsiElement> arguments = getCallArguments().getElements();
		if(callType.isFileRef())
		{
			List<PsiReference> refs = new ArrayList<PsiReference>();
			for(RPsiElement argument : arguments)
			{
				// add variants for File.dirname(__FILE__) + 'some string here'
				if(argument instanceof RBinaryExpression)
				{
					final RBinaryExpression expr = (RBinaryExpression) argument;
					final RPsiElement right = expr.getRightOperand();
					if(expr.getOperationType() == RubyTokenTypes.tPLUS &&
							RFileUtil.FILE_DIRNAME.equals(expr.getLeftOperand().getText()) &&
							right instanceof RBaseString)
					{
						refs.add(new RFileReference(this, argument, right, true));
					}
				}
				else
				{
					refs.add(new RFileReference(this, argument, argument, false));
				}
			}
			return refs.toArray(new PsiReference[refs.size()]);
		}

		// Adding references from attributes to fields
		if(callType.isAttributeCall())
		{
			final RFieldHolder holder = PsiTreeUtil.getParentOfType(this, RFieldHolder.class);
			assert holder != null;
			List<PsiReference> refs = new ArrayList<PsiReference>();
			for(RPsiElement argument : arguments)
			{
				refs.add(new RFieldAttrReference(this, argument, holder));
			}
			return refs.toArray(new PsiReference[refs.size()]);
		}

		// Include_class and include_package references
		if(callType == RubyCallType.INCLUDE_CLASS_CALL || callType == RubyCallType.INCLUDE_PACKAGE_CALL)
		{
			final List<RPsiElement> args = getArguments();
			if(args.size() == 1)
			{
				final RPsiElement arg = args.get(0);
				if(arg instanceof RBaseString)
				{
					final RBaseString string = (RBaseString) arg;
					final List<PsiReference> refs = new ArrayList<PsiReference>();
					RubyInterop.forEach(provider -> refs.addAll(provider.createStringReferences(this, string)));
					return refs.toArray(new PsiReference[refs.size()]);
				}
			}
		}

		// Gems references
		if(callType.isGemCall())
		{
			if(arguments.size() >= 1)
			{
				final RPsiElement gemName = arguments.get(0);
				if(gemName instanceof RBaseString)
				{
					return new PsiReference[]{new GemReference(this, gemName)};
				}
			}
		}
		return PsiReference.EMPTY_ARRAY;
	}

	@Override
	@Nonnull
	public RListOfExpressions getCallArguments()
	{
		//noinspection ConstantConditions
		return getChildByType(RListOfExpressions.class, 0);
	}

	@Override
	@Nonnull
	public String getCommand()
	{
		return getPsiCommand().getText();
	}

	@Override
	@Nonnull
	public List<RPsiElement> getArguments()
	{
		return getCallArguments().getElements();
	}


	@Override
	public StructureType getType()
	{
		return RCallBaseUtil.getType(this);
	}

	@Override
	@Nonnull
	public FieldAttrType getFieldAttrType()
	{
		final FieldAttrType type = RCallBaseUtil.getFieldAttrType(this);
		return type != null ? type : FieldAttrType.ATTR_ACCESSOR;
	}

	@Override
	public boolean equalsToVirtual(@Nonnull RStructuralElement element)
	{
		final StructureType myType = getType();
		if(myType == StructureType.FAKE)
		{
			return false;
		}
		if(myType != element.getType())
		{
			return false;
		}
		if(myType == StructureType.CALL_REQUIRE)
		{
			if(!(element instanceof RRequire))
			{
				return false;
			}
			final RRequire require = (RRequire) element;
			return require.getNames().equals(RCallBaseUtil.gatherUrls(this, getContainingFile().getVirtualFile()));
		}
		if(myType == StructureType.CALL_LOAD)
		{
			if(!(element instanceof RLoad))
			{
				return false;
			}
			final RLoad load = (RLoad) element;
			return load.getNames().equals(RCallBaseUtil.gatherUrls(this, getContainingFile().getVirtualFile()));
		}
		if(myType == StructureType.CALL_INCLUDE)
		{
			if(!(element instanceof RInclude))
			{
				return false;
			}
			final RInclude include = (RInclude) element;
			return include.getVirtualNames().equals(RCallBaseUtil.gatherVirtualNames(this));
		}
		if(myType == StructureType.CALL_EXTEND)
		{
			if(!(element instanceof RExtend))
			{
				return false;
			}
			final RExtend extend = (RExtend) element;
			return extend.getVirtualNames().equals(RCallBaseUtil.gatherVirtualNames(this));
		}
		if(myType == StructureType.FIELD_ATTR_CALL)
		{
			if(!(element instanceof RFieldAttr))
			{
				return false;
			}
			final RFieldAttr fieldAttr = (RFieldAttr) element;
			return fieldAttr.getFieldAttrType() == getFieldAttrType() && fieldAttr.getNames().equals(RCallBaseUtil.gatherStrings(this));
		}
		if(myType == StructureType.CALL_IMPORT)
		{
			if(!(element instanceof RImportJavaClass))
			{
				return false;
			}
			final RImportJavaClass importtt = (RImportJavaClass) element;
			return importtt.getVirtualNames().equals(RCallBaseUtil.gatherVirtualNames(this));
		}
		if(myType == StructureType.CALL_INCLUDE_CLASS)
		{
			if(!(element instanceof RIncludeJavaClass))
			{
				return false;
			}
			final RIncludeJavaClass include = (RIncludeJavaClass) element;
			return Comparing.equal(RCallBaseUtil.getJavaQualifiedName(this), include.getQualifiedName());
		}
		if(myType == StructureType.CALL_INCLUDE_PACKAGE)
		{
			if(!(element instanceof RIncludeJavaPackage))
			{
				return false;
			}
			final RIncludeJavaPackage include = (RIncludeJavaPackage) element;
			return Comparing.equal(RCallBaseUtil.getJavaQualifiedName(this), include.getQualifiedName());
		}
		return false;
	}

	@Nullable
	public Image getIcon(final int flags)
	{
		final FieldAttrType type = getFieldAttrType();
		if(type != null)
		{
			return RFieldAttrPresentationUtil.getAttrIcon(type);
		}

		if(getCallType() == RubyCallType.IMPORT_CALL ||
				getCallType() == RubyCallType.INCLUDE_CLASS_CALL ||
				getCallType() == RubyCallType.INCLUDE_PACKAGE_CALL)
		{
			return ForeignImportPresentationUtil.getIncludeIcon();
		}
		return null;
	}

	@Override
	public ItemPresentation getPresentation()
	{
		final StructureType type = getType();
		if(type == StructureType.FIELD_ATTR_CALL)
		{
			return RFieldAttrPresentationUtil.getPresentation(this);
		}
		if(getCallType() == RubyCallType.IMPORT_CALL ||
				type == StructureType.CALL_INCLUDE_CLASS ||
				type == StructureType.CALL_INCLUDE_PACKAGE)
		{
			return ForeignImportPresentationUtil.getIncludeJavaPresentation(this);
		}
		return null;
	}

	@Override
	@Nonnull
	public RType getType(@Nullable final FileSymbol fileSymbol)
	{
		final TypeInferenceHelper helper = TypeInferenceHelper.getInstance(getProject());
		helper.testAndSet(fileSymbol);
		return helper.inferCallType(this);
	}

	@Override
	@Nonnull
	public List<String> getNames()
	{
		final StructureType type = getType();
		if(type == StructureType.CALL_REQUIRE || type == StructureType.CALL_LOAD)
		{
			final VirtualFile file = getContainingFile() != null ? getContainingFile().getVirtualFile() : null;
			return RCallBaseUtil.gatherUrls(this, file);
		}
		if(type == StructureType.FIELD_ATTR_CALL)
		{
			return RCallBaseUtil.gatherStrings(this);
		}
		return Collections.emptyList();
	}

	@Override
	@Nonnull
	public String getPresentableText()
	{
		return getText();
	}

	@Override
	@Nonnull
	public List<RVirtualName> getVirtualNames()
	{
		final StructureType type = getType();
		if(type == StructureType.CALL_INCLUDE || type == StructureType.CALL_EXTEND || type == StructureType.CALL_IMPORT)
		{
			return RCallBaseUtil.gatherVirtualNames(this);
		}
		return Collections.emptyList();
	}

	@Override
	@Nullable
	public String getQualifiedName()
	{
		final StructureType type = getType();
		if(type == StructureType.CALL_INCLUDE_CLASS || type == StructureType.CALL_INCLUDE_PACKAGE)
		{
			return RCallBaseUtil.getJavaQualifiedName(this);
		}
		return null;
	}
}
