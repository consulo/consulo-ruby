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

package org.jetbrains.plugins.ruby.settings;

import jakarta.annotation.Nonnull;

import org.jdom.Element;

import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.support.ui.checkableDir.CheckableDirectoriesContainer;
import consulo.util.xml.serializer.InvalidDataException;
import consulo.util.xml.serializer.WriteExternalException;

/**
 * Created by IntelliJ IDEA.
 * User: Roman.Chernyatchik
 * Date: Sep 11, 2007
 */

public class RSupportPerModuleSettingsImpl implements RSupportPerModuleSettings
{

	private boolean changed;
	private boolean shouldUseRSpecTestFramework = true;
	private CheckableDirectoriesContainer myLoadPathDirs;
	private String myUnitTestsRootUrl;

	public RSupportPerModuleSettingsImpl()
	{
		myLoadPathDirs = new CheckableDirectoriesContainer();
	}

	public void readExternal(Element element) throws InvalidDataException
	{
		RSupportPerModuleSettingsExternalizer.getInstance().readExternal(this, element);
	}

	public void writeExternal(Element element) throws WriteExternalException
	{
		RSupportPerModuleSettingsExternalizer.getInstance().writeExternal(this, element);
	}

	@Override
	@Nonnull
	public CheckableDirectoriesContainer getLoadPathDirs()
	{
		return myLoadPathDirs;
	}

	public boolean isChanged()
	{
		return changed;
	}

	public void setChanged(boolean changed)
	{
		this.changed = changed;
	}

	@Override
	public void setLoadPathDirs(@Nonnull final CheckableDirectoriesContainer loadPathDirs)
	{
		myLoadPathDirs = loadPathDirs;
	}

	@Override
	public boolean shouldUseTestUnitTestFramework()
	{
		return shouldUseRSpecTestFramework;
	}

	@Override
	public void setShouldUseTestUnitTestFramework(final boolean shouldUse)
	{
		shouldUseRSpecTestFramework = shouldUse;
	}

	public void setUnitTestsRootUrl(final String testsUnitRootUrl)
	{
		myUnitTestsRootUrl = testsUnitRootUrl;
	}

	@Nullable
	public String getUnitTestsRootUrl()
	{
		return myUnitTestsRootUrl;
	}
}
