package org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.controlStructures;

import consulo.language.ast.IElementType;
import consulo.language.ast.TokenSet;
import jakarta.annotation.Nonnull;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.RubyElementTypes;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.EXPR;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.basicTypes.VARIABLE;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsing.primary.PRIMARY;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.ErrorMsg;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RBuilder;
import org.jetbrains.plugins.ruby.ruby.lang.parser.parsingUtils.RMarker;

/**
 * Patterns of {@code case/in}, {@code expr => pattern} and {@code expr in pattern}.
 *
 * <pre>
 * p_top     : p_expr [',' p_expr ...] | hash pattern without braces
 * p_expr    : p_alt ['=>' identifier]
 * p_alt     : p_primary ['|' p_primary ...]
 * p_primary : '[' p_list ']' | '{' hash ']' | '^' (identifier | '(' expr ')') | value ['..' value] | Const['(' p_list ')' | '[' p_list ']']
 * </pre>
 *
 * @author VISTALL
 * @since 2026-11-01
 */
public class Pattern implements RubyTokenTypes
{
	private static final TokenSet ALTERNATIVE_TOKENS = TokenSet.create(tPIPE, tBIT_OR);
	private static final TokenSet LBRACKS = TokenSet.create(tLBRACK, tfLBRACK);
	private static final TokenSet LBRACES = TokenSet.create(tLBRACE, tfLBRACE);
	private static final TokenSet HASH_VALUE_END = TokenSet.create(tCOMMA, tRBRACE, tEOL, tSEMICOLON, kTHEN, kIF_MOD, kUNLESS_MOD, tRPAREN);

	/**
	 * @param guardAllowed {@code in pattern if cond} - only in {@code case/in}
	 */
	public static void parseTop(final RBuilder builder, final boolean guardAllowed)
	{
		RMarker marker = builder.mark();
		if(builder.compare(tLABEL) || builder.compare(tDSTAR))
		{
			parseHashBody(builder);
		}
		else
		{
			if(parseExpr(builder) == RubyElementTypes.EMPTY_INPUT)
			{
				builder.error(ErrorMsg.expected("pattern"));
			}
			// in a, b - an array pattern without brackets
			while(builder.compareAndEat(tCOMMA))
			{
				parseListElement(builder);
			}
		}
		marker.done(RubyElementTypes.PATTERN);

		if(guardAllowed && (builder.compare(kIF_MOD) || builder.compare(kUNLESS_MOD)))
		{
			RMarker guardMarker = builder.mark();
			builder.advanceLexer();
			if(EXPR.parse(builder) == RubyElementTypes.EMPTY_INPUT)
			{
				builder.error(ErrorMsg.EXPRESSION_EXPECTED_MESSAGE);
			}
			guardMarker.done(RubyElementTypes.PATTERN_GUARD);
		}
	}

	@Nonnull
	private static IElementType parseExpr(final RBuilder builder)
	{
		RMarker marker = builder.mark();
		IElementType result = parsePrimary(builder);
		if(result == RubyElementTypes.EMPTY_INPUT)
		{
			marker.drop();
			return result;
		}

		while(builder.compareAndEat(ALTERNATIVE_TOKENS))
		{
			if(parsePrimary(builder) == RubyElementTypes.EMPTY_INPUT)
			{
				builder.error(ErrorMsg.expected("pattern"));
			}
			marker.done(RubyElementTypes.PATTERN);
			marker = marker.precede();
		}

		// pattern => binding
		if(builder.compareAndEat(tASSOC))
		{
			if(VARIABLE.parse(builder) == RubyElementTypes.EMPTY_INPUT)
			{
				builder.error(ErrorMsg.expected("variable"));
			}
			marker.done(RubyElementTypes.PATTERN);
			return RubyElementTypes.PATTERN;
		}
		marker.drop();
		return RubyElementTypes.PATTERN;
	}

