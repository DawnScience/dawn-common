package org.dawb.common.ui.macro;

import org.eclipse.dawnsci.macro.api.MacroUtils;
import org.eclipse.dawnsci.macro.api.MethodEventObject;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;

public class TraceMacroEvent extends MethodEventObject<Object> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2007087602132919423L;
	
	/**
	 * Specific command not generated, only the command to assign the trace.
	 * @param trace
	 */
	public TraceMacroEvent(ITrace trace) {
		super(trace);
		setPythonCommand(getTraceCommand());
	}
	
	/**
	 * Generates the method name from the stack.
	 * @param trace
	 * @param values
	 */
	public TraceMacroEvent(ITrace trace, Object... values) {
	    this(trace, getCallingMethodName(Thread.currentThread().getStackTrace()), values);
	}
	
	/**
	 * Specifies the method name from the stack.
	 * @param trace
	 * @param values
	 */
	public TraceMacroEvent(ITrace trace, String methodName, Object... values) {
		super(getVarName(trace), methodName, trace, values);
		prepend(getTraceCommand(trace));
	}
		
	public static final String getTraceCommand(ITrace trace) {
		return getVarName(trace)+" = ps.getTrace(\""+trace.getName()+"\")";
	}

	public static String getVarName(ITrace trace) {
		return "trace_"+MacroUtils.getLegalName(trace.getName());
	}
	
	public String getVarName() {
		return getVarName((ITrace)getSource());
	}

	public String getTraceCommand() {
		return getTraceCommand((ITrace)getSource());
	}
}
