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

package org.jetbrains.plugins.ruby.addins.rspec;

import consulo.content.bundle.Sdk;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.logging.Logger;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.addins.gems.GemUtil;
import org.jetbrains.plugins.ruby.addins.gems.GemsRunner;
import org.jetbrains.plugins.ruby.rails.RailsConstants;
import org.jetbrains.plugins.ruby.ruby.lang.psi.iterators.RBlockCall;
import org.jetbrains.plugins.ruby.ruby.lang.psi.methodCall.RCall;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.run.Output;
import org.jetbrains.plugins.ruby.ruby.run.Runner;
import org.jetbrains.plugins.ruby.support.utils.VirtualFileUtil;

import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Sep 5, 2007
 */
public class RSpecUtil
{
	private static final Logger LOG = Logger.getInstance(RSpecUtil.class.getName());

	public static final String RSPEC_PROJECT_SVN_URL = "svn://rubyforge.org/var/svn/rspec/";
	public static final String RSPEC_PROJECT_SVN_TAGS_URL = RSPEC_PROJECT_SVN_URL + "tags/";
	public static final String RSPEC_PLUGIN_NAME = "rspec ";
	public static final String RSPEC_RAILS_PLUGIN_NAME = "rspec_on_rails";
	public static final String RSPEC_TAG_CURRENT = "CURRENT";
	public static final String RSPEC_TAG_TRUNK = "trunk";
	public static final String RSPEC_TAG_REL_PREFIX = "REL_";
	public static final String RSPEC_TAG_XYZ = "X_Y_X";
	public static final String RSPEC_VERSION_PREFIX = "RSpec-";
	public static final String RSPEC_VERSION_ARG = RailsConstants.PARAM_VERSION;
	public static final String SPEC_GENERATOR_NAME = "rspec";

	public static final String SVN_PATH_SEPARATOR = "/";
	@NonNls
	public static final String RSPEC_HOME_PAGE_URL = "http://rspec.rubyforge.org";
	public static final String SPECS_FOLDER = "spec";
	public static final String COLOURED_COMMAND_LINE_ARG = "-c";
	public static final String WIN_32_CONSOLE_GEM = "win32console";

	private static final String SPEC_TEST_FILE_SUFFIX = "_spec.rb";

	public static boolean checkIfRSpecGemExists(@Nullable final Sdk sdk)
	{
		return GemUtil.isGemExecutableRubyScriptExists(sdk, RailsConstants.RSPEC_GEM_EXECUTABLE);
	}

	public static String getRSpecPluginCurrentUrl()
	{
		return getRSpecPluginUrl(RSPEC_TAG_CURRENT);
	}

	public static String getRSpecPluginTrunkUrl()
	{
		return RSPEC_PROJECT_SVN_URL + RSPEC_TAG_TRUNK + SVN_PATH_SEPARATOR + RSPEC_PLUGIN_NAME;
	}

	public static String getRSpecPluginXYZUrl()
	{
		return getRSpecPluginUrl(RSPEC_TAG_XYZ);
	}

	public static String getRSpecRailsPluginCurrentUrl()
	{
		return getRSpecRailsPluginUrl(RSPEC_TAG_CURRENT);
	}

	public static String getRSpecRailsPluginTrunkUrl()
	{
		return RSPEC_PROJECT_SVN_URL + RSPEC_TAG_TRUNK + SVN_PATH_SEPARATOR + RSPEC_RAILS_PLUGIN_NAME;
	}

	public static String getRSpecRailsPluginXYZUrl()
	{
		return getRSpecRailsPluginUrl(RSPEC_TAG_XYZ);
	}

	@Nullable
	public static String getRSpecGemVersion(@Nonnull final Sdk sdk, final boolean runWithModalProgress, @Nullable final Function<Object, Boolean> shouldCancelFun)
	{
		final Output output;
		try
		{
			final String progressTitle = RBundle.message("execution.get.rspec.gem.version");
			final Runner.ExecutionMode mode;
			if(runWithModalProgress)
			{
				mode = new Runner.ModalProgressMode(progressTitle);
			}
			else
			{
				mode = new Runner.SameThreadMode(progressTitle);
			}
			if(shouldCancelFun != null)
			{
				mode.setShouldCancelFun(shouldCancelFun);
			}
			output = GemsRunner.runGemsExecutableScript(sdk, null, RailsConstants.RSPEC_GEM_EXECUTABLE, null, mode, false, null, RSPEC_VERSION_ARG);
		}
		catch(Exception e)
		{
			LOG.error(e);
			return null;
		}
		if(output == null)
		{
			return null;
		}
		final String stdout = output.getStdout();
		if(TextUtil.isEmpty(stdout))
		{
			return null;
		}
		if(!stdout.contains(RSPEC_VERSION_PREFIX))
		{
			return null;
		}
		final int start = stdout.indexOf(RSPEC_VERSION_PREFIX);
		int end = start + RSPEC_VERSION_PREFIX.length();
		for(; end < stdout.length(); end++)
		{
			if(Character.isWhitespace(stdout.charAt(end)))
			{
				break;
			}
		}
		if(end != start)
		{
			return stdout.substring(start, end);
		}
		return null;
	}

