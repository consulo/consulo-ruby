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
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.IntBox;
import consulo.ui.Label;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import consulo.ruby.api.localize.RubyApiLocalize;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration.TestType;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.tests.AbstractRTestsTargetFragment;

import java.util.List;

public class RSpecTargetFragment extends AbstractRTestsTargetFragment<RSpecRunConfiguration> {
    @Override
    public LocalizeValue getName() {
        return RubyApiLocalize.runConfigurationFragmentSpec();
    }

    @Override
    protected List<TestType> getTestTypes() {
        return List.of(TestType.ALL_IN_FOLDER, TestType.TEST_SCRIPT, TestType.TEST_METHOD);
    }

    @Override
    protected LocalizeValue getTestTypeName(TestType testType) {
        return switch (testType) {
            case ALL_IN_FOLDER -> RubyApiLocalize.rspecRunConfigurationTestsDialogRbAllinfolder();
            case TEST_METHOD -> RubyApiLocalize.rspecRunConfigurationTestsDialogRbExample();
            default -> RubyApiLocalize.rspecRunConfigurationTestsDialogRbFile();
        };
    }

    @Override
    protected LocalizeValue getTestTypeLabel() {
        return RubyApiLocalize.rspecRunConfigurationTestsFormTesttypeTitle();
    }

    @Override
    protected LocalizeValue getFolderLabel() {
        return LocalizeValue.join(RubyApiLocalize.rspecRunConfigurationTestsDialogComponentsFolder(), LocalizeValue.colon());
    }

    @Override
    protected LocalizeValue getFileMaskLabel() {
        return RubyApiLocalize.rspecRunConfigurationTestsDialogComponentsSearchMask();
    }

    @Override
    protected LocalizeValue getScriptLabel() {
        return RubyApiLocalize.rspecRunConfigurationMessagesScriptPath();
    }

    @Override
    protected String getDefaultFileMask() {
        return RSpecRunConfiguration.DEFAULT_TESTS_SEARCH_MASK;
    }

    @RequiredUIAccess
    @Override
    protected TargetEditor createTargetEditor(Project project, Disposable parent) {
        return new ExampleTargetEditor(project, parent);
    }

    private class ExampleTargetEditor extends TargetEditor {
        private final IntBox myLineBox;
        private final Label myLineLabel;

        @RequiredUIAccess
        private ExampleTargetEditor(Project project, Disposable parent) {
            super(project, parent);

            myLineBox = IntBox.create();
            myLineLabel = Label.create(RubyApiLocalize.rspecRunConfigurationTestsDialogComponentsExampleLine());
        }

        @RequiredUIAccess
        @Override
        protected void addExtraRows(FormBuilder builder) {
            builder.addLabeled(myLineLabel, myLineBox);
        }

        @RequiredUIAccess
        @Override
        protected void updateTestType(TestType testType) {
            super.updateTestType(testType);

            setRowVisible(myLineLabel, myLineBox, testType == TestType.TEST_METHOD);
        }

        @RequiredUIAccess
        @Override
        public void reset(RSpecRunConfiguration target) {
            myLineBox.setValue(target.getExampleLine());

            super.reset(target);
        }

        @RequiredUIAccess
        @Override
        public void apply(RSpecRunConfiguration target) {
            super.apply(target);

            Integer line = myLineBox.getValue();
            target.setExampleLine(line == null ? 0 : line);
        }
    }
}
