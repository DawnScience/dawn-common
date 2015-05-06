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
import java.util.Collection;

import org.dawb.common.services.ISystemService;
import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.conversion.converters.ConversionInfoBean;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.downsample.DownsampleMode;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

/**
 * Page for exporting slice to video.
 * 
 * @author Matthew Gerring
 *
 */
public final class AVIConvertPage extends AbstractSliceConversionPage {
		
	private static final String[] DOWNSAMPLE_TYPES;
	static {
		DownsampleMode[] modes = DownsampleMode.values();
		DOWNSAMPLE_TYPES = new String[modes.length];
		for (int i = 0; i < modes.length; i++) {
			DOWNSAMPLE_TYPES[i] = modes[i].name();
		}
	}
	private static final String[] DOWNSAMPLE_SIZES =  new String[]{"1","2","4","8"};
	
	
	private String         downsampleName;
	private int            downsampleSize;
	private int            frameRate;
	private boolean        alwaysShowTitle   = false;
	private boolean        useCurrentColours = true;

	public AVIConvertPage() {
		super("wizardPage", "Page for exporting HDF5 data slices into a video.", null);
		setTitle("Convert to Images");
		setDirectory(false);
		setOverwriteVisible(true);
		setFileLabel("Export video");
		setPathEditable(true);
		setNewFile(true);
	}
	
