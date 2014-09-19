/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.project;

import java.util.Map;

import org.dawb.common.util.xml.XSDChecker;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.xml.sax.SAXException;

/**
 * Builder currently does nothing. In future can check that 
 * workflow contains nodes which are loadable.
 * 
 * @author gerring
 *
 */
public class XMLBuilder extends IncrementalProjectBuilder {

	public static String ID = "org.dawb.common.ui.DataBuilder";	
	public static final String MARKER_ID = "org.dawb.common.ui.XMLValidationMarker"; // Note same as in plugin.xml ID
	
	public XMLBuilder() {
		
	}

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				validateXML(monitor);
			}
		}, monitor);
		return null;
	}

	protected void validateXML(IProgressMonitor monitor) throws CoreException {
		
		if (!deleteMarkers()) return;
		
		try {		    
            final IProject project = getProject();
            final IResource[]   fa = project.members();
		    monitor.beginTask("Validating Scan Configurations", fa.length);
            
		    for (int i = 0; i < fa.length; i++) {
				if (fa[i] instanceof IFile) {
					validateFile((IFile)fa[i]);
					monitor.worked(1);
				}
			}
            
		} finally {
			monitor.done();
		}

	}
	
	
	private void validateFile(final IFile iFile) throws CoreException {
		
		if (iFile.getLocation().getFileExtension()==null) return;
		if (!iFile.getLocation().getFileExtension().toLowerCase().endsWith("xml")) return;
		try {
			XSDChecker.validateFile(iFile.getLocation().toOSString());
		} catch (SAXException ne) {
			createMarker(iFile, ne.getMessage().split(":")[1]);// May be able to do more with SAXException
		} catch (Exception ne) {
			createMarker(iFile, ne.getMessage());
		}
		
	}

	private void createMarker(IFile file, String message) throws CoreException {
		final IMarker marker  = file.createMarker(MARKER_ID);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		
		// Location might be scan name?
		marker.setAttribute(IMarker.LOCATION, file.getFullPath().toString());
	}

	private boolean deleteMarkers() {
		try {
			getProject().deleteMarkers(MARKER_ID, false, IResource.DEPTH_INFINITE);
			return true;
		} catch (Exception ne) {
			return false;
		}
	}

}
