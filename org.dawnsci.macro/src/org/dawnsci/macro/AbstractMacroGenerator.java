package org.dawnsci.macro;

import java.util.Arrays;

import org.eclipse.dawnsci.macro.api.MacroEventObject;

public abstract class AbstractMacroGenerator {

	/**
	 * Looks at the class of the source of this event 
	 * and tries to see if there is a standard macro 
	 * @param evt
	 * @return
	 */
	public abstract MacroEventObject generate(MacroEventObject evt);
	
	
	/**
	 * Deals with primitive arrays
	 * @param value
	 */
	public static String toPythonString(Object value) {
		
		if (value==null) return null;
		
        if (value instanceof String) {
        	return "'"+(String)value+"'";
        	
        } else if (value instanceof Boolean) {
        	return ((Boolean)value).booleanValue() ? "True" : "False";
        	
        } else if (value instanceof short[]) {
        	return Arrays.toString((short[])value);
        	
        } else if  (value instanceof int[]) {
        	return Arrays.toString((int[])value);
        	
        } else if  (value instanceof long[]) {
        	return Arrays.toString((long[])value);
        	
        } else if  (value instanceof char[]) {
        	return Arrays.toString((char[])value);
        	
        } else if  (value instanceof float[]) {
        	return Arrays.toString((float[])value);
        	
        } else if  (value instanceof double[]) {
        	return Arrays.toString((double[])value);
        	
        } else if  (value instanceof boolean[]) {
        	return Arrays.toString((boolean[])value);
        	
        } else if  (value instanceof byte[]) {
        	return Arrays.toString((byte[])value);
        	
        } else if  (value instanceof Object[]) {
        	return Arrays.toString((Object[])value);
        }
        
        return value.toString();
	}

}
