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
package org.jetbrains.plugins.ruby.addins.rspec.run.configuration;

import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.annotation.RequiredUIAccess;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationFragmentUtil;

/**
 * A spec runner script used instead of the RSpec gem or plugin runner.
 */
public class RSpecCustomRunnerFragment implements RunConfigurationFragment<RSpecRunConfiguration> {
    @Override
    public String getId() {
        return "rspecCustomRunner";
    }

    @Override
    public LocalizeValue getName() {
        return RubyApiLocalize.runConfigurationFragmentCustomSpecRunner();
    }

    @Override
    public boolean isVisibleByDefault(RSpecRunConfiguration target) {
        return target.shouldUseCustomSpecRunner();
    }

    @Override
    public void applyHidden(RSpecRunConfiguration target) {
        target.setShouldUseCustomSpecRunner(false);
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<RSpecRunConfiguration> createEditor(Project project, Disposable parent) {
        FileChooserTextBoxBuilder.Controller customRunnerBox = RubyRunConfigurationFragmentUtil.createPathBox(
            project,
            parent,
            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor(),
            RubyApiLocalize.rspecRunConfigurationTestsDialogComponentsSelectSpecCustomRunner()
        );
        customRunnerBox.getComponent().setPlaceholder(RubyApiLocalize.rspecRunConfigurationCustomRunnerPlaceholder());

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(
                    LocalizeValue.join(RubyApiLocalize.rspecRunConfigurationTestsDialogComponentsSpecCustomRunner(), LocalizeValue.colon()),
                    customRunnerBox.getComponent()
                );
            }

            @RequiredUIAccess
            @Override
            public void reset(RSpecRunConfiguration target) {
                RubyRunConfigurationFragmentUtil.setPath(
                    customRunnerBox,
                    target.shouldUseCustomSpecRunner() ? target.getCustomSpecsRunnerPath() : null
                );
            }

            @RequiredUIAccess
            @Override
            public void apply(RSpecRunConfiguration target) {
                String customRunnerPath = RubyRunConfigurationFragmentUtil.getPath(customRunnerBox);
                boolean useCustomRunner = !customRunnerPath.isEmpty();
                target.setShouldUseCustomSpecRunner(useCustomRunner);
                if (useCustomRunner) {
                    target.setCustomSpecsRunnerPath(customRunnerPath);
                }
            }
        };
    }
}
