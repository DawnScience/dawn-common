/**
 * Sync 2.1
 * Copyright 2007 Zach Scrivena
 * 2007-12-09
 * zachscrivena@gmail.com
 * http://syncdir.sourceforge.net/
 *
 * Sync performs one-way directory or file synchronization.
 *
 * TERMS AND CONDITIONS:
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dawb.common.util.sync;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * Represent a node in the filter tree.
 */
class FilterNode
{
	/** this node's children, if any */
	private final List<FilterNode> children;

	/** enum type: logic applied to this node's children (AND, NAND, OR, NOR) */
	static enum LogicType
	{
		AND,
		NAND,
		OR,
		NOR;
	}

	/** logic applied to this node's children (AND, NAND, OR, NOR) */
	private final FilterNode.LogicType logic;

	/** enum type: type of filter pattern for this leaf node (REGEX, GLOB) */
	static enum FilterType
	{
		REGEX,
		GLOB;
	}

	/** compiled Java regex Pattern for this leaf node */
	private final Pattern pattern;

	/** true if the match result for this leaf node is inverted; false otherwise */
	private final boolean inverted;

	/** string description of the filter represented by this leaf node */
	private final String stringValue;


	/**
	 * Constructor for a non-leaf node with children.
	 *
	 * @param logic
	 *     Logic applied to this node's children (AND, NAND, OR, NOR)
	 */
	FilterNode(
			final FilterNode.LogicType logic)
	{
		this.logic = logic;
		this.children = new ArrayList<FilterNode>();
		this.pattern = null;
		this.inverted = false;
		this.stringValue = null;
	}


	/**
	 * Constructor for a leaf node.
	 *
	 * @param type
	 *     Type of filter pattern for this leaf node (REGEX, GLOB)
	 * @param inverted
	 *     true if the match result for this leaf node is inverted; false otherwise
	 * @param expression
	 *     Regex/glob expression to be compiled to an equivalent Java regex pattern
	 */
	FilterNode(
			final FilterType type,
			final boolean inverted,
			final String expression)
			throws PatternSyntaxException
	{
		this.logic = null;
		this.children = null;
		this.inverted = inverted;

		String s = null;

		/* compile specified expression to an equivalent regex pattern */
		Pattern p = null;

		switch (type)
		{
			case REGEX:
				p = Pattern.compile(expression);
				s = inverted ? "regexnot" : "regex";
				break;

			case GLOB:
				p = globToRegexPattern(expression);
				s = inverted ? "globnot" : "glob";
				break;
		}

		this.pattern = p;
		this.stringValue = s + "(\"" + expression + "\")";
	}


	/**
	 * Add a child node to this non-leaf node.
	 *
	 * @param child
	 *     Child node to be added
	 */
	public void addFilter(
			final FilterNode child)
	{
		this.children.add(child);
	}


	/**
	 * Return true if the specified string matches this filter node.
	 * If this node is a leaf, then the string must match the specified pattern;
	 * if this node has children, then the string must satisfy the specified logic.
	 *
	 * @param s
	 *     String to be matched against this filter
	 * @return
	 *     true if the specified string matches this filter; false otherwise
	 */
	public boolean matches(
			final String s)
	{
		if (this.children == null)
		{
			/* this is a leaf node; match the specified pattern */
			return (this.pattern.matcher(s).matches() != this.inverted);
		}
		else
		{
			/* this node has children; check if specified logic is satisfied */
			if (this.children.isEmpty())
				throw new RuntimeException("FilterNode object is a non-leaf node without any children");

			switch (this.logic)
			{
				case AND:
					/* return true unless a child is false */
					for (FilterNode n : this.children)
					{
						if (!n.matches(s))
							return false;
					}

					return true;

				case NAND:
					/* return false unless a child is false */
					for (FilterNode n : this.children)
					{
						if (!n.matches(s))
							return true;
					}

					return false;

				case OR:
					/* return false unless a child is true */
					for (FilterNode n : this.children)
					{
						if (n.matches(s))
							return true;
					}

					return false;

				case NOR:
					/* return true unless a child is true */
					for (FilterNode n : this.children)
					{
						if (n.matches(s))
							return false;
					}

					return true;
			}
		}

		return false; // should never be reached
	}


