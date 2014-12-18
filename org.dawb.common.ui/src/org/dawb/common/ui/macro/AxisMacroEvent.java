package org.dawb.common.ui.macro;

import org.eclipse.dawnsci.macro.api.MethodEventObject;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;

public class AxisMacroEvent extends MethodEventObject<Object> {


	/**
	 * 
	 */
	private static final long serialVersionUID = 7132061975399993924L;

	public AxisMacroEvent(String varName, Object source, Object... args) {
		super(varName, Thread.currentThread().getStackTrace(), source, args);
	}
	
	
	protected String createPythonCommand(String varName, String methodName, Object source, Object... args) {
		
		StringBuilder buf = new StringBuilder();
				
		// Make method call
		buf.append(varName);
		buf.append(".");
		
		if (source instanceof IAxis) {
			IAxis axis = (IAxis)source;
			String toAdd = null;
        	if (axis.isPrimaryAxis()) {
        		toAdd = axis.isYAxis() ? "getSelectedYAxis()."
        				               : "getSelectedXAxis().";
        		
        	} else if (axis.getTitle()!=null && !"".equals(axis.getTitle())) {
        		toAdd = "getAxis(\""+axis.getTitle()+"\").";
        	}
            if (toAdd!=null) buf.append(toAdd);	
		}

		
		buf.append(methodName);
		buf.append("(");
		buf.append(getPythonArgs(args));
		buf.append(")");
		
		return buf.toString();
	}


}
