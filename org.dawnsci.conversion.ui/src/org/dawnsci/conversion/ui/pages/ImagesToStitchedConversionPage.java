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
import org.dawnsci.conversion.converters.ImagesToStitchedConverter.ConversionStitchedBean;
import org.dawnsci.conversion.ui.IConversionWizardPage;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.NumberFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

/**
 * 
 * 
 * @author wqk87977
 *
 */
public class ImagesToStitchedConversionPage extends ResourceChoosePage
		implements IConversionWizardPage {

	private IConversionContext context;
	private Spinner rowsSpinner;
	private Spinner columnsSpinner;
	private FormattedText angleText;

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
		if (context == null)
			return null;
		context.setOutputPath(getAbsoluteFilePath());
		final File dir = new File(getSourcePath(context)).getParentFile();
		context.setWorkSize(dir.list().length);

		final ConversionStitchedBean bean = new ConversionStitchedBean();
		bean.setRows(rowsSpinner.getSelection());
		bean.setColumns(columnsSpinner.getSelection());
		Object val = angleText.getValue();
		if (val instanceof Long)
			bean.setAngle((Long)val);
		else if (val instanceof Double)
			bean.setAngle((Double)val);
		context.setUserObject(bean);
		
		return context;
	}

	@Override
	protected void createContentAfterFileChoose(Composite container) {
		Composite comp = new Composite(container, SWT.NONE);
		comp.setLayout(new GridLayout(6, false));
		comp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 4, 1));

		final Label labelRow = new Label(comp, SWT.NONE);
		labelRow.setText("Rows");
		labelRow.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		rowsSpinner = new Spinner(comp, SWT.BORDER);
		rowsSpinner.setMinimum(1);
		rowsSpinner.setSelection(3);
		rowsSpinner.setToolTipText("Number of rows for the resulting stitched image");
		rowsSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final Label labelColumn = new Label(comp, SWT.NONE);
		labelColumn.setText("Columns");
		labelColumn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		columnsSpinner = new Spinner(comp, SWT.BORDER);
		columnsSpinner.setMinimum(1);
		columnsSpinner.setSelection(3);
		columnsSpinner.setToolTipText("Number of columns for the resulting stitched image");
		columnsSpinner.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		final Label labelAngle = new Label(comp, SWT.NONE);
		labelAngle.setText("Angle");
		labelAngle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		angleText = new FormattedText(comp, SWT.BORDER);
		NumberFormatter formatter = new NumberFormatter("-##0.0");
		formatter.setFixedLengths(false, true);
		angleText.setFormatter(formatter);
		angleText.setValue(new Double(-49.0));
		angleText.getControl().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

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
		setPath(FileUtils.getUnique(dir, "StitchedImage", "tif").getAbsolutePath());

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
