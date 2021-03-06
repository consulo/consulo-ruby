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

package org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.cache.info.RFileInfo;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RVirtualStructuralElement;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RVirtualUtil;
import org.jetbrains.plugins.ruby.ruby.cache.psi.containers.RVirtualContainer;
import org.jetbrains.plugins.ruby.ruby.cache.psi.impl.RVirtualContainerBase;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.scope.RootScope;
import org.jetbrains.plugins.ruby.ruby.codeInsight.resolve.scope.ScopeBuilder;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.TypeInferenceContext;
import org.jetbrains.plugins.ruby.ruby.codeInsight.types.TypeInferenceHelper;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RPsiElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RStructuralElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RVirtualPsiUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlFlow.Instruction;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlFlow.impl.RControlFlowBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.AccessModifier;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.blocks.RBodyStatement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RConstantHolder;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RContainer;
import org.jetbrains.plugins.ruby.ruby.lang.psi.holders.RFieldHolder;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.RPsiElementBase;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.controlStructures.RNameUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.impl.holders.utils.RContainerUtil;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 21.07.2006
 */
public abstract class RContainerBase extends RPsiElementBase implements RContainer
{
	private AccessModifier myAccessModifier = AccessModifier.PUBLIC;
	protected List<RStructuralElement> myStructureElements;
	private Instruction[] myControlFlow;

	public RContainerBase(final ASTNode astNode)
	{
		super(astNode);
	}


	@Override
	@Nonnull
	public AccessModifier getDefaultChildAccessModifier()
	{
		return AccessModifier.PUBLIC;
	}

	@Override
	@Nonnull
	public AccessModifier getAccessModifier()
	{
		// getSubContainers of parentContainer will set correct access modifiers to all of it`s children
		final RContainer parentContainer = getParentContainer();
		if(parentContainer != null)
		{
			parentContainer.getStructureElements();
		}
		return myAccessModifier;
	}

	public void setAccessModifier(final AccessModifier modifier)
	{
		myAccessModifier = modifier;
	}

	@Override
	@Nonnull
	public final List<RStructuralElement> getStructureElements()
	{
		if(myStructureElements == null)
		{
			myStructureElements = RContainerUtil.getStructureElements(this);
		}
		return myStructureElements;
	}

	@Override
	public synchronized void subtreeChanged()
	{
		clearMyCaches();
		super.subtreeChanged();
	}

	private void clearMyCaches()
	{
		myStructureElements = null;
		myScope = null;

		// Clear control flow and inference info
		final TypeInferenceContext context = TypeInferenceHelper.getInstance(getProject()).getContext();
		if(context != null)
		{
			context.localVariablesTypesCache.remove(this);
		}
		myControlFlow = null;
	}

	@Override
	@Nullable
	public RFileInfo getContainingFileInfo()
	{
		return null;
	}

	@Override
	@Nonnull
	public String getContainingFileUrl()
	{
		//noinspection ConstantConditions
		return getContainingFile().getVirtualFile().getUrl();
	}

	@Override
	@Nullable
	public VirtualFile getVirtualFile()
	{
		return getContainingFile().getVirtualFile();
	}

	protected void addVirtualData(@Nonnull final RVirtualContainer virtualCopy, @Nonnull final RFileInfo fileInfo)
	{
		if(this instanceof RFieldHolder)
		{
			RVirtualUtil.addVirtualFields(virtualCopy, (RFieldHolder) this);
		}
		if(this instanceof RConstantHolder)
		{
			RVirtualUtil.addVirtualConstants(virtualCopy, (RConstantHolder) this);
		}

		final List<RVirtualStructuralElement> elements = new ArrayList<RVirtualStructuralElement>();

		// recursive generate all the info about all the children
		for(RStructuralElement structuralElement : getStructureElements())
		{
			elements.add(structuralElement.createVirtualCopy(virtualCopy, fileInfo));
		}
		((RVirtualContainerBase) virtualCopy).setStructureElements(elements);
	}


	@Override
	@Nullable
	public RVirtualContainer getVirtualParentContainer()
	{
		return getParentContainer();
	}

	@Override
	@Nonnull
	public List<RVirtualStructuralElement> getVirtualStructureElements()
	{
		final ArrayList<RVirtualStructuralElement> elements = new ArrayList<RVirtualStructuralElement>();
		for(RStructuralElement element : getStructureElements())
		{
			elements.add(element);
		}
		return elements;
	}

	@Override
	public int getIndexOf(@Nonnull RVirtualStructuralElement element)
	{
		final List<RStructuralElement> structuralElements = getStructureElements();
		for(int i = 0; i < structuralElements.size(); i++)
		{
			if(element == structuralElements.get(i))
			{
				return i;
			}
		}
		return -1;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///// ScopeHolder
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	// scope for scopeHolder
	private RootScope myScope;

	@Override
	@Nonnull
	public synchronized RootScope getScope()
	{
		if(myScope == null)
		{
			myScope = ScopeBuilder.buildScope(this);
		}
		return myScope;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// Name related methods
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	@Override
	@Nonnull
	public String getName()
	{
		return RNameUtil.getName(getFullPath());
	}

	@Override
	@Nonnull
	public List<String> getFullPath()
	{
		return RNameUtil.getPath(getNameElement());
	}

	@Override
	@Nonnull
	public String getFullName()
	{
		return RNameUtil.getPresentableName(getNameElement().getText());
	}

	@Override
	public boolean isGlobal()
	{
		return RNameUtil.isGlobal(getNameElement());
	}

	protected abstract RPsiElement getNameElement();

	@Override
	public boolean equalsToVirtual(@Nonnull final RVirtualStructuralElement element)
	{
		if(!(element instanceof RVirtualContainer))
		{
			return false;
		}
		final RVirtualContainer container = (RVirtualContainer) element;
		// Container changes
		if(getType() != container.getType())
		{
			return false;
		}
		if(getAccessModifier() != container.getAccessModifier())
		{
			return false;
		}
		if(!getFullName().equals(container.getFullName()))
		{
			return false;
		}
		//noinspection SimplifiableIfStatement
		if(isGlobal() != container.isGlobal())
		{
			return false;
		}

		return RVirtualPsiUtil.areSubStructureEqual(this, container);
	}

	@Nonnull
	public RBodyStatement getBody()
	{
		final List<RBodyStatement> list = getChildrenByType(RBodyStatement.class);
		assert list.size() == 1;
		return list.get(0);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///// ControlFlowOwner
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public Instruction[] getControlFlow()
	{
		if(myControlFlow == null)
		{
			myControlFlow = new RControlFlowBuilder().buildControlFlow(null, this, null, null);
		}
		return myControlFlow;
	}
}
