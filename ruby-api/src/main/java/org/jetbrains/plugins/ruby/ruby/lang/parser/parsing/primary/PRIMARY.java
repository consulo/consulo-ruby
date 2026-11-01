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

import consulo.language.ast.IElementType;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.COMPSTMT;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.EXPR;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.assocs.ASSOCS;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.basicTypes.Array;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.basicTypes.LITERAL;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.basicTypes.OPERATION;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.basicTypes.REFS;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.basicTypes.VARIABLE;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.basicTypes.stringLike.Heredoc;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.basicTypes.stringLike.Regexp;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.basicTypes.stringLike.Strings;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.basicTypes.stringLike.Words;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.commands.BRACE_BLOCK;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.commands.CALL_ARGS;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.definitions.method.BODYSTMT;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.definitions.method.F_ARGS;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.BeginEndBlock;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.Case;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.Defined;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.For;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.If;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.Unless;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.Until;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.While;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures.Yield;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.definitions.Class;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.definitions.Module;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.definitions.method.Method;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.ErrorMsg;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RMarker;
import consulo.language.ast.TokenSet;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 27.06.2006
 */
/*
primary_value 	: primary
		;

primary	: literal
		| strings
		| xstring
		| regexp
		| words
		| qwords
		| var_ref
		| backref
		| tFID
		| kBEGIN
		  bodystmt
		  kEND
		| tLPAREN_ARG expr  opt_nl ')'
		| tLPAREN compstmt ')'
		| primary_value tCOLON2 tCONSTANT
		| tCOLON3 tCONSTANT
		| primary_value '[' aref_args ']'
		| tLBRACK aref_args ']'
		| tLBRACE assoc_list RCURLY
		| kRETURN
		| kYIELD '(' call_args ')'
		| kYIELD '(' ')'
		| kYIELD
		| kDEFINED opt_nl '('  expr ')'
		| operation brace_block
		| method_call
		| method_call brace_block
		| kIF expr_value then
		  compstmt
		  if_tail
		  kEND
		| kUNLESS expr_value then
		  compstmt
		  opt_else
		  kEND
		| kWHILE  expr_value do
		  compstmt
		  kEND
		| kUNTIL  expr_value do
		  compstmt
		  kEND
		| kCASE expr_value opt_terms
		  case_body
		  kEND
		| kCASE opt_terms case_body kEND
		| kCASE opt_terms kELSE compstmt kEND
		| kFOR block_var kIN  expr_value do
		  compstmt
		  kEND
		| kCLASS cpath superclass
		  bodystmt
		  kEND
		| kCLASS tLSHFT expr
		  term
		  bodystmt
		  kEND
		| kMODULE cpath
		  bodystmt
		  kEND
		| kDEF fname
		  f_arglist
		  bodystmt
		  kEND
		| kDEF singleton dot_or_colon  fname
		  f_arglist
		  bodystmt
		  kEND
		| kBREAK
		| kNEXT
		| kREDO
		| kRETRY
		;

method_call	: operation paren_args
		| primary_value '.' operation2 opt_paren_args
		| primary_value tCOLON2 operation2 paren_args
		| primary_value tCOLON2 operation3
		| kSUPER paren_args
		| kSUPER
		;
*/

public class PRIMARY implements RubyTokenTypes
{
	private static final TokenSet TOKENS = TokenSet.orSet(BNF.tDOT_OR_COLON, BNF.tCODE_BLOCK_BEG_TOKENS, TokenSet.create(tfLPAREN, tfLBRACK));

