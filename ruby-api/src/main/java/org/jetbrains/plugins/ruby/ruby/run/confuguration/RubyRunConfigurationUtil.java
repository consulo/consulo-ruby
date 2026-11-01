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

package org.jetbrains.plugins.ruby.ruby.run.confuguration;

import consulo.ruby.runtime.RubyRuntime;
import consulo.ruby.runtime.SdkRubyRuntime;
import java.io.File;

import consulo.execution.RunManager;
import jakarta.annotation.Nullable;

import consulo.execution.RunnerAndConfigurationSettings;
import consulo.execution.configuration.RunConfiguration;
import consulo.language.psi.PsiElement;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.cache.RCacheUtil;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RFile;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript.RubyRunConfiguration;
import org.jetbrains.plugins.ruby.ruby.run.confuguration.rubyScript.RubyRunConfigurationFactory;
import org.jetbrains.plugins.ruby.ruby.sdk.RubySdkUtil;
import consulo.process.ExecutionException;
import consulo.module.Module;
import consulo.content.bundle.Sdk;
import consulo.language.psi.PsiFile;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Sep 18, 2006
 */
public class RubyRunConfigurationUtil
{
	/**
	 * Creates new run configuration settings with configuration of specified factory.
	 *
	 * @param project Project
	 * @param factory RunConfiguration Factory
	 * @param name    Name for new run configuration
	 * @return Run settings, able to register in RunManagerEx
	 */
	public static RunnerAndConfigurationSettings create(final Project project, final RubyRunConfigurationFactory factory, final String name)
	{
		return RunManager.getInstance(project).createRunConfiguration(name, factory);
	}

	public static String collectArguments(final String[] args)
	{
		final StringBuilder buff = new StringBuilder();
		for(String arg : args)
		{
			if(buff.length() != 0)
			{
				buff.append(TextUtil.SPACE_STRING);
			}
			buff.append(arg);
		}
		return buff.toString();
	}

	public static void inspectWorkingDirectory(final AbstractRubyRunConfiguration conf, final boolean isExecution) throws Exception
	{
		// Working directory inspection
		if(conf.getWorkingDirectory().trim().length() != 0)
		{
			File workDir = new File(conf.getWorkingDirectory());
			if(!workDir.exists())
			{
				RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.working.directory.not.exists"), isExecution);
			}

			if(!workDir.isDirectory())
			{
				RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.working.directory.error"), isExecution);
			}
		}
	}

	public static void inspectSDK(final AbstractRubyRunConfiguration conf, final boolean isExecution) throws Exception
	{
		final RubyRuntime runtime = conf.getRuntime();
		if(runtime == null)
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("sdk.no.specified"), isExecution);
		}
		if(runtime instanceof SdkRubyRuntime sdkRuntime && !RubySdkUtil.isSDKHomeExist(sdkRuntime.getSdk()))
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("sdk.type.incorrect.homepath"), isExecution);
		}
	}

	public static void inspectModule(final AbstractRubyRunConfiguration conf, final boolean isExecution) throws Exception
	{
		// Module inspection
		if(conf.getModule() == null)
		{
			RubyRunConfigurationUtil.throwExecutionOrRuntimeException(RBundle.message("run.configuration.no.module.specified"), isExecution);
		}
	}

	public static void throwExecutionOrRuntimeException(final String msg, final boolean isExecution) throws Exception
	{
		if(isExecution)
		{
			throw new ExecutionException(msg);
		}
		throw new RuntimeException(msg);
	}

	//    public static void inspectAlternativeSdk(@NotNull final AbstractRubyRunConfiguration configuration,
	//                                             final boolean isExecution) throws Exception {
	//        if (configuration.shouldUseAlternativeSdk()) {
	//            final Sdk sdk = configuration.getAlternativeSdk();
	//            if (!RubySdkType.isKindOfRubySDK(sdk)) {
	//                throwExecutionOrRuntimeException(RBundle.message("run.configuration.no.alternative.skd.specified"), isExecution);
	//            }
	//        }
	//    }

	/**
	 * Changes RubyArgs, if it hasn't been already set in conf
	 *
	 * @param conf     RunConfiguration
	 * @param rubyArgs New ruby args
	 */
	public static void setupRubyArgs(final AbstractRubyRunConfiguration conf, final String rubyArgs)
	{
		if(TextUtil.isEmptyOrWhitespaces(conf.getRubyArgs()))
		{
			conf.setRubyArgs(rubyArgs);
		}
	}

	@Nullable
	public static Module getCorrespondingModule(final Project project, final VirtualFile file, final AbstractRubyRunConfiguration configuration)
	{
		// search module for file
		final Module candidate_module = RCacheUtil.getModuleByFile(file, project);

		return candidate_module;
		//TODO it's buggy style!!! Remove!
		//        // chooses first valid module
		//        final Module[] modules = configuration.getModules();
		//        if (modules.length > 0) {
		//            if (RModuleUtil.hasRubySupport(candidate_module)) {
		//                for (Module module : modules) {
		//                    if (candidate_module == module) {
		//                        return candidate_module;
		//                    }
		//                }
		//            }
		//            return modules[0];
		//        }
		// return null;
	}

	public static boolean isConfigurationByElement(final RunConfiguration configuration, final PsiElement element)
	{
		// Check is configuration is Ruby Run configuration
		if(!(configuration instanceof RubyRunConfiguration))
		{
			return false;
		}

		final String configurationScriptPath = ((RubyRunConfiguration) configuration).getScriptPath();

		final PsiFile containingFile = element.getContainingFile();
		if(containingFile == null || !(containingFile instanceof RFile))
		{
			return false;
		}

		final VirtualFile vFile = containingFile.getVirtualFile();
		//noinspection SimplifiableIfStatement
		if(vFile == null)
		{
			return false;
		}

		return configurationScriptPath.trim().equals(vFile.getPath().trim());
	}
}
