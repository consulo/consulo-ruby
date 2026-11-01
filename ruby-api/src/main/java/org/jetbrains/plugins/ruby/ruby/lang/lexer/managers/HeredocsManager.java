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

import java.util.LinkedList;
import java.util.Queue;

import jakarta.annotation.Nonnull;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.plugins.ruby.ruby.lang.TextUtil;
import org.jetbrains.plugins.ruby.ruby.lang.lexer.RubyRawLexer;

/**
 * Created by IntelliJ IDEA.
 * User: oleg
 * Date: 21.08.2006
 */
public class HeredocsManager
{
	private ContentManager contentManager;

	private Queue<Heredoc> myHeredocs;

	@NonNls
	private static final String REPLACE_REGEX = "<<|-|~|\\\"|'";
	@NonNls
	private static final String INDENTED_PREFIX = "<<-";
	private static final String SQUIGGLY_PREFIX = "<<~";

	public HeredocsManager(@Nonnull final RubyRawLexer lexer)
	{
		this.contentManager = lexer.getContentManager();
		myHeredocs = new LinkedList<Heredoc>();
	}

	public void reset()
	{
		myHeredocs.clear();
	}

	/**
	 * Registers new heredoc id in heredocs list.
	 *
	 * @param id Heredoc id to register, as string
	 */
	public void registerHeredoc(@Nonnull final String id)
	{
		final boolean isIndented = isIndented(id);
		final String name = getName(id);
		myHeredocs.add(new Heredoc(name, isIndented));
	}

	public static String getName(@Nonnull final String id)
	{
		return id.replaceAll(REPLACE_REGEX, "");
	}

	public static boolean isIndented(@Nonnull final String id)
	{
		return id.startsWith(INDENTED_PREFIX) || id.startsWith(SQUIGGLY_PREFIX);
	}

	/**
	 * @return current heredoc id length
	 */
	public int getIdLength()
	{
		assert (size() > 0);
		return myHeredocs.peek().getId().length();
	}

	/**
	 * @return current heredoc id length
	 */
	public boolean isIndented()
	{
		assert (size() > 0);
		return myHeredocs.peek().isIndented();
	}

	public boolean isEndSeen()
	{
		assert (size() > 0);
		return myHeredocs.peek().isEndSeen();
	}


	/**
	 * @param initialPos Position to check
	 * @return true if heredoc end delimiter can be found at offset pos, false otherwise
	 */
	private boolean checkForHeredocEnd(final int initialPos)
	{
		boolean atEol;
		if((atEol = TextUtil.isEol(contentManager.safeReadAt(initialPos))) || TextUtil.isEol(contentManager.safeReadAt(initialPos - 1)))
		{
			final Heredoc heredoc = myHeredocs.peek();
			final String id = heredoc.getId();
			final boolean isIndented = heredoc.isIndented();
			final int len = id.length();

			// if not indented, id starts now
			boolean idStarted = !isIndented;
			int readLength = 0;
			// if we are at eol, we start reading from next symbol
			int pos = atEol ? initialPos + 1 : initialPos;

			boolean result = true;
			while(result && contentManager.canReadAt(pos) && readLength < len)
			{
				char currentChar = contentManager.safeReadAt(pos);
				if(!idStarted && !TextUtil.isWhiteSpace(currentChar))
				{
					idStarted = true;
				}
				if(idStarted)
				{
					result = id.charAt(readLength) == currentChar;
					readLength++;
				}
				pos++;
			}
			result = result &&
					readLength == len &&
					(!contentManager.canReadAt(pos) || TextUtil.isWhiteSpaceOrEol(contentManager.safeReadAt(pos)));
			if(result)
			{
				heredoc.setEndSeen(true);
			}
			return result;
		}
		return false;
	}

	/**
	 * Eats heredoc content up to the end delimiter or expression substitution
	 *
	 * @return length
	 */
	public int eatHereDocContent()
	{
		int pos = 0;
		while(true)
		{
			if(!contentManager.canReadAt(pos) || checkForHeredocEnd(pos))
			{
				return pos > 0 ? pos : ContentManager.END_SEEN;
			}
			if(contentManager.checkForExprSubtitution(pos))
			{
				return pos > 0 ? pos : ContentManager.EXPR_SUBT_SEEN;
			}
			pos++;
		}
	}

	public Heredoc poll()
	{
		assert (size() > 0);
		return myHeredocs.poll();
	}

	public int size()
	{
		return myHeredocs.size();
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Heredoc
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public class Heredoc
	{

		// Heredoc ID string
		private String id;
		// If heredoc indented or not
		private boolean isIndented;

		private boolean endSeen = false;

		public Heredoc(String id, boolean isIndented)
		{
			this.id = id;
			this.isIndented = isIndented;
			endSeen = false;
		}

		public String getId()
		{
			return id;
		}

		public boolean isIndented()
		{
			return isIndented;
		}

		public boolean isEndSeen()
		{
			return endSeen;
		}

		public void setEndSeen(boolean endSeen)
		{
			this.endSeen = endSeen;
		}
	}

}
