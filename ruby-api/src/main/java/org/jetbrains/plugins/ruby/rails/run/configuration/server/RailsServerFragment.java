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
package org.jetbrains.plugins.ruby.rails.run.configuration.server;

import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.execution.configuration.fragment.StandardRunConfigurationFragmentOrder;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.ComboBox;
import consulo.ui.RadioGroup;
import consulo.ui.TextBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.DockLayout;
import consulo.ui.layout.HorizontalLayout;
import consulo.util.lang.StringUtil;
import org.jetbrains.plugins.ruby.rails.run.configuration.server.RailsServerRunConfiguration.RailsEnvironmentType;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyModuleComboBox;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationFragmentUtil;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Rails application module, its server script and the server address and environment.
 */
public class RailsServerFragment implements RunConfigurationFragment<RailsServerRunConfiguration> {
    @Override
    public String getId() {
        return "railsServer";
    }

    @Override
    public LocalizeValue getName() {
        return RubyApiLocalize.runConfigurationServerTitle();
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
    public RunConfigurationFragmentEditor<RailsServerRunConfiguration> createEditor(Project project, Disposable parent) {
        Map<String, @Nullable String> serverScriptPaths = new HashMap<>();

        RubyModuleComboBox moduleBox = new RubyModuleComboBox();
        FileChooserTextBoxBuilder.Controller scriptPathBox = RubyRunConfigurationFragmentUtil.createPathBox(
            project,
            parent,
            FileChooserDescriptorFactory.createSingleFileNoJarsDescriptor(),
            RubyApiLocalize.runConfigurationMessagesSelectRubySciptPath()
        );
        RadioGroup<Boolean> choosePortManuallyGroup = RadioGroup.create();
        TextBox portBox = TextBox.create();
        TextBox ipAddressBox = TextBox.create();
        ComboBox<RailsEnvironmentType> environmentBox = ComboBox.create(RailsEnvironmentType.values());
        environmentBox.setTextRenderer(type -> type == null ? LocalizeValue.empty() : LocalizeValue.of(type.getParamName()));

        choosePortManuallyGroup.addValueListener(manually -> portBox.setEnabled(Boolean.TRUE.equals(manually)));
        moduleBox.addModuleListener(moduleName -> RubyRunConfigurationFragmentUtil.setPath(scriptPathBox, serverScriptPaths.get(moduleName)));

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(RubyApiLocalize.runConfigurationModuleLabel(), moduleBox.getComponent());
                layout.addLabeled(RubyApiLocalize.runConfigurationMessagesScriptPath(), scriptPathBox.getComponent());

                HorizontalLayout portChoiceLayout = choosePortManuallyGroup.fillHorizontal(
                    List.of(Boolean.FALSE, Boolean.TRUE),
                    manually -> manually
                        ? RubyApiLocalize.runConfigurationServerDialogManuallyPort()
                        : RubyApiLocalize.runConfigurationServerDialogUseFreePort()
                );
                layout.addLabeled(
                    LocalizeValue.join(RubyApiLocalize.runConfigurationServerDialogPort(), LocalizeValue.colon()),
                    DockLayout.create().left(portChoiceLayout).center(portBox)
                );
                layout.addLabeled(LocalizeValue.join(RubyApiLocalize.runConfigurationServerDialogIp(), LocalizeValue.colon()), ipAddressBox);
                layout.addLabeled(
                    LocalizeValue.join(RubyApiLocalize.runConfigurationServerDialogEnvironment(), LocalizeValue.colon()),
                    environmentBox
                );
            }

            @RequiredUIAccess
            @Override
            public void reset(RailsServerRunConfiguration target) {
                Module[] modules = target.getModules();
                serverScriptPaths.clear();
                for (Module module : modules) {
                    serverScriptPaths.put(module.getName(), RailsServerRunConfiguration.getServerScriptPathByModule(module));
                }
                moduleBox.reset(modules, target.getModuleName());

                RubyRunConfigurationFragmentUtil.setPath(scriptPathBox, target.getScriptPath());

                boolean choosePortManually = target.isChoosePortManually();
                choosePortManuallyGroup.setValue(choosePortManually, false);
                portBox.setEnabled(choosePortManually);
                portBox.setValue(StringUtil.notNullize(target.getPort()));
                ipAddressBox.setValue(StringUtil.notNullize(target.getIPAddr()));
                environmentBox.setValue(target.getRailsEnvironmentType(), false);
            }

            @RequiredUIAccess
            @Override
            public void apply(RailsServerRunConfiguration target) {
                target.setModuleName(moduleBox.getModuleName());
                target.setScriptPath(RubyRunConfigurationFragmentUtil.getPath(scriptPathBox));
                target.setChoosePortManually(Boolean.TRUE.equals(choosePortManuallyGroup.getValue()));
                target.setPort(RubyRunConfigurationFragmentUtil.getText(portBox));
                target.setIPAddr(RubyRunConfigurationFragmentUtil.getText(ipAddressBox));

                RailsEnvironmentType environmentType = environmentBox.getValue();
                if (environmentType != null) {
                    target.setRailsEnvironmentType(environmentType);
                }
            }
        };
    }
}
