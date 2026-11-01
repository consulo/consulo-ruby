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

package org.jetbrains.plugins.ruby.ruby.lang.highlighter;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Nonnull;

import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import consulo.language.editor.highlight.SyntaxHighlighterBase;
import consulo.language.lexer.Lexer;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyMergeLexer;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import consulo.colorScheme.TextAttributesKey;


public class RubySyntaxHighlighter extends SyntaxHighlighterBase implements RubyTokenTypes
{


	private static final TokenSet tBRACES = TokenSet.orSet(BNF.tLPARENS, BNF.tLBRACKS, BNF.tLBRACES, TokenSet.create(tRPAREN, tRBRACK, tRBRACE));


	private static final Map<IElementType, TextAttributesKey> ATTRIBUTES = new HashMap<IElementType, TextAttributesKey>();

	@Override
	@Nonnull
	public Lexer getHighlightingLexer()
	{
		return new RubyMergeLexer();
	}

	static
	{
		fillMap(ATTRIBUTES, BNF.kALL_RESWORDS, RubyHighlighterKeys.KEYWORD);
		fillMap(ATTRIBUTES, BNF.tOPS, RubyHighlighterKeys.OPERATION_SIGN);
		fillMap(ATTRIBUTES, BNF.tCOMMENTS, RubyHighlighterKeys.COMMENT);
		fillMap(ATTRIBUTES, BNF.tNUMBERS, RubyHighlighterKeys.NUMBER);

		fillMap(ATTRIBUTES, tBRACES, RubyHighlighterKeys.BRACKETS);

		ATTRIBUTES.put(T_STRING_CONTENT, RubyHighlighterKeys.STRING);
		fillMap(ATTRIBUTES, BNF.tSTRING_DELIMITERS, RubyHighlighterKeys.STRING);

		ATTRIBUTES.put(tREGEXP_CONTENT, RubyHighlighterKeys.REGEXPS);
		fillMap(ATTRIBUTES, BNF.tREGEXP_DELIMITERS, RubyHighlighterKeys.REGEXPS);

		ATTRIBUTES.put(tWORDS_CONTENT, RubyHighlighterKeys.WORDS);
		fillMap(ATTRIBUTES, BNF.tWORDS_DELIMITERS, RubyHighlighterKeys.WORDS);

		ATTRIBUTES.put(tHEREDOC_CONTENT, RubyHighlighterKeys.HEREDOC_CONTENT);
		fillMap(ATTRIBUTES, BNF.tHEREDOC_ALL_IDS, RubyHighlighterKeys.HEREDOC_ID);

		fillMap(ATTRIBUTES, BNF.tEXPR_SUBT_TOKENS, RubyHighlighterKeys.EXPR_SUBST_MARKS);

		ATTRIBUTES.put(tCOMMA, RubyHighlighterKeys.COMMA);
		ATTRIBUTES.put(tSEMICOLON, RubyHighlighterKeys.SEMICOLON);
		ATTRIBUTES.put(tLINE_CONTINUATION, RubyHighlighterKeys.LINE_CONTINUATION);

		ATTRIBUTES.put(tIDENTIFIER, RubyHighlighterKeys.IDENTIFIER);
		ATTRIBUTES.put(tCONSTANT, RubyHighlighterKeys.CONSTANT);
		ATTRIBUTES.put(tCVAR, RubyHighlighterKeys.CVAR);
		ATTRIBUTES.put(tGVAR, RubyHighlighterKeys.GVAR);
		ATTRIBUTES.put(tIVAR, RubyHighlighterKeys.IVAR);

		ATTRIBUTES.put(tNTH_REF, RubyHighlighterKeys.NTH_REF);
		ATTRIBUTES.put(tBACK_REF, RubyHighlighterKeys.BACK_REF);

		ATTRIBUTES.put(tESCAPE_SEQUENCE, RubyHighlighterKeys.ESCAPE_SEQUENCE);
		ATTRIBUTES.put(tINVALID_ESCAPE_SEQUENCE, RubyHighlighterKeys.INVALID_ESCAPE_SEQUENCE);

		ATTRIBUTES.put(tBAD_CHARACTER, RubyHighlighterKeys.BAD_CHARACTER);

		ATTRIBUTES.put(tASSOC, RubyHighlighterKeys.HASH_ASSOC);

		ATTRIBUTES.put(tLABEL, RubyHighlighterKeys.SYMBOL);
		ATTRIBUTES.put(tLAMBDA, RubyHighlighterKeys.OPERATION_SIGN);
		ATTRIBUTES.put(tANDDOT, RubyHighlighterKeys.OPERATION_SIGN);
		ATTRIBUTES.put(tDSTAR, RubyHighlighterKeys.OPERATION_SIGN);

	}


	@Override
	@Nonnull
	public TextAttributesKey[] getTokenHighlights(IElementType tokenType)
	{
		return pack(ATTRIBUTES.get(tokenType));
	}

}

