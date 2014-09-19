/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Widget that creates a canvas to display a text given a specific font
 * @author wqk87977
 *
 */
public class FontExtenderWidget extends Composite {

	private Font font;
	private String fontText;
	private Canvas canvas;
	private boolean isAutoResize = false;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 * @param title
	 *          the name of the widget
	 */
	public FontExtenderWidget(final Composite parent, int style, String title) {
		super(parent, style);
		font = new Font(parent.getDisplay(), "Helvetica", 30, SWT.BOLD);
		fontText = "";
		GridData gd = new GridData(GridData.FILL_BOTH);
		setLayoutData(gd);
		GridLayout gl = new GridLayout();
		setLayout(gl);

		ScrolledComposite scrolledComposite = new ScrolledComposite(this, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		canvas = new Canvas(scrolledComposite, SWT.BORDER | SWT.FILL);
		canvas.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		FormData fd = new FormData();
		fd.left = new FormAttachment(0, 5);
		fd.right = new FormAttachment(100, -5);
		fd.top = new FormAttachment(0, 5);
		fd.bottom = new FormAttachment(100, -35);
		canvas.setLayoutData(fd);
		canvas.setToolTipText("Shows the "+title);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 9;

		canvas.setLayoutData(gridData);
		canvas.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent event) {
				if(isAutoResize){
					FontData[] fontData = font.getFontData();
					// Figure out how big our drawing area is
					Rectangle rect = canvas.getBounds();
					int width = rect.width;
					// Get the width of each character
					int fontWidth = event.gc.getFontMetrics().getAverageCharWidth();
					// Get the current Font height
					int fontHeight = event.gc.getFontMetrics().getHeight();
					//Calculate the fontHeight/fontWidth ratio
					double ratio = fontHeight / fontWidth;
					//Calculate the new height
					int height = (int)((width * ratio) / 20);
					// Reset the font with new font calculated height
					font.dispose();
					font = new Font(parent.getShell().getDisplay(), fontData[0].getName(), height, fontData[0].getStyle());
				}
				event.gc.setFont(font);
				event.gc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
				event.gc.drawText(fontText, 0, 0);
			}
		});

		canvas.setSize(600, 100);
		
		scrolledComposite.setContent(canvas);
//		scrolledComposite.setMinHeight(100);
//		scrolledComposite.setMinWidth(300);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	/**
	 * Method that updates the text to draw on the canvas
	 * @param text
	 */
	public void update(String text){
		fontText = text;
		if(canvas != null && !canvas.isDisposed())
			canvas.redraw();
	}

	/**
	 * Returns the Font
	 */
	public Font getFont(){
		return font;
	}

	public void setFont(Font font){
		this.font = font;
	}

	public String getText(){
		return fontText;
	}

	public void setAutoResize(boolean b) {
		this.isAutoResize  = b;
	}
}
