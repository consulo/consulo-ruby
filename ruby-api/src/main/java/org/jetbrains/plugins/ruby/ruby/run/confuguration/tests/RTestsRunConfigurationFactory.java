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

package org.jetbrains.plugins.ruby.ruby.run.confuguration.tests;

import consulo.localize.LocalizeValue;

import consulo.execution.configuration.ConfigurationType;
import consulo.execution.configuration.RunConfiguration;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.RubyIcons;
import org.jetbrains.plugins.ruby.ruby.RubyUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.RubyRunConfigurationUtil;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript.RubyRunConfigurationFactory;
import consulo.project.Project;
import consulo.ui.image.Image;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 18.07.2007
 */
public class RTestsRunConfigurationFactory extends RubyRunConfigurationFactory
{
	public RTestsRunConfigurationFactory(final ConfigurationType type)
	{
		super(type);
	}

	@Override
	public String getId()
	{
		return "RubyTests";
	}

	@Override
	public RunConfiguration createTemplateConfiguration(final Project project)
	{
		final RTestsRunConfiguration conf = new RTestsRunConfiguration(project, this, "");

		conf.setRubyArgs(RubyRunConfigurationUtil.collectArguments(RubyUtil.RUN_IN_CONSOLE_HACK_ARGUMENTS));
		return conf;
	}

	@Override
	public LocalizeValue getDisplayName()
	{
		return LocalizeValue.localizeTODO(RBundle.message("run.configuration.test.name"));
	}

	@Override
	public Image getIcon()
	{
		return RubyIcons.RTEST_RUN_CONFIGURATION;
	}
}
