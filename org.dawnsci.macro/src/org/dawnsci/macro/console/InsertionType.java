package org.dawnsci.macro.console;

public enum InsertionType {
	
    PYTHON(0), JYTHON(1), JYTHON_SAMEVM(2);
    
    private int pydevType;

	InsertionType(int pydevType) {
    	this.pydevType = pydevType;
    }
	
	public static InsertionType forPydevCode(int code) {
		for (InsertionType t : values()) {
			if (t.pydevType == code) return t;
		}
		return PYTHON;
	}
}
