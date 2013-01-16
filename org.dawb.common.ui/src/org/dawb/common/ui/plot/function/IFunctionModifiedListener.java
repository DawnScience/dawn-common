package org.dawb.common.ui.plot.function;

import java.util.EventListener;


/**
 * A listener which is notified when a viewer's selection changes.
 *
 */
public interface IFunctionModifiedListener extends EventListener{

	/**
	 * Notifies that the selection has changed.
	 *
	 * @param event FunctionModifiedEvent object describing the change
	 */
	public void functionModified(FunctionModifiedEvent event);
}
