/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.wizard.persistence.PersistenceExportWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class PlotDataConversionPage extends ResourceChoosePage {

	public PlotDataConversionPage() {
		super("wizardPage", "Page exporting plotted data", null);
		setTitle("Export plotted data to file");
		setDirectory(true);
		
		this.setDirectory(false);
		this.setNewFile(true);
		this.setPathEditable(true);
	}

	private boolean asDat = true;
	private boolean asSingle = true;
	private boolean allXEqual = false;
	private boolean asSingleX = allXEqual;
	final List<Button> allButtons = new ArrayList<>();

	@Override
	protected void createContentAfterFileChoose(Composite container) {
		if (PersistenceExportWizard.FILE_EXTENSION.equalsIgnoreCase(getFileExtension())) {
			return;
		}

		Label label = new Label(container, SWT.NONE);
		label.setText("Format");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		Composite c = new Composite(container, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		c.setLayout(new FillLayout(SWT.HORIZONTAL));

		SelectionListener radioListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button btn = (Button) e.widget;

				int n = allButtons.indexOf(btn);
				if (n < 2) { // first 2 buttons for format
					setIsDat(n == 0);
				} else if (n < 4) { // last 2 buttons for single/multiple files
					asSingle = n == 2;
					asSingleX = allXEqual && asSingle;
					if (asSingle) {
						allButtons.get(4).setEnabled(allXEqual);
						allButtons.get(4).setSelection(asSingleX);
					}
				} else { // 5th button to use only one x column
					asSingleX = btn.getSelection();
				}
			}
		};
		Button bFormat = new Button(c, SWT.RADIO);
		allButtons.add(bFormat);
		bFormat.setSelection(asDat);
		bFormat.setText("dat");
		bFormat.setToolTipText("save traces in columns as ASCII dat");
		bFormat.addSelectionListener(radioListener);
		bFormat = new Button(c, SWT.RADIO);
		allButtons.add(bFormat);
		bFormat.setSelection(!asDat);
		bFormat.setText("csv");
		bFormat.setToolTipText("save traces in rows as CSV");
		bFormat.addSelectionListener(radioListener);

		label = new Label(container, SWT.NONE);
		label.setText("Number of files");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		c = new Composite(container, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		c.setLayout(new FillLayout(SWT.HORIZONTAL));

		Button bNumber = new Button(c, SWT.RADIO);
		allButtons.add(bNumber);
		bNumber.setSelection(asSingle);
		bNumber.setEnabled(asDat);
		bNumber.setText("single");
		bNumber.setToolTipText("save as single file (with NaNs padding if columns have different lengths)");
		bNumber.addSelectionListener(radioListener);

		bNumber = new Button(c, SWT.RADIO);
		allButtons.add(bNumber);
		bNumber.setSelection(!asSingle);
		bNumber.setEnabled(asDat);
		bNumber.setText("multiple");
		bNumber.setToolTipText("save in multiple files");
		bNumber.addSelectionListener(radioListener);

		label = new Label(container, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		c = new Composite(container, SWT.NONE);
		c.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		c.setLayout(new FillLayout(SWT.HORIZONTAL));

		bNumber = new Button(c, SWT.CHECK);
		allButtons.add(bNumber);
		bNumber.setSelection(asSingleX);
		bNumber.setEnabled(allXEqual);
		bNumber.setText("single x column");
		bNumber.setToolTipText("x columns have been found to be the same so can save just one x");
		bNumber.addSelectionListener(radioListener);
		label = new Label(c, SWT.NONE);

		// spacer
		label = new Label(container, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 4, 1));

		label = new Label(container, SWT.NONE);
		label.setText("Plot data is saved in x/y pairs (in rows or columns according to the format)");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, true, false, 4, 1));
	}

	public boolean isSingle() {
		return asSingle;
	}

	
	public void setAllXEqual(boolean allXEqual) {
		asSingleX = this.allXEqual = allXEqual;
		this.asSingle = allXEqual;
	}

	public boolean isAsSingleX() {
		return asSingleX;
	}

	public boolean isDat() {
		return asDat;
	}

	public void setIsDat(boolean isDat) {
		this.asDat = isDat;
		if (allButtons.size() > 3) {
			allButtons.get(2).setEnabled(asDat); // only enable when choosing dat
			allButtons.get(3).setEnabled(asDat);
			allButtons.get(4).setEnabled(asDat && allXEqual);
			allButtons.get(4).setSelection(asSingleX);
		}
	}

	@Override
	public void setPath(String path) {
		super.setPath(path);
		updateIsDat(path);
	}

	@Override
	protected void pathChanged() {
		super.pathChanged();
		updateIsDat(getPath());
	}

	private void updateIsDat(String path) {
		path = path.toLowerCase();
		setIsDat(!path.endsWith(Plot1DConversionVisitor.EXTENSION_CSV));
	}

	@Override
	public void dispose() {
		allButtons.clear();
		super.dispose();
	}
}