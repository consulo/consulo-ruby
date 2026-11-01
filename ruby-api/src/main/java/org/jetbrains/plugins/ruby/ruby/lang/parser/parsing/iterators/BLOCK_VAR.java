package org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.iterators;


import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.RBundle;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.assignments.MLHS_OR_LHS;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.ErrorMsg;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RMarker;
import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.basicTypes.VARIABLE;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.primary.PRIMARY;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.ListParsingUtil;

/**
 * @author oleg
 */
public class BLOCK_VAR
{
	/*
		block_var	: lhs
				| mlhs
				;
	*/
	@Nonnull
	public static IElementType parse(final RBuilder builder)
	{
		RMarker iteratorVariablesMarker = builder.mark();
		IElementType result = MLHS_OR_LHS.parse(builder);
		// |a, b| of a block, or the variables of for a, b in ...
		if(result != RubyElementTypes.EMPTY_INPUT && (builder.compare(RubyTokenTypes.tPIPE) || builder.compare(RubyTokenTypes.kIN)))
		{
			iteratorVariablesMarker.done(RubyElementTypes.BLOCK_VARIABLES);
			return RubyElementTypes.BLOCK_VARIABLES;
		}
		iteratorVariablesMarker.rollbackTo();

		iteratorVariablesMarker = builder.mark();
		int count = ListParsingUtil.parseCommaDelimitedExpressions(builder, BLOCK_VAR::parseParameter);
		// |a; b| - block local variables
		if(builder.compareAndEat(RubyTokenTypes.tSEMICOLON))
		{
			ListParsingUtil.parseCommaDelimitedExpressions(builder, BLOCK_VAR::parseLocalVariable);
			count++;
		}
		if(count > 0)
		{
			iteratorVariablesMarker.done(RubyElementTypes.BLOCK_VARIABLES);
			return RubyElementTypes.BLOCK_VARIABLES;
		}
		iteratorVariablesMarker.rollbackTo();
		builder.error(ErrorMsg.expected(RBundle.message("parsing.block.variables")));
		return RubyElementTypes.EMPTY_INPUT;
	}

	private static final TokenSet KEYWORD_VALUE_END = TokenSet.create(RubyTokenTypes.tCOMMA, RubyTokenTypes.tPIPE, RubyTokenTypes.tSEMICOLON);

	/**
	 * A parameter of a block: defaults and keywords take a primary only, a binary '|' would close the parameters
	 */
	@Nonnull
	private static IElementType parseParameter(final RBuilder builder)
	{
		if(builder.compare(BNF.tLPARENS))
		{
			RMarker marker = builder.mark();
			builder.advanceLexer();
			MLHS_OR_LHS.parse(builder);
			builder.match(RubyTokenTypes.tRPAREN);
			marker.done(RubyElementTypes.BLOCK_VARIABLES);
			return RubyElementTypes.BLOCK_VARIABLES;
		}
		if(builder.compare(RubyTokenTypes.tSTAR))
		{
			return parsePrefixed(builder, RubyElementTypes.ARRAY_ARGUMENT);
		}
		if(builder.compare(RubyTokenTypes.tDSTAR))
		{
			return parsePrefixed(builder, RubyElementTypes.KEYWORD_REST_ARGUMENT);
		}
		if(builder.compare(RubyTokenTypes.tAMPER))
		{
			return parsePrefixed(builder, RubyElementTypes.BLOCK_ARGUMENT);
		}
		if(builder.compare(RubyTokenTypes.tLABEL))
		{
			RMarker marker = builder.mark();
			builder.advanceLexer();
			if(!builder.compare(KEYWORD_VALUE_END))
			{
				PRIMARY.parse(builder);
			}
			marker.done(RubyElementTypes.KEYWORD_ARGUMENT);
			return RubyElementTypes.KEYWORD_ARGUMENT;
		}
		if(builder.compare(BNF.tF_NORMARGS))
		{
			RMarker marker = builder.mark();
			VARIABLE.parse(builder);
			if(builder.compareAndEat(RubyTokenTypes.tASSGN))
			{
				if(PRIMARY.parse(builder) == RubyElementTypes.EMPTY_INPUT)
				{
					builder.error(ErrorMsg.EXPRESSION_EXPECTED_MESSAGE);
				}
				marker.done(RubyElementTypes.PREDEFINED_ARGUMENT);
				return RubyElementTypes.PREDEFINED_ARGUMENT;
			}
			marker.done(RubyElementTypes.ARGUMENT);
			return RubyElementTypes.ARGUMENT;
		}
		return RubyElementTypes.EMPTY_INPUT;
	}

	@Nonnull
	private static IElementType parsePrefixed(final RBuilder builder, final IElementType type)
	{
		RMarker marker = builder.mark();
		builder.advanceLexer();
		if(builder.compare(BNF.tF_NORMARGS))
		{
			VARIABLE.parse(builder);
		}
		marker.done(type);
		return type;
	}

	@Nonnull
	private static IElementType parseLocalVariable(final RBuilder builder)
	{
		if(!builder.compare(BNF.tF_NORMARGS))
		{
			return RubyElementTypes.EMPTY_INPUT;
		}
		RMarker marker = builder.mark();
		VARIABLE.parse(builder);
		marker.done(RubyElementTypes.ARGUMENT);
		return RubyElementTypes.ARGUMENT;
	}
}