	@Nonnull
	public static IElementType parse(final RBuilder builder)
	{
		if(builder.isDEBUG())
		{
			builder.PRIMARY();
		}

		if(!builder.compare(BNF.tPRIMARY_FISRT_TOKEN))
		{
			return RubyElementTypes.EMPTY_INPUT;
		}

		RMarker statementMarker = builder.mark();
		IElementType result = parseSinglePrimary(builder);

		if(result == RubyElementTypes.EMPTY_INPUT)
		{
			statementMarker.drop();
			return RubyElementTypes.EMPTY_INPUT;
		}

/*
primary : primary tCOLON2 tCONSTANT
        | primary '[' aref_args ']'
		| operation brace_block
        | operation paren_args [brace_block]
		| primary '.' operation2 opt_paren_args [brace_block]
		| primary tCOLON2 operation2 paren_args [brace_block]
		| primary tCOLON2 operation3 [brace_block]
		| kSUPER paren_args [brace_block]
		| kSUPER [brace_block]
		;
*/

		//TODO: Now we ignore the differences between operations
		while(builder.compare(TOKENS))
		{

/*
			| primary '[' aref_args ']'
*/
			if(builder.compareAndEat(tfLBRACK))
			{
				AREF_ARGS.parse(builder);
				builder.match(tRBRACK);
				statementMarker.done(RubyElementTypes.ARRAY_REFERENCE);
				result = RubyElementTypes.ARRAY_REFERENCE;
				statementMarker = statementMarker.precede();
			}

/*
            | primary_value '.' operation2 opt_paren_args
*/
			if(builder.compareAndEat(tDOT) || builder.compareAndEat(tANDDOT))
			{
				// callable.(args)
				if(builder.compare(BNF.tLPARENS))
				{
					builder.advanceLexer();
					CALL_ARGS.parse(builder);
					builder.passEOLs();
					builder.match(tRPAREN);
					statementMarker.done(RubyElementTypes.FUNCTION_CALL);
					result = RubyElementTypes.FUNCTION_CALL;
					statementMarker = statementMarker.precede();
					continue;
				}
				if(OPERATION.parse2(builder) == RubyElementTypes.EMPTY_INPUT)
				{
					builder.error(ErrorMsg.expected(RBundle.message("parsing.operation")));
				}
				statementMarker.done(RubyElementTypes.DOT_REFERENCE);
				result = RubyElementTypes.DOT_REFERENCE;
				statementMarker = statementMarker.precede();
			}

/*
            | primary_value tCOLON2 operation2 paren_args
            | primary_value tCOLON2 operation3
*/
			if(builder.compareAndEat(tCOLON2))
			{
				if(OPERATION.parse2(builder) == RubyElementTypes.EMPTY_INPUT)
				{
					builder.error(ErrorMsg.expected(RBundle.message("parsing.operation")));
				}
				statementMarker.done(RubyElementTypes.COLON_REFERENCE);
				result = RubyElementTypes.COLON_REFERENCE;
				statementMarker = statementMarker.precede();
			}

/*
            | operation paren_args [brace_block]
*/
			if(builder.compare(tfLPAREN))
			{
				if(BNF.COMMAND_OBJECTS.contains(result))
				{
					PAREN_ARGS.parse(builder);
					statementMarker.done(RubyElementTypes.FUNCTION_CALL);
					result = RubyElementTypes.FUNCTION_CALL;
					statementMarker = statementMarker.precede();
				}
				else
				{
					statementMarker.drop();
					return result;
				}
			}

			//          [brace_block]
			if(builder.compare(BNF.tCODE_BLOCK_BEG_TOKENS))
			{
				// in command arguments a do block belongs to the command
				if(BNF.ITERATOR_OBJECTS.contains(result) && (builder.isDoBlockAllowed() || !builder.compare(kDO)))
				{
					BRACE_BLOCK.parse(builder);
					statementMarker.done(RubyElementTypes.BLOCK_CALL);
					result = RubyElementTypes.BLOCK_CALL;
					statementMarker = statementMarker.precede();
				}
				else
				{
					statementMarker.drop();
					return result;
				}
			}

		}

		statementMarker.drop();
		return result;
	}

