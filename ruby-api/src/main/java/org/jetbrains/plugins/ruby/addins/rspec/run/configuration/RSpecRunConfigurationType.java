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

package org.jetbrains.plugins.ruby.addins.rspec.run.configuration;

import consulo.annotation.component.ExtensionImpl;
import consulo.execution.RunManager;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.action.Location;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.ConfigurationTypeUtil;
import consulo.execution.configuration.ConfigurationType;
import consulo.execution.configuration.RunConfiguration;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.localize.LocalizeValue;
import consulo.module.Module;
import consulo.navigation.OpenFileDescriptor;
import consulo.project.Project;
import consulo.ui.image.Image;
import consulo.virtualFileSystem.VirtualFile;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.addins.rspec.RSpecIcons;
import org.jetbrains.plugins.ruby.addins.rspec.RSpecModuleSettings;
import org.jetbrains.plugins.ruby.addins.rspec.RSpecUtil;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;
import org.jetbrains.plugins.ruby.ruby.RubyUtil;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyPsiUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.AbstractRubyRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationUtil;
import org.jetbrains.plugins.ruby.support.utils.RModuleUtil;
import org.jetbrains.plugins.ruby.support.utils.RubyVirtualFileScanner;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: May 8, 2008
 */
@ExtensionImpl
public class RSpecRunConfigurationType implements ConfigurationType
{
	private final RSpecRunConfigurationFactory myRSpecTestsFactory;

	public RSpecRunConfigurationType()
	{
		myRSpecTestsFactory = new RSpecRunConfigurationFactory(this);
	}

	public static RSpecRunConfigurationType getInstance()
	{
		return ConfigurationTypeUtil.findConfigurationType(RSpecRunConfigurationType.class);
	}

	@Override
	public LocalizeValue getDisplayName()
	{
		return LocalizeValue.localizeTODO(RBundle.message("rspec.run.configuration.type.name"));
	}

	@Override
	public LocalizeValue getConfigurationTypeDescription()
	{
		return LocalizeValue.localizeTODO(RBundle.message("rspec.run.configuration.type.description"));
	}

	@Override
	public Image getIcon()
	{
		return RSpecIcons.RUN_CONFIGURATION_ICON;
	}

	@Nonnull
	@Override
	public String getId()
	{
		return "RSpecRunConfigurationType";
	}

	@Override
	public ConfigurationFactory[] getConfigurationFactories()
	{
		return new ConfigurationFactory[]{myRSpecTestsFactory};
	}

	public RSpecRunConfigurationFactory getFactory()
	{
		return myRSpecTestsFactory;
	}
}
