package org.dawb.gda.extensions.spec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpecSyntax {

	public static final Pattern COMMENT = Pattern.compile("\\#.*");
	
	public static final Pattern CMD;
	public static final Pattern PRINT;
	public static final Pattern SCAN_LINE;
	public static final Pattern HEADER_LINE;
	
	private static final String TXT    = "([a-zA-Z][_a-zA-Z\\d]*)";
    private static final String NUM    = "([-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)";
	private static final String TXTWS  = "([ \t]+[a-zA-Z][_a-zA-Z\\d]*)";
	
	private static final String TXTWS2_NOSPC  = "([ \t]*[_a-zA-Z\\d]*[ ]?[a-zA-Z]?[\\d]*)";
	private static final String TXTWS2 = "([ \t]+[_a-zA-Z\\d]*[ ]?[a-zA-Z]?[\\d]*)";
	
    private static final String NUMWS  = "([ \t]+[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?)";
	
	static {
		
		StringBuilder buf = new StringBuilder();
		buf.append(TXT);
		buf.append(TXTWS);
		buf.append(NUMWS);
		for (int i = 0; i < 3; i++) {
			buf.append(NUMWS);
			buf.append("?");
		}
		CMD = Pattern.compile(buf.toString());
		
		buf = new StringBuilder();
		buf.append(NUM);
		buf.append(NUMWS);
		for (int i = 0; i < 50; i++) {
			buf.append(NUMWS);
			buf.append("?");
		}
		SCAN_LINE = Pattern.compile(buf.toString());
		
		buf = new StringBuilder();
		buf.append("#");
		buf.append(TXTWS2_NOSPC);
		for (int i = 0; i < 50; i++) {
			buf.append(TXTWS2);
			buf.append("?");
		}
		HEADER_LINE = Pattern.compile(buf.toString());

		PRINT = Pattern.compile("print ([a-zA-Z0-9\\'\"]+)");
	}
	
	public static void main(String[] args) throws Exception {
		System.out.println(CMD.pattern());
		System.out.println("1.0".matches(NUM));
		
		Matcher test = CMD.matcher("ascan phi 0.1 10 10 .1");
		System.out.println(test.matches());
		
		System.out.println(CMD.matcher("ascan phi 0 10 10 1").matches());
		System.out.println(CMD.matcher("ascan kap1 0 10 10 1").matches());
		
		test = SCAN_LINE.matcher("1.0 0.0 1.0 1.0 0.107 1.0");
		System.out.println(test.matches());
		System.out.println(test.groupCount());
		
		test = SCAN_LINE.matcher("0  0.0000  0 0   0.107  0");
		System.out.println(test.matches());
		System.out.println(test.groupCount());
		
		test = SCAN_LINE.matcher("0    0.0000        0        0      0.107        0");
		System.out.println(test.matches());
		System.out.println(test.groupCount());

		test = HEADER_LINE.matcher("#       Phi Detector  Monitor    Seconds  Flux I0");
		System.out.println(test.matches());
		System.out.println(test.groupCount());

		System.out.println("FINISHED!");

	}
}
