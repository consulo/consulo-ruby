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
package org.jetbrains.plugins.ruby.ruby.run.confuguration;

import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.configuration.fragment.StandardRunConfigurationFragmentOrder;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.TextBoxWithExpandAction;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.StringUtil;

/**
 * Arguments passed to the Ruby interpreter itself.
 */
public class RubyArgumentsFragment<T extends AbstractRubyRunConfiguration> implements RunConfigurationFragment<T> {
    @Override
    public String getId() {
        return "rubyArguments";
    }

    @Override
    public LocalizeValue getName() {
        return RubyApiLocalize.runConfigurationMessagesRubyArgs();
    }

    @Override
    public int getOrder() {
        return StandardRunConfigurationFragmentOrder.MODULE + 20;
    }

    @Override
    public boolean isVisibleByDefault(T target) {
        return !StringUtil.isEmptyOrSpaces(target.getRubyArgs());
    }

    @Override
    public void applyHidden(T target) {
        target.setRubyArgs(null);
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<T> createEditor(Project project, Disposable parent) {
        TextBoxWithExpandAction argumentsBox =
            RubyRunConfigurationFragmentUtil.createArgumentsBox(RubyApiLocalize.runConfigurationMessagesEditRubyArgs());

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(
                    LocalizeValue.join(RubyApiLocalize.runConfigurationMessagesRubyArgs(), LocalizeValue.colon()),
                    argumentsBox
                );
            }

            @RequiredUIAccess
            @Override
            public void reset(T target) {
                argumentsBox.setValue(target.getRubyArgs());
            }

            @RequiredUIAccess
            @Override
            public void apply(T target) {
                target.setRubyArgs(RubyRunConfigurationFragmentUtil.getText(argumentsBox));
            }
        };
    }
}
