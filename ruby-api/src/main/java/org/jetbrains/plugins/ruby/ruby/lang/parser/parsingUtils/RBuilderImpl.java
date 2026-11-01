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

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import consulo.language.ast.ASTNode;
import consulo.logging.Logger;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.basicTypes.stringLike.Heredoc;
import org.jetbrains.plugins.ruby.ruby.lang.psi.RubyElementType;
import consulo.language.parser.PsiBuilder;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 28.05.2006
 */
public class RBuilderImpl implements RubyTokenTypes, RBuilder
{
	private boolean myDoBlockAllowed = true;

	// builder used in parsing
	private PsiBuilder myBuilder;

	// Next token of myBuilder
	private IElementType myNextToken = null;
	// Next Not EOL token of myBuilder
	private IElementType myNextNotEolToken = null;

	private boolean isInHeredoc = false;

	private final long startTime;

	private final HashMap<TokenSet, String> myErrorCache = new HashMap<TokenSet, String>();

	public RBuilderImpl(final PsiBuilder psiBuilder)
	{
		startTime = System.currentTimeMillis();
		myBuilder = psiBuilder;
		initNextTokens();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////// Engine core functions //////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void advanceLexer()
	{
		myBuilder.getTokenType();
		myBuilder.advanceLexer();
		initNextTokens();
	}

	@Override
	public void error(@Nonnull final String error)
	{
		myBuilder.getTokenType();
		myBuilder.error(error);
	}

	@Override
	public RMarker mark()
	{
		return mark(true);
	}


	@Override
	public RMarker mark(boolean passWhiteSpacesAndComments)
	{
		if(passWhiteSpacesAndComments)
		{
			myBuilder.getTokenType();
		}
		return new RMarkerImpl(this, myBuilder.mark());
	}

	@Override
	@Nullable
	public IElementType getTokenType()
	{
		return myNextToken;
	}

	@Override
	@Nullable
	public IElementType getNotEolTokenType()
	{
		return myNextNotEolToken;
	}

	@Override
	public boolean eof()
	{
		return myNextToken == null;
	}

	@Override
	public boolean eofIgnoreEOL()
	{
		return myNextNotEolToken == null;
	}


	@Override
	public void initNextTokens()
	{
		final PsiBuilder.Marker rollBackMarker = myBuilder.mark();
		myNextToken = myBuilder.getTokenType();
		while(myBuilder.getTokenType() == tEOL)
		{
			myBuilder.advanceLexer();
		}
		myNextNotEolToken = myBuilder.getTokenType();
		rollBackMarker.rollbackTo();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////// Compare Token //////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean compare(final IElementType type)
	{
		boolean found = type == myNextToken;
		if(found)
		{
			// move to the token
			myBuilder.getTokenType();
		}
		return found;
	}

	@Override
	public boolean compare(final TokenSet types)
	{
		boolean found = types.contains(myNextToken);
		if(found)
		{
			// move to the token
			myBuilder.getTokenType();
		}
		return found;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////// CompareToken with ignore tokens/////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean compareIgnoreEOL(final IElementType type)
	{
		boolean found = type == myNextNotEolToken;
		if(found)
		{
			// move to the token
			passEOLs();
			myBuilder.getTokenType();
		}
		return found;
	}

	@Override
	public boolean compareIgnoreEOL(final TokenSet types)
	{
		boolean found = types.contains(myNextNotEolToken);
		if(found)
		{
			// move to the token
			passEOLs();
			myBuilder.getTokenType();
		}
		return found;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////// Check and Eat /////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public boolean compareAndEat(final IElementType type)
	{
		boolean found = compare(type);
		if(found)
		{
			advanceLexer();
		}
		return found;
	}

	@Override
	public boolean compareAndEat(final TokenSet types)
	{
		boolean found = compare(types);
		if(found)
		{
			advanceLexer();
		}
		return found;
	}

	@Override
	public boolean compareAndEatIgnoreEOL(final IElementType type)
	{
		boolean found = compareIgnoreEOL(type);
		if(found)
		{
			advanceLexer();
		}
		return found;
	}

	@Override
	public boolean compareAndEatIgnoreEOL(final TokenSet types)
	{
		boolean found = compareIgnoreEOL(types);
		if(found)
		{
			advanceLexer();
		}
		return found;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////// Check Matches //////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void match(final IElementType token)
	{
		match(token, ErrorMsg.expected(token));
	}

	@Override
	public void match(final IElementType token, final String errorMessage)
	{
		if(!compareAndEat(token))
		{
			error(errorMessage);
		}
	}

	@Override
	public void match(final TokenSet tokens)
	{
		match(tokens, ErrorMsg.expected(tokens, this));
	}

	@Override
	public void match(final TokenSet tokens, final String errorMessage)
	{
		if(!compareAndEat(tokens))
		{
			error(errorMessage);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////// checkMatches ignoring EOL ////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void matchIgnoreEOL(final IElementType token)
	{
		matchIgnoreEOL(token, ErrorMsg.expected(token));
	}

	@Override
	public void matchIgnoreEOL(final IElementType token, final String errorMessage)
	{
		passHeredocs();
		boolean found = compareAndEatIgnoreEOL(token);
		if(!found)
		{
			error(errorMessage);
		}
	}

	@Override
	public void matchIgnoreEOL(final TokenSet tokens)
	{
		matchIgnoreEOL(tokens, ErrorMsg.expected(tokens, this));
	}

	@Override
	public void matchIgnoreEOL(final TokenSet tokens, final String errorMessage)
	{
		passHeredocs();
		boolean found = compareAndEatIgnoreEOL(tokens);
		if(!found)
		{
			error(errorMessage);
		}
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////// Pass Tokens //////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	@Override
	public boolean isDoBlockAllowed()
	{
		return myDoBlockAllowed;
	}

	@Override
	public boolean setDoBlockAllowed(final boolean allowed)
	{
		final boolean previous = myDoBlockAllowed;
		myDoBlockAllowed = allowed;
		return previous;
	}

	@Override
	public boolean passHeredocs()
	{
		if(!isInHeredoc && BNF.tHEREDOC_VALUE_BEGINNINGS.contains(myNextNotEolToken))
		{
			isInHeredoc = true;
			passEOLs();
			Heredoc.parse(this);
			isInHeredoc = false;
			initNextTokens();
			return true;
		}
		return false;
	}

	@Override
	public boolean passEOLs()
	{
		boolean seen = false;
		while(compareAndEat(tEOL))
		{
			seen = true;
		}
		return seen;
	}

	@Override
	public void passJunks()
	{
		myBuilder.getTokenType();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////// Parse Single Token ///////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public IElementType parseSingleToken(final IElementType type, final IElementType statementType)
	{
		return parseSingleToken(type, statementType, ErrorMsg.expected(type));
	}

	@Override
	public IElementType parseSingleToken(final IElementType type, final IElementType statementType, final String errorMessage)
	{
		final RMarker statementMarker = mark();
		match(type, errorMessage);
		statementMarker.done(statementType);
		return statementType;
	}

	@Override
	public IElementType parseSingleToken(final TokenSet types, final IElementType statementType)
	{
		return parseSingleToken(types, statementType, ErrorMsg.expected(types, this));
	}

	@Override
	public IElementType parseSingleToken(final TokenSet types, final IElementType statementType, final String errorMessage)
	{
		final RMarker statementMarker = mark();
		match(types, errorMessage);
		statementMarker.done(statementType);
		return statementType;
	}

	@Override
	public ASTNode getTreeBuilt()
	{
		return myBuilder.getTreeBuilt();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//// DEBUG /////////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private int STMT = 0;
	private int EXPR = 0;
	private int ARG = 0;
	private int PRIMARY = 0;

	@Override
	public boolean isDEBUG()
	{
		return Logger.getInstance(RBuilderImpl.class.getName()).isDebugEnabled();
	}

	@Override
	public void STMT()
	{
		STMT++;
	}

	@Override
	public void EXPR()
	{
		EXPR++;
	}

	@Override
	public void ARG()
	{
		ARG++;
	}

	@Override
	public void PRIMARY()
	{
		PRIMARY++;
	}

	@Override
	public void printDebugStats()
	{
		System.err.println("TIME " + (System.currentTimeMillis() - startTime) + "\n");
		System.err.println("STMT " + STMT);
		System.err.println("EXPR " + EXPR);
		System.err.println("ARG " + ARG);
		System.err.println("PRIMARY " + PRIMARY);
	}

	@Override
	public boolean isAcceptibleErrorToken(IElementType myToken)
	{
		return myToken instanceof RubyElementType;
	}

	@Override
	public Map<TokenSet, String> getErrorCache()
	{
		return myErrorCache;
	}
}
