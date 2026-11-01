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

import consulo.language.ast.TokenSet;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.ParsingMethod;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.STMT;
import consulo.language.ast.IElementType;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 07.06.2006
 */

public class ListParsingUtil implements RubyTokenTypes
{
	private static final consulo.language.ast.TokenSet CLOSING_BRACKETS = consulo.language.ast.TokenSet.create(tRPAREN, tRBRACK, tRBRACE);


	public static int parseCommaDelimitedExpressions(final RBuilder builder, final ParsingMethod parsingMethod)
	{
		return parseCommaDelimitedExpressionWithLeadExpr(builder, parsingMethod.parse(builder), parsingMethod, true);
	}


	public static int parseCommaDelimitedExpressions(RBuilder builder, ParsingMethod parsingMethod, boolean eatFollowComma)
	{
		return parseCommaDelimitedExpressionWithLeadExpr(builder, parsingMethod.parse(builder), parsingMethod, eatFollowComma);
	}

	public static int parseCommaDelimitedExpressionWithLeadExpr(final RBuilder builder, final IElementType result, final ParsingMethod parsingMethod)
	{
		return parseCommaDelimitedExpressionWithLeadExpr(builder, result, parsingMethod, true);
	}

	/**
	 * Parses comma delimited list of expressions
	 *
	 * @param parsingMethod  method, used in parsing each single expression in list
	 * @param builder        Current builder wrapper
	 * @param eatFollowComma If true, the following comma will be eaten
	 * @param result         Result of parsing first element
	 * @return number of expressions in list
	 */
	public static int parseCommaDelimitedExpressionWithLeadExpr(final RBuilder builder, final IElementType result, final ParsingMethod parsingMethod, final boolean eatFollowComma)
	{
		if(result == RubyElementTypes.EMPTY_INPUT)
		{
			if(builder.compare(tCOMMA))
			{
				builder.error(ErrorMsg.EXPRESSION_EXPECTED_MESSAGE);
			}
			else
			{
				return 0;
			}
		}
		int count = 1;
		RMarker beforeLastCommaMarker = builder.mark(false);
		while(builder.compareAndEat(tCOMMA))
		{
			// a trailing comma before the closing bracket
			if(builder.compareIgnoreEOL(CLOSING_BRACKETS))
			{
				beforeLastCommaMarker.drop();
				beforeLastCommaMarker = builder.mark(false);
				break;
			}
			if(parsingMethod.parse(builder) != RubyElementTypes.EMPTY_INPUT)
			{
				count++;
				beforeLastCommaMarker.drop();
				beforeLastCommaMarker = builder.mark(false);
			}
			else
			{
				builder.error(ErrorMsg.EXPRESSION_EXPECTED_MESSAGE);
				// if we see another comma, we should just move
				if(builder.compare(tCOMMA))
				{
					count++;
					beforeLastCommaMarker.drop();
					beforeLastCommaMarker = builder.mark(false);
				}
			}
		}
		if(eatFollowComma)
		{
			beforeLastCommaMarker.drop();
		}
		else
		{
			beforeLastCommaMarker.rollbackTo();
		}
		return count;

	}


	/**
	 * Parses STMTS up to one of endDelimiters
	 *
	 * @param endDelimiters Set of end delimiters
	 * @param builder       Current builder wrapper
	 */
	public static void parseSTMTS(final RBuilder builder, final TokenSet endDelimiters)
	{
		assert (endDelimiters != TokenSet.EMPTY);

		passSEMICOLONS(builder);
		if(lookForwardEndDelimiter(builder, endDelimiters))
		{
			return;
		}

		boolean separatorFound = true;

		while(true)
		{
			if(!separatorFound)
			{
				builder.error(ErrorMsg.expected(BNF.tTERM_TOKENS, builder));
			}

			if(STMT.parse(builder) == RubyElementTypes.EMPTY_INPUT)
			{
				builder.error(ErrorMsg.unexpected(builder.getTokenType()));
				builder.advanceLexer();
			}

			boolean semicolonFound = passSEMICOLONS(builder);

			// checks for endDelimiter. if true, exit, not to pass following eols and junks
			if(lookForwardEndDelimiter(builder, endDelimiters))
			{
				return;
			}

			separatorFound = builder.passEOLs() || semicolonFound;
		}
	}


	/**
	 * Checks for end delimiters or for eof
	 *
	 * @param endDelimiters The set of end delimiters
	 * @param builder       Current builder wrapper
	 * @return true if end delimiters seen
	 */
	private static boolean lookForwardEndDelimiter(final RBuilder builder, TokenSet endDelimiters)
	{
		//noinspection SimplifiableIfStatement
		if(builder.eofIgnoreEOL())
		{
			return true;
		}

		return endDelimiters.contains(builder.getNotEolTokenType());
	}


	/**
	 * Passes tSEMICOLONs
	 *
	 * @param builder Current builder wrapper
	 * @return true if at least one Semicilon was passed
	 */
	private static boolean passSEMICOLONS(final RBuilder builder)
	{
		boolean semicolonSeen = false;
		while(builder.compareAndEatIgnoreEOL(BNF.tSEMICOLONS))
		{
			semicolonSeen = true;
		}
		return semicolonSeen;
	}


}
