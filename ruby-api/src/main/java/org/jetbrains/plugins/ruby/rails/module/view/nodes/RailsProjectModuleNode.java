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

package org.jetbrains.plugins.ruby.rails.module.view.nodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import org.jetbrains.plugins.ruby.rails.RailsIcons;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;
import org.jetbrains.plugins.ruby.rails.facet.configuration.StandardRailsPaths;
import org.jetbrains.plugins.ruby.rails.module.view.RailsProjectNodeComparator;
import org.jetbrains.plugins.ruby.rails.module.view.RailsViewFoldersManager;
import org.jetbrains.plugins.ruby.rails.module.view.id.NodeId;
import org.jetbrains.plugins.ruby.rails.module.view.id.NodeIdUtil;
import org.jetbrains.plugins.ruby.rails.module.view.nodes.folders.RailsApplicationFolderNode;
import org.jetbrains.plugins.ruby.rails.module.view.nodes.folders.RailsControllersFolderNode;
import org.jetbrains.plugins.ruby.rails.module.view.nodes.folders.RailsModelFolderNode;
import org.jetbrains.plugins.ruby.rails.module.view.nodes.folders.RailsSharedPatialsFolderNode;
import org.jetbrains.plugins.ruby.rails.module.view.nodes.folders.RailsUserFolderNode;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.treeStructure.SimpleNode;
import consulo.roots.ContentFolderScopes;
import consulo.roots.impl.TestContentFolderTypeProvider;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 28.09.2006
 */
public class RailsProjectModuleNode extends RailsNode
{

	public RailsProjectModuleNode(@Nonnull final Module module)
	{
		super(module);

		init(generateNodeId(module), initPresentationData());
	}

	@Nonnull
	public static NodeId generateNodeId(final Module module)
	{
		final StandardRailsPaths railsPaths = RailsFacetUtil.getRailsAppPaths(module);
		assert railsPaths != null; //Not null for modules with Rails Support

		final String url = railsPaths.getRailsApplicationRootURL();
		return NodeIdUtil.createForDirectory(url, false);
	}

	@Override
	@Nonnull
	public RailsProjectNodeComparator.NodeType getType()
	{
		return RailsProjectNodeComparator.NodeType.RMODULE;
	}

	@Override
	public SimpleNode[] getChildren()
	{
		final ArrayList<RailsNode> nodesList = new ArrayList<RailsNode>();
		final Module module = getModule();

		final StandardRailsPaths railsPaths = RailsFacetUtil.getRailsAppPaths(module);
		assert railsPaths != null; //Not null for modules with Rails Support

		final VirtualFileManager fileManager = VirtualFileManager.getInstance();

		VirtualFile root = fileManager.findFileByUrl(railsPaths.getControllerRootURL());
		if(root != null)
		{
			nodesList.add(new RailsApplicationFolderNode(module, root));
		}

		root = fileManager.findFileByUrl(railsPaths.getModelRootURL());
		if(root != null)
		{
			nodesList.add(new RailsModelFolderNode(module, root));
		}

		root = fileManager.findFileByUrl(railsPaths.getControllerRootURL());
		if(root != null)
		{
			nodesList.add(new RailsControllersFolderNode(module, root));
		}

		//TODO correct
		//        final boolean isRSpecSupportEnabled = railsPaths.rSpecSupportType();
		//        if (!isRSpecSupportEnabled) {
		//            root = fileManager.findFileByUrl(railsPaths.getTestsStdUnitRootURL());
		//            if (root != null) {
		//                nodesList.add(new RailsTestsFolderNode(module, root));
		//            }
		//        }

		root = fileManager.findFileByUrl(railsPaths.getDefaultSharedPartialsRootURL());
		if(root != null)
		{
			nodesList.add(new RailsSharedPatialsFolderNode(module, root));
		}

		final Set<String> urls = RailsViewFoldersManager.getInstance(module).getRailsViewUserFolderUrls();
		final Set<String> testUrls = new HashSet<String>();
		for(ContentEntry o : ModuleRootManager.getInstance(module).getContentEntries())
		{
			for(String url : o.getFolderUrls(ContentFolderScopes.of(TestContentFolderTypeProvider.getInstance())))
			{
				testUrls.add(url);
			}
		}

		for(String url : urls)
		{
			final VirtualFile file = fileManager.findFileByUrl(url);
			if(file != null)
			{
				nodesList.add(new RailsUserFolderNode(module, file, this, testUrls.contains(url)));
			}
		}
		return nodesList.toArray(new RailsNode[nodesList.size()]);
	}

	private PresentationData initPresentationData()
	{
		//TODO add JRails or Rails modules
		String name = getModule().getName();
		return new PresentationData(name, name, RailsIcons.RAILS_MODULE_OPENED, RailsIcons.RAILS_MODULE_CLOSED, null);

	}
}