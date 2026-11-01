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

import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.WriteExternalException;
import org.jdom.Element;

import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.support.utils.VirtualFileUtil;
import consulo.module.Module;
import consulo.content.bundle.Sdk;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: Mar 13, 2008
 */
public class BaseRailsFacetConfigurationImpl implements BaseRailsFacetConfigurationLowLevel
{

	// Serializable in BaseRailsFacetConfigurationExternalizer
	public boolean myShouldUseRSpecPlugin;

	private String myRailsRootDirPath;

	private boolean isInitialized;

	// Transient, we shouldn't serialize it!
	private String myRailsRootDirPathUrl;

	private StandardRailsPaths myRailsPaths;

	private Module myModule;

	private Sdk mySdk;


	@Override
	public boolean shouldUseRSpecPlugin()
	{
		return myShouldUseRSpecPlugin;
	}

	@Override
	public void setShouldUseRSpecPlugin(final boolean useRSpecPlugin)
	{
		myShouldUseRSpecPlugin = useRSpecPlugin;
	}

	@Override
	@Nonnull
	public String getRailsApplicationRootPath()
	{
		return myRailsRootDirPath;
	}

	@Override
	@Nonnull
	public String getRailsApplicationRootPathUrl()
	{
		return myRailsRootDirPathUrl;
	}

	@Override
	public void setRailsApplicationRootPath(@Nonnull final String railsRootDirPath)
	{
		myRailsRootDirPath = railsRootDirPath;
		myRailsRootDirPathUrl = VirtualFileUtil.constructLocalUrl(railsRootDirPath);
		myRailsPaths = new StandardRailsPaths(railsRootDirPath);
	}

	@Override
	@Nonnull
	public StandardRailsPaths getPaths()
	{
		return myRailsPaths;
	}


	@Override
	public void setModule(@Nonnull final Module uncommitedModule)
	{
		myModule = uncommitedModule;

		final String path = BaseRailsFacetConfigurationExternalizer.getInstance().expandPathIfPossible(this, getNullableRailsApplicationRootPath());
		if(path != null)
		{
			setRailsApplicationRootPath(path);
		}
	}

	@Override
	public Module getModule()
	{
		return myModule;
	}


	// Externalizing
	public void readExternal(final Element element) throws InvalidDataException
	{
		BaseRailsFacetConfigurationExternalizer.getInstance().readExternal(this, element);
	}

	public void writeExternal(final Element element) throws WriteExternalException
	{
		BaseRailsFacetConfigurationExternalizer.getInstance().writeExternal(this, element);
	}

	@Override
	@Nullable
	public String getNullableRailsApplicationRootPath()
	{
		return myRailsRootDirPath;
	}


	@Override
	public void setSdk(@Nullable final Sdk sdk)
	{
		mySdk = sdk;
	}


	@Override
	@Nullable
	public Sdk getSdk()
	{
		return mySdk;
	}

	@Override
	public void setInitialized()
	{
		isInitialized = true;
	}

	@Override
	public boolean isInitialized()
	{
		return isInitialized;
	}
}
