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

package org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils;

import java.util.Map;

import jakarta.annotation.Nullable;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import consulo.language.ast.ASTNode;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: 10.08.2006
 */
public interface RBuilder extends RubyTokenTypes
{
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////// core functions /////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Advances system builder
	 */
	public void advanceLexer();

	/**
	 * Gerenerates an error in system builder
	 *
	 * @param error Error message
	 */
	public void error(String error);

	/**
	 * Return marker from system builder
	 *
	 * @return Marker object
	 */
	public RMarker mark();

	/**
	 * Return marker from system builder
	 *
	 * @param passWhiteSpacesAndComments Pass whitespaces and comments
	 * @return Marker object
	 */
	public RMarker mark(boolean passWhiteSpacesAndComments);

	/**
	 * @return Current token type of system builder
	 */
	@Nullable
	public IElementType getTokenType();

	/**
	 * @return Current not EOL token type of system builder
	 */
	@Nullable
	public IElementType getNotEolTokenType();

	/**
	 * @return true is system builder is at eof
	 */
	public boolean eof();

	/**
	 * @return true is system builder is at eof, ignoring EOLs
	 */
	public boolean eofIgnoreEOL();


	/**
	 * Inits nextToken and nextNotEolToken values.
	 * Always call this function after rollbackTo or startParsing or builder.advanceLexer()
	 */
	public void initNextTokens();

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////// Compare Token //////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * @param type Expected token type
	 * @return True if found, false otherwise
	 */
	public boolean compare(final IElementType type);

	/**
	 * @param types Set of expected token types
	 * @return true if found, false otherwise
	 */
	public boolean compare(final TokenSet types);


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////// CompareToken with ignore tokens/////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * @param type Expected token type
	 * @return True if found, false otherwise
	 */
	public boolean compareIgnoreEOL(final IElementType type);

	/**
	 * @param types The set of expected token types
	 * @return True if found, false otherwise
	 */
	public boolean compareIgnoreEOL(final TokenSet types);


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////// Check Matches //////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Checks if current token is <code>type</code> type. If not, generates error message.
	 *
	 * @param type Expected token type
	 */
	public void match(final IElementType type);

	/**
	 * Checks if current token is <code>type</code> type. If not, generates error message.
	 *
	 * @param type         Expected token type
	 * @param errorMessage Corresponding error message
	 */
	public void match(final IElementType type, final String errorMessage);

	/**
	 * Checks if current token is one of the <code>types</code> type. If not, generates error message.
	 *
	 * @param types Set of expected token type
	 */
	public void match(final TokenSet types);

	/**
	 * Checks if current token is one of the <code>types</code> type. If not, generates error message.
	 *
	 * @param types        Set of expected token type
	 * @param errorMessage Corresponding error message
	 */
	public void match(final TokenSet types, final String errorMessage);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////// checkMatches ignoring EOL ////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Checks if first significant, except tEOL, token is <code>type</code> type. If not, generates error message.
	 *
	 * @param type Expected token type
	 */
	public void matchIgnoreEOL(final IElementType type);

	/**
	 * Checks if first significant, except tEOL, token is <code>type</code> type. If not, generates error message.
	 *
	 * @param type         Expected token type
	 * @param errorMessage Corresponding error message
	 */
	public void matchIgnoreEOL(final IElementType type, final String errorMessage);

	/**
	 * Checks if first significant, except tEOL, token is one of the <code>types</code> type. If not, generates error message.
	 *
	 * @param types Set of expected token type
	 */
	public void matchIgnoreEOL(final TokenSet types);

	/**
	 * Checks if first significant, except tEOL, token is one of the <code>types</code> type. If not, generates error message.
	 *
	 * @param types        Set of expected token type
	 * @param errorMessage Corresponding error message
	 */
	public void matchIgnoreEOL(final TokenSet types, final String errorMessage);


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////// Compare and Eat ///////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * checks if current token is <code>type</code>, and eats it if it`s true
	 *
	 * @param type Expected token type
	 * @return true if found
	 */
	public boolean compareAndEat(final IElementType type);

	/**
	 * checks if current token is one of the <code>types</code> type, and eats it if it`s true
	 *
	 * @param types Set of expected token types
	 * @return true if found
	 */
	public boolean compareAndEat(final TokenSet types);

	/**
	 * checks if current token, except tEOL, has <code>type</code> type, and eats it if it`s true
	 *
	 * @param type Expected token type
	 * @return true if found
	 */
	public boolean compareAndEatIgnoreEOL(final IElementType type);

	/**
	 * checks if current token, except tEOL, is one of the <code>types</code> type, and eats it if it`s true
	 *
	 * @param types Set of expected token types
	 * @return true if found
	 */
	public boolean compareAndEatIgnoreEOL(final TokenSet types);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////// Pass Tokens //////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Passes all following EOLs
	 *
	 * @return true if at least one EOL passed
	 */
	public boolean passEOLs();
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////// Parse Single Token ///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Parses single token
	 *
	 * @param type          Token type(s)
	 * @param statementType New statement type
	 * @return result of parsing - statementType
	 */
	public IElementType parseSingleToken(final IElementType type, final IElementType statementType);

	/**
	 * Parses single token
	 *
	 * @param type          Token type(s)
	 * @param statementType New statement type
	 * @param errorMessage  Error Message of corresponding token(s) not found
	 * @return result of parsing - statementType
	 */
	public IElementType parseSingleToken(final IElementType type, final IElementType statementType, final String errorMessage);

	/**
	 * Parses single token
	 *
	 * @param types         Token type(s)
	 * @param statementType New statement type
	 * @return result of parsing - statementType
	 */
	public IElementType parseSingleToken(final TokenSet types, final IElementType statementType);

	/**
	 * Parses single token
	 *
	 * @param types         Token type(s)
	 * @param statementType New statement type
	 * @param errorMessage  Error Message of corresponding token(s) not found
	 * @return result of parsing - statementType
	 */
	public IElementType parseSingleToken(final TokenSet types, final IElementType statementType, final String errorMessage);

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////// Block Tokens //////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Reads whitespaces and comments up to end
	 */
	public void passJunks();

	/**
	 * The same as PsiBuilder.getTreeBuilt
	 *
	 * @return Psi tree
	 */
	public ASTNode getTreeBuilt();

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////// DEBUG /////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean isDEBUG();

	public void STMT();

	public void EXPR();

	public void ARG();

	public void PRIMARY();

	public void printDebugStats();

	public boolean isAcceptibleErrorToken(final IElementType myToken);

	public Map<TokenSet, String> getErrorCache();

	public boolean passHeredocs();

	/**
	 * @return false while command arguments without parentheses are parsed: a do block there belongs to the command
	 */
	public boolean isDoBlockAllowed();

	/**
	 * @return the previous value
	 */
	public boolean setDoBlockAllowed(boolean allowed);
}
