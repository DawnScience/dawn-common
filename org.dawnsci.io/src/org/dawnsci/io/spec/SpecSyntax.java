/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.io.spec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecSyntax {

	/**
	 * Comment lines start with &#35;
	 */
	public static final Pattern COMMENT_LINE = Pattern.compile("#.*");

	private static final String LABEL          = "[a-zA-Z]\\w*(?: ?\\w+)*"; // can include single spaces
	private static final String LABEL_GROUP    = "(" + LABEL + ")";
	private static final String WS_LABEL_GROUP = "(  +" + LABEL + ")"; // labels are separated by two (or more) spaces

	/**
	 * A label comprises multiple words separated by a single space where each word must begin with a letter (not digit)
	 */
	public static final Pattern LABEL_VALUE = Pattern.compile(LABEL_GROUP);
	/**
	 * A label comment line starts with "#L" followed by spaces then labels separated by two spaces
	 */
	public static final Pattern LABEL_LINE = Pattern.compile("#L[ ]+" + LABEL_GROUP + WS_LABEL_GROUP + "*");

	private static final String NUMBER          = "[-+]?\\d+(?:\\.\\d*)?(?:[eE][-+]?\\d+)?";
	private static final String NUMBER_GROUP    = "(" + NUMBER + ")";
	private static final String WS_NUMBER_GROUP = "([ \t]+" + NUMBER + ")";

	/**
	 * A scan value is an integer or floating point number
	 */
	public static final Pattern SCAN_VALUE = Pattern.compile(NUMBER_GROUP);

	/**
	 * A scan line comprises scan values separated by white space characters (space or tabs)
	 */
	public static final Pattern SCAN_LINE = Pattern.compile(NUMBER_GROUP + WS_NUMBER_GROUP + "*");


	public static void main(String[] args) throws Exception {
		check(COMMENT_LINE,
				"#blahblah",
				"234#"
				);

		check(SCAN_LINE,
				"1.0 0.0 1.0 1.0 0.107 1.0",
				"0  0.0000  0 0   0.107  7",
				"0    0.0000        0        0      0.107        -3"
				);

		check(LABEL_LINE,
				"#L abc",
				"#L  abc",
				"#L  abc efg",
				"#L  abc  efg",
				"#L  abc  efg hij",
				"#L  abc  efg  hij",
				"#L  abc  efg  hij klm",
				"#L  abc  efg  hij klm nop",
				"#L       Phi Detector  Monitor    Seconds  Flux I0"
				);

		System.out.println("FINISHED!");
	}

	private static void check(Pattern p, String... strings) {
		for (String s : strings) {
			Matcher m = p.matcher(s);
			if (m.matches()) {
				int imax = m.groupCount();
				System.out.println("Groups: " + imax + " for |" + s + "|");
				for (int i = 1; i <= imax; i++) {
					System.out.println(i + ": " + m.group(i));
				}
			} else {
				System.out.println("No match for " + s);
			}
		}
	}
}
