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
import consulo.module.ui.BundleBox;
import consulo.module.ui.BundleBoxBuilder;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.Project;
import consulo.ruby.api.localize.RubyApiLocalize;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.util.lang.StringUtil;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkType;

/**
 * An alternative Ruby SDK instead of the SDK of the module.
 */
public class RubySdkFragment<T extends AbstractRubyRunConfiguration> implements RunConfigurationFragment<T> {
    @Override
    public String getId() {
        return "rubySdk";
    }

    @Override
    public LocalizeValue getName() {
        return RubyApiLocalize.runConfigurationDialogComponentsSdk();
    }

    @Override
    public int getOrder() {
        return StandardRunConfigurationFragmentOrder.MODULE + 10;
    }

    @Override
    public boolean isVisibleByDefault(T target) {
        return target.shouldUseAlternativeSdk();
    }

    @Override
    public void applyHidden(T target) {
        target.setShouldUseAlternativeSdk(false);
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<T> createEditor(Project project, Disposable parent) {
        BundleBox sdkBox = BundleBoxBuilder.create(parent)
            .withSdkTypeFilterByClass(RubySdkType.class)
            .withNoneItem(RubyApiLocalize.runConfigurationSdkFromModule(), PlatformIconGroup.nodesModule())
            .build();

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(
                    LocalizeValue.join(RubyApiLocalize.runConfigurationDialogComponentsSdk(), LocalizeValue.colon()),
                    sdkBox.getComponent()
                );
            }

            @RequiredUIAccess
            @Override
            public void reset(T target) {
                sdkBox.setSelectedBundle(target.shouldUseAlternativeSdk() ? StringUtil.nullize(target.getAlternativeSdkName()) : null);
            }

            @RequiredUIAccess
            @Override
            public void apply(T target) {
                BundleBox.BundleBoxItem sdkItem = sdkBox.getComponent().getValue();
                if (sdkItem == null || sdkItem instanceof BundleBox.NullBundleBoxItem) {
                    target.setShouldUseAlternativeSdk(false);
                }
                else {
                    target.setShouldUseAlternativeSdk(true);
                    target.setAlternativeSdkName(sdkItem.getBundleName());
                }
            }
        };
    }
}
