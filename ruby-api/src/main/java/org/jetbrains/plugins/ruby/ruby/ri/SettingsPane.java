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

package org.jetbrains.plugins.ruby.ruby.ri;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import consulo.content.bundle.Sdk;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.ri.ui.CheckBoxList;
import org.jetbrains.plugins.ruby.ruby.ri.ui.CheckBoxListListener;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;
import org.jetbrains.plugins.ruby.support.ui.checkableDir.CheckableDirectoriesContainer;
import org.jetbrains.plugins.ruby.support.ui.checkableDir.CheckableDirectoryItem;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserFactory;
import consulo.util.io.FileUtil;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Nov 2, 2006
 */
class SettingsPane implements CheckBoxListListener
{
	private JPanel mainPanel;
	private JButton addButton;
	private JButton removeButton;
	private JRadioButton defaultRadioButton;
	private JRadioButton selectedRadioButton;
	private CheckBoxList myList;
	private JLabel myLabel;
	private JLabel myDescription;

	final private RDocPanel myDocPanel;
	private DefaultListModel myListModel;
	private RDocSettings mySettings;
	private CheckableDirectoriesContainer myDocDirs;

	public SettingsPane(final RDocPanel docPanel, final RDocSettings settings)
	{
		myDocPanel = docPanel;
		mySettings = settings;
		myDocDirs = settings.getDocDirs();

		addButton.setText(RBundle.message("ruby.ri.settings.pane.add"));
		addButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				FileChooserDescriptor dirChooser = FileChooserDescriptorFactory.createSingleFolderDescriptor();
				dirChooser.setShowFileSystemRoots(true);
				dirChooser.setHideIgnored(true);
				dirChooser.setTitle("Select directory");
				FileChooserFactory.getInstance().newBuilder(dirChooser).project(docPanel.getProject()).parent(mainPanel).showSingle().whenComplete((path, error) -> {
					if(path != null)
					{
						// adding to the end
						CheckableDirectoryItem docDirectory = new CheckableDirectoryItem(FileUtil.toSystemIndependentName(path.toString()), true);
						myDocDirs.addCheckableDir(docDirectory);
						myListModel.addElement(docDirectory.createCheckBox());
					}
				});
			}
		});

		removeButton.setText(RBundle.message("ruby.ri.settings.pane.remove"));
		removeButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int selected = myList.getSelectedIndex();
				if(selected != -1)
				{
					// removing index
					final JCheckBox checkBox = (JCheckBox) myListModel.get(selected);
					myDocDirs.removeDirByPath(checkBox.getText());
					myListModel.remove(selected);
				}
			}
		});
		defaultRadioButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mySettings.setUseDefaults(defaultRadioButton.isSelected());
				updateUI();
			}
		});
		selectedRadioButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				mySettings.setUseDefaults(defaultRadioButton.isSelected());
				updateUI();
			}
		});

		myDescription.setText(RBundle.message("ruby.ri.settings.pane.description"));
		myLabel.setText(RBundle.message("ruby.ri.settings.pane.label"));

		// restoring settings
		for(CheckableDirectoryItem docDirectory : getDocDirs())
		{
			myListModel.addElement(docDirectory.createCheckBox());
		}
		updateUI();
	}

	public JComponent getPanel()
	{
		return mainPanel;
	}


	private void createUIComponents()
	{
		myListModel = new DefaultListModel();
		myList = new CheckBoxList(myListModel, this);
	}

	private void updateUI()
	{
		if(doUseDefaults())
		{
			defaultRadioButton.setSelected(true);
			selectedRadioButton.setSelected(false);
			myList.setEnabled(false);
			myList.setSelectedIndices(new int[]{});
			addButton.setEnabled(false);
			removeButton.setEnabled(false);
		}
		else
		{
			defaultRadioButton.setSelected(false);
			selectedRadioButton.setSelected(true);
			myList.setEnabled(true);
			addButton.setEnabled(true);
			removeButton.setEnabled(true);
		}
	}

	public boolean doUseDefaults()
	{
		return mySettings.doUseDefaults();
	}

	public String[] getSelectedDirs()
	{
		LinkedList<String> selected = new LinkedList<String>();
		for(CheckableDirectoryItem docDir : getDocDirs())
		{
			if(docDir.isChecked())
			{
				selected.add(docDir.getDirectoryPath());
			}
		}
		return selected.toArray(new String[selected.size()]);
	}

	private List<CheckableDirectoryItem> getDocDirs()
	{
		return myDocDirs.getCheckableDirectories();
	}

	@Override
	public void checkBoxSelectionChanged(int index, boolean value)
	{
		final JCheckBox checkBox = (JCheckBox) myListModel.getElementAt(index);
		myDocDirs.stateChanged(checkBox.getText(), value);
		checkIfSomethingSelected();
	}

	private void checkIfSomethingSelected()
	{
		for(CheckableDirectoryItem docDirectory : getDocDirs())
		{
			if(docDirectory.isChecked())
			{
				return;
			}
		}
		mySettings.setUseDefaults(true);
		updateUI();
	}

	public void fireJDKChanged()
	{
		Sdk jdk = myDocPanel.getSdk();
		if(!(RubySdkUtil.isKindOfRubySDK(jdk) && RIUtil.checkIfRiExists(jdk)))
		{
			defaultRadioButton.setEnabled(false);
			selectedRadioButton.setEnabled(false);
			myList.setEnabled(false);
			addButton.setEnabled(false);
			removeButton.setEnabled(false);
		}
		else
		{
			defaultRadioButton.setEnabled(true);
			selectedRadioButton.setEnabled(true);
			myList.setEnabled(true);
			addButton.setEnabled(true);
			removeButton.setEnabled(true);
			updateUI();
		}
	}
}
