/*
 * Copyright 2013-2026 consulo.io
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
package org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript;

import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.configuration.fragment.StandardRunConfigurationFragmentOrder;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.annotation.RequiredUIAccess;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationFragmentUtil;

public class RubyScriptFragment<T extends RubyRunConfiguration> implements RunConfigurationFragment<T> {
    @Override
    public String getId() {
        return "rubyScript";
    }

    @Override
    public LocalizeValue getName() {
        return RubyApiLocalize.runConfigurationFragmentScript();
    }

    @Override
    public int getOrder() {
        return StandardRunConfigurationFragmentOrder.FIRST;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<T> createEditor(Project project, Disposable parent) {
        FileChooserTextBoxBuilder.Controller scriptPathBox = RubyRunConfigurationFragmentUtil.createPathBox(
            project,
            parent,
            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor(),
            RubyApiLocalize.runConfigurationMessagesSelectRubySciptPath()
        );

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(RubyApiLocalize.runConfigurationMessagesScriptPath(), scriptPathBox.getComponent());
            }

            @RequiredUIAccess
            @Override
            public void reset(T target) {
                RubyRunConfigurationFragmentUtil.setPath(scriptPathBox, target.getScriptPath());
            }

            @RequiredUIAccess
            @Override
            public void apply(T target) {
                target.setScriptPath(RubyRunConfigurationFragmentUtil.getPath(scriptPathBox));
            }
        };
    }
}
