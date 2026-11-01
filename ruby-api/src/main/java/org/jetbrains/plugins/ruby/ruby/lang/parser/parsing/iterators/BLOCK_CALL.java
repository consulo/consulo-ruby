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

package org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.iterators;


import jakarta.annotation.Nonnull;

import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.basicTypes.OPERATION;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.commands.BRACE_BLOCK;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.commands.COMMAND;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.primary.PAREN_ARGS;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.ErrorMsg;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RMarker;
import consulo.language.ast.IElementType;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 06.07.2006
 */
public class BLOCK_CALL implements RubyTokenTypes
{
/*
   block_call	: command do_block
                | block_call '.' operation2 opt_paren_args
                | block_call tCOLON2 operation2 opt_paren_args
*/

	/**
	 * Block call parsing
	 * if do block not found returns result of COMMAND parsing
	 *
	 * @param builder current RBuilder
	 * @return result
	 */
	@Nonnull
	public static IElementType parse(final RBuilder builder)
	{
		return parseWithLeadSINGLE_BLOCK(builder, builder.mark(), parseBlockCallSingle(builder));
	}

	/*
		: command do_block
	*/
	@Nonnull
	private static IElementType parseBlockCallSingle(final RBuilder builder)
	{
		return parseSingleWithLeadCOMMAND(builder, builder.mark(), COMMAND.parse(builder));
	}

	/**
	 * Parses single block_call (command do_block) with leading command parsed
	 *
	 * @param builder Current builder
	 * @param marker  Marker before command
	 * @param result  Command parsing result
	 * @return result of block_call parsing
	 */
	private static IElementType parseSingleWithLeadCOMMAND(final RBuilder builder, RMarker marker, IElementType result)
	{
		if(result != RubyElementTypes.COMMAND_CALL || !builder.compare(kDO) || !builder.isDoBlockAllowed())
		{
			marker.drop();
			return result;
		}
		BRACE_BLOCK.parse(builder);
		marker.done(RubyElementTypes.BLOCK_CALL);
		return RubyElementTypes.BLOCK_CALL;
	}

	/**
	 * Parses block_call with leading singleblock parsed
	 *
	 * @param builder           Current builder
	 * @param marker            Marker before singleblock
	 * @param singleBlockresult result of parsing singleBlock
	 * @return result of block_call parsing
	 */
	private static IElementType parseWithLeadSINGLE_BLOCK(final RBuilder builder, RMarker marker, IElementType singleBlockresult)
	{
		if(singleBlockresult != RubyElementTypes.BLOCK_CALL)
		{
			marker.drop();
			return singleBlockresult;
		}


		while(builder.compare(BNF.tDOT_OR_COLON))
		{
/*
		| block_call '.' operation2 opt_paren_args
*/
			if(builder.compareAndEat(tDOT) || builder.compareAndEat(tANDDOT))
			{
				if(OPERATION.parse2(builder) == RubyElementTypes.EMPTY_INPUT)
				{
					builder.error(ErrorMsg.expected(RBundle.message("parsing.operation")));
				}
				singleBlockresult = RubyElementTypes.DOT_REFERENCE;
				marker.done(RubyElementTypes.DOT_REFERENCE);
				marker = marker.precede();

				if(PAREN_ARGS.parse(builder) != RubyElementTypes.EMPTY_INPUT)
				{
					singleBlockresult = RubyElementTypes.FUNCTION_CALL;
					marker.done(RubyElementTypes.FUNCTION_CALL);
					marker = marker.precede();
				}
			}

/*
        | block_call '::' operation2 opt_paren_args
*/
			if(builder.compareAndEat(tCOLON2))
			{
				if(OPERATION.parse2(builder) == RubyElementTypes.EMPTY_INPUT)
				{
					builder.error(ErrorMsg.expected(RBundle.message("parsing.identifier.or.operation")));
				}
				singleBlockresult = RubyElementTypes.COLON_REFERENCE;
				marker.done(RubyElementTypes.COLON_REFERENCE);
				marker = marker.precede();

				if(PAREN_ARGS.parse(builder) != RubyElementTypes.EMPTY_INPUT)
				{
					singleBlockresult = RubyElementTypes.FUNCTION_CALL;
					marker.done(RubyElementTypes.FUNCTION_CALL);
					marker = marker.precede();
				}
			}

		}
		marker.drop();
		return singleBlockresult;
	}

	/**
	 * Parses block_call with leading command parsed
	 *
	 * @param builder Current builder
	 * @param marker  Marker before command
	 * @param result  command parsing result
	 * @return result of block_call parsing
	 */
	public static IElementType parseWithLeadCOMMAND(final RBuilder builder, RMarker marker, IElementType result)
	{
		return parseWithLeadSINGLE_BLOCK(builder, marker.precede(), parseSingleWithLeadCOMMAND(builder, marker, result));
	}
}
