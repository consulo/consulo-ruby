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

package org.jetbrains.plugins.ruby.rails.run;

import consulo.content.bundle.Sdk;
import consulo.execution.ui.console.Filter;
import consulo.module.Module;
import consulo.process.ExecutionException;
import consulo.process.event.ProcessListener;
import consulo.ui.ex.action.AnAction;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.rails.facet.RailsFacetUtil;
import org.jetbrains.plugins.ruby.ruby.run.CommandLineArgumentsProvider;
import org.jetbrains.plugins.ruby.ruby.run.RubyScriptRunner;
import org.jetbrains.plugins.ruby.ruby.run.RunContentDescriptorFactory;
import org.jetbrains.plugins.ruby.support.utils.RModuleUtil;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Apr 16, 2008
 */
public class RailsScriptRunner
{
	/**
	 * @param module                Commited module
	 * @param processListener       Listener
	 * @param consoleFilters        Filters
	 * @param userActions           Action
	 * @param runInBackgroundThread To Run in background
	 * @param consoleTitle          Title of console
	 * @param provider              Arguments provider
	 * @param descFactory           Content descriptor provider
	 */
	public static void runRailsScriptInCosole(@Nonnull final Module module, @Nullable final ProcessListener processListener, @Nullable final Filter[] consoleFilters, @Nullable final AnAction[] userActions, final boolean runInBackgroundThread, @Nonnull final String consoleTitle, @Nonnull final CommandLineArgumentsProvider provider, @Nullable final RunContentDescriptorFactory descFactory)
	{
		runRailsScriptInCosole(RModuleUtil.getModuleSdk(module), module, processListener, consoleFilters, userActions, runInBackgroundThread, consoleTitle, provider, descFactory);
	}

	/**
	 * @param sdk                   SDK
	 * @param uncommitedModule      Uncommited module (i.e. may be without SDK)
	 * @param processListener       Listener
	 * @param consoleFilters        Filters
	 * @param userActions           Action
	 * @param runInBackgroundThread To Run in background
	 * @param consoleTitle          Title of console
	 * @param provider              Arguments provider
	 * @param descFactory           Content descriptor provider
	 */
	public static void runRailsScriptInCosole(@Nullable final Sdk sdk, @Nonnull final Module uncommitedModule, @Nullable final ProcessListener processListener, @Nullable final Filter[] consoleFilters, @Nullable final AnAction[] userActions, final boolean runInBackgroundThread, @Nonnull final String consoleTitle, @Nonnull final CommandLineArgumentsProvider provider, @Nullable final RunContentDescriptorFactory descFactory)
	{
		try
		{
			validateModule(uncommitedModule);
		}
		catch(ExecutionException e)
		{
			RubyScriptRunner.showExecutionErrorDialog(e);
			return;
		}

		RubyScriptRunner.runRubyScriptInCosole(processListener, consoleFilters, userActions, runInBackgroundThread, consoleTitle, provider, descFactory, RailsFacetUtil.getRailsAppHomeDirPath(uncommitedModule), sdk, uncommitedModule.getProject());
	}

	private static void validateModule(final Module module) throws ExecutionException
	{
		if(module != null)
		{
			if(!RModuleUtil.hasRubySupport(module))
			{
				throw new ExecutionException(RBundle.message("error.module.ror.valid.expected", module));
			}
		}
	}
}
