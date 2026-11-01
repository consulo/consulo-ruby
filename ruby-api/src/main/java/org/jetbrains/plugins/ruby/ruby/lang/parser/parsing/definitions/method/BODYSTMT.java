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

package org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.definitions.method;


import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.COMPSTMT;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.OPT_ELSE;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.OPT_RESCUE;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RMarker;
import consulo.language.ast.IElementType;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 05.07.2006
 */
public class BODYSTMT implements RubyTokenTypes
{
	/*
		opt_ensure	: kENSURE compstmt
				| none
				;
	*/
	@Nonnull
	private static IElementType parseOPT_ENSURE(final RBuilder builder)
	{
		if(!builder.compare(kENSURE))
		{
			return RubyElementTypes.EMPTY_INPUT;
		}
		final RMarker statementMarker = builder.mark();
		builder.match(kENSURE);

		COMPSTMT.parse(builder, kEND);

		statementMarker.done(RubyElementTypes.ENSURE_BLOCK);
		return RubyElementTypes.ENSURE_BLOCK;
	}

	/*
	bodystmt	: compstmt
			  opt_rescue
			  opt_else
			  opt_ensure
			;

*/
	public static void parse(final RBuilder builder)
	{
		builder.passEOLs();

		RMarker bodyMarker = builder.mark();
		COMPSTMT.parse(builder, kEND, kRESCUE, kELSE, kENSURE);

		parseRescueElseEnsure(builder);
		bodyMarker.done(RubyElementTypes.BODY_STATEMENT);
	}

	/**
	 * The {@code rescue} / {@code else} / {@code ensure} clauses after a body
	 */
	public static void parseRescueElseEnsure(final RBuilder builder)
	{
		while(builder.compareIgnoreEOL(kRESCUE))
		{
			OPT_RESCUE.parse(builder);
		}
		if(builder.compareIgnoreEOL(kELSE))
		{
			OPT_ELSE.parse(builder);
		}
		if(builder.compareIgnoreEOL(kENSURE))
		{
			parseOPT_ENSURE(builder);
		}
	}
}
