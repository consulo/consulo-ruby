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

package org.jetbrains.plugins.ruby.rails.langs;

import org.jetbrains.annotations.NonNls;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.rails.RailsIcons;
import org.jetbrains.plugins.ruby.ruby.lang.RubyFileType;
import com.intellij.openapi.fileTypes.LanguageFileType;
import consulo.ui.image.Image;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 17.10.2006
 */
public class RXMLFileType extends LanguageFileType
{
	public static final String VALID_EXTENTIONS = "rxml;builder;";

	public static final RXMLFileType RXML = new RXMLFileType();
	@NonNls
	private static final String NAME = "RXML";
	@NonNls
	private static final String DESCRIPTION = RBundle.message("filetype.description.rxml");
	@NonNls
	private static final String DEFAULT_EXTENSION = "rxml";
	@NonNls
	private static final String BUILDER_EXTENSION = "builder";
	private static final Image ICON = RailsIcons.RXTML_ICON;

	private RXMLFileType()
	{
		super(RubyFileType.INSTANCE.getLanguage());
	}

	@Override
	@Nonnull
	public String getId()
	{
		return NAME;
	}

	@Override
	@Nonnull
	public String getDescription()
	{
		return DESCRIPTION;
	}

	@Override
	@Nonnull
	public String getDefaultExtension()
	{
		return DEFAULT_EXTENSION;
	}

	@Nonnull
	public String getBuilderExtension()
	{
		return BUILDER_EXTENSION;
	}

	@Override
	@Nullable
	public Image getIcon()
	{
		return ICON;
	}
}

