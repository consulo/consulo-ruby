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

package org.jetbrains.plugins.ruby.ruby.lang.lexer.managers;

import consulo.language.ast.IElementType;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyRawLexer;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyTokenTypes;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.managers.state.Expr;
import org.jetbrains.plugins.ruby.ruby.lang.parser.bnf.BNF;
import jakarta.annotation.Nonnull;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author: oleg
 * @date: 10.09.2006
 */
public class TokensManager implements RubyTokenTypes
{
	private static final Map<String, IElementType> PSEUDO_KEYWORDS = Map.of(
			"true", kTRUE,
			"false", kFALSE,
			"nil", kNIL,
			"self", kSELF,
			"super", kSUPER,
			"__FILE__", kFILE,
			"__LINE__", kLINE);

	private static final Map<String, IElementType> KEYWORDS = Map.ofEntries(
			Map.entry("alias", kALIAS), Map.entry("and", kAND), Map.entry("begin", kBEGIN), Map.entry("break", kBREAK),
			Map.entry("case", kCASE), Map.entry("class", kCLASS), Map.entry("def", kDEF), Map.entry("else", kELSE),
			Map.entry("elsif", kELSIF), Map.entry("end", kEND), Map.entry("ensure", kENSURE), Map.entry("for", kFOR),
			Map.entry("in", kIN), Map.entry("module", kMODULE), Map.entry("next", kNEXT), Map.entry("not", kNOT),
			Map.entry("or", kOR), Map.entry("redo", kREDO), Map.entry("retry", kRETRY), Map.entry("return", kRETURN),
			Map.entry("then", kTHEN), Map.entry("undef", kUNDEF), Map.entry("when", kWHEN), Map.entry("yield", kYIELD),
			Map.entry("do", kDO), Map.entry("if", kIF), Map.entry("unless", kUNLESS), Map.entry("while", kWHILE),
			Map.entry("until", kUNTIL), Map.entry("rescue", kRESCUE));

	private static final Map<IElementType, IElementType> MODIFIERS = Map.of(
			kIF, kIF_MOD,
			kUNLESS, kUNLESS_MOD,
			kWHILE, kWHILE_MOD,
			kUNTIL, kUNTIL_MOD,
			kRESCUE, kRESCUE_MOD);

	final private StatesManager myStatesManager;
	private boolean spaceSeen;
	private IElementType myLastSignificantToken;

	public TokensManager(final RubyRawLexer lexer)
	{
		myStatesManager = lexer.getStatesManager();
	}

	public void reset()
	{
		spaceSeen = false;
		myLastSignificantToken = null;
	}

	/**
	 * @return true if {@code name:} is a label (hash key, keyword argument or parameter) and not
	 * {@code name} followed by the colon of a ternary operator
	 */
	public boolean isLabelAllowed()
	{
		if(isFnameAllowed() || myLastSignificantToken == tQUESTION)
		{
			return false;
		}
		final Expr expr = myStatesManager.getExpr();
		return expr == Expr.BEG || expr == Expr.MID || expr == Expr.ARG || expr == Expr.CMD_ARG || expr == Expr.CMD_BRACE;
	}

	/**
	 * @param text identifier, constant or fid that is not a label
	 * @return its token type, keywords included
	 */
	public IElementType getWordType(@Nonnull final CharSequence text)
	{
		final String word = text.toString();
		final char last = word.charAt(word.length() - 1);
		if(last == '?' || last == '!')
		{
			return tFID;
		}
		final IElementType pseudoKeyword = PSEUDO_KEYWORDS.get(word);
		if(pseudoKeyword != null)
		{
			return pseudoKeyword;
		}
		final IElementType keyword = KEYWORDS.get(word);
		if(keyword != null && reswordAllowed())
		{
			final IElementType modifier = MODIFIERS.get(keyword);
			return modifier != null && isExprEnd() ? modifier : keyword;
		}
		return Character.isUpperCase(word.charAt(0)) ? tCONSTANT : tIDENTIFIER;
	}

