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

package org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures;


import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.ParsingMethod;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.COMPSTMT;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.EXPR;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.TERMS;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.arg.ARG;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.commands.CALL_ARGS;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.ListParsingUtil;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RMarker;
import consulo.language.ast.IElementType;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 11.06.2006
 */
public class Case implements RubyTokenTypes
{
	/*
			| kCASE expr_value opt_terms
			  case_body
			  kEND
			| kCASE opt_terms case_body kEND
			| kCASE opt_terms kELSE compstmt kEND

	case_body	: kWHEN when_args then
			  compstmt
			  cases
			;

	cases		: opt_else
			| case_body
			;
	*/
	@Nonnull
	public static IElementType parse(final RBuilder builder)
	{
		if(!builder.compare(kCASE))
		{
			return RubyElementTypes.EMPTY_INPUT;
		}

		RMarker statementMarker = builder.mark();
		builder.match(kCASE);

		EXPR.parse(builder);

		TERMS.parseOpt(builder);

		// when, in
		while(builder.compareIgnoreEOL(kWHEN) || builder.compareIgnoreEOL(kIN))
		{
			if(builder.compareIgnoreEOL(kIN))
			{
				RMarker inMarker = builder.mark();
				builder.matchIgnoreEOL(kIN);
				Pattern.parseTop(builder, true);
				THEN.parse(builder);
				COMPSTMT.parse(builder, kIN, kELSE, kEND);
				inMarker.done(RubyElementTypes.IN_CASE);
				continue;
			}
			RMarker whenMarker = builder.mark();
			builder.match(kWHEN);
			parseWhenArgs(builder);
			THEN.parse(builder);
			COMPSTMT.parse(builder, kWHEN, kELSE, kEND);
			whenMarker.done(RubyElementTypes.WHEN_CASE);
		}

		if(builder.compareIgnoreEOL(kELSE))
		{
			OPT_ELSE.parse(builder);
		}

		builder.matchIgnoreEOL(kEND);
		statementMarker.done(RubyElementTypes.CASE_STATEMENT);
		return RubyElementTypes.CASE_STATEMENT;
	}

	/*
		when_args	: args
				| args ',' tSTAR arg_value
				| tSTAR arg_value
				;
	*/
	private static void parseWhenArgs(final RBuilder builder)
	{
		RMarker statementMarker = builder.mark();
		ParsingMethod parsingMethod = new ParsingMethod()
		{
			boolean afterStar = false;

			@Override
			@Nonnull
			public IElementType parse(final RBuilder builder)
			{
				if(afterStar)
				{
					return RubyElementTypes.EMPTY_INPUT;
				}
				if(builder.compare(tSTAR))
				{
					afterStar = true;
					return CALL_ARGS.parseArrayToArguments(builder);
				}
				return ARG.parse(builder);
			}
		};
		ListParsingUtil.parseCommaDelimitedExpressions(builder, parsingMethod);
		statementMarker.done(RubyElementTypes.LIST_OF_EXPRESSIONS);
	}


}
