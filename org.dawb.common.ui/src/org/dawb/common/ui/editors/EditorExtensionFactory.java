package org.dawb.common.ui.editors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.util.io.FileUtils;
import org.dawb.common.util.io.PropUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;

public class EditorExtensionFactory {

	/**
	 * Read editors from extension point. These can then be added to a multipage editor.
	 * @param input
	 * @return list of editors - may be empty or null
	 */
	public static final Collection<IEditorPart> getEditors(final IEditorPart part) throws Exception {
		
	    final String path = EclipseUtils.getFilePath(part.getEditorInput());
	    final String ext  = FileUtils.getFileExtension(path);

	    final IPerspectiveDescriptor des = part.getSite().getPage().getPerspective();
		final String perspectiveId;
		final Properties props = PropUtils.loadProperties(getPropertiesPath());
		if (des==null) {
			// We get the id the last time we opened this editor.
			perspectiveId = props.getProperty(path);
		} else {
			perspectiveId = des.getId();
			props.put(path, perspectiveId);
			PropUtils.storeProperties(props, getPropertiesPath());
		}
		
	    final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.editorExtension");
	    if (configs==null || configs.length<1) return null;
	    
	    
	    final Collection<IEditorPart> editors = new ArrayList<IEditorPart>(3);
	    for (IConfigurationElement e : configs) {
			final IEditorExtension extension = (IEditorExtension)e.createExecutableExtension("class");
			if (!extension.isApplicable(path, ext, perspectiveId)) continue;
			editors.add(extension);
		}

	    return editors;
	}
	
	
	private static final String getPropertiesPath() {
		return ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString()+"/.metadata/.dawn/perspective.properties";
	}
}
