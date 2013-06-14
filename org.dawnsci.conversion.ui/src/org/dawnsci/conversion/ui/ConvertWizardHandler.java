/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.conversion.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.util.io.FileUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 *   ConvertWizard shows a wizard for converting synchrotron data
 *   to more common file types.
 *
 *   @author gerring
 *   @date Aug 31, 2010
 *   @project org.edna.workbench.actions
 **/
public class ConvertWizardHandler extends AbstractHandler implements IObjectActionDelegate {


	private IWorkbenchPart targetPart;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
        openWizard(HandlerUtil.getActiveShell(event));
		return Boolean.FALSE;
	}

	private void openWizard(final Shell shell) {
		WizardDialog dialog = new WizardDialog(shell, new ConvertWizard());
        dialog.setPageSize(new Point(400, 450));
        dialog.create();
        dialog.open();
	}
	
	@Override
	public void run(IAction action) {
	    openWizard(targetPart.getSite().getShell());
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		
	}
	
	public boolean isEnabled() {
		final ISelection selection = EclipseUtils.getActivePage().getSelection();
		if (selection instanceof StructuredSelection) {
			StructuredSelection s = (StructuredSelection)selection;
			final Object        o = s.getFirstElement();
			
			// Currently can only parse nexus files with conversion
			// tool
			final String path = FileUtils.getPath(o);
			if (path!=null && isH5(path)) return true;
		}
        return false;
	}
	
	public final static List<String> EXT;
	static {
		List<String> tmp = new ArrayList<String>(7);
		tmp.add("h5");
		tmp.add("nxs");
		tmp.add("hd5");
		tmp.add("hdf5");
		tmp.add("hdf");
		tmp.add("nexus");
		EXT = Collections.unmodifiableList(tmp);
	}	

	public static boolean isH5(final String filePath) {
		if (filePath==null) return false;
		final String ext = FileUtils.getFileExtension(filePath);
		if (ext==null) return false;
		return EXT.contains(ext.toLowerCase());
	}

	
	public boolean isHandled() {
		return  isEnabled();
	}
	
	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}
}
