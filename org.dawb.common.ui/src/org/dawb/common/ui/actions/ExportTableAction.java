/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.actions;

import java.io.ByteArrayInputStream;

import org.dawb.common.ui.Activator;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exports any table (passed in) to CSV
 * @author fcp94556
 *
 */
public class ExportTableAction extends Action {
	
	private static Logger logger = LoggerFactory.getLogger(ExportTableAction.class);

	private TableViewer viewer;
	private int         columnCount;
	
	public ExportTableAction(TableViewer viewer, int columnCount) {
		super("Export to CSV", Activator.getImageDescriptor("icons/export_wiz.gif"));
		this.viewer      = viewer;
		this.columnCount = columnCount;
	}
	
	public void run() {
		
		try {
			IFile file = getFile();
			if (file==null) return;
			
			final StringBuilder contents = getTableContents();
			file.create(new ByteArrayInputStream(contents.toString().getBytes("UTF-8")), true, new NullProgressMonitor());
		} catch (Exception ne) {
			logger.error("Cannot export the file!", ne);
		}
	}

	private StringBuilder getTableContents() {
		
		final StringBuilder buf = new StringBuilder();
        final Table table = viewer.getTable();
        
        for (int irow = 0; irow<table.getItemCount(); ++irow) {
        	final TableItem item = table.getItem(irow);
        	for (int icol = 0; icol < columnCount; icol++) {
				String txt = item.getText(icol);
				if (txt==null) txt = "";
				buf.append(txt);
				if (icol<columnCount-1) buf.append(",");
			}
        	buf.append("\n");
        }
        return buf;
	}

	private IFile getFile() throws Exception {
		
		IFile file = WorkspaceResourceDialog.openNewFile(viewer.getTable().getShell(), "Choose CSV", "Please choose a CSV to export the table", null, null);
		if (file!=null) {
			if (!file.getName().toLowerCase().endsWith(".csv")) {
				file = file.getParent().getFile(new Path(file.getName()+".csv"));
			}
			
			if (file.exists()) {
				boolean ok = MessageDialog.openQuestion(viewer.getTable().getShell(), "Confirm Overwrite", "The file '"+file.getName()+"' exists. Would you like to overwrite:\n\n"+file.getLocation().toOSString()+"?");
			    if (!ok) return null;
			    
			    file.delete(true, new NullProgressMonitor());
			}
            return file;
		}
		return null;
	}

}