	/*
			primary	: literal
					| strings
					| xstring
					| regexp
					| words
					| qwords
					| var_ref
					| backref
					| tFID
					| tCOLON3 tCONSTANT
					;
	*/
	@Nonnull
	private static IElementType parseSinglePrimary(final RBuilder builder)
	{
		if(builder.compare(tLAMBDA))
		{
			return parseLambda(builder);
		}

		// strings parsing
		if(builder.compare(BNF.tSTRINGS_BEGINNINGS))
		{
			return Strings.parse(builder);
		}

		// regexp parsing
		if(builder.compare(BNF.tREGEXP_BEGINNINGS))
		{
			return Regexp.parse(builder);
		}

		// words parsing
		if(builder.compare(BNF.tWORDS_BEGINNINGS))
		{
			return Words.parse(builder);
		}

		// heredocs parsing
		if(builder.compare(tHEREDOC_ID))
		{
			return Heredoc.parse(builder);
		}

/*
        | backref
        | nthref
*/
		if(builder.compare(BNF.tREFS))
		{
			return REFS.parse(builder);
		}
/*
        | tFID
*/
		if(builder.compare(tFID))
		{
			return builder.parseSingleToken(tFID, RubyElementTypes.FID);
		}
/*
        | literal
*/
		if(builder.compare(BNF.tLITERAL_FIRST_TOKEN))
		{
			return LITERAL.parse(builder);
		}
/*
        | var_ref
*/
		if(builder.compare(BNF.tVARIABLES))
		{
			return VARIABLE.parse(builder);
		}
/*
        | tLPAREN compstmt ')'
*/
		if(builder.compare(tLPAREN))
		{
			RMarker statementMarker = builder.mark();
			builder.match(tLPAREN);
			COMPSTMT.parse(builder, tRPAREN);
			builder.matchIgnoreEOL(tRPAREN);
			statementMarker.done(RubyElementTypes.EXPRESSION_IN_PARENS);
			return RubyElementTypes.EXPRESSION_IN_PARENS;
		}
/*
        | tLPAREN_ARG expr  opt_nl ')'
*/
		if(builder.compare(tLPAREN_ARG))
		{
			RMarker statementMarker = builder.mark();
			builder.match(tLPAREN_ARG);
			EXPR.parse(builder);
			builder.matchIgnoreEOL(tRPAREN);
			statementMarker.done(RubyElementTypes.EXPRESSION_IN_PARENS);
			return RubyElementTypes.EXPRESSION_IN_PARENS;
		}

/*
        | tLBRACK aref_args ']'
*/
		if(builder.compare(tLBRACK))
		{
			return Array.parse(builder);
		}
/*
        | tLBRACE assoc_list RCURLY
*/
		if(builder.compare(tLBRACE))
		{
			return ASSOCS.parse(builder);
		}
/*
        | tCOLON3 tCONSTANT
*/
		if(builder.compare(tCOLON3))
		{
			RMarker exprMarker = builder.mark();
			builder.match(tCOLON3);

			builder.parseSingleToken(tCONSTANT, RubyElementTypes.CONSTANT);
			exprMarker.done(RubyElementTypes.COLON_REFERENCE);
			return RubyElementTypes.COLON_REFERENCE;
		}

		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////// Control structures ///////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


/*
        | kRETURN
*/
		if(builder.compare(kRETURN))
		{
			return builder.parseSingleToken(kRETURN, RubyElementTypes.RETURN_STATEMENT);
		}

/*
        | kDEFINED opt_nl '('  expr ')'
*/
		if(builder.compare(kDEFINED))
		{
			return Defined.parseWithParenthes(builder);
		}

/*
        | kYIELD '(' call_args ')'
        | kYIELD '(' ')'
        | kYIELD
*/
		if(builder.compare(kYIELD))
		{
			return Yield.parseWithParenthes(builder);
		}

/*
        | kIF expr_value then
          compstmt
          if_tail
          kEND
*/
		if(builder.compare(kIF))
		{
			return If.parse(builder);
		}

/*
        | kUNLESS expr_value then
          compstmt
          opt_else
          kEND
*/
		if(builder.compare(kUNLESS))
		{
			return Unless.parse(builder);
		}

/*
        | kCASE expr_value opt_terms
          case_body
          kEND
        | kCASE opt_terms case_body kEND
        | kCASE opt_terms kELSE compstmt kEND
*/
		if(builder.compare(kCASE))
		{
			return Case.parse(builder);
		}

/*
        | kWHILE  expr_value do
          compstmt
          kEND
*/
		if(builder.compare(kWHILE))
		{
			return While.parse(builder);
		}

/*
        | kUNTIL  expr_value do
          compstmt
          kEND
*/
		if(builder.compare(kUNTIL))
		{
			return Until.parse(builder);
		}

/*
        | kFOR block_var kIN  expr_value do
          compstmt
          kEND
*/
		if(builder.compare(kFOR))
		{
			return For.parse(builder);
		}

/*
        | kBEGIN
          bodystmt
          kEND
*/
		if(builder.compare(kBEGIN))
		{
			return BeginEndBlock.parse(builder);
		}

/*
        | kRETRY
*/
		if(builder.compare(kRETRY))
		{
			return builder.parseSingleToken(kRETRY, RubyElementTypes.RETRY_STATEMENT);
		}

/*
        | kBREAK
*/
		if(builder.compare(kBREAK))
		{
			return builder.parseSingleToken(kBREAK, RubyElementTypes.BREAK_STATEMENT);
		}

/*
        | kNEXT
*/
		if(builder.compare(kNEXT))
		{
			return builder.parseSingleToken(kNEXT, RubyElementTypes.NEXT_STATEMENT);
		}

/*
        | kREDO
*/
		if(builder.compare(kREDO))
		{
			return builder.parseSingleToken(kREDO, RubyElementTypes.REDO_STATEMENT);
		}

/*
        | kMODULE cpath
          bodystmt
          kEND
*/
		if(builder.compare(kMODULE))
		{
			return Module.parse(builder);
		}

/*
		| kCLASS cpath superclass
		  bodystmt
		  kEND
		| kCLASS tLSHFT expr
		  term
		  bodystmt
		  kEND
*/
		if(builder.compare(kCLASS))
		{
			return Class.parse(builder);
		}

/*
        | kDEF fname
          f_arglist
          bodystmt
          kEND
        | kDEF singleton dot_or_colon  fname
          f_arglist
          bodystmt
          kEND
*/
		if(builder.compare(kDEF))
		{
			return Method.parse(builder);
		}


		return RubyElementTypes.EMPTY_INPUT;
	}


