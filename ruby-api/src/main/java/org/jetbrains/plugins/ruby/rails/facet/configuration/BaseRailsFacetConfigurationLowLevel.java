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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.content.bundle.Sdk;
import consulo.module.Module;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Apr 18, 2008
 */
public interface BaseRailsFacetConfigurationLowLevel extends BaseRailsFacetConfiguration
{



	public void setRailsApplicationRootPath(@Nonnull final String rootPath);

	public void setModule(@Nonnull final Module uncommitedModule);

	@Nullable
	public String getNullableRailsApplicationRootPath();

	public void setSdk(@Nullable final Sdk sdk);

	@Nullable
	public Sdk getSdk();

	@Nullable
	public Module getModule();

	public void setInitialized();
}