	@Override
	protected void createAdvanced(final Composite parent) {
		
		final ExpandableComposite advancedComposite = new ExpandableComposite(parent, SWT.NONE);
		advancedComposite.setExpanded(false);
		advancedComposite.setText("Advanced");
		advancedComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		final Composite advanced = new Composite(parent, SWT.NONE);
		advanced.setLayout(new GridLayout(3, false));
		advanced.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
			
		Label label = new Label(advanced, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Downsample Type");
		
		final CCombo imf = new CCombo(advanced, SWT.READ_ONLY|SWT.BORDER);
		imf.setItems(DOWNSAMPLE_TYPES);
		imf.select(2);
		downsampleName = DOWNSAMPLE_TYPES[2];
		imf.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		label = new Label(advanced, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Downsample Bin");
		
		final CCombo bd = new CCombo(advanced, SWT.READ_ONLY|SWT.BORDER);
		bd.setItems(DOWNSAMPLE_SIZES);
		bd.select(1);
		downsampleSize = 2;
		bd.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		bd.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				final String str = bd.getItem(bd.getSelectionIndex());
				downsampleSize = Integer.parseInt(str);
				pathChanged();
			}
		});
		imf.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				downsampleName = imf.getItem(imf.getSelectionIndex());				
				pathChanged();
			}
		});
		
		
		label = new Label(advanced, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Frame rate");

        final Text rate = new Text(advanced, SWT.BORDER);
        rate.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        rate.setText("20");
        frameRate = 20;
        rate.addModifyListener(new ModifyListener() {			
			@Override
			public void modifyText(ModifyEvent e) {
				try {
					frameRate = Integer.parseInt(rate.getText());
					rate.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
				} catch (Throwable ne) {
					rate.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
				}
			}
		});
		
		label = new Label(advanced, SWT.NULL);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		label.setText("image/s");
		
        final Button useMap = new Button(advanced, SWT.CHECK);
        useMap.setText("Try to use colour map from current plot for all images.");
        useMap.setToolTipText("If off the colours will be taken from the current default color map and the max and min will be regenerated for each image.");
        useMap.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        useMap.setSelection(useCurrentColours);
        useMap.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		useCurrentColours = useMap.getSelection();
        	}
		});
        
        final Button showTitle = new Button(advanced, SWT.CHECK);
        showTitle.setText("Show title and axes when exporting image stacks.");
        showTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        showTitle.setSelection(alwaysShowTitle);
        showTitle.addSelectionListener(new SelectionAdapter() {
        	public void widgetSelected(SelectionEvent e) {
        		alwaysShowTitle = showTitle.getSelection();
        	}
		});



		GridUtils.setVisible(advanced, false);
		ExpansionAdapter expansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				GridUtils.setVisible(advanced, !advanced.isVisible());
				parent.layout(new Control[]{advanced, advancedComposite});
				parent.layout();
				parent.getParent().layout();
			}
		};
		advancedComposite.addExpansionListener(expansionListener);
		
	}
	
	@Override
	protected void createContentAfterFileChoose(Composite container) {
		super.createContentAfterFileChoose(container);
		updateAllowedActions();
	}
	
	private static final String getFileNameNoExtension(File file) {
		final String fileName = file.getName();
		int posExt = fileName.lastIndexOf(".");
		// No File Extension
		return posExt == -1 ? fileName : fileName.substring(0, posExt);
	}

	@Override
	public boolean isOpen() {
		return false;
	}
	
	/**
	 * Checks the path is ok.
	 */
	protected void pathChanged() {

		super.pathChanged();
		final String path = getAbsoluteFilePath();
		if (path!=null && getErrorMessage()==null) {
			final File outputAVI = new File(path);
			try {
				if (outputAVI.exists() && !outputAVI.isFile()) {
					setErrorMessage("The file '"+outputAVI+"' is not a valid file.");
					return;			
				}
			} catch (Exception ne) {
				setErrorMessage(ne.getMessage()); // Not very friendly...
				return;			
			}
			setErrorMessage(null);
			return;
		}
	}
	
	private void updateAllowedActions() {
		sliceComponent.setSliceActionsEnabled(false);
		sliceComponent.setSliceActionEnabled(PlotType.XY,      true);
		sliceComponent.setSliceActionEnabled(PlotType.IMAGE,   true);
		sliceComponent.setSliceActionEnabled(PlotType.SURFACE, true);
	}

	@Override
	public void setContext(IConversionContext context) {
		
		super.setContext(context);
		
		if (context==null) return;
		
		// We either are directories if we are choosing multiple files or
		// we are single file output and specifying a single output file.
        if (context.getFilePaths().size()>1) { // Multi
    		final File source = new File(getSourcePath(context));
    		setPath(source.getParent());
       	    setDirectory(true);
        	setFileLabel("Output folder");
    		GridUtils.setVisible(multiFileMessage, true);
        } else {
    		final File source = new File(getSourcePath(context));
    		setPath(source.getParent()+File.separator+getFileNameNoExtension(source)+".avi");
        	setDirectory(false);
        	setFileLabel("Export video");
    		GridUtils.setVisible(multiFileMessage, false);
        }
        multiFileMessage.getParent().layout();
	}

	
	@Override
	public IConversionContext getContext() {
		if (context == null) return null;
		IConversionContext context = super.getContext();
		
		final ConversionInfoBean bean = new ConversionInfoBean();
		bean.setDownsampleMode(DownsampleMode.valueOf(downsampleName));
		bean.setDownsampleBin(downsampleSize);
		bean.setFrameRate(frameRate);
		bean.setSliceType((PlotType)sliceComponent.getSliceType());
		bean.setAlwaysShowTitle(alwaysShowTitle);
		
		if (useCurrentColours) {
			ISystemService<IPlottingSystem> service = (ISystemService<IPlottingSystem>)PlatformUI.getWorkbench().getService(ISystemService.class);
			if (service!=null) {
				// If we have a plotting system for the input file, use that.
				final File file = new File(context.getFilePaths().get(0));
				IPlottingSystem system = service.getSystem(file.getName());
				if (system!=null) {
					IImageTrace image = getImageTrace(system);
					if (image!=null) bean.setImageServiceBean(image.getImageServiceBean());
				}
			}
		}
		
		context.setUserObject(bean);
		
		return context;
	}
	private IImageTrace getImageTrace(IPlottingSystem system) {
		final ITrace trace = getTrace(system);
		return trace instanceof IImageTrace ? (IImageTrace)trace : null;
	}
	private ITrace getTrace(IPlottingSystem system) {
		if (system == null) return null;

		final Collection<ITrace> traces = system.getTraces();
		if (traces==null || traces.size()==0) return null;
        return  traces.iterator().next();
	}


	
	@Override
	public IWizardPage getNextPage() {
		return null;
	}

}
