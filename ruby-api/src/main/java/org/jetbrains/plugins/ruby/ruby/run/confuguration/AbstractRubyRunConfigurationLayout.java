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
import consulo.execution.configuration.EnvironmentVariablesData;
import consulo.execution.localize.ExecutionLocalize;
import consulo.execution.ui.awt.EnvironmentVariablesTextFieldWithBrowseButton;
import consulo.fileChooser.FileChooserDescriptor;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.process.cmd.ParametersListUtil;
import consulo.project.Project;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.ComboBox;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.TextAttribute;
import consulo.ui.TextBoxWithExpandAction;
import consulo.ui.ValueComponent;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.model.FlatDataModel;
import consulo.ui.model.MutableFlatDataModel;
import consulo.ui.util.FormBuilder;
import consulo.util.io.FileUtil;
import consulo.util.lang.StringUtil;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2026-09-30
 */
public abstract class AbstractRubyRunConfigurationLayout<C extends AbstractRubyRunConfiguration> {
    private static final String NO_MODULE = "";

    private final Project myProject;
    private final Disposable myUIDisposable;

    private final TextBoxWithExpandAction myRubyArgumentsBox;
    private final FileChooserTextBoxBuilder.Controller myWorkingDirectoryBox;
    private final EnvironmentVariablesTextFieldWithBrowseButton myEnvironmentVariablesBox;
    private final Set<String> myModuleNames = new HashSet<>();
    private final MutableFlatDataModel<String> myModuleModel;
    private final ComboBox<String> myModuleBox;
    private final BundleBox mySdkBox;

    @RequiredUIAccess
    protected AbstractRubyRunConfigurationLayout(Project project, Disposable uiDisposable, Module[] modules) {
        myProject = project;
        myUIDisposable = uiDisposable;

        myRubyArgumentsBox = createArgumentsBox(RubyApiLocalize.runConfigurationMessagesEditRubyArgs());
        myWorkingDirectoryBox = createPathBox(
            FileChooserDescriptorFactory.createSingleFolderDescriptor(),
            ExecutionLocalize.selectWorkingDirectoryMessage()
        );
        myEnvironmentVariablesBox = new EnvironmentVariablesTextFieldWithBrowseButton();

        List<String> moduleItems = new ArrayList<>(modules.length + 1);
        moduleItems.add(NO_MODULE);
        for (Module module : modules) {
            myModuleNames.add(module.getName());
            moduleItems.add(module.getName());
        }
        myModuleModel = FlatDataModel.of(moduleItems);
        myModuleBox = ComboBox.create(myModuleModel);
        myModuleBox.setRenderer((presentation, item) -> {
            String name = item.getValue();
            if (StringUtil.isEmpty(name)) {
                presentation.append(RubyApiLocalize.runConfigurationMessagesNone());
            }
            else {
                presentation.withIcon(PlatformIconGroup.nodesModule());
                presentation.append(name, myModuleNames.contains(name) ? TextAttribute.REGULAR : TextAttribute.ERROR);
            }
        });

        mySdkBox = BundleBoxBuilder.create(uiDisposable)
            .withSdkTypeFilterByClass(RubySdkType.class)
            .withNoneItem(RubyApiLocalize.runConfigurationSdkFromModule(), PlatformIconGroup.nodesModule())
            .build();
    }

    @RequiredUIAccess
    public Component build() {
        FormBuilder builder = FormBuilder.create();

        addBefore(builder);

        builder.addLabeled(
            LocalizeValue.join(RubyApiLocalize.runConfigurationMessagesRubyArgs(), LocalizeValue.colon()),
            myRubyArgumentsBox
        );
        builder.addLabeled(ExecutionLocalize.runConfigurationWorkingDirectoryLabel(), myWorkingDirectoryBox.getComponent());
        builder.addLabeled(
            LocalizeValue.join(ExecutionLocalize.environmentVariablesComponentTitle(), LocalizeValue.colon()),
            myEnvironmentVariablesBox.getComponent()
        );
        builder.addLabeled(RubyApiLocalize.runConfigurationModuleLabel(), myModuleBox);
        builder.addLabeled(
            LocalizeValue.join(RubyApiLocalize.runConfigurationDialogComponentsSdk(), LocalizeValue.colon()),
            mySdkBox.getComponent()
        );

        addBottom(builder);

        return builder.build();
    }

