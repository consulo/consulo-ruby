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

package org.jetbrains.plugins.ruby.ruby.cache.psi.containers;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.plugins.ruby.ruby.cache.info.RFileInfo;
import org.jetbrains.plugins.ruby.ruby.cache.psi.RVirtualStructuralElement;
import org.jetbrains.plugins.ruby.ruby.lang.psi.controlStructures.AccessModifier;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Oct 2, 2006
 */
public interface RVirtualContainer extends RVirtualStructuralElement
{
	/**
	 * @return this container access modifier
	 */
	@Nonnull
	public AccessModifier getAccessModifier();

	/**
	 * @return default access modifier for subclasses
	 */
	@Nonnull
	AccessModifier getDefaultChildAccessModifier();

	@Nullable
	public RFileInfo getContainingFileInfo();

	@Nonnull
	public String getContainingFileUrl();

	@Nullable
	public VirtualFile getVirtualFile();

	@Nullable
	public ItemPresentation getPresentation();

	@Nonnull
	public List<RVirtualStructuralElement> getVirtualStructureElements();

	public int getIndexOf(@Nonnull RVirtualStructuralElement element);

	/**
	 * @return only name without path
	 */
	@Nonnull
	public String getName();

	/**
	 * @return List of paths
	 */
	@Nonnull
	public List<String> getFullPath();

	/**
	 * @return full name,i.e. name with path
	 */
	@Nonnull
	public String getFullName();

	public boolean isGlobal();

	public Project getProject();
}
