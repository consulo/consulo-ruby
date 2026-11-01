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

import consulo.language.ast.IElementType;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.COMPSTMT;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.definitions.method.BODYSTMT;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.iterators.OPT_BLOCK_VAR;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RMarker;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 05.07.2006
 */
public class BRACE_BLOCK implements RubyTokenTypes
{

/*
	brace_block	: LCURLY
              opt_block_var
              compstmt RCURLY
            | kDO
              opt_block_var
              compstmt kEND
            ;
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

		if(!builder.compare(BNF.tCODE_BLOCK_BEG_TOKENS))
		{
			return RubyElementTypes.EMPTY_INPUT;
		}

		final IElementType blockType = builder.compare(kDO) ? RubyElementTypes.DO_CODE_BLOCK : RubyElementTypes.BRACE_CODE_BLOCK;
		final IElementType endDelim = blockType == RubyElementTypes.DO_CODE_BLOCK ? kEND : tRBRACE;

		RMarker iteratorBlockMarker = builder.mark();

		builder.match(BNF.tCODE_BLOCK_BEG_TOKENS);

		OPT_BLOCK_VAR.parse(builder);

		builder.passEOLs();
		if(blockType == RubyElementTypes.DO_CODE_BLOCK)
		{
			// do ... rescue ... else ... ensure ... end
			COMPSTMT.parse(builder, kEND, kRESCUE, kELSE, kENSURE);
			BODYSTMT.parseRescueElseEnsure(builder);
		}
		else
		{
			COMPSTMT.parse(builder, endDelim);
		}

		builder.matchIgnoreEOL(endDelim);
		iteratorBlockMarker.done(blockType);
		return blockType;
	}

}