	/**
	 * Return the compiled Java regex Pattern equivalent to the specified GLOB expression.
	 *
	 * @param glob
	 *     GLOB expression to be compiled
	 * @return
	 *     Equivalent compiled Java regex Pattern
	 */
	private static Pattern globToRegexPattern(
			final String glob)
			throws PatternSyntaxException
	{
		/* Stack to keep track of the parser mode: */
		/* "--" : Base mode (first on the stack)   */
		/* "[]" : Square brackets mode "[...]"     */
		/* "{}" : Curly braces mode "{...}"        */
		final Deque<String> parserMode = new ArrayDeque<String>();
		parserMode.push("--"); // base mode

		final int globLength = glob.length();
		int index = 0; // index in glob

		/* equivalent REGEX expression to be compiled */
		final StringBuilder t = new StringBuilder();

		while (index < globLength)
		{
			char c = glob.charAt(index++);

			if (c == '\\')
			{
				/***********************
				 * (1) ESCAPE SEQUENCE *
				 ***********************/

				if (index == globLength)
				{
					/* no characters left, so treat '\' as literal char */
					t.append(Pattern.quote("\\"));
				}
				else
				{
					/* read next character */
					c = glob.charAt(index);
					final String s = c + "";

					if (("--".equals(parserMode.peek()) && "\\[]{}?*".contains(s)) ||
							("[]".equals(parserMode.peek()) && "\\[]{}?*!-".contains(s)) ||
							("{}".equals(parserMode.peek()) && "\\[]{}?*,".contains(s)))
					{
						/* escape the construct char */
						index++;
						t.append(Pattern.quote(s));
					}
					else
					{
						/* treat '\' as literal char */
						t.append(Pattern.quote("\\"));
					}
				}
			}
			else if (c == '*')
			{
				/************************
				 * (2) GLOB PATTERN '*' *
				 ************************/

				/* create non-capturing group to match zero or more characters */
				t.append(".*");
			}
			else if (c == '?')
			{
				/************************
				 * (3) GLOB PATTERN '?' *
				 ************************/

				/* create non-capturing group to match exactly one character */
				t.append('.');
			}
			else if (c == '[')
			{
				/****************************
				 * (4) GLOB PATTERN "[...]" *
				 ****************************/

				/* opening square bracket '[' */
				/* create non-capturing group to match exactly one character */
				/* inside the sequence */
				t.append('[');
				parserMode.push("[]");

				/* check for negation character '!' immediately after */
				/* the opening bracket '[' */
				if ((index < globLength) &&
						(glob.charAt(index) == '!'))
				{
					index++;
					t.append('^');
				}
			}
			else if ((c == ']') && "[]".equals(parserMode.peek()))
			{
				/* closing square bracket ']' */
				t.append(']');
				parserMode.pop();
			}
			else if ((c == '-') && "[]".equals(parserMode.peek()))
			{
				/* character range '-' in "[...]" */
				t.append('-');
			}
			else if (c == '{')
			{
				/****************************
				 * (5) GLOB PATTERN "{...}" *
				 ****************************/

				/* opening curly brace '{' */
				/* create non-capturing group to match one of the */
				/* strings inside the sequence */
				t.append("(?:(?:");
				parserMode.push("{}");
			}
			else if ((c == '}') && "{}".equals(parserMode.peek()))
			{
				/* closing curly brace '}' */
				t.append("))");
				parserMode.pop();
			}
			else if ((c == ',') && "{}".equals(parserMode.peek()))
			{
				/* comma between strings in "{...}" */
				t.append(")|(?:");
			}
			else
			{
				/*************************
				 * (6) LITERAL CHARACTER *
				 *************************/

				/* convert literal character to a regex string */
				t.append(Pattern.quote(c + ""));
			}
		}
		/* done parsing all chars of the source pattern string */

		/* check for mismatched [...] or {...} */
		if ("[]".equals(parserMode.peek()))
			throw new PatternSyntaxException("Cannot find matching closing square bracket ] in GLOB expression", glob, -1);

		if ("{}".equals(parserMode.peek()))
			throw new PatternSyntaxException("Cannot find matching closing curly brace } in GLOB expression", glob, -1);

		return Pattern.compile(t.toString());
	}


	/**
	 * Return a string description of the filter represented by this subtree.
	 */
	@Override
	public String toString()
	{
		final StringBuilder s = new StringBuilder();

		if (this.children == null)
		{
			/* this is a leaf node */
			s.append(this.stringValue);
		}
		else
		{
			/* this node has children */
			if (this.children.isEmpty())
				throw new RuntimeException("FilterNode object is a non-leaf node without any children");

			String delimiter = null;

			switch (this.logic)
			{
				case AND:
					delimiter = " AND ";
					break;

				case NAND:
					s.append("NOT ");
					delimiter = " AND ";
					break;

				case OR:
					delimiter = " OR ";
					break;

				case NOR:
					s.append("NOT ");
					delimiter = " OR ";
					break;
			}

			final int n = this.children.size();

			if (n > 1)
				s.append("{");

			for (int i = 0; i < n - 1; i++)
			{
				s.append(this.children.get(i).toString());
				s.append(delimiter);
			}

			s.append(this.children.get(n - 1).toString());

			if (n > 1)
				s.append("}");
		}

		return s.toString();
	}
}