    @RequiredUIAccess
    protected void addBefore(FormBuilder builder) {
    }

    @RequiredUIAccess
    protected void addBottom(FormBuilder builder) {
    }

    @RequiredUIAccess
    public void reset(C configuration) {
        myRubyArgumentsBox.setValue(StringUtil.notNullize(configuration.getRubyArgs()));
        setPath(myWorkingDirectoryBox, configuration.getWorkingDirectory());
        myEnvironmentVariablesBox.setData(EnvironmentVariablesData.create(configuration.getEnvs(), configuration.isPassParentEnvs()));

        String moduleName = StringUtil.notNullize(configuration.getModuleName());
        if (myModuleModel.indexOf(moduleName) < 0) {
            myModuleModel.add(moduleName);
        }
        myModuleBox.setValue(moduleName, false);

        mySdkBox.setSelectedBundle(
            configuration.shouldUseAlternativeSdk() ? StringUtil.nullize(configuration.getAlternativeSdkName()) : null
        );
    }

    @RequiredUIAccess
    public void apply(C configuration) {
        configuration.setRubyArgs(getText(myRubyArgumentsBox));
        configuration.setWorkingDirectory(getPath(myWorkingDirectoryBox));
        configuration.setEnvs(new LinkedHashMap<>(myEnvironmentVariablesBox.getEnvs()));
        configuration.setPassParentEnvs(myEnvironmentVariablesBox.isPassParentEnvs());
        configuration.setModuleName(StringUtil.notNullize(myModuleBox.getValue()));

        BundleBox.BundleBoxItem sdkItem = mySdkBox.getComponent().getValue();
        if (sdkItem == null || sdkItem instanceof BundleBox.NullBundleBoxItem) {
            configuration.setShouldUseAlternativeSdk(false);
        }
        else {
            configuration.setShouldUseAlternativeSdk(true);
            configuration.setAlternativeSdkName(sdkItem.getBundleName());
        }
    }

    protected final void addModuleListener(Consumer<String> listener) {
        myModuleBox.addValueListener(event -> listener.accept(StringUtil.notNullize(event.getValue())));
    }

    @RequiredUIAccess
    protected final void setWorkingDirectory(@Nullable String path) {
        setPath(myWorkingDirectoryBox, path);
    }

    @RequiredUIAccess
    protected final FileChooserTextBoxBuilder.Controller createPathBox(FileChooserDescriptor descriptor, LocalizeValue dialogTitle) {
        return FileChooserTextBoxBuilder.create(myProject)
            .fileChooserDescriptor(descriptor)
            .dialogTitle(dialogTitle)
            .uiDisposable(myUIDisposable)
            .build();
    }

    @RequiredUIAccess
    protected static TextBoxWithExpandAction createArgumentsBox(LocalizeValue dialogTitle) {
        return TextBoxWithExpandAction.create(
            PlatformIconGroup.actionsShow(),
            dialogTitle.get(),
            ParametersListUtil.DEFAULT_LINE_PARSER,
            ParametersListUtil.DEFAULT_LINE_JOINER
        );
    }

    @RequiredUIAccess
    protected static Label addRow(FormBuilder builder, LocalizeValue text, Component field) {
        Label label = Label.create(text);
        builder.addLabeled(label, field);
        return label;
    }

    @RequiredUIAccess
    protected static void setRowVisible(@Nullable Label label, Component field, boolean visible) {
        if (label != null) {
            label.setVisible(visible);
        }
        field.setVisible(visible);
    }

    @RequiredUIAccess
    protected static void setPath(FileChooserTextBoxBuilder.Controller box, @Nullable String path) {
        box.setValue(FileUtil.toSystemDependentName(StringUtil.notNullize(path)));
    }

    @RequiredUIAccess
    protected static String getPath(FileChooserTextBoxBuilder.Controller box) {
        return FileUtil.toSystemIndependentName(box.getValue().trim());
    }

    @RequiredUIAccess
    protected static String getText(ValueComponent<String> box) {
        return StringUtil.notNullize(box.getValue()).trim();
    }
}
