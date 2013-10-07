/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.conversion.ui.pages;

import java.io.File;
import java.util.Arrays;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.util.io.FileUtils;
import org.dawnsci.conversion.converters.CompareConverter;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;


/**
 *   AsciiConvertPage used if the context is a 1D ascii one.
 *
 *   @author gerring
 *   @date Aug 31, 2010
 *   @project org.edna.workbench.actions
 **/
public class CompareConvertPage extends AbstractDatasetChoosePage  {
	
	private StyledText info;

	/**
	 * Create the wizard.
	 */
	public CompareConvertPage() {
		super("wizardPage", "Compare multiple data sets in multiple files.", null);
		setTitle("Compare Data");
		dataSetNames = new String[]{"Loading..."};
		setDirectory(false);
		setOverwriteVisible(false);
		setNewFile(true);
		setPathEditable(true);
    	setFileLabel("Output file");
    }
	
	public void createContentAfterFileChoose(Composite container) {
        super.createContentAfterFileChoose(container);
        
		this.info = new StyledText(container, SWT.NONE);
		info.setEditable(false);
		info.setBackground(container.getBackground());
		info.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
        
	}
	
	/**
	 * Ensures that both text fields are set.
	 */
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
			setErrorMessage("Compare can only be done on more than one file!");
			setPageComplete(false);
			return;
		} else {
			if (!isOverwrite() && path.exists()) {
				setErrorMessage("The file '"+path.getName()+"' exists, please tick overwrite or choose a different file.");
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
        
        if (context.getFilePaths().size()<2) { // !Multi
			setErrorMessage("Compare can only be done on more than one file!");
			setPageComplete(false);
			return;
        }
        
    	setPath(FileUtils.getUnique(source.getParentFile(), "compare_file", "nxs").getAbsolutePath());
    	setDirectory(false);
    	
    	GridUtils.setVisible(multiFileMessage, true);
       	this.overwriteButton.setSelection(false);
       	this.overwrite = false;
    	this.openButton.setSelection(true);
    	this.open = true;
    	this.openButton.setEnabled(true);

        final File firstFile = new File(context.getFilePaths().get(0));
        String label = "*The data written is based on the first file, '"+firstFile.getName()+"'.";
        ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
        info.setText(label);
        info.setStyleRange(new StyleRange(0, label.length(), colorRegistry.get(JFacePreferences.DECORATIONS_COLOR), info.getParent().getBackground()));
        info.setToolTipText("The compare is done by forcing each file in the stack to\n"
        		          + "use the same data size as the data in the first file.\n\n"
        		          + "So the required size may be obtained by making a local copy of the\n"
        		          + "data and ensuring the first file has the correct size. It is also\n"
        		          + "possible to set the sizes manually, please contact your support\n"
        		          + "representative if you require this.");

	}	

	@Override
	public IConversionContext getContext() {
		if (context==null) return null;
		final CompareConverter.ConversionInfoBean bean = new CompareConverter.ConversionInfoBean();
		
        // TODO Set sizes
		
		context.setUserObject(bean);
		context.setOutputPath(getAbsoluteFilePath()); // cvs or dat file.
		context.setDatasetNames(Arrays.asList(getSelected()));
		return context;
	}

	protected String getExtension() {
		return "nxs";
	}

	protected int getMinimumDataSize() {
		return 1; // Allows scalar data!
	}

	@Override
	protected String getDataTableTooltipText() {
		return "Choose data (based on the first file in the compare list).";
	}
}
