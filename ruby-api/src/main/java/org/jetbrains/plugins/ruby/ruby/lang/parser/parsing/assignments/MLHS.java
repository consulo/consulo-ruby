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

package org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.assignments;


import jakarta.annotation.Nonnull;

import consulo.util.lang.ref.Ref;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.ParsingMethod;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.ListParsingUtil;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RMarker;
import consulo.language.ast.IElementType;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 02.07.2006
 */
public class MLHS implements RubyTokenTypes
{
/*
mlhs		: mlhs_basic
		| tLPAREN mlhs_basic ')'
		;

mlhs_basic	: mlhs_head 
		| mlhs_head mlhs_item
		| mlhs_head tSTAR mlhs_node
		| mlhs_head tSTAR mlhs_node ',' mlhs_post
		| mlhs_head tSTAR
		| mlhs_head tSTAR ',' mlhs_post
		| tSTAR mlhs_node
		| tSTAR mlhs_node ',' mlhs_post
		| tSTAR
		| tSTAR ',' mlhs_post
		;

mlhs_head	: mlhs_item ',' (mlhs_item ',')* ;

mlhs_node == lhs

*/

	/**
	 * Returns MLHS, if MLHS parsing was successful, or
	 * RubyElementTypes.EMPTY_INPUT otherwise
	 *
	 * @param builder Current builder
	 * @return parsing result
	 */
	@Nonnull
	public static IElementType parse(final RBuilder builder)
	{
		if(!builder.compare(BNF.tMLHS_FIRST_TOKEN))
		{
			return RubyElementTypes.EMPTY_INPUT;
		}
		if(builder.compare(tLPAREN))
		{
			RMarker marker = builder.mark();
			builder.match(tLPAREN);
			if(parseMLHS_BASIC(builder) != RubyElementTypes.EMPTY_INPUT)
			{
				builder.match(tRPAREN);
				marker.done(RubyElementTypes.MLHS);
				return RubyElementTypes.MLHS;
			}
			marker.rollbackTo();
		}
		return parseMLHS_BASIC(builder);
	}

	public static IElementType parseMLHS_BASIC(RBuilder builder)
	{
		// *head, last
		if(builder.compare(tSTAR))
		{
			RMarker marker = builder.mark();
			parseTail(builder);
			marker.done(RubyElementTypes.LIST_OF_EXPRESSIONS);
			return RubyElementTypes.MLHS;
		}


		RMarker statementMarker = builder.mark();
		int count = parseTail(builder);

		if((count == 1 && builder.compareAndEat(tCOMMA)))
		{
			statementMarker.done(RubyElementTypes.LIST_OF_EXPRESSIONS);
			return RubyElementTypes.MLHS;
		}
		if(count >= 2)
		{
			statementMarker.done(RubyElementTypes.LIST_OF_EXPRESSIONS);
			return RubyElementTypes.MLHS;
		}
		statementMarker.rollbackTo();
		return RubyElementTypes.EMPTY_INPUT;
	}

	/**
	 * Parses MLHS with leading LHS
	 *
	 * @param builder Current builder
	 * @param marker  Marker before leading LHS
	 * @return result of MLHS parsing
	 */
	public static IElementType parseWithLeadLHS(final RBuilder builder, final RMarker marker)
	{
		builder.match(tCOMMA);
		parseTail(builder);
		builder.compareAndEat(tCOMMA);
		marker.done(RubyElementTypes.LIST_OF_EXPRESSIONS);
		return RubyElementTypes.MLHS;
	}

	/**
	 * Parses tail of mlhs
	 *
	 * @param builder Current builder
	 * @return number of tail elements
	 */
	private static int parseTail(final RBuilder builder)
	{
		if(!builder.compare(BNF.tMLHS_FIRST_TOKEN))
		{
			return 0;
		}
		final consulo.util.lang.ref.Ref<Boolean> starSeen = new Ref<Boolean>(false);
		ParsingMethod parsignMethod = new ParsingMethod()
		{
			@Override
			@Nonnull
			public IElementType parse(final RBuilder builder)
			{

				// one splat, anywhere: first, *middle, last
				if(builder.compare(tSTAR))
				{
					if(starSeen.get())
					{
						return RubyElementTypes.EMPTY_INPUT;
					}
					starSeen.set(true);
					return parseArgumentsToArray(builder);
				}

				return MLHS_ITEM.parse(builder);
			}
		};

		return ListParsingUtil.parseCommaDelimitedExpressions(builder, parsignMethod, false);
	}

	/**
	 * Parses ArgumentToArray ie *LHS
	 *
	 * @param builder Current builder
	 * @return result of parsing (ARGFUMENTS_TO_ARRAY)
	 */
	private static IElementType parseArgumentsToArray(final RBuilder builder)
	{
		RMarker starMarker = builder.mark();
		builder.match(tSTAR);
		LHS.parse(builder);
		starMarker.done(RubyElementTypes.ARRAY_ARGUMENT);
		return RubyElementTypes.ARRAY_ARGUMENT;
	}
}
