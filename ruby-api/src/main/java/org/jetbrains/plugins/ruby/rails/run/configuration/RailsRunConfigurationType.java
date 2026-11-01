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

package org.jetbrains.plugins.ruby.rails.run.configuration;

import consulo.annotation.component.ExtensionImpl;
import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.action.Location;
import consulo.execution.configuration.ConfigurationFactory;
import consulo.execution.configuration.ConfigurationType;
import consulo.execution.configuration.ConfigurationTypeUtil;
import consulo.execution.configuration.RunConfiguration;
import consulo.localize.LocalizeValue;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.rails.RailsIcons;
import org.jetbrains.plugins.ruby.rails.run.configuration.server.RailsServerRunConfigurationFactory;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationUtil;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: May 8, 2008
 */
@ExtensionImpl
public class RailsRunConfigurationType implements ConfigurationType
{
	private final RailsServerRunConfigurationFactory myRailsServerFactory;

	public RailsRunConfigurationType()
	{
		myRailsServerFactory = new RailsServerRunConfigurationFactory(this);
	}

	public static RailsRunConfigurationType getInstance()
	{
		return ConfigurationTypeUtil.findConfigurationType(RailsRunConfigurationType.class);
	}

	@Nonnull
	@Override
	public LocalizeValue getDisplayName()
	{
		return LocalizeValue.localizeTODO(RBundle.message("rails.run.configuration.type.name"));
	}

	@Nonnull
	@Override
	public LocalizeValue getConfigurationTypeDescription()
	{
		return LocalizeValue.localizeTODO(RBundle.message("rails.run.configuration.type.description"));
	}

	@Override
	public Image getIcon()
	{
		return RailsIcons.RAILS_RUN_CONFIGURATION_FOLDER;
	}

	@Nonnull
	@Override
	public String getId()
	{
		return "RailsRunConfigurationType";
	}

	@Override
	public ConfigurationFactory[] getConfigurationFactories()
	{
		return new ConfigurationFactory[]{myRailsServerFactory};
	}

	@Nonnull
	public RailsServerRunConfigurationFactory getServerFactory()
	{
		return myRailsServerFactory;
	}

	//@Override
	public RunnerAndConfigurationSettings createConfigurationByLocation(final Location location)
	{
		return null;
	}

	//@Override
	public boolean isConfigurationByLocation(RunConfiguration runConfiguration, Location location)
	{
		return RubyRunConfigurationUtil.isConfigurationByElement(runConfiguration, location.getPsiElement());

	}
}
