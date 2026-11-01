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

package org.jetbrains.plugins.ruby.rails.facet.configuration;

import jakarta.annotation.Nullable;

import jakarta.annotation.Nonnull;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Mar 13, 2008
 */
public interface BaseRailsFacetConfiguration
{
	public boolean shouldUseRSpecPlugin();

	public void setShouldUseRSpecPlugin(final boolean useRSpecPlugin);

	@Nonnull
	public String getRailsApplicationRootPath();

	@Nonnull
	public String getRailsApplicationRootPathUrl();

	@Nonnull
	public StandardRailsPaths getPaths();

	public boolean isInitialized();
}
