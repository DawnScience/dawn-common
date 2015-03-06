package org.dawnsci.macro;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.dawnsci.macro";
	private static Activator plugin;
	
	public Activator() {
		// TODO Auto-generated constructor stub
	}
	
    public void start(BundleContext context) throws Exception {
    	 super.start(context);
    	 plugin = this;
    }

    public void stop(BundleContext context) throws Exception {
    	super.stop(context);
    	plugin = this;
    }

  	public static ImageDescriptor getImage(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
  	
  	public static Activator getPlugin() {
  		return plugin;
  	}
 }
