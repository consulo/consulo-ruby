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
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.configuration.fragment.StandardRunConfigurationFragmentOrder;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.RadioGroup;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import consulo.util.lang.StringUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration.TestType;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationFragmentUtil;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * The test kind and its folder, file mask or script of a Ruby test configuration.
 */
public abstract class AbstractRTestsTargetFragment<T extends AbstractRubyRunConfiguration & AbstractRTestsRunConfigurationParams>
    implements RunConfigurationFragment<T> {

    @Override
    public String getId() {
        return "rubyTestTarget";
    }

    @Override
    public int getOrder() {
        return StandardRunConfigurationFragmentOrder.FIRST;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    protected abstract List<TestType> getTestTypes();

    protected abstract LocalizeValue getTestTypeName(TestType testType);

    protected abstract LocalizeValue getTestTypeLabel();

    protected abstract LocalizeValue getFolderLabel();

    protected abstract LocalizeValue getFileMaskLabel();

    protected abstract LocalizeValue getScriptLabel();

    protected abstract String getDefaultFileMask();

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<T> createEditor(Project project, Disposable parent) {
        return createTargetEditor(project, parent);
    }

    @RequiredUIAccess
    protected TargetEditor createTargetEditor(Project project, Disposable parent) {
        return new TargetEditor(project, parent);
    }

    protected class TargetEditor implements RunConfigurationFragmentEditor<T> {
        private final RadioGroup<TestType> myTestTypeGroup;
        private final FileChooserTextBoxBuilder.Controller myFolderBox;
        private final TextBox myFileMaskBox;
        private final FileChooserTextBoxBuilder.Controller myScriptBox;

        private @Nullable Label myFolderLabel;
        private @Nullable Label myFileMaskLabel;
        private @Nullable Label myScriptLabel;

        @RequiredUIAccess
        protected TargetEditor(Project project, Disposable parent) {
            myTestTypeGroup = RadioGroup.create();
            myFolderBox = RubyRunConfigurationFragmentUtil.createPathBox(
                project,
                parent,
                FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                RubyApiLocalize.runConfigurationMessagesSelectFolderPath()
            );
            myFileMaskBox = TextBox.create();
            myScriptBox = RubyRunConfigurationFragmentUtil.createPathBox(
                project,
                parent,
                FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor(),
                RubyApiLocalize.runConfigurationMessagesSelectRubySciptPath()
            );

            myTestTypeGroup.addValueListener(this::onTestTypeSelected);
        }

        @RequiredUIAccess
        @Override
        public void addRows(RunConfigurationFragmentLayout layout) {
            FormBuilder builder = FormBuilder.create();
            builder.addLabeled(getTestTypeLabel(), myTestTypeGroup.fillHorizontal(getTestTypes(), AbstractRTestsTargetFragment.this::getTestTypeName));

            myFolderLabel = RubyRunConfigurationFragmentUtil.addRow(builder, getFolderLabel(), myFolderBox.getComponent());
            myFileMaskLabel = RubyRunConfigurationFragmentUtil.addRow(builder, getFileMaskLabel(), myFileMaskBox);
            myScriptLabel = RubyRunConfigurationFragmentUtil.addRow(builder, getScriptLabel(), myScriptBox.getComponent());

            addExtraRows(builder);

            layout.addBottom(builder.build());
        }

        @RequiredUIAccess
        protected void addExtraRows(FormBuilder builder) {
        }

        @RequiredUIAccess
        protected void updateTestType(TestType testType) {
            boolean allInFolder = testType == TestType.ALL_IN_FOLDER;

            setRowVisible(myFolderLabel, myFolderBox.getComponent(), allInFolder);
            setRowVisible(myFileMaskLabel, myFileMaskBox, allInFolder);
            setRowVisible(myScriptLabel, myScriptBox.getComponent(), !allInFolder);
        }

        @RequiredUIAccess
        protected final void setRowVisible(@Nullable Label label, Component field, boolean visible) {
            if (label != null) {
                RubyRunConfigurationFragmentUtil.setRowVisible(label, field, visible);
            }
            else {
                field.setVisible(visible);
            }
        }

        @RequiredUIAccess
        private void onTestTypeSelected(@Nullable TestType testType) {
            if (testType == null) {
                return;
            }

            if (testType == TestType.ALL_IN_FOLDER && StringUtil.isEmpty(myFileMaskBox.getValue())) {
                myFileMaskBox.setValue(getDefaultFileMask());
            }

            updateTestType(testType);
        }

        @RequiredUIAccess
        @Override
        public void reset(T target) {
            RubyRunConfigurationFragmentUtil.setPath(myFolderBox, target.getTestsFolderPath());
            myFileMaskBox.setValue(StringUtil.notNullize(target.getTestFileMask()));
            RubyRunConfigurationFragmentUtil.setPath(myScriptBox, target.getTestScriptPath());

            TestType testType = target.getTestType();
            myTestTypeGroup.setValue(testType, false);
            updateTestType(testType);
        }

        @RequiredUIAccess
        @Override
        public void apply(T target) {
            TestType testType = myTestTypeGroup.getValue();
            if (testType != null) {
                target.setTestType(testType);
            }
            target.setTestsFolderPath(RubyRunConfigurationFragmentUtil.getPath(myFolderBox));
            target.setTestFileMask(RubyRunConfigurationFragmentUtil.getText(myFileMaskBox));
            target.setTestScriptPath(RubyRunConfigurationFragmentUtil.getPath(myScriptBox));
        }
    }
}
