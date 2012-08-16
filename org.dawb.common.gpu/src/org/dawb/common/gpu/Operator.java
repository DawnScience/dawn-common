package org.dawb.common.gpu;


public enum Operator {

	ADD("     +     "),
	SUBTRACT("     -     "),
	MULTIPLY("     x     "),
	DIVIDE("     ÷     ");
	
	private String name;

	Operator(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public int getIndex() {
		final Operator[] ops = Operator.values();
		for (int i = 0; i < ops.length; i++) if (ops[i]==this) return i;
		return -1;
	}

	public static String[] getOperators() {
		final Operator[] ops = Operator.values();
		final String[] names = new String[ops.length];
		for (int i = 0; i < ops.length; i++) {
			names[i] = ops[i].getName();
		}
		return names;
	}

	public static Operator getOperator(int index) {
		final Operator[] ops = Operator.values();
		return ops[index];
	}
	
}