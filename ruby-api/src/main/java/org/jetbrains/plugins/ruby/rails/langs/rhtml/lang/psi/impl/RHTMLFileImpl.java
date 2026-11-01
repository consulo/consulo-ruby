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

package org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.psi.impl;

import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.global.RGlobalVariable;

import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.fields.RField;

import org.jetbrains.plugins.ruby.ruby.lang.psi.variables.RConstant;

import java.util.List;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.content.bundle.Sdk;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.impl.psi.PsiFileImpl;
import consulo.language.psi.PsiElementVisitor;
import consulo.language.template.TemplateLanguageFileViewProvider;
import consulo.module.Module;
import consulo.virtualFileSystem.fileType.FileType;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.RHTMLFileType;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.psi.RHTMLFile;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.psi.eRubyElementTypes;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.psi.visitors.RHTMLElementTypeVisitor;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RRequire;
import org.jetbrains.plugins.ruby.ruby.cache.psi.StructureType;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.scope.RootScope;
import org.jetbrains.plugins.ruby.ruby.codeInsight.symbols.structure.FileSymbol;
import org.jetbrains.plugins.ruby.ruby.lang.RubyLanguage;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RStructuralElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlFlow.Instruction;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.AccessModifier;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.blocks.RCompoundStatement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.ConstantDefinitions;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.FieldDefinition;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.GlobalVarDefinition;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;
import org.jetbrains.plugins.ruby.ruby.lang.psi.visitors.RubyElementVisitor;
import consulo.language.file.FileViewProvider;
import consulo.language.psi.PsiElement;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 03.04.2007
 */
public class RHTMLFileImpl extends PsiFileImpl implements RHTMLFile
{

	public RHTMLFileImpl(FileViewProvider viewProvider)
	{
		super(eRubyElementTypes.RHTML_FILE, eRubyElementTypes.RHTML_FILE, viewProvider);
	}

	public String toString()
	{
		return "RHTMLFile:" + getName();
	}

	@Override
	public int getTextLength()
	{
		//For prevent DirtyScope exception in ExternalToolPassFactory.calculateRangeToProcessForSyntaxPass()
		return getViewProvider().getContents().length();
	}

	@Override
	@Nonnull
	public TemplateLanguageFileViewProvider getViewProvider()
	{
		return (TemplateLanguageFileViewProvider) super.getViewProvider();
	}

	@Override
	public void accept(@Nonnull PsiElementVisitor visitor)
	{
		if(visitor instanceof RHTMLElementTypeVisitor)
		{
			((RHTMLElementTypeVisitor) visitor).visitRHTMLFile(this);
		}
		else if(visitor instanceof RubyElementVisitor)
		{
			((RubyElementVisitor) visitor).visitRFile(this);
		}
	}

	@Override
	@Nonnull
	public FileType getFileType()
	{
		return RHTMLFileType.INSTANCE;
	}

	///////////////////// RFile methods ///////////////////////////////

	@Override
	@Nonnull
	public RFile getInnerRubyFile()
	{
		return (RFile) getViewProvider().getPsi(RubyLanguage.INSTANCE);
	}

	@Override
	@Nonnull
	public RCompoundStatement getCompoundStatement()
	{
		return getInnerRubyFile().getCompoundStatement();
	}

	@Override
	public StructureType getType()
	{
		return getInnerRubyFile().getType();
	}

	@Override
	@Nullable
	public RContainer getParentContainer()
	{
		return getInnerRubyFile().getParentContainer();
	}

	@Override
	@Nullable
	public Module getModule()
	{
		return getInnerRubyFile().getModule();
	}

	@Override
	public boolean isInteropEnabled()
	{
		return false;
	}

	@Override
	@Nullable
	public Sdk getSdk()
	{
		return getInnerRubyFile().getSdk();
	}

	@Override
	@Nullable
	public String getPresentableLocation()
	{
		return getInnerRubyFile().getPresentableLocation();
	}

	@Override
	@Nonnull
	public AccessModifier getAccessModifier()
	{
		return getInnerRubyFile().getAccessModifier();
	}

	@Override
	@Nonnull
	public AccessModifier getDefaultChildAccessModifier()
	{
		return getInnerRubyFile().getDefaultChildAccessModifier();
	}