	@Nonnull
	private static IElementType parsePrimary(final RBuilder builder)
	{
		if(builder.compare(LBRACKS))
		{
			RMarker marker = builder.mark();
			builder.advanceLexer();
			builder.passEOLs();
			parseList(builder, tRBRACK);
			builder.matchIgnoreEOL(tRBRACK);
			marker.done(RubyElementTypes.PATTERN);
			return RubyElementTypes.PATTERN;
		}

		if(builder.compare(LBRACES))
		{
			RMarker marker = builder.mark();
			builder.advanceLexer();
			builder.passEOLs();
			parseHashBody(builder);
			builder.matchIgnoreEOL(tRBRACE);
			marker.done(RubyElementTypes.PATTERN);
			return RubyElementTypes.PATTERN;
		}

		// ^variable, ^@ivar, ^(expression)
		if(builder.compare(tXOR))
		{
			RMarker marker = builder.mark();
			builder.advanceLexer();
			if(builder.compareAndEat(BNF.tLPARENS))
			{
				EXPR.parse(builder);
				builder.matchIgnoreEOL(tRPAREN);
			}
			else if(VARIABLE.parse(builder) == RubyElementTypes.EMPTY_INPUT)
			{
				builder.error(ErrorMsg.expected("variable"));
			}
			marker.done(RubyElementTypes.PATTERN);
			return RubyElementTypes.PATTERN;
		}

		return parseValue(builder);
	}

	/**
	 * A value, a range of values, a binding or a constant - {@code Point(x:, y:)} and {@code Point[x, y]} parse
	 * as a call and an array reference of the constant
	 */
	@Nonnull
	private static IElementType parseValue(final RBuilder builder)
	{
		RMarker marker = builder.mark();

		// ..5
		if(builder.compareAndEat(BNF.tRANGE_TOKENS))
		{
			parseRangeEnd(builder);
			marker.done(RubyElementTypes.RANGE_EXPRESSION);
			return RubyElementTypes.RANGE_EXPRESSION;
		}

		builder.compareAndEat(BNF.tMINUSES);
		IElementType result = PRIMARY.parse(builder);
		if(result == RubyElementTypes.EMPTY_INPUT)
		{
			marker.rollbackTo();
			return RubyElementTypes.EMPTY_INPUT;
		}

		// 1..5, 1..
		if(builder.compareAndEat(BNF.tRANGE_TOKENS))
		{
			parseRangeEnd(builder);
			marker.done(RubyElementTypes.RANGE_EXPRESSION);
			return RubyElementTypes.RANGE_EXPRESSION;
		}
		marker.drop();
		return result;
	}

	private static void parseRangeEnd(final RBuilder builder)
	{
		builder.compareAndEat(BNF.tMINUSES);
		PRIMARY.parse(builder);
	}

	private static void parseList(final RBuilder builder, final IElementType end)
	{
		if(builder.compare(end))
		{
			return;
		}
		parseListElement(builder);
		while(builder.compareAndEatIgnoreEOL(tCOMMA))
		{
			builder.passEOLs();
			if(builder.compare(end))
			{
				return;
			}
			parseListElement(builder);
		}
	}

	/**
	 * A pattern or a splat - {@code *rest}, {@code *} (the find pattern {@code [*, x, *]} included)
	 */
	private static void parseListElement(final RBuilder builder)
	{
		if(builder.compare(tSTAR))
		{
			RMarker marker = builder.mark();
			builder.advanceLexer();
			VARIABLE.parse(builder);
			marker.done(RubyElementTypes.PATTERN);
			return;
		}
		if(parseExpr(builder) == RubyElementTypes.EMPTY_INPUT)
		{
			builder.error(ErrorMsg.expected("pattern"));
		}
	}

	/**
	 * {@code key: pattern}, {@code key:}, {@code "key": pattern}, {@code **rest}, {@code **nil}
	 */
	private static void parseHashBody(final RBuilder builder)
	{
		do
		{
			builder.passEOLs();
			if(builder.compare(tRBRACE))
			{
				return;
			}
			RMarker marker = builder.mark();
			if(builder.compareAndEat(tLABEL))
			{
				if(!builder.compare(HASH_VALUE_END))
				{
					parseExpr(builder);
				}
			}
			else if(builder.compareAndEat(tDSTAR))
			{
				if(!builder.compareAndEat(kNIL))
				{
					VARIABLE.parse(builder);
				}
			}
			else
			{
				marker.drop();
				builder.error(ErrorMsg.expected("key"));
				return;
			}
			marker.done(RubyElementTypes.PATTERN);
		}
		while(builder.compareAndEat(tCOMMA));
	}
}
