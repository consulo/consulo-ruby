/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.plugins.ruby.ruby.sdk;

import consulo.configurable.ConfigurationException;
import consulo.content.bundle.AdditionalDataConfigurable;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkModificator;
import consulo.disposer.Disposable;
import consulo.ui.Component;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.awtUnsafe.TargetAWT;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.sdk.ui.RubySdkConfigurablePanel;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: Nov 28, 2007
 */
public class RubySdkConfigurable implements AdditionalDataConfigurable {
    private Sdk mySdk;
    private RubySdkConfigurablePanel myRubyConfigurablePanel;

    private String myGemsPath;

    @Nullable
    public String getGemsPath() {
        return myGemsPath;
    }

    public void setGemsPath(@Nullable final String gemsPath) {
        myGemsPath = gemsPath;
    }

    public RubySdkConfigurable() {
        myRubyConfigurablePanel = new RubySdkConfigurablePanel();
    }

    @Override
    public void setSdk(@Nonnull final Sdk sdk) {
        mySdk = sdk;
    }

    @RequiredUIAccess
    @Override
    public Component createUIComponent(Disposable disposable) {
        return TargetAWT.wrap(myRubyConfigurablePanel.getPanel());
    }

    @RequiredUIAccess
    @Override
    public boolean isModified() {
        return !RubySdkUtil.getGemsBinFolderPath(mySdk).equals(myRubyConfigurablePanel.getGemsBinFolder());
    }

    @RequiredUIAccess
    @Override
    public void apply() throws ConfigurationException {
        //Gems bin folder
        final String newGemsBinFolder = myRubyConfigurablePanel.getGemsBinFolder();
        ((RubySdkType) mySdk.getSdkType()).setGemsBinDirectory(mySdk, newGemsBinFolder);

        //Patch source roots according to classpath
        final SdkModificator modificator = mySdk.getSdkModificator();

        RubySdkType.findAndSaveGemsRootsBy(modificator);

        modificator.commitChanges();

        // Change libraries facet libraries
//		IdeaInternalUtil.runInsideWriteAction(new ThrowableRunnable<Exception>()
//		{
//			@Override
//			public void run() throws Exception
//			{
//				JRubySdkTableListener.updateLibrary(mySdk.getName(), modificator.getRoots(OrderRootType.CLASSES));
//			}
//		});
    }

    @RequiredUIAccess
    @Override
    public void reset() {
        final RubySdkType type = (RubySdkType) mySdk.getSdkType();
        myRubyConfigurablePanel.setRubyText(type.getVMExecutablePath(mySdk));
        myRubyConfigurablePanel.setGemsBinFolder(RubySdkUtil.getGemsBinFolderPath(mySdk));
        myRubyConfigurablePanel.getPanel().repaint();
    }

    @RequiredUIAccess
    @Override
    public void disposeUIResources() {
        // do nothing. we don`t have them
    }

}
