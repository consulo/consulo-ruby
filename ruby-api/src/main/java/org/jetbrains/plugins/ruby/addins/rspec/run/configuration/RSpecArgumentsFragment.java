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
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.TextBoxWithExpandAction;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.StringUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationFragmentUtil;

public class RSpecArgumentsFragment implements RunConfigurationFragment<RSpecRunConfiguration> {
    @Override
    public String getId() {
        return "rspecArguments";
    }

    @Override
    public LocalizeValue getName() {
        return RubyApiLocalize.rspecRunConfigurationMessagesArgs();
    }

    @Override
    public boolean isVisibleByDefault(RSpecRunConfiguration target) {
        return !StringUtil.isEmptyOrSpaces(target.getSpecArgs());
    }

    @Override
    public void applyHidden(RSpecRunConfiguration target) {
        target.setSpecArgs(null);
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<RSpecRunConfiguration> createEditor(Project project, Disposable parent) {
        TextBoxWithExpandAction argumentsBox =
            RubyRunConfigurationFragmentUtil.createArgumentsBox(RubyApiLocalize.rspecRunConfigurationMessagesEditSpecArgs());

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(
                    LocalizeValue.join(RubyApiLocalize.rspecRunConfigurationMessagesSpecArgs(), LocalizeValue.colon()),
                    argumentsBox
                );
            }

            @RequiredUIAccess
            @Override
            public void reset(RSpecRunConfiguration target) {
                argumentsBox.setValue(target.getSpecArgs());
            }

            @RequiredUIAccess
            @Override
            public void apply(RSpecRunConfiguration target) {
                target.setSpecArgs(RubyRunConfigurationFragmentUtil.getText(argumentsBox));
            }
        };
    }
}
