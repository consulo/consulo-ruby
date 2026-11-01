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


import consulo.language.ast.IElementType;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.assignments.MLHS;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.assignments.MLHS_OR_LHS;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.assignments.MRHS;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.commands.COMMAND_CALL;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.commands.COMMAND_CALL_OR_ARG;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.Alias;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.Condition;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.LBegin;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.Pattern;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.LEnd;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.Undef;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.primary.PRIMARY;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.ErrorMsg;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RMarker;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 25.04.2005
 */
public class STMT implements RubyTokenTypes
{
/*
stmt	: kALIAS fitem  fitem
		| kALIAS tGVAR tGVAR
		| kALIAS tGVAR tBACK_REF
		| kALIAS tGVAR tNTH_REF
		| kUNDEF undef_list
		| klBEGIN
		  LCURLY compstmt RCURLY
		| klEND LCURLY compstmt RCURLY
		| lhs '=' command_call
		| mlhs '=' command_call
		| var_lhs tOP_ASGN command_call
		| primary_value '[' aref_args ']' tOP_ASGN command_call
		| primary_value '.' tIDENTIFIER tOP_ASGN command_call
		| primary_value '.' tCONSTANT tOP_ASGN command_call
		| primary_value tCOLON2 tIDENTIFIER tOP_ASGN command_call
		| backref tOP_ASGN command_call
		| lhs '=' mrhs
		| mlhs '=' arg_value
		| mlhs '=' mrhs
		| expr

		|tHEREDOC_VALUE_CONTENT
		;
*/

	@Nonnull
	public static IElementType parse(final RBuilder builder)
	{
		if(builder.isDEBUG())
		{
			builder.STMT();
		}

		if(builder.passHeredocs())
		{
			return RubyElementTypes.HEREDOC_VALUE;
		}

		if(!builder.compare(BNF.tSTMT_FIRST_TOKENS))
		{
			return RubyElementTypes.EMPTY_INPUT;
		}

		RMarker marker = builder.mark();

		IElementType result = parseSingleStatement(builder);
		if(result == RubyElementTypes.EMPTY_INPUT)
		{
			marker.rollbackTo();
			return RubyElementTypes.EMPTY_INPUT;
		}

/*
        | arg tASSOC p_top_expr_body
        | arg keyword_in p_top_expr_body
*/
		if(builder.compare(tASSOC) || builder.compare(kIN))
		{
			builder.advanceLexer();
			Pattern.parseTop(builder, false);
			marker.done(RubyElementTypes.PATTERN_MATCH);
			marker = marker.precede();
			result = RubyElementTypes.PATTERN_MATCH;
		}
/*
		| stmt kIF_MOD expr_value
*/
		while(builder.compare(BNF.kMOD_RESWORDS))
		{
			if(builder.compareAndEat(kIF_MOD))
			{
				Condition.parse(builder);
				marker.done(RubyElementTypes.IF_MOD_STATEMENT);
				marker = marker.precede();
				result = RubyElementTypes.IF_MOD_STATEMENT;
			}

/*
        | stmt kUNLESS_MOD expr_value
*/
			if(builder.compareAndEat(kUNLESS_MOD))
			{
				Condition.parse(builder);
				marker.done(RubyElementTypes.UNLESS_MOD_STATEMENT);
				marker = marker.precede();
				result = RubyElementTypes.UNLESS_MOD_STATEMENT;
			}

/*
        | stmt kWHILE_MOD expr_value
*/
			if(builder.compareAndEat(kWHILE_MOD))
			{
				Condition.parse(builder);
				marker.done(RubyElementTypes.WHILE_MOD_STATEMENT);
				marker = marker.precede();
				result = RubyElementTypes.WHILE_MOD_STATEMENT;
			}

/*
        | stmt kUNTIL_MOD expr_value
*/
			if(builder.compareAndEat(kUNTIL_MOD))
			{
				Condition.parse(builder);
				marker.done(RubyElementTypes.UNTIL_MOD_STATEMENT);
				marker = marker.precede();
				result = RubyElementTypes.UNTIL_MOD_STATEMENT;
			}

/*
        | stmt kRESCUE_MOD stmt
*/
			if(builder.compareAndEat(kRESCUE_MOD))
			{
				if(STMT.parse(builder) == RubyElementTypes.EMPTY_INPUT)
				{
					builder.error(ErrorMsg.EXPRESSION_EXPECTED_MESSAGE);
				}
				marker.done(RubyElementTypes.RESCUE_MOD_STATEMENT);
				marker = marker.precede();
				result = RubyElementTypes.RESCUE_MOD_STATEMENT;
			}
		}

		marker.drop();
		return result;
	}


