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

package org.jetbrains.plugins.ruby.ruby.module.ui.roots.loadPath;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.nio.file.Path;

import jakarta.annotation.Nonnull;

import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserFactory;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.ri.ui.CheckBoxList;
import org.jetbrains.plugins.ruby.ruby.ri.ui.CheckBoxListListener;
import org.jetbrains.plugins.ruby.support.ui.checkableDir.CheckableDirectoriesContainer;
import org.jetbrains.plugins.ruby.support.ui.checkableDir.CheckableDirectoryItem;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.module.Module;
import consulo.ui.ex.awt.Messages;
import consulo.util.io.FileUtil;
import consulo.virtualFileSystem.VirtualFile;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Jan 19, 2008
 */
public class RLoadPathCooserPanel implements CheckBoxListListener
{
	private JPanel myContentPane;
	private CheckBoxList myList;
	private JButton myAddButton;
	private JButton myRemoveButton;
	private DefaultListModel myListModel;
	private final Module myModule;
	private final CheckableDirectoriesContainer myDirsContainer;

	public RLoadPathCooserPanel(@Nonnull final Module module, @Nonnull final CheckableDirectoriesContainer dirsContainer)
	{
		myModule = module;
		myDirsContainer = dirsContainer;

		// fill list
		final List<CheckableDirectoryItem> dirs = myDirsContainer.getCheckableDirectories();
		for(CheckableDirectoryItem directoryItem : dirs)
		{
			myListModel.addElement(directoryItem.createCheckBox());
		}

		// listeners
		myAddButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				final FileChooserDescriptor dirChooser = FileChooserDescriptorFactory.createSingleFolderDescriptor();
				dirChooser.setShowFileSystemRoots(true);
				dirChooser.setHideIgnored(true);
				dirChooser.setTitle(RBundle.message("module.settings.dialog.load.path.filechooser.add.dialog.title"));
				// dirChooser.setContextModule(module);
				FileChooserFactory.getInstance().newBuilder(dirChooser).project(module.getProject()).parent(myContentPane).showMultiple().whenComplete((paths, error) -> {
					if(paths == null)
					{
						return;
					}
					for(Path chosen : paths)
					{
						// adding to the end
						CheckableDirectoryItem docDirectory = new CheckableDirectoryItem(FileUtil.toSystemIndependentName(chosen.toString()), true);
						if(myDirsContainer.addCheckableDir(docDirectory))
						{
							myListModel.addElement(docDirectory.createCheckBox());
						}
						else
						{
							final String msg = RBundle.message("module.settings.dialog.load.path.error.add.message");
							final String title = RBundle.message("module.settings.dialog.load.path.error.add.title");
							Messages.showErrorDialog(module.getProject(), msg, title);
						}
					}
				});
			}
		});

		myRemoveButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int selected = myList.getSelectedIndex();
				if(selected != -1)
				{
					// removing index
					final JCheckBox checkBox = (JCheckBox) myListModel.get(selected);
					myDirsContainer.removeDirByPath(getPath(checkBox));
					myListModel.remove(selected);
				}
			}
		});
	}

	public JPanel getContentPane()
	{
		return myContentPane;
	}

	@Override
	public void checkBoxSelectionChanged(int index, boolean value)
	{
		final JCheckBox checkBox = (JCheckBox) myListModel.getElementAt(index);
		myDirsContainer.stateChanged(getPath(checkBox), value);
	}

	private String getPath(final JCheckBox checkBox)
	{
		return FileUtil.toSystemIndependentName(checkBox.getText());
	}

	private void createUIComponents()
	{
		myListModel = new DefaultListModel();
		myList = new CheckBoxList(myListModel, this);
	}
}