	@Nullable
	public static String getRSpecGemExecutablePath(@Nonnull final Sdk sdk)
	{
		return GemUtil.getGemExecutableRubyScriptPath(sdk, RailsConstants.RSPEC_GEM_EXECUTABLE);
	}

	public static String getRSpecGemTag(@Nullable final String rspecVersion)
	{
		final String rSpecVersionTag = rspecVersion == null || !rspecVersion.startsWith(RSPEC_VERSION_PREFIX) ? RSPEC_TAG_XYZ : rspecVersion.substring(RSPEC_VERSION_PREFIX.length()).replaceAll("\\.", "_");
		return RSPEC_TAG_REL_PREFIX + rSpecVersionTag;
	}

	public static String getRSpecPluginUrl(final String tag)
	{
		return RSPEC_PROJECT_SVN_TAGS_URL + tag + SVN_PATH_SEPARATOR + RSPEC_PLUGIN_NAME;
	}

	public static String getRSpecRailsPluginUrl(final String tag)
	{
		return RSPEC_PROJECT_SVN_TAGS_URL + tag + SVN_PATH_SEPARATOR + RSPEC_RAILS_PLUGIN_NAME;
	}

 
   /* Requires root privilegies
*/

	@Nonnull
	public static String getRailsSpecFolderPathOrUrl(@Nonnull final String railsAppRootPathOrUlr)
	{
		return railsAppRootPathOrUlr + VirtualFileUtil.VFS_PATH_SEPARATOR + SPECS_FOLDER;
	}

	@Nonnull
	public static String getRailsSpecScriptPathOrUrl(@Nonnull final String railsAppRootPathOrUlr)
	{
		return railsAppRootPathOrUlr + VirtualFileUtil.VFS_PATH_SEPARATOR + RailsConstants.SPECS_SCRIPT_PATH;
	}

	@SuppressWarnings({"BooleanMethodIsAlwaysInverted"})
	public static boolean isSpecScriptSupportInstalledInRailsProject(@Nonnull final String rorAppliactionContentRootUrl)
	{
		final VirtualFileManager manager = VirtualFileManager.getInstance();
		final VirtualFile specFolder = manager.findFileByUrl(getRailsSpecFolderPathOrUrl(rorAppliactionContentRootUrl));
		if(VirtualFileUtil.fileExists(specFolder))
		{
			final VirtualFile specScript = manager.findFileByUrl(getRailsSpecScriptPathOrUrl(rorAppliactionContentRootUrl));
			return VirtualFileUtil.fileExists(specScript);
		}
		return false;
	}

	public static boolean isRSpecTestFile(@Nullable final VirtualFile file)
	{
		//noinspection SimplifiableIfStatement
		return file != null && !file.isDirectory() && isFileWithRSpecTestFileName(file);
	}

	private static final java.util.Set<String> EXAMPLE_AND_GROUP_METHODS = java.util.Set.of(
			"describe", "context", "feature", "xdescribe", "xcontext", "fdescribe", "fcontext",
			"it", "specify", "example", "scenario", "its", "xit", "xspecify", "xexample", "xscenario", "fit", "fspecify", "fexample", "focus");

	/**
	 * @return the innermost example or example group (<code>describe</code>, <code>it</code>, ...) around the element
	 */
	@Nullable
	public static RBlockCall findExampleOrGroup(@Nonnull final PsiElement element)
	{
		RBlockCall blockCall = PsiTreeUtil.getParentOfType(element, RBlockCall.class, false);
		while(blockCall != null && !isExampleOrGroup(blockCall))
		{
			blockCall = PsiTreeUtil.getParentOfType(blockCall, RBlockCall.class, true);
		}
		return blockCall;
	}

	/**
	 * @return true for an example or an example group: <code>describe "..." do</code>, <code>RSpec.describe X do</code>, <code>it "..." do</code>
	 */
	public static boolean isExampleOrGroup(@Nonnull final RBlockCall blockCall)
	{
		if(!(blockCall.getCall() instanceof RCall call))
		{
			return false;
		}
		final String command = call.getCommand();
		return EXAMPLE_AND_GROUP_METHODS.contains(command.substring(command.lastIndexOf('.') + 1));
	}

	/**
	 * @return true if the folder or one of its subfolders holds a spec file
	 */
	public static boolean containsSpecFiles(@Nonnull final VirtualFile folder)
	{
		return !consulo.virtualFileSystem.util.VirtualFileUtil.iterateChildrenRecursively(folder, null, file -> !isRSpecTestFile(file));
	}

	public static boolean isFileWithRSpecTestFileName(VirtualFile file)
	{
		return file.getName().endsWith(SPEC_TEST_FILE_SUFFIX);
	}
}