	@Nonnull
	private static IElementType parseSingleStatement(final RBuilder builder)
	{
/*
    : kALIAS fitem  fitem
    | kALIAS tGVAR tGVAR
    | kALIAS tGVAR tBACK_REF
    | kALIAS tGVAR tNTH_REF
*/
		if(builder.compare(kALIAS))
		{
			return Alias.parse(builder);
		}

/*
    | kUNDEF undef_list
*/
		if(builder.compare(kUNDEF))
		{
			return Undef.parse(builder);
		}

/*
    | klBEGIN LCURLY compstmt RCURLY
*/
		if(builder.compare(klBEGIN))
		{
			return LBegin.parse(builder);
		}

/*
    | klEND LCURLY compstmt RCURLY
*/
		if(builder.compare(klEND))
		{
			return LEnd.parse(builder);
		}

		// !!! if token is one of indicator tokens, no primary should be parsed, but expr !!!
		if(builder.compare(EXPR.INDICATOR_TOKENS))
		{
			return EXPR.parse(builder);
		}

		// multiassingment handling
		if(builder.compare(tLPAREN) || builder.compare(tSTAR))
		{
			RMarker marker = builder.mark();
			IElementType multiAssgn = parseMultiAssgnWithLeadMLHS(builder, marker, MLHS.parse(builder));
			if(multiAssgn != RubyElementTypes.EMPTY_INPUT)
			{
				return multiAssgn;
			}
			marker.rollbackTo();
		}

		RMarker beforePrimaryMarker = builder.mark();
		IElementType primaryResult = PRIMARY.parse(builder);

		if(BNF.LHS.contains(primaryResult))
		{
			RMarker beforeMLHS = beforePrimaryMarker;
			beforePrimaryMarker = beforePrimaryMarker.precede();
			IElementType result = MLHS_OR_LHS.parseWithLeadPRIMARY(builder, beforeMLHS, primaryResult);

			IElementType multiAssgn = parseMultiAssgnWithLeadMLHS(builder, beforePrimaryMarker, result);
			if(multiAssgn != RubyElementTypes.EMPTY_INPUT)
			{
				return multiAssgn;
			}

			IElementType assgn = parseAssgnWithLeadLHS(builder, beforePrimaryMarker, primaryResult);
			if(assgn != RubyElementTypes.EMPTY_INPUT)
			{
				return assgn;
			}
		}

/*
    | expr
*/
		if(primaryResult != RubyElementTypes.EMPTY_INPUT)
		{
			return EXPR.parseWithLeadPRIMARY(builder, beforePrimaryMarker, primaryResult);
		}

		// Just parse expression without any preparsed expressions
		beforePrimaryMarker.rollbackTo();
		return EXPR.parse(builder);
	}

	private static IElementType parseMultiAssgnWithLeadMLHS(final RBuilder builder, final RMarker marker, final IElementType result)
	{
		if(result != RubyElementTypes.MLHS)
		{
			return RubyElementTypes.EMPTY_INPUT;
		}
/*
    | mlhs '=' command_call
    | mlhs '=' arg_value
    | mlhs '=' mrhs
*/
		builder.match(tASSGN);
		if(MRHS.parse(builder) != RubyElementTypes.EMPTY_INPUT)
		{
			marker.done(RubyElementTypes.MULTI_ASSIGNMENT_EXPRESSION);
			return RubyElementTypes.MULTI_ASSIGNMENT_EXPRESSION;
		}

		if(COMMAND_CALL_OR_ARG.parse(builder) != RubyElementTypes.EMPTY_INPUT)
		{
			marker.done(RubyElementTypes.MULTI_ASSIGNMENT_EXPRESSION);
			return RubyElementTypes.MULTI_ASSIGNMENT_EXPRESSION;
		}
		builder.error(ErrorMsg.EXPRESSION_EXPECTED_MESSAGE);
		marker.done(RubyElementTypes.MULTI_ASSIGNMENT_EXPRESSION);
		return RubyElementTypes.MULTI_ASSIGNMENT_EXPRESSION;
	}

	private static IElementType parseAssgnWithLeadLHS(final RBuilder builder, final RMarker marker, final IElementType result)
	{
		if(!BNF.LHS.contains(result))
		{
			return RubyElementTypes.EMPTY_INPUT;
		}

/*
    | lhs '=' mrhs
*/
		if(builder.compare(tASSGN))
		{
			final RMarker beforOP_ASGN = builder.mark();
			builder.match(tASSGN);
			if(MRHS.parse(builder) != RubyElementTypes.EMPTY_INPUT)
			{
				beforOP_ASGN.drop();
				marker.done(RubyElementTypes.MULTI_ASSIGNMENT_EXPRESSION);
				return RubyElementTypes.MULTI_ASSIGNMENT_EXPRESSION;
			}
			beforOP_ASGN.rollbackTo();
		}
/*
    | lhs '=' command_call
*/
		if(builder.compare(tASSGN))
		{
			final RMarker beforOP_ASGN = builder.mark();
			builder.match(tASSGN);
			if(COMMAND_CALL.parse(builder) != RubyElementTypes.EMPTY_INPUT)
			{
				beforOP_ASGN.drop();
				marker.done(RubyElementTypes.ASSIGNMENT_EXPRESSION);
				return RubyElementTypes.ASSIGNMENT_EXPRESSION;
			}
			beforOP_ASGN.rollbackTo();
		}

/*
    | var_lhs tOP_ASGN command_call
    | primary_value '[' aref_args ']' tOP_ASGN command_call
    | primary_value '.' tIDENTIFIER tOP_ASGN command_call
    | primary_value '.' tCONSTANT tOP_ASGN command_call
    | primary_value tCOLON2 tIDENTIFIER tOP_ASGN command_call
    | backref tOP_ASGN command_call
*/
		if(builder.compare(BNF.tOP_ASGNS))
		{
			final RMarker beforOP_ASGN = builder.mark();
			builder.match(BNF.tOP_ASGNS);
			if(COMMAND_CALL.parse(builder) != RubyElementTypes.EMPTY_INPUT)
			{
				beforOP_ASGN.drop();
				marker.done(RubyElementTypes.SELF_ASSIGNMENT_EXPRESSION);
				return RubyElementTypes.SELF_ASSIGNMENT_EXPRESSION;
			}
			beforOP_ASGN.rollbackTo();
		}
		return RubyElementTypes.EMPTY_INPUT;
	}
}
