package org.dawnsci.macro;

import org.eclipse.dawnsci.macro.api.MacroEventObject;

public interface IMacroGenerator {

	/**
	 * Looks at the class of the source of this event 
	 * and tries to see if there is a standard macro 
	 * @param evt
	 * @return
	 */
	MacroEventObject generate(MacroEventObject evt);
}
