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

package org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.arg;


import jakarta.annotation.Nonnull;

import consulo.language.ast.IElementType;
import org.jetbrains.plugins.ruby.ruby.lang.parser.ParsingMethod;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import consulo.language.ast.TokenSet;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.ErrorMsg;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.ParsingMethodWithAssignmentLookup;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RMarker;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 08.06.2006
 */
class RangeExpression implements RubyTokenTypes
{
	private static final TokenSet ENDLESS_RANGE_FOLLOWERS = TokenSet.create(tRPAREN, tRBRACK, tRBRACE, tEOL, tSEMICOLON, tCOMMA,
			kTHEN, kDO, kDO_COND, kEND, kIF_MOD, kUNLESS_MOD, tASSOC);

	/**
	 * Range expression parsing
	 *
	 * @param builder current builder
	 * @return result of parsing
	 */
	@Nonnull
	public static IElementType parse(final RBuilder builder)
	{
		return parseWithLeadBool(builder, builder.mark(), BooleanExpression.parse(builder));
	}

	/**
	 * Range expression parsing with lead Bool
	 *
	 * @param builder current builder
	 * @param marker  Marker before Bool expr
	 * @param result  result of Bool parsing
	 * @return result of parsing
	 */
	@Nonnull
	public static IElementType parseWithLeadBool(final RBuilder builder, RMarker marker, IElementType result)
	{
		ParsingMethod parsingMethod = new ParsingMethodWithAssignmentLookup()
		{
			@Override
			@Nonnull
			public IElementType parseInner(final RBuilder builder)
			{
				return BooleanExpression.parse(builder);
			}
		};

		if(result == RubyElementTypes.EMPTY_INPUT)
		{
			// ..5 - beginless range
			if(builder.compareAndEat(BNF.tRANGE_TOKENS))
			{
				if(parsingMethod.parse(builder) == RubyElementTypes.EMPTY_INPUT)
				{
					builder.error(ErrorMsg.EXPRESSION_EXPECTED_MESSAGE);
				}
				marker.done(RubyElementTypes.RANGE_EXPRESSION);
				return RubyElementTypes.RANGE_EXPRESSION;
			}
			marker.drop();
			return RubyElementTypes.EMPTY_INPUT;
		}

		while(builder.compareAndEat(BNF.tRANGE_TOKENS))
		{
			// 1.. - endless range
			if(!builder.eof() && !builder.compare(ENDLESS_RANGE_FOLLOWERS) && parsingMethod.parse(builder) == RubyElementTypes.EMPTY_INPUT)
			{
				builder.error(ErrorMsg.EXPRESSION_EXPECTED_MESSAGE);
			}
			marker.done(RubyElementTypes.RANGE_EXPRESSION);
			marker = marker.precede();
			result = RubyElementTypes.RANGE_EXPRESSION;
		}
		marker.drop();
		return result;

	}

	/**
	 * Range expression parsing with lead PRIMARY
	 *
	 * @param builder current builder
	 * @param marker  Marker before PRIMARY
	 * @param result  result of PRIMARY parsed
	 * @return result of parsing
	 */
	public static IElementType parseWithLeadPRIMARY(final RBuilder builder, final RMarker marker, final IElementType result)
	{
		return parseWithLeadBool(builder, marker.precede(), BooleanExpression.parseWithLeadPRIMARY(builder, marker, result));
	}
}
