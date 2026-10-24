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
package org.jetbrains.plugins.ruby.ruby.run.confuguration.tests;

import consulo.disposer.Disposable;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.Label;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.StringUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration.TestType;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationFragmentUtil;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class RTestsTargetFragment extends AbstractRTestsTargetFragment<RTestsRunConfiguration> {
    @Override
    public LocalizeValue getName() {
        return RubyApiLocalize.runConfigurationFragmentTest();
    }

    @Override
    protected List<TestType> getTestTypes() {
        return List.of(TestType.ALL_IN_FOLDER, TestType.TEST_SCRIPT, TestType.TEST_CLASS, TestType.TEST_METHOD);
    }

    @Override
    protected LocalizeValue getTestTypeName(TestType testType) {
        return switch (testType) {
            case ALL_IN_FOLDER -> RubyApiLocalize.runConfigurationTestsDialogRbAllinfolder();
            case TEST_SCRIPT -> RubyApiLocalize.runConfigurationTestsDialogRbFile();
            case TEST_CLASS -> RubyApiLocalize.runConfigurationTestsDialogRbClass();
            case TEST_METHOD -> RubyApiLocalize.runConfigurationTestsDialogRbMethod();
        };
    }

    @Override
    protected LocalizeValue getTestTypeLabel() {
        return RubyApiLocalize.runConfigurationTestFormTesttypeTitle();
    }

    @Override
    protected LocalizeValue getFolderLabel() {
        return LocalizeValue.join(RubyApiLocalize.runConfigurationTestsDialogComponentsFolder(), LocalizeValue.colon());
    }

    @Override
    protected LocalizeValue getFileMaskLabel() {
        return RubyApiLocalize.runConfigurationTestsDialogComponentsSearchMask();
    }

    @Override
    protected LocalizeValue getScriptLabel() {
        return RubyApiLocalize.runConfigurationMessagesScriptPath();
    }

    @Override
    protected String getDefaultFileMask() {
        return RTestsRunConfiguration.DEFAULT_TESTS_SEARCH_MASK;
    }

    @RequiredUIAccess
    @Override
    protected TargetEditor createTargetEditor(Project project, Disposable parent) {
        return new TestCaseTargetEditor(project, parent);
    }

    private class TestCaseTargetEditor extends TargetEditor {
        private final TextBox myClassBox;
        private final TextBox myMethodBox;

        private @Nullable Label myClassLabel;
        private @Nullable Label myMethodLabel;

        @RequiredUIAccess
        private TestCaseTargetEditor(Project project, Disposable parent) {
            super(project, parent);

            myClassBox = TextBox.create();
            myMethodBox = TextBox.create();
        }

        @RequiredUIAccess
        @Override
        protected void addExtraRows(FormBuilder builder) {
            myClassLabel = RubyRunConfigurationFragmentUtil.addRow(builder, RubyApiLocalize.runConfigurationTestsMessagesClassName(), myClassBox);
            myMethodLabel = RubyRunConfigurationFragmentUtil.addRow(builder, RubyApiLocalize.runConfigurationTestsMessagesMethodName(), myMethodBox);
        }

        @RequiredUIAccess
        @Override
        protected void updateTestType(TestType testType) {
            super.updateTestType(testType);

            setRowVisible(myClassLabel, myClassBox, testType == TestType.TEST_CLASS || testType == TestType.TEST_METHOD);
            setRowVisible(myMethodLabel, myMethodBox, testType == TestType.TEST_METHOD);
        }

        @RequiredUIAccess
        @Override
        public void reset(RTestsRunConfiguration target) {
            myClassBox.setValue(StringUtil.notNullize(target.getTestQualifiedClassName()));
            myMethodBox.setValue(StringUtil.notNullize(target.getTestMethodName()));

            super.reset(target);
        }

        @RequiredUIAccess
        @Override
        public void apply(RTestsRunConfiguration target) {
            super.apply(target);

            target.setTestQualifiedClassName(RubyRunConfigurationFragmentUtil.getText(myClassBox));
            target.setTestMethodName(RubyRunConfigurationFragmentUtil.getText(myMethodBox));
        }
    }
}
