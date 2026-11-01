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

package org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.commands;


import jakarta.annotation.Nonnull;

import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.Break;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.Next;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.Return;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.iterators.BLOCK_COMMAND;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RMarker;
import consulo.language.ast.IElementType;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 06.07.2006
 */
public class COMMAND_CALL implements RubyTokenTypes
{
/*
	command_call	: command
            | block_command
            | kRETURN call_args
            | kBREAK call_args
            | kNEXT call_args
            ;

*/

	@Nonnull
	public static IElementType parse(final RBuilder builder)
	{
		if(!builder.compare(BNF.tCOMMAND_CALL_FIRST_TOKENS))
		{
			return RubyElementTypes.EMPTY_INPUT;
		}
/*
        | kRETURN call_args
*/
		if(builder.compare(kRETURN))
		{
			IElementType result = Return.parseWithCallArgs(builder);
			if(result != RubyElementTypes.EMPTY_INPUT)
			{
				return RubyElementTypes.RETURN_STATEMENT;
			}
		}

/*
        | kBREAK call_args
*/
		if(builder.compare(kBREAK))
		{
			IElementType result = Break.parseWithCallArgs(builder);
			if(result != RubyElementTypes.EMPTY_INPUT)
			{
				return RubyElementTypes.BREAK_STATEMENT;
			}
		}

/*
        | kNEXT call_args
*/
		if(builder.compare(kNEXT))
		{
			IElementType result = Next.parseWithCallArgs(builder);
			if(result != RubyElementTypes.EMPTY_INPUT)
			{
				return RubyElementTypes.NEXT_STATEMENT;
			}
		}

/*
        | block_command
        | command
*/
		return parseWithLeadCOMMAND(builder, builder.mark(), COMMAND.parse(builder));
	}


	/**
	 * Parses command call with leading COMMAND parsed
	 *
	 * @param builder Current builder
	 * @param marker  Marker before leading COMMAND
	 * @param result  result of leading COMMAND parsing
	 * @return result of parsing
	 */
	private static IElementType parseWithLeadCOMMAND(final RBuilder builder, RMarker marker, IElementType result)
	{
/*
        | block_command
        | command
*/
		if(result != RubyElementTypes.EMPTY_INPUT && builder.compare(kDO) && builder.isDoBlockAllowed())
		{
			return BLOCK_COMMAND.parseWithLeadCOMMAND(builder, marker, result);
		}
		if(result != RubyElementTypes.EMPTY_INPUT)
		{
			marker.drop();
			return result;
		}
		marker.rollbackTo();
		return RubyElementTypes.EMPTY_INPUT;
	}


	/**
	 * Parses command call with leading ARG parsed
	 *
	 * @param builder   Current builder
	 * @param marker    Marker before leading ARG
	 * @param argResult result of leading ARG parsing
	 * @return result of parsing
	 */
	public static IElementType parseWithLeadARG(final RBuilder builder, final RMarker marker, final IElementType argResult)
	{
		return parseWithLeadCOMMAND(builder, marker.precede(), COMMAND.parseWithLeadARG(builder, marker, argResult));
	}
}
