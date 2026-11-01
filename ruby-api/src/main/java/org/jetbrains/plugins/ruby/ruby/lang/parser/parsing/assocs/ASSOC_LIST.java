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

package org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.assocs;


import jakarta.annotation.Nonnull;

import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.ParsingMethod;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.TRAILER;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.ErrorMsg;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.ListParsingUtil;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;
import consulo.language.ast.IElementType;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 27.06.2006
 */
public class ASSOC_LIST implements RubyTokenTypes
{

/*
assoc_list	: none
		| assocs trailer
		| args trailer
		;
*/

	/**
	 * @param builder Current builder
	 * @return Number of elements parsed
	 */
	public static int parse(final RBuilder builder)
	{
		ParsingMethod parsingMethod = new ParsingMethod()
		{
			boolean assocSeen = false;

			@Override
			@Nonnull
			public IElementType parse(final RBuilder builder)
			{
				IElementType result = ASSOC_OR_ARG.parse(builder);

				if(result == RubyElementTypes.ASSOC)
				{
					assocSeen = true;
					return result;
				}

				if(assocSeen && !ASSOC_OR_ARG.isAllowedAfterAssoc(result))
				{
					builder.error(ErrorMsg.expected(tASSOC));
				}
				return result;
			}
		};
		int count = ListParsingUtil.parseCommaDelimitedExpressions(builder, parsingMethod, false);
		if(count != 0)
		{
			TRAILER.parse(builder);

		}
		return count;
	}


}
