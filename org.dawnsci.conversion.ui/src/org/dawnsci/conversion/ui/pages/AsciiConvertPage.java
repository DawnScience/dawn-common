/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.conversion.ui.pages;

import java.io.File;
import java.util.Arrays;

import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.conversion.converters.AsciiConvert1D;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 *   AsciiConvertPage used if the context is a 1D ascii one.
 *
 *   @author gerring
 *   @date Aug 31, 2010
 *   @project org.dawb.workbench.actions
 **/
public class AsciiConvertPage extends AbstractDatasetChoosePage {
	
	
	private static final String[] CONVERT_OPTIONS = new String[] {"Tab Separated Values (*.dat)", 
		                                                          "Comma Separated Values (*.csv)"};

	protected int conversionSelection;

	/**
	 * Create the wizard.
	 */
	public AsciiConvertPage() {
		super("wizardPage", "Convert data from synchrotron formats and compressed files to common simple data formats.", null);
		setTitle("Convert Data");
		dataSetNames = new String[]{"Loading..."};
		setDirectory(false);
		setOverwriteVisible(false);
		setNewFile(true);
		setPathEditable(true);
    	setFileLabel("Output file");
    }

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	@Override
	public void createContentBeforeFileChoose(Composite container) {
				
		Label convertLabel = new Label(container, SWT.NONE);
		convertLabel.setText("Convert to");
		
		final Combo combo = new Combo(container, SWT.READ_ONLY|SWT.BORDER);
		combo.setItems(CONVERT_OPTIONS);
		combo.setToolTipText("Convert to file type by file extension");
		combo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
		combo.select(0);
		
		conversionSelection = 0;
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				conversionSelection = combo.getSelectionIndex();
			}
		});

	}
		
	/**
	 * Ensures that both text fields are set.
	 */
	@Override
	protected void pathChanged() {

        final String p = getAbsoluteFilePath();
		if (p==null || p.length() == 0) {
			setErrorMessage("Please select a file to export to.");
			setPageComplete(false);
			return;
		}
		final File path = new File(p);
		if (path.exists() && !path.canWrite()) {
			setErrorMessage("Please choose another location to export to; this one is read only.");
			setPageComplete(false);
			return;
		}
		if (context.getFilePaths().size()<2) {
			if (path.exists() && !overwrite) {
				setErrorMessage("Please confirm overwrite of the file.");
				setPageComplete(false);
				return;
			}
			if (!path.getName().toLowerCase().endsWith("."+getExtension())) {
				setErrorMessage("Please set the file name to export as a file with the extension '"+getExtension()+"'.");
				setPageComplete(false);
				return;
			}
		} else {
			if (!path.exists()) {
				setErrorMessage("Please choose an existing folder to export to.");
				setPageComplete(false);
				return;
			}
		}
		setErrorMessage(null);
		setPageComplete(true);
	}
	
	@Override
	public void setContext(IConversionContext context) {
		
		if (context!=null && context.equals(this.context)) return;
		
		this.context = context;
		setErrorMessage(null);
		if (context==null) { // new context being prepared.
			this.imeta  = null;
			this.holder = null;
	        setPageComplete(false);
			return;
		}
		// We populate the names later using a wizard task.
        try {
			getDataSetNames();
		} catch (Exception e) {
			logger.error("Cannot extract data sets!", e);
		}
        
		final File source = new File(getSourcePath(context));
       
        setPageComplete(true);
        
        if (context.getFilePaths().size()>1 || source.isDirectory()) { // Multi
        	setPath(source.getParent());
        	setDirectory(true);
        	setFileLabel("Output folder");
        	GridUtils.setVisible(multiFileMessage, true);
        	this.overwriteButton.setSelection(true);
        	this.overwrite = true;
        	this.overwriteButton.setEnabled(false);
        	this.openButton.setSelection(false);
        	this.open = false;
        	this.openButton.setEnabled(false);
        } else {
        	if (source.isFile()) {
        	    final String strName = source.getName().substring(0, source.getName().indexOf('.'))+"."+getExtension();
	        	setPath((new File(source.getParentFile(), strName)).getAbsolutePath());
	        	setDirectory(false);
	        	setFileLabel("Output file");
	        	GridUtils.setVisible(multiFileMessage, false);
	        	this.overwriteButton.setEnabled(true);
	        	this.openButton.setEnabled(true);
        	} 
        }

	}	
	
	@Override
	public IConversionContext getContext() {
		if (context==null) return null;
		final AsciiConvert1D.ConversionInfoBean bean = new AsciiConvert1D.ConversionInfoBean();
		bean.setConversionType(getExtension());
		context.setUserObject(bean);
		context.setOutputPath(getAbsoluteFilePath()); // cvs or dat file.
		context.setDatasetNames(Arrays.asList(getSelected()));
		return context;
	}

	
	protected String getExtension() {
		return conversionSelection==0?"dat":"csv";
	}


	protected int getMinimumDataSize() {
		return 1; // Data must be 1D or better
	}

	@Override
	protected String getDataTableTooltipText() {
		return "Select data to export to the "+getExtension();
	}

}
