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

package org.jetbrains.plugins.ruby.jruby;

import jakarta.annotation.Nonnull;

import consulo.module.Module;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nullable;

import consulo.language.util.ModuleUtilCore;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: Sep 12, 2007
 */
public class JRubyUtil
{
	@NonNls
	public static final String JAVA = "java";

	/**
	 * The id of the JRuby module extension
	 */
	@NonNls
	public static final String EXTENSION_ID = "jruby";

	@NonNls
	public static final String GROUP_ID = "org.jruby";

	/**
	 * The artifacts of JRuby that a Java build depends on to run Ruby code
	 */
	public static final Set<String> ARTIFACT_IDS = Set.of("jruby", "jruby-complete", "jruby-core", "jruby-base");

	/**
	 * Matches the libraries of build tools: <code>org.jruby:jruby-complete:9.4.8.0</code>, <code>Gradle: org.jruby:jruby-core:9.4.8.0</code>
	 */
	private static final Pattern LIBRARY_NAME = Pattern.compile("(?:^|[\\s:])org\\.jruby:([\\w.\\-]+)(?::|$)");

	/**
	 * @return true if the library of a build tool is JRuby itself
	 */
	public static boolean isJRubyLibraryName(@Nullable final String libraryName)
	{
		if(libraryName == null)
		{
			return false;
		}
		final Matcher matcher = LIBRARY_NAME.matcher(libraryName);
		return matcher.find() && ARTIFACT_IDS.contains(matcher.group(1));
	}

	/**
	 * @param module some module
	 * @return true if the module has the JRuby extension
	 */
	public static boolean hasJRubySupport(@Nonnull final Module module)
	{
		return ModuleUtilCore.getExtension(module, EXTENSION_ID) != null;
	}
}
