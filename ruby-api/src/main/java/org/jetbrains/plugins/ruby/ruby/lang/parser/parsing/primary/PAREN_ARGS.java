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
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.assocs.ASSOC_OR_ARG;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.commands.CALL_ARGS;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.iterators.BLOCK_CALL;
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
public class PAREN_ARGS implements RubyTokenTypes
{
/*
	paren_args	: '(' none ')'
            | '(' call_args opt_nl ')'
            | '(' block_call opt_nl ')'
            | '(' args ',' block_call opt_nl ')'
            ;
block_call	: command do_block
		| block_call '.' operation2 opt_paren_args
		| block_call tCOLON2 operation2 opt_paren_args
		;

CALL_ARGS	: ARGS
		| ARGS [`,' ASSOCS] [`,' `*' ARG] [`,' `&' ARG]
		| ASSOCS [`,' `*' ARG] [`,' `&' ARG]
		| `*' ARG [`,' `&' ARG]
		| `&' ARG
		| COMMAND

*/

	@Nonnull
	public static IElementType parse(final RBuilder builder)
	{
		final boolean doBlockAllowed = builder.setDoBlockAllowed(true);
		try
		{
			return doParse(builder);
		}
		finally
		{
			builder.setDoBlockAllowed(doBlockAllowed);
		}
	}

	@Nonnull
	private static IElementType doParse(final RBuilder builder)
	{
		// TODO: optimize!!!
		if(!builder.compareAndEat(tfLPAREN))
		{
			return RubyElementTypes.EMPTY_INPUT;
		}

		RMarker statementMarker = builder.mark();
		final consulo.util.lang.ref.Ref<Boolean> assocSeen = new Ref<Boolean>(false);
		final Ref<Boolean> starSeen = new Ref<Boolean>(false);
		final Ref<Boolean> amperSeen = new Ref<Boolean>(false);
		final Ref<Boolean> blockCallSeen = new Ref<Boolean>(false);
		final ParsingMethod parsignMethod = new ParsingMethod()
		{
			@Override
			@Nonnull
			public IElementType parse(final RBuilder builder)
			{
				if(blockCallSeen.get())
				{
					return RubyElementTypes.EMPTY_INPUT;
				}

				if(amperSeen.get())
				{
					return RubyElementTypes.EMPTY_INPUT;
				}

				if(builder.compare(tAMPER))
				{
					amperSeen.set(true);
					return CALL_ARGS.parseBlockToArg(builder);
				}

				if(builder.compare(tSTAR))
				{
					starSeen.set(true);
					return CALL_ARGS.parseArrayToArguments(builder);
				}

				if(CALL_ARGS.isArgumentsForwarding(builder))
				{
					return CALL_ARGS.parseArgumentsForwarding(builder);
				}

				IElementType result = BLOCK_CALL.parse(builder);
				if(result != RubyElementTypes.EMPTY_INPUT)
				{
					if(result == RubyElementTypes.COMMAND_CALL || result == RubyElementTypes.BLOCK_CALL)
					{
						blockCallSeen.set(true);
					}
					return result;
				}

				result = ASSOC_OR_ARG.parse(builder);
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


		ListParsingUtil.parseCommaDelimitedExpressions(builder, parsignMethod);
		statementMarker.done(RubyElementTypes.LIST_OF_EXPRESSIONS);

		builder.matchIgnoreEOL(tRPAREN);
		return RubyElementTypes.LIST_OF_EXPRESSIONS;
	}

}
