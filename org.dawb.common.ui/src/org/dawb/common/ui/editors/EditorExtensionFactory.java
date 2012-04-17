package org.dawb.common.ui.editors;

import java.util.ArrayList;
import java.util.Collection;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.util.io.FileUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public class EditorExtensionFactory {

	/**
	 * Read editors from extension point. These can then be added to a multipage editor.
	 * @param input
	 * @return list of editors - may be empty or null
	 */
	public static final Collection<IEditorPart> getEditors(final IEditorInput input) throws Exception {
		
	    final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.editorExtension");
	    if (configs==null || configs.length<1) return null;
	    
	    final String path = EclipseUtils.getFilePath(input);
	    final String ext  = FileUtils.getFileExtension(path);
	    
	    final Collection<IEditorPart> editors = new ArrayList<IEditorPart>(3);
	    for (IConfigurationElement e : configs) {
			final IEditorExtension extension = (IEditorExtension)e.createExecutableExtension("class");
			if (!extension.isApplicable(path, ext)) continue;
			editors.add(extension);
		}

	    return editors;
	}
}
