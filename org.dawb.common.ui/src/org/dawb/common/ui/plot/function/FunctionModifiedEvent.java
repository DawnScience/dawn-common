package org.dawb.common.ui.plot.function;

import java.util.EventObject;

/**
 * Event object describing a selection change. The source of these
 * events is a selection provider.
 *
 */
public class FunctionModifiedEvent extends EventObject{

	/**
	 * Generated serial version UID for this class.
	 */
	private static final long serialVersionUID = 7844172734140467876L;

	/**
	 * Creates a new event for the given object.
	 *
	 * @param object the selection provider
	 */
	public FunctionModifiedEvent(Object object) {
		super(object);
	}

}
