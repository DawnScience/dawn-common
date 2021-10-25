/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.common.ui.printing;

import org.dawb.common.ui.printing.PrintSettings.Orientation;
import org.dawb.common.ui.printing.PrintSettings.Scale;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.PrintFigureOperation;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class based on the preview SWT dialog example found in "Professional Java Interfaces with SWT/JFace" Jackwind Li
 * Guojie John Wiley & Sons 2005
 * 
 * @author Baha El Kassaby
 * 
 * Refactored 2021/10/13, now intended to be called after the standard PrintDialog, allowing the printer to have been pre-selected,
 * including Orientation.
 * Then, changes scaling and aspect ratio are passed to the print job and an example of how the print job will look on the page displayed.
 */
public class PrintPreviewDialog extends Dialog {
	private Shell shell;
	private Canvas canvas;
	private Combo comboScale;
	private Button buttonAspectRatio;

	private static final String TEXT_COMBO_SCALE = "Scale:";
	private static final String TEXT_BUTTON_ASPECT_RATIO = "Preserve aspect ratio?";
	private static final String TEXT_BUTTON_PRINT = "Print";
	private static final String TEXT_DIALOG_NAME = "Print preview";
	private final PrintSettings settings;
	private final Printer printer;
	
	private Image image;
	private final IPrintImageProvider printImageProvider;
	
	private static final String JOB_NAME = "Print Dawn plot";
	
	private static final Logger logger = LoggerFactory.getLogger(PrintPreviewDialog.class);

	/**
	 * PlotPrintPreviewDialog constructor
	 * 
	 * @param viewerApp
	 *            the IPrintImageProvider object used to create the image
	 * @param device
	 *            the display device
	 * @param legendTable
	 *            the legend of the plot
	 * @param settings
	 *            The input PrintSettings. Will construct a default one if null.
	 */
	public PrintPreviewDialog(Shell shell, IPrintImageProvider imageProvider, PrintSettings settings, PrinterData data) {
		super(shell);
		this.printer = (data == null) ? null : new Printer(data);
		if (settings != null) {
			this.settings = settings.clone();
		} else {
			this.settings = new PrintSettings();
		}
		this.printImageProvider = imageProvider;
	}
		
	private Rectangle getPageShape() {
		Rectangle maxSize = canvas.getBounds();
		Rectangle correctRatio = printer.getClientArea();
		// Either width of canvas = width of paper, or height of canvas = height of paper, whichever limits first

		double smallestMultiplier = Math.min((double) maxSize.height / correctRatio.height,
				(double) maxSize.width / correctRatio.width);
		correctRatio.width *= smallestMultiplier;
		correctRatio.height *= smallestMultiplier;
		return correctRatio;
	}
	
	private Rectangle getSize(Rectangle toFitWithin) {
		boolean respectingAspectRatio = settings.isAspectRatioKept();
		double scale = settings.getScale().getValue();
		Rectangle toReturn;
		Rectangle nonAspectRatioRectangle = toFitWithin;
		

		if (respectingAspectRatio) {
			toReturn = printImageProvider.getBounds();
			// Either width of image = width of page, or height of image = height of page, whichever limits first
			double smallestMultiplier = Math.min((double) nonAspectRatioRectangle.height / toReturn.height,
					(double) nonAspectRatioRectangle.width / toReturn.width);
			toReturn.width *= smallestMultiplier;
			toReturn.height *= smallestMultiplier;
		} else {
			// Width of image = width of page, height of image = height of page
			toReturn = clone(nonAspectRatioRectangle);
		}
		toReturn.width *= scale;
		toReturn.height *= scale;
		// XYGraph adds 6px to height, width to give proper margins, we remove them again here
		toReturn.height -= 6;
		toReturn.width -= 6;
		toReturn.x = toFitWithin.x;
		toReturn.y = toFitWithin.y;
		return toReturn;
	}
	
	private Rectangle clone(Rectangle rect) {
		return new Rectangle(rect.x, rect.y, rect.width, rect.height);
	}
	
	private Rectangle convertPixelsToPoints(Rectangle rect) {
		Point point = printer.getDPI();
		double xPixelsPerPoint = ((double) point.x) / 72;
		double yPixelsPerPoint = ((double) point.y) / 72;
		rect.height /= yPixelsPerPoint;
		rect.width /= xPixelsPerPoint;
		rect.x /= xPixelsPerPoint;
		rect.y /= yPixelsPerPoint;
		return rect;
	}
	
	/*
	 * Creates and then opens the dialog. Note that setting or getting whether
	 * to use portrait or not must be handled separately.
	 * 
	 * @return The new value of the PrintSettings.
	 */
	public PrintSettings open() {		
		// Previous dialog closed within selecting a printer
		if (printer == null) return settings;
		shell = new Shell(this.shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		shell.setText(TEXT_DIALOG_NAME);
		shell.setLayout(new GridLayout());

		final Composite controlComposite = new Composite(shell, SWT.TOP);
		RowLayout controlLayout = new RowLayout();
		controlLayout.center=true;
		controlComposite.setLayout(controlLayout);
		
		Button buttonPrint = new Button(controlComposite, SWT.PUSH);
		buttonPrint.setText(TEXT_BUTTON_PRINT);
		buttonPrint.addListener(SWT.Selection, (Event event)  -> {
			print();
			shell.dispose();
			if (image != null) image.dispose();
		});
		
		comboScale = new Combo(controlComposite, SWT.READ_ONLY);
		for (Scale scale : Scale.values()) {
			comboScale.add(scale.getName());
		}
		comboScale.setText(TEXT_COMBO_SCALE);
		comboScale.select(getPreferencePrintScale());
		comboScale.addSelectionListener(scaleSelection);

		buttonAspectRatio = new Button(controlComposite, SWT.CHECK);
		buttonAspectRatio.setText(TEXT_BUTTON_ASPECT_RATIO);
		buttonAspectRatio.setSelection(getPreferenceAspectRatio());
		buttonAspectRatio.addSelectionListener(aspectRatioListener);

		canvas = new Canvas(shell, SWT.BORDER);
		canvas.addListener(SWT.Paint, this::paint);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		canvas.setLayoutData(gridData);

		shell.setSize(800, 650);
		// Set Canvas to mimic area of printer client area
		canvas.setBounds(getPageShape());
		shell.addListener(SWT.RESIZE, this::paint);
		updateImageAndRedraw();
		shell.open();

		addPropertyListeners();
		
		// Set up the event loop.
		while (!shell.isDisposed()) {
			if (!shell.getDisplay().readAndDispatch()) {
				// If no more entries in event queue
				shell.getDisplay().sleep();
			}
		}
		return settings;
	}
	
	private SelectionAdapter scaleSelection = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			setScalePreference(comboScale.getSelectionIndex());
			updateImageAndRedraw();
		}
	};

	private SelectionAdapter aspectRatioListener = new SelectionAdapter(){
		@Override
		public void widgetSelected(SelectionEvent e) {
			setAspectRatioPreference(buttonAspectRatio.getSelection());
			// set aspect ratio
			updateImageAndRedraw();
		}
	};
	
	private void updateImageAndRedraw() {
		Image oldImage = image;
		image = printImageProvider.getImage(getSize(getPageShape()));
		if (oldImage != null) oldImage.dispose();
		canvas.redraw();
	}
	
	/**
	 * PlotPrintPreviewDialog is listening to eventual property changes done through the Preference Page
	 */
	private void addPropertyListeners() {
		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, INSTANCE_SCOPE_QUALIFIER);
		//final int index = store.getInt("printSettingsPreferencePage");
		
		store.addPropertyChangeListener(new IPropertyChangeListener() {

		//AnalysisRCPActivator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				String property = event.getProperty();
				//IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
				ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, INSTANCE_SCOPE_QUALIFIER);
				if (property.equals(PrintingPrefValues.PRINTSETTINGS_PRINTER_NAME)
						|| property.equals(PrintingPrefValues.PRINTSETTINGS_SCALE)
						|| property.equals(PrintingPrefValues.PRINTSETTINGS_ORIENTATION)
						|| property.equals(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO)) {

					int scale;
					if (preferenceStore.isDefault(PrintingPrefValues.PRINTSETTINGS_SCALE)) {
						scale = preferenceStore.getDefaultInt(PrintingPrefValues.PRINTSETTINGS_SCALE);
					} else {
						scale = preferenceStore.getInt(PrintingPrefValues.PRINTSETTINGS_SCALE);
					}
					settings.setScale(Scale.values()[scale]);
						

					boolean keepAspectRatio;
					if (preferenceStore.isDefault(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO)) {
						keepAspectRatio = preferenceStore.getDefaultBoolean(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO);
					} else {
						keepAspectRatio = preferenceStore.getBoolean(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO);
					}
					settings.setKeepAspectRatio(keepAspectRatio);

					int orientation;
					if (preferenceStore.isDefault(PrintingPrefValues.PRINTSETTINGS_ORIENTATION)) {
						orientation = preferenceStore.getDefaultInt(PrintingPrefValues.PRINTSETTINGS_ORIENTATION);
					} else {
						orientation = preferenceStore.getInt(PrintingPrefValues.PRINTSETTINGS_ORIENTATION);
					}
					settings.setOrientation(Orientation.values()[orientation]);
				}
			}
		});
	}
	
	private static final String INSTANCE_SCOPE_QUALIFIER = "uk.ac.diamond.scisoft.analysis.rcp";
		
	private int getPreferencePrintScale() {
		final ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, INSTANCE_SCOPE_QUALIFIER);
		return preferenceStore.isDefault(PrintingPrefValues.PRINTSETTINGS_SCALE)
				? preferenceStore.getDefaultInt(PrintingPrefValues.PRINTSETTINGS_SCALE)
				: preferenceStore.getInt(PrintingPrefValues.PRINTSETTINGS_SCALE);
	}
	
	private boolean getPreferenceAspectRatio() {
		final ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, INSTANCE_SCOPE_QUALIFIER);
		return preferenceStore.isDefault(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO)
				? preferenceStore.getDefaultBoolean(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO)
				: preferenceStore.getBoolean(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO);
	}

	private void setScalePreference(int value) {
		final ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, INSTANCE_SCOPE_QUALIFIER);
		settings.setScale(Scale.values()[value]);
		preferenceStore.setValue(PrintingPrefValues.PRINTSETTINGS_SCALE, value);
	}

	private void setAspectRatioPreference(boolean value) {
		final ScopedPreferenceStore preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, INSTANCE_SCOPE_QUALIFIER);
		settings.setKeepAspectRatio(value);
		preferenceStore.setValue(PrintingPrefValues.PRINTSETTINGS_ASPECTRATIO, value);
	}
		
	private void paint(Event e) {
		Rectangle background = getPageShape();
		Rectangle displaySize = getSize(background);
		
		e.gc.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		// draws the page layout
		e.gc.fillRectangle(background);

		// draws the margin.
		e.gc.setLineStyle(SWT.LINE_DASH);
		e.gc.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
		e.gc.drawImage(printImageProvider.getImage(displaySize), 0, 0);
	}
	
	/**
	 * Prints the image current displayed to the specified printer.
	 * 
	 * @param printer
	 */
	private void print() {
		// Must convert from Pixels to Points
		Rectangle pointBounds = convertPixelsToPoints(printer.getClientArea());
		image = printImageProvider.getImage(getSize(pointBounds));
		ImageFigure imageFig = new ImageFigure(image);
		// Must set bounds else doesn't display
		imageFig.setBounds(new org.eclipse.draw2d.geometry.Rectangle(pointBounds));
		logger.info("Printing image on printer {}", printer.getPrinterData().name);
		final PrintFigureOperation op = new PrintFigureOperation(printer, imageFig);
		op.run(JOB_NAME);
		printer.dispose();
	}
		
}
