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

import jakarta.annotation.Nonnull;

import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyRawLexer;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 08.09.2006
 */

/**
 * Class used for advanced reading operations from lexers zzBuffer.
 * One instance per each _RubyLexer instance.
 */
public class ContentManager extends ReadingManager
{
	public static final int END_SEEN = 0;
	public static final int SIMPLE_ESCAPE_SEEN = -1;
	public static final int BACKSLASH_SEEN = -2;
	public static final int EXPR_SUBT_SEEN = -3;

	private StatesManager stateManager;

	public ContentManager(@Nonnull final RubyRawLexer lexer)
	{
		super(lexer);
		stateManager = lexer.getStatesManager();
	}

	@Override
	public void reset(final int zzStart, final int zzEnd)
	{
		super.reset(zzStart, zzEnd);
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////// ReadAccess string like content ////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * checks for expression subtitution at the offset pos
	 *
	 * @param pos position to check
	 * @return true if expression subtitution begin found at offset
	 */
	public boolean checkForExprSubtitution(final int pos)
	{
		if(safeReadAt(pos - 1) == '\\')
		{
			return false;
		}
		String s = safeReadStringAt(pos, 2);
		return (s.equals("#{") || s.equals("#@") || s.equals("#$"));
	}

	/**
	 * Checks for end delimiter at the offset pos
	 *
	 * @param pos        position to check
	 * @param endDelim   expected end delimiter
	 * @param beginDelim begin delimiter
	 * @return true if endDelimiter can be find
	 */
	private boolean checkForEndDelimiter(final int pos, final char beginDelim, final char endDelim)
	{
		char c = safeReadAt(pos);
		if(c == endDelim)
		{
			stateManager.processCloseBrace();
			return stateManager.wasEndBraceSeen();
		}
		if(c == beginDelim && TextUtil.isBraceLikeDelim(beginDelim))
		{
			stateManager.processOpenBrace();
		}
		return false;
	}

	/**
	 * Checks for simple escape sequence, i.e. Backslash and end delimiter,
	 * backslash and begin delimiter (if begin delimiter is brace like) or two backslashes at the offset pos
	 *
	 * @param pos        position to check
	 * @param endDelim   end Delimiter
	 * @param beginDelim beginDelimiter
	 * @return true if simple esc was found
	 */
	private boolean checkForSimpleEsc(final int pos, final char beginDelim, final char endDelim)
	{
		char c0 = safeReadAt(pos);
		char c1 = safeReadAt(pos + 1);
		// \\ or \( or \)
		return c0 == '\\' && (c1 == '\\' ||
				c1 == endDelim || (c1 == beginDelim && TextUtil.isBraceLikeDelim(beginDelim)));
	}

	/**
	 * Reads string like content. No expression subtitutions, no escape sequences
	 *
	 * @param beginDelim begin delimiter
	 * @param endDelim   Current state end delimiter
	 * @return length, if>0, or END_SEEN or SIMPLE_ESCAPE_SEEN otherwise
	 */
	public int eatNoExprNoEsc(final char beginDelim, final char endDelim)
	{
		int pos = 0;
		while(true)
		{
			// end seen
			if(!canReadAt(pos) || checkForEndDelimiter(pos, beginDelim, endDelim))
			{
				return pos > 0 ? pos : END_SEEN;
			}
			// simple escape sequence
			if(checkForSimpleEsc(pos, beginDelim, endDelim))
			{
				return pos > 0 ? pos : SIMPLE_ESCAPE_SEEN;
			}
			pos++;
		}
	}

	/**
	 * Reads string like content. Expression subtitutions permitted, no escape sequences
	 *
	 * @param beginDelim begin delimiter
	 * @param endDelim   Current state end delimiter
	 * @return length, if>0, or END_SEEN or SIMPLE_ESCAPE_SEEN or EXPR_SUBT_SEEN otherwise
	 */
	public int eatExprNoEsc(final char beginDelim, final char endDelim)
	{
		int pos = 0;
		while(true)
		{
			// end seen
			if(!canReadAt(pos) || checkForEndDelimiter(pos, beginDelim, endDelim))
			{
				return pos > 0 ? pos : END_SEEN;
			}
			// simple escape sequence
			if(checkForSimpleEsc(pos, beginDelim, endDelim))
			{
				return pos > 0 ? pos : SIMPLE_ESCAPE_SEEN;
			}
			// expr subtitution
			if(checkForExprSubtitution(pos))
			{
				return pos > 0 ? pos : EXPR_SUBT_SEEN;
			}
			pos++;
		}
	}

	/**
	 * Reads string like content. Expression subtitutions permitted, escape sequences permitted
	 *
	 * @param beginDelim begin delimiter
	 * @param endDelim   Current state end delimiter
	 * @return length, if>0, or END_SEEN or SIMPLE_ESCAPE_SEEN or BACKSLASH_SEEN or EXPR_SUBT_SEEN otherwise
	 */
	public int eatExprEsc(final char beginDelim, final char endDelim)
	{
		int pos = 0;
		while(true)
		{
			// end seen
			if(!canReadAt(pos) || checkForEndDelimiter(pos, beginDelim, endDelim))
			{
				return pos > 0 ? pos : END_SEEN;
			}
			// simple escape sequence
			if(checkForSimpleEsc(pos, beginDelim, endDelim))
			{
				return pos > 0 ? pos : SIMPLE_ESCAPE_SEEN;
			}
			// backslash
			if(safeReadAt(pos) == '\\')
			{
				return pos > 0 ? pos : BACKSLASH_SEEN;
			}
			// expr subtitution
			if(checkForExprSubtitution(pos))
			{
				return pos > 0 ? pos : EXPR_SUBT_SEEN;
			}
			pos++;
		}
	}

	/**
	 * @param offset offset after the end of line
	 * @return true if the next line goes on with a leading {@code .call} or {@code &.call}
	 */
	public boolean isLeadingDotAhead(final int offset)
	{
		int i = offset;
		while(canReadAt(i) && TextUtil.isWhiteSpaceOrEol(safeReadAt(i)))
		{
			i++;
		}
		if(!canReadAt(i + 1))
		{
			return false;
		}
		final char c = safeReadAt(i);
		final char next = safeReadAt(i + 1);
		return c == '.' && next != '.' || c == '&' && next == '.';
	}
}
