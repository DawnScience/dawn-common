package org.dawb.common.ui.plot.tool;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;

public class ToolPageFactory {

	/**
	 * Get a tool by id
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public static IToolPage getToolPage(final String id) throws Exception {
		
	    final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.toolPage");
	    for (final IConfigurationElement e : configs) {
	    	if (id.equals(e.getAttribute("id"))) {
	    		return (IToolPage)e.createExecutableExtension("class");
	    	}
	    }
        return null;
	}
}
