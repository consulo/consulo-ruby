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

package org.jetbrains.plugins.ruby.ruby.lang.parser.parsing;


import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.ListParsingUtil;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RMarker;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 27.06.2006
 */
public class COMPSTMT
{

/*
	compstmt	: stmts opt_terms
            ;

    stmts	: none
            | stmt
            | stmts terms stmt
            ;
*/


	/**
	 * @param builder       Current builder
	 * @param endDelimiters Set of end delimiters
	 */
	public static void parse(final RBuilder builder, IElementType... endDelimiters)
	{
		final boolean doBlockAllowed = builder.setDoBlockAllowed(true);
		try
		{
			doParse(builder, endDelimiters);
		}
		finally
		{
			builder.setDoBlockAllowed(doBlockAllowed);
		}
	}

	private static void doParse(final RBuilder builder, IElementType... endDelimiters)
	{
		builder.passJunks();
		parseSTMTS(builder, TokenSet.create(endDelimiters));
	}

	/**
	 * Parsing STATEMENTS up to the one of end delimiters or up to the end
	 *
	 * @param builder       Current builder
	 * @param endDelimiters Set of end delimiters
	 */
	private static void parseSTMTS(final RBuilder builder, TokenSet endDelimiters)
	{
		RMarker blockMarker = builder.mark();
		ListParsingUtil.parseSTMTS(builder, endDelimiters);
		blockMarker.done(RubyElementTypes.COMPOUND_STATEMENT);
	}

}
