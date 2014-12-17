package org.dawnsci.macro;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.dawnsci.macro";
	
	public Activator() {
		// TODO Auto-generated constructor stub
	}

  	public static ImageDescriptor getImage(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
 }
