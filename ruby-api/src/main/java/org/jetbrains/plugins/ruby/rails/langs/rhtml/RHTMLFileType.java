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

package org.jetbrains.plugins.ruby.rails.langs.rhtml;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NonNls;

import javax.annotation.Nullable;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.rails.RailsIcons;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.eRubyLanguage;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.highlighting.RHTMLEditorHighlighter;
import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.ui.image.Image;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 17.10.2006
 */
public class RHTMLFileType extends XmlLikeFileType
{
	public static final String VALID_EXTENTIONS = "rhtml;erb;";
	public static final RHTMLFileType INSTANCE = new RHTMLFileType();

	@NonNls
	private static final String NAME = "RHTML";
	@NonNls
	private static final String DEFAULT_EXTENSION = "rhtml";
	@NonNls
	private static final String ERB_EXTENSION = "erb";
	@NonNls
	private static final String DESCRIPTION = RBundle.message("filetype.description.rhtml");
	private static final Image ICON = RailsIcons.RHTML_ICON;

	private RHTMLFileType()
	{
		super(eRubyLanguage.INSTANCE);
	}

	@Override
	@Nonnull
	public String getName()
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
	public String getERBExtension()
	{
		return ERB_EXTENSION;
	}

	@Override
	@Nullable
	public Image getIcon()
	{
		return ICON;
	}

	public EditorHighlighter getEditorHighlighter(@Nullable Project project, @Nullable VirtualFile virtualFile, @Nonnull EditorColorsScheme colors)
	{
		return new RHTMLEditorHighlighter(colors, project, virtualFile);
	}
}