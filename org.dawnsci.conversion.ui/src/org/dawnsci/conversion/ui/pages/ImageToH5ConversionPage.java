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

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.ui.wizard.ResourceChoosePage;
import org.dawb.common.util.io.FileUtils;
import org.dawnsci.common.widgets.decorator.RegexDecorator;
import org.dawnsci.conversion.ui.IConversionWizardPage;
import org.dawnsci.io.h5.H5Loader;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ImageToH5ConversionPage extends ResourceChoosePage implements IConversionWizardPage {

	private IConversionContext context;
	private Text datasetPath;

	public ImageToH5ConversionPage() {
		
		super("Convert image directory", null, null);
		setDirectory(false);
		setFileLabel("Nexus-HDF5 file");
		setNewFile(true);
		setOverwriteVisible(true);
		setPathEditable(true);
	
		setDescription("Convert a directory of images into a single HDF5 page");
	}
	
	protected void createContentAfterFileChoose(Composite container) {
		

		final Label label = new Label(container, SWT.NONE);
		label.setText("Dataset path");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		
		this.datasetPath = new Text(container, SWT.BORDER);
		datasetPath.setText("/entry/data");
		datasetPath.setToolTipText("The data set path, allows only alphanumerics, underscore and path separator.\nShould not end with a '/'.");
		new RegexDecorator(datasetPath, "[a-zA-Z0-9_/]");
		datasetPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
	}

	@Override
	public IConversionContext getContext() {
		
		context.setOutputPath(getAbsoluteFilePath());
		context.setDatasetName(datasetPath.getText());
		final File dir = new File(getSourcePath(context)).getParentFile();
		context.setWorkSize(dir.list().length);
		return context;
	}

	@Override
	public void setContext(IConversionContext context) {
		if (context!=null && context.equals(this.context)) return;
		
		this.context = context;
		setErrorMessage(null);
		if (context==null) { // new context being prepared.
	        setPageComplete(false);
			return;
		}
		
		final File dir = new File(getSourcePath(context)).getParentFile();
		setPath(FileUtils.getUnique(dir, "ConvertedImageStack", "nxs").getAbsolutePath());

	}

	@Override
	public boolean isOpen() {
		return true;
	}

	public void pathChanged() {
        final String p = getAbsoluteFilePath();
		if (p==null || p.length() == 0) {
			setErrorMessage("Please select a file to export to.");
			return;
		}
		final File path = new File(p);
		if (path.exists()) {
			
			if (!overwrite.getSelection()) {
				setErrorMessage("The file "+path.getName()+" already exists.");
				return;
			}
				
			if (!path.canWrite()) {
				setErrorMessage("Please choose another location to export to; this one is read only.");
				return;
			}
	    }
		if (!H5Loader.isH5(p)) {
			setErrorMessage("Please choose a file with extension 'h5' or 'nxs' to convert to.");
			return;

		}
		
		setErrorMessage(null);

	}
}
