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
import java.util.HashMap;
import java.util.Map;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.conversion.converters.ConversionInfoBean;
import org.dawnsci.conversion.ui.Activator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.slf4j.LoggerFactory;

public final class ImageConvertPage extends AbstractSliceConversionPage {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ImageConvertPage.class);
	
	private static final String[] IMAGE_FORMATS = new String[]{"tif", "png", "jpg"};
	private static final Map<String,int[]> BIT_DEPTHS;
	static {
		BIT_DEPTHS = new HashMap<String, int[]>(3);
		// TODO investigate other bit depths for different formats.
		BIT_DEPTHS.put("tif", new int[]{32,16});
		BIT_DEPTHS.put("png", new int[]{16});
		BIT_DEPTHS.put("jpg", new int[]{8});
	}


	private String         imageFormat;
	private int            bitDepth;
	private Text           imagePrefixBox;
	private CLabel         warningLabel;

	private Text sliceIndexFormat;

	public ImageConvertPage() {
		super("wizardPage", "Page for slicing HDF5 data into a directory of images.", null);
		setTitle("Convert to Images");
		setDirectory(true);
		setFileLabel("Export to");
	}
	
	@Override
	protected void createAdvanced(final Composite parent) {
				
		final File source = new File(getSourcePath(context));
		setPath(source.getParent()+File.separator+"output");

		final ExpandableComposite advancedComposite = new ExpandableComposite(parent, SWT.NONE);
		advancedComposite.setExpanded(false);
		advancedComposite.setText("Advanced");
		advancedComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		final Composite advanced = new Composite(parent, SWT.NONE);
		advanced.setLayout(new GridLayout(3, false));
		advanced.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
			
		Label label = new Label(advanced, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Image Format");
		
		final CCombo imf = new CCombo(advanced, SWT.READ_ONLY|SWT.BORDER);
		imf.setItems(IMAGE_FORMATS);
		imf.select(0);
		imageFormat = "tif";
		imf.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		label = new Label(advanced, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Bit Depth");
		
		final CCombo bd = new CCombo(advanced, SWT.READ_ONLY|SWT.BORDER);
		bd.setItems(getStringArray(BIT_DEPTHS.get(imageFormat)));
		bd.select(0);
		bitDepth = 32;
		bd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		bd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				final String str = bd.getItem(bd.getSelectionIndex());
				bitDepth = Integer.parseInt(str);
				GridUtils.setVisible(warningLabel, bitDepth<32);
				warningLabel.getParent().layout();
				pathChanged();
			}
		});
		imf.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				imageFormat = imf.getItem(imf.getSelectionIndex());
				
				final int [] depths = BIT_DEPTHS.get(imageFormat);
				final String[] sa   = getStringArray(depths);
				bd.setItems(sa);
				bitDepth = depths[0];
				bd.select(0);
				GridUtils.setVisible(warningLabel, bitDepth<32);
				warningLabel.getParent().layout();
				pathChanged();
			}
		});
		
		this.warningLabel = new CLabel(advanced, SWT.NONE);
		warningLabel.setImage(Activator.getImage("icons/warning.gif"));
		warningLabel.setText("Lower dit depths will not support larger data values.");
		warningLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		GridUtils.setVisible(warningLabel, false);

		label = new Label(advanced, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Image Prefix");

		this.imagePrefixBox = new Text(advanced, SWT.BORDER);
		imagePrefixBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		imagePrefixBox.setText("image");
		
		
		label = new Label(advanced, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Slice Index Format");

		this.sliceIndexFormat = new Text(advanced, SWT.BORDER);
		sliceIndexFormat.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		sliceIndexFormat.setText("#000");
		label = new Label(advanced, SWT.NULL);
		label.setLayoutData(new GridData());


		GridUtils.setVisible(advanced, false);
		ExpansionAdapter expansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				GridUtils.setVisible(advanced, !advanced.isVisible());
				parent.layout(new Control[]{advanced, advancedComposite});
				Composite comp = parent.getParent();
				if (comp instanceof ScrolledComposite) {
					Rectangle r = ((ScrolledComposite)comp).getClientArea();
					((ScrolledComposite)comp).setMinSize(parent.computeSize(r.width, SWT.DEFAULT));
					parent.layout();
				} else {
					parent.layout();
					parent.getParent().layout();
				}
			}
		};
		advancedComposite.addExpansionListener(expansionListener);
		
	}

	private String[] getStringArray(int[] is) {
		final String[] sa = new String[is.length];
		for (int i = 0; i < is.length; i++) {
			sa[i] = String.valueOf(is[i]);
		}
		return sa;
	}
	
	@Override
	public boolean isOpen() {
		return false;
	}
	
	protected void nameChanged() {

		try {
			super.nameChanged();
			try {
				final String name = datasetName.substring(datasetName.lastIndexOf('/')+1);
				imagePrefixBox.setText(name);
			} catch (Exception ignored) {
				imagePrefixBox.setText(datasetName);
			}

			
		} catch (Exception ne) {
			setErrorMessage("Cannot read data set '"+datasetName+"'");
			logger.error("Cannot get data", ne);
		}
	}

	/**
	 * Checks the path is ok.
	 */
	protected void pathChanged() {

		super.pathChanged();
		final File outputDir = new File(getAbsoluteFilePath());
		try {
			if (outputDir.isFile()) {
				setErrorMessage("The directory "+outputDir+" is a file.");
				return;			
			}
		} catch (Exception ne) {
			setErrorMessage(ne.getMessage()); // Not very friendly...
			return;			
		}
		setErrorMessage(null);
		return;
	}
	
	@Override
	public IConversionContext getContext() {
		if (context == null) return null;
		IConversionContext context = super.getContext();
		
		final ConversionInfoBean bean = new ConversionInfoBean();
		bean.setExtension(imageFormat);
		bean.setBits(bitDepth);
		bean.setAlternativeNamePrefix(imagePrefixBox.getText());
		bean.setSliceIndexFormat(sliceIndexFormat.getText());
		context.setUserObject(bean);
		
		return context;
	}

	@Override
	public void setContext(IConversionContext context) {
		super.setContext(context);
		
		if (context==null) return;
		
		// We either are directories if we are choosing multiple files or
		// we are single file output and specifying a single output file.
        if (context.getFilePaths().size()>1) { // Multi
     		GridUtils.setVisible(multiFileMessage, true);
        } else {
    		GridUtils.setVisible(multiFileMessage, false);
        }
        multiFileMessage.getParent().layout();
	}

}