	@Override
	@Nonnull
	public String getContainingFileUrl()
	{
		return getInnerRubyFile().getContainingFileUrl();
	}

	@Override
	@Nullable
	public RContainer getVirtualParentContainer()
	{
		return getInnerRubyFile().getVirtualParentContainer();
	}

	@Override
	@Nonnull
	public List<RStructuralElement> getVirtualStructureElements()
	{
		return getInnerRubyFile().getVirtualStructureElements();
	}

	@Override
	@Nonnull
	public List<RField> getVirtualFields()
	{
		return getInnerRubyFile().getVirtualFields();
	}

	@Override
	@Nonnull
	public List<RConstant> getVirtualConstants()
	{
		return getInnerRubyFile().getVirtualConstants();
	}

	@Override
	@Nonnull
	public List<RGlobalVariable> getVirtualGlobalVars()
	{
		return getInnerRubyFile().getVirtualGlobalVars();
	}

	@Override
	@Nonnull
	public List<RStructuralElement> getStructureElements()
	{
		return getInnerRubyFile().getStructureElements();
	}

	@Override
	@Nonnull
	public List<RRequire> getRequires()
	{
		return getInnerRubyFile().getRequires();
	}

	@Override
	@Nonnull
	public List<String> getRequiredUrls()
	{
		return getInnerRubyFile().getRequiredUrls();
	}

	@Override
	@Nonnull
	public List<PsiElement> getChildrenByFilter(IElementType filter)
	{
		return getInnerRubyFile().getChildrenByFilter(filter);
	}

	@Override
	@Nullable
	public PsiElement getChildByFilter(TokenSet filter, int number)
	{
		return getInnerRubyFile().getChildByFilter(filter, number);
	}

	@Override
	@Nullable
	public PsiElement getChildByFilter(IElementType filter, int number)
	{
		return getInnerRubyFile().getChildByFilter(filter, number);
	}

	@Override
	@Nonnull
	public <T extends PsiElement> List<T> getChildrenByType(Class<T> c)
	{
		return getInnerRubyFile().getChildrenByType(c);
	}

	@Override
	@Nullable
	public <T extends PsiElement> T getChildByType(Class<T> c, int number)
	{
		return getInnerRubyFile().getChildByType(c, number);
	}

	@Override
	@Nonnull
	public List<FieldDefinition> getFieldsDefinitions()
	{
		return getInnerRubyFile().getFieldsDefinitions();
	}

	@Override
	@Nullable
	public FieldDefinition getDefinition(@Nonnull RField field)
	{
		return getInnerRubyFile().getDefinition(field);
	}

	@Override
	@Nonnull
	public List<ConstantDefinitions> getConstantDefinitions()
	{
		return getInnerRubyFile().getConstantDefinitions();
	}

	@Override
	@Nullable
	public ConstantDefinitions getDefinition(@Nonnull RConstant constant)
	{
		return getInnerRubyFile().getDefinition(constant);
	}

	@Override
	public int getIndexOf(@Nonnull RStructuralElement element)
	{
		return getInnerRubyFile().getIndexOf(element);
	}

	@Override
	@Nonnull
	public List<GlobalVarDefinition> getGlobalVarDefinitions()
	{
		return getInnerRubyFile().getGlobalVarDefinitions();
	}

	@Override
	@Nullable
	public GlobalVarDefinition getDefinition(@Nonnull RGlobalVariable globalVar)
	{
		return getInnerRubyFile().getDefinition(globalVar);
	}

	@Override
	@Nullable
	public FileSymbol getFileSymbol()
	{
		return getInnerRubyFile().getFileSymbol();
	}

	@Override
	@Nonnull
	public RootScope getScope()
	{
		return getInnerRubyFile().getScope();
	}

	@Override
	public Instruction[] getControlFlow()
	{
		return getInnerRubyFile().getControlFlow();
	}

	@Override
	@Nonnull
	public List<String> getFullPath()
	{
		return getInnerRubyFile().getFullPath();
	}

	@Override
	@Nonnull
	public String getFullName()
	{
		return getInnerRubyFile().getFullName();
	}

	@Override
	public boolean isGlobal()
	{
		return getInnerRubyFile().isGlobal();
	}

	@Override
	public boolean equalsToVirtual(@Nonnull RStructuralElement element)
	{
		return getInnerRubyFile().equalsToVirtual(element);
	}
}
