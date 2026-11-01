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

package org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.primary;


import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.ParsingMethod;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.TRAILER;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.assocs.ASSOC_OR_ARG;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.commands.CALL_ARGS;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.commands.COMMAND_OR_ARG;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.ErrorMsg;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.ListParsingUtil;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RMarker;
import consulo.util.lang.ref.Ref;
import consulo.language.ast.IElementType;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 06.07.2006
 */
public class AREF_ARGS implements RubyTokenTypes
{
	/*
		aref_args	: none
				| command opt_nl
				| args trailer
				| args ',' tSTAR arg opt_nl
				| assocs trailer
				| tSTAR arg opt_nl
				;
	*/
	public static void parse(final RBuilder builder)
	{
		final boolean doBlockAllowed = builder.setDoBlockAllowed(true);
		try
		{
			doParse(builder);
		}
		finally
		{
			builder.setDoBlockAllowed(doBlockAllowed);
		}
	}

	private static void doParse(final RBuilder builder)
	{
		if(!builder.compare(BNF.tAREF_ARGS_FIRST_TOKENS))
		{
			return;
		}

		RMarker marker = builder.mark();
		IElementType result = COMMAND_OR_ARG.parse(builder);
		if(result == RubyElementTypes.COMMAND_CALL)
		{
			marker.done(RubyElementTypes.LIST_OF_EXPRESSIONS);
			builder.passEOLs();
			return;
		}

		final Ref<Boolean> starSeen = new consulo.util.lang.ref.Ref<Boolean>(false);
		final Ref<Boolean> assocSeen = new Ref<Boolean>(false);
		ParsingMethod parsingMethod = new ParsingMethod()
		{
			@Override
			@Nonnull
			public IElementType parse(final RBuilder builder)
			{
				if(builder.compare(tSTAR) && !assocSeen.get())
				{
					starSeen.set(true);
					IElementType result = CALL_ARGS.parseArrayToArguments(builder);
					builder.passEOLs();
					return result;
				}

				IElementType result = ASSOC_OR_ARG.parse(builder);
				if(result == RubyElementTypes.ASSOC)
				{
					assocSeen.set(true);
					return result;
				}

				if(assocSeen.get() && !ASSOC_OR_ARG.isAllowedAfterAssoc(result))
				{
					builder.error(ErrorMsg.expected(tASSOC));
				}
				return result;
			}
		};

		if(result != RubyElementTypes.EMPTY_INPUT)
		{
			if(builder.compare(tASSOC))
			{
				ASSOC_OR_ARG.parseWithLeadArg(builder, marker, result);
				assocSeen.set(true);
				marker = marker.precede();
			}
			if(builder.compare(tCOMMA))
			{
				ListParsingUtil.parseCommaDelimitedExpressionWithLeadExpr(builder, result, parsingMethod, false);
			}
			if(starSeen.get())
			{
				marker.done(RubyElementTypes.LIST_OF_EXPRESSIONS);
				builder.passEOLs();
			}
			else
			{
				TRAILER.parse(builder);
				marker.done(RubyElementTypes.LIST_OF_EXPRESSIONS);
			}
			return;
		}

		int count = ListParsingUtil.parseCommaDelimitedExpressions(builder, parsingMethod, false);
		if(count == 0)
		{
			marker.rollbackTo();
			return;
		}

		if(starSeen.get())
		{
			marker.done(RubyElementTypes.LIST_OF_EXPRESSIONS);
			builder.passEOLs();
		}
		else
		{
			TRAILER.parse(builder);
			marker.done(RubyElementTypes.LIST_OF_EXPRESSIONS);
		}
	}
}