	/*
		lambda	: tLAMBDA f_larglist lambda_body
		f_larglist	: '(' f_args opt_bv_decl ')' | f_args
		lambda_body	: tLAMBEG compstmt '}' | keyword_do_LAMBDA bodystmt keyword_end
	*/
	@Nonnull
	private static IElementType parseLambda(final RBuilder builder)
	{
		RMarker marker = builder.mark();
		builder.match(tLAMBDA);

		if(builder.compare(BNF.tLPARENS))
		{
			builder.advanceLexer();
			RMarker argumentsMarker = builder.mark();
			F_ARGS.parse(builder);
			argumentsMarker.done(RubyElementTypes.FUNCTION_ARGUMENT_LIST);
			builder.passEOLs();
			builder.match(tRPAREN);
		}
		else if(!builder.compare(BNF.tCODE_BLOCK_BEG_TOKENS) && !builder.compare(kDO_COND))
		{
			RMarker argumentsMarker = builder.mark();
			F_ARGS.parse(builder);
			argumentsMarker.done(RubyElementTypes.COMMAND_ARGUMENT_LIST);
		}

		if(builder.compare(BNF.tLBRACES))
		{
			builder.advanceLexer();
			builder.passEOLs();
			COMPSTMT.parse(builder, tRBRACE);
			builder.matchIgnoreEOL(tRBRACE);
		}
		else if(builder.compareAndEat(kDO) || builder.compareAndEat(kDO_COND))
		{
			builder.passEOLs();
			COMPSTMT.parse(builder, kEND, kRESCUE, kELSE, kENSURE);
			BODYSTMT.parseRescueElseEnsure(builder);
			builder.matchIgnoreEOL(kEND);
		}
		else
		{
			builder.error(ErrorMsg.expected(tLBRACE));
		}

		marker.done(RubyElementTypes.LAMBDA);
		return RubyElementTypes.LAMBDA;
	}
}
