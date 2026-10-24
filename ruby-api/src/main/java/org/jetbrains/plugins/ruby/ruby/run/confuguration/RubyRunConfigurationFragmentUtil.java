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
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.process.cmd.ParametersListUtil;
import consulo.project.Project;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.TextBoxWithExpandAction;
import consulo.ui.ValueComponent;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.util.FormBuilder;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import org.jspecify.annotations.Nullable;

public final class RubyRunConfigurationFragmentUtil {
    private RubyRunConfigurationFragmentUtil() {
    }

    @RequiredUIAccess
    public static FileChooserTextBoxBuilder.Controller createPathBox(
        Project project,
        Disposable uiDisposable,
        FileChooserDescriptor descriptor,
        LocalizeValue dialogTitle
    ) {
        return FileChooserTextBoxBuilder.create(project)
            .fileChooserDescriptor(descriptor)
            .dialogTitle(dialogTitle)
            .uiDisposable(uiDisposable)
            .build();
    }

    @RequiredUIAccess
    public static TextBoxWithExpandAction createArgumentsBox(LocalizeValue dialogTitle) {
        return TextBoxWithExpandAction.create(
            PlatformIconGroup.actionsShow(),
            dialogTitle.get(),
            ParametersListUtil.DEFAULT_LINE_PARSER,
            ParametersListUtil.DEFAULT_LINE_JOINER
        );
    }

    @RequiredUIAccess
    public static Label addRow(FormBuilder builder, LocalizeValue text, Component field) {
        Label label = Label.create(text);
        builder.addLabeled(label, field);
        return label;
    }

    @RequiredUIAccess
    public static void setRowVisible(Label label, Component field, boolean visible) {
        label.setVisible(visible);
        field.setVisible(visible);
    }

    @RequiredUIAccess
    public static void setPath(FileChooserTextBoxBuilder.Controller box, @Nullable String path) {
        box.setValue(FileUtil.toSystemDependentName(StringUtil.notNullize(path)));
    }

    @RequiredUIAccess
    public static String getPath(FileChooserTextBoxBuilder.Controller box) {
        return FileUtil.toSystemIndependentName(StringUtil.notNullize(box.getValue()).trim());
    }

    @RequiredUIAccess
    public static String getText(ValueComponent<String> box) {
        return StringUtil.notNullize(box.getValue()).trim();
    }
}
