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

package org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.highlighting.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.highlighting.RHTMLHighlighterKeys;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.parsing.RHTMLTokenType;
import org.jetbrains.plugins.ruby.rails.langs.rhtml.lang.parsing.lexer._RHTMLLexer;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase;
import com.intellij.psi.tree.IElementType;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: Roman Chernyatchik
 * @date: 04.04.2007
 */
public class RHTMLFileHighlighterImpl extends SyntaxHighlighterBase
{
	private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<IElementType, TextAttributesKey>();

	static
	{
		fillMap(ATTRIBUTES, RHTMLTokenType.RHTML_ALL_COMMENT_TOKENS, RHTMLHighlighterKeys.COMMENT);

		ATTRIBUTES.put(RHTMLTokenType.RHTML_SCRIPTLET_START, RHTMLHighlighterKeys.RHTML_SCRIPTLET_START);
		ATTRIBUTES.put(RHTMLTokenType.RHTML_SCRIPTLET_END, RHTMLHighlighterKeys.RHTML_SCRIPTLET_END);

		ATTRIBUTES.put(RHTMLTokenType.RHTML_EXPRESSION_START, RHTMLHighlighterKeys.RHTML_EXPRESSION_START);
		ATTRIBUTES.put(RHTMLTokenType.RHTML_EXPRESSION_END, RHTMLHighlighterKeys.RHTML_EXPRESSION_END);

		ATTRIBUTES.put(RHTMLTokenType.OMIT_NEW_LINE, RHTMLHighlighterKeys.OMIT_NEW_LINE);

		ATTRIBUTES.put(RHTMLTokenType.FLEX_ERROR, RHTMLHighlighterKeys.FLEX_ERROR);
	}

	@Override
	@Nonnull
	public Lexer getHighlightingLexer()
	{
		return new _RHTMLLexer();
	}

	@Override
	@Nonnull
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType)
	{
		return pack(ATTRIBUTES.get(tokenType));
	}
}
