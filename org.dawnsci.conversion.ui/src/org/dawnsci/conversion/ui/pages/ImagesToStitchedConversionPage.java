/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
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
import org.dawnsci.conversion.ui.IConversionWizardPage;

/**
 * TODO add UI spinners and other UI widget to set necessary parameters
 * @author wqk87977
 *
 */
public class ImagesToStitchedConversionPage extends ResourceChoosePage
		implements IConversionWizardPage {

	private IConversionContext context;

	public ImagesToStitchedConversionPage() {
		super("Convert image directory", null, null);
		setDirectory(false);
		setFileLabel("Stitched image file");
		setNewFile(true);
		setOverwriteVisible(true);
		setPathEditable(true);
		setDescription("Returns a stitched image given a stack of images");
	}

	@Override
	public IConversionContext getContext() {
		context.setOutputPath(getAbsoluteFilePath());
		final File dir = new File(getSourcePath(context)).getParentFile();
		context.setWorkSize(dir.list().length);
		return context;
	}

	@Override
	public void setContext(IConversionContext context) {
		if (context != null && context.equals(this.context))
			return;

		this.context = context;
		setErrorMessage(null);
		if (context == null) { // new context being prepared.
			setPageComplete(false);
			return;
		}

		final File dir = new File(getSourcePath(context)).getParentFile();
		setPath(FileUtils.getUnique(dir, "StitchedImage", "tif")
				.getAbsolutePath());

	}

	@Override
	public boolean isOpen() {
		return true;
	}

	public void pathChanged() {
		final String p = getAbsoluteFilePath();
		if (p == null || p.length() == 0) {
			setErrorMessage("Please select a file to export to.");
			return;
		}
		final File path = new File(p);
		if (path.exists()) {

			if (!overwrite.getSelection()) {
				setErrorMessage("The file " + path.getName()
						+ " already exists.");
				return;
			}

			if (!path.canWrite()) {
				setErrorMessage("Please choose another location to export to; this one is read only.");
				return;
			}
		}
		setErrorMessage(null);
	}
}