	/**
	 * @return true if resword is allowed now
	 */
	/**
	 * @return true right after <code>.</code>, <code>&.</code> or <code>::</code>: <code>value.false</code> calls a method
	 */
	public boolean isMethodNameAfterDot()
	{
		return myLastSignificantToken != null && BNF.tDOT_OR_COLON.contains(myLastSignificantToken);
	}

	public boolean reswordAllowed()
	{
		return !isFnameAllowed();
	}

	/**
	 * @return true if "Identifier=" allowed
	 */
	public boolean isAssignOpAllowed()
	{
		return myStatesManager.getExpr() == Expr.FNAME;
	}

	/**
	 * @return true if fname allowed, i.e. tAREF, tASET, tUPLUS_OP, tUMINUS_OP
	 */
	public boolean isFnameAllowed()
	{
		final Expr expr = myStatesManager.getExpr();
		return expr == Expr.FNAME || expr == Expr.DOT_OR_COLON;
	}

	public boolean isColon2Allowed()
	{
		final Expr expr = myStatesManager.getExpr();
		return expr == Expr.END ||
				expr == Expr.CMD_ARG ||
				expr == Expr.ARG && !spaceSeen;
	}

	/**
	 * @return true if stringlike beginnig with % allowed
	 */
	public boolean stringAllowed()
	{
		return stringAllowed(false);
	}

	/**
	 * @param followCharIsWhiteSpaceOrEol Following character is whitespace or Eol
	 * @return true if string or regexp, beginning with /, % allowed
	 */
	public boolean stringAllowed(final boolean followCharIsWhiteSpaceOrEol)
	{
		final Expr expr = myStatesManager.getExpr();
		return !isFnameAllowed() && (expr == Expr.BEG || expr == Expr.CMD_BRACE || expr == Expr.MID ||
				expr == Expr.ARG && spaceSeen && !followCharIsWhiteSpaceOrEol);
	}

	/**
	 * @param followCharIsWhiteSpaceOrEol Following character is whitespace or Eol
	 * @return true if unary operation is allowed
	 */
	public boolean unaryAllowed(final boolean followCharIsWhiteSpaceOrEol)
	{
		final Expr expr = myStatesManager.getExpr();
		return !isFnameAllowed() && (expr == Expr.BEG || expr == Expr.CMD_BRACE || expr == Expr.MID || expr == Expr.CMD_ARG ||
				expr == Expr.ARG && spaceSeen && !followCharIsWhiteSpaceOrEol);
	}

	public boolean isHeredocAllowed()
	{
		return myStatesManager.getExpr() != Expr.CLASS && unaryAllowed(false);
	}

	/**
	 * Process the token seen and returns is
	 *
	 * @param type DuckType of token seen
	 * @return type
	 */
	public IElementType process(IElementType type)
	{
		spaceSeen = (type == tWHITE_SPACE);
		if(!BNF.tWHITESPACES_OR_COMMENTS.contains(type))
		{
			myLastSignificantToken = type;
		}
		myStatesManager.process(type);
		return type;
	}

	public boolean ignoreEOL()
	{
		final Expr expr = myStatesManager.getExpr();
		return !myStatesManager.isAfterHeredoc() && (expr == Expr.BEG || expr == Expr.MID || expr == Expr.CMD_BRACE && myStatesManager.ignoreEol ||
				expr == Expr.DOT_OR_COLON || expr == Expr.CLASS || expr == Expr.FNAME);
	}

	/**
	 * @return true if fTokens allowed, i.e. func(, func{, func[, tCOLON2
	 */
	public boolean isFTokenAllowed()
	{
		return !spaceSeen && isExprEnd();
	}

	/**
	 * @return true if Arg token allowed, i.e. tLPAREN_ARG
	 */
	public boolean isArgTokenAllowed()
	{
		return spaceSeen && myStatesManager.getExpr() == Expr.ARG;
	}

	public boolean isExprEnd()
	{
		final Expr expr = myStatesManager.getExpr();
		return expr == Expr.END || expr == Expr.ARG;
	}
}
