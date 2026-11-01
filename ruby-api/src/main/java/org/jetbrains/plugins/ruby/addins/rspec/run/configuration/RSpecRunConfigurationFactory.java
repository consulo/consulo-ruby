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

import consulo.localize.LocalizeValue;

import consulo.execution.configuration.RunConfiguration;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.addins.rspec.RSpecIcons;
import org.jetbrains.plugins.ruby.ruby.RubyUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript.RubyRunConfigurationFactory;
import consulo.execution.configuration.ConfigurationType;
import consulo.project.Project;
import consulo.ui.image.Image;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Oct 18, 2007
 */
public class RSpecRunConfigurationFactory extends RubyRunConfigurationFactory
{
	public RSpecRunConfigurationFactory(final ConfigurationType type)
	{
		super(type);
	}

	@Override
	public RunConfiguration createTemplateConfiguration(final Project project)
	{
		final RSpecRunConfiguration conf = new RSpecRunConfiguration(project, this, "");

		conf.setRubyArgs(RubyRunConfigurationUtil.collectArguments(RubyUtil.RUN_IN_CONSOLE_HACK_ARGUMENTS));
		conf.setTestFileMask(RSpecRunConfiguration.DEFAULT_TESTS_SEARCH_MASK);
		return conf;
	}

	@Override
	public LocalizeValue getDisplayName()
	{
		return LocalizeValue.localizeTODO(RBundle.message("rspec.run.configuration.rspec.name"));
	}

	@Override
	public Image getIcon()
	{
		return RSpecIcons.RUN_CONFIGURATION_ICON;
	}
}
