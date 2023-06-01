/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.image;

import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CursorUtils {

	private final static int   CURSOR_SIZE = 28;
	private final static Color BLACK_COLOR = ColorConstants.black;
	private final static Color WHITE_COLOR = ColorConstants.white;

	private static final int EXTRA_DECIMAL_PLACES = 2; // number of extra decimal places for cursor coordinates (over axes precision)

	private static final Color TRANSPARENT_COLOR;

	static {
		String os = System.getProperty("os.name", "");
		if (os.startsWith("Mac OS X")) {
			TRANSPARENT_COLOR = new Color(null, new RGB(253, 254, 255));
		} else {
			TRANSPARENT_COLOR = null;
		}
	}

	/**
	 * Get a cursor with position and value (if image given)
	 * @param me
	 * @param axis
	 * @param yAxis
	 * @param image - may be null
	 * @return cursor
	 */
	public static Cursor getPositionCursor(int mx, int my, IAxis xAxis, IAxis yAxis, IImageTrace imageTrace) {
		double xCoordinate = xAxis.getValueFromPosition(mx);
		double yCoordinate = yAxis.getValueFromPosition(my);
		String intensityText = null;

		if (imageTrace != null) {
			Dataset image = DatasetUtils.convertToDataset(imageTrace.getData());
			int[] shape = image.getShapeRef();

			boolean transpose = imageTrace.getImageServiceBean().isTransposed();
			boolean leading = imageTrace.getImageOrigin().isOnLeadingDiagonal();
			boolean flip = !(transpose ^ leading);
			if (!imageTrace.hasTrueAxes()) {
				if (flip) {
					double t = xCoordinate;
					xCoordinate = yCoordinate;
					yCoordinate = t;
				}
				int i = (int) Math.floor(xCoordinate);
				if (i >= shape[1]) {
					i = shape[1] - 1;
				} else if (i < 0) {
					i = 0;
				}
				int j = (int) Math.floor(yCoordinate);
				if (j >= shape[0]) {
					j = shape[0] - 1;
				} else if (j < 0) {
					j = 0;
				}

				double intensity = image.getDouble(j, i);
				if (!Double.isNaN(intensity)) {
					intensityText = image.getString(j, i);
				}
				try {
					double[] axisPnt = imageTrace.getPointInAxisCoordinates(new double[] { xCoordinate, yCoordinate });
					xCoordinate = axisPnt[flip ? 1 : 0];
					yCoordinate = axisPnt[flip ? 0 : 1];
				} catch (Exception ignored) {
					// It is not fatal for the custom axes not to work.
				}
			} else {
				double[] globalRange = imageTrace.getGlobalRange();
				int x = (int) Math.floor((xCoordinate - globalRange[0]) / (globalRange[1] - globalRange[0]) * shape[1]);
				int y = (int) Math.floor((yCoordinate - globalRange[2]) / (globalRange[3] - globalRange[2]) * shape[0]);
				if (!(x < 0 || y < 0 || x >= shape[1] || y >= shape[0])) {
					double intensity = image.getDouble(y, x);
					if (!Double.isNaN(intensity)) {
						intensityText = image.getString(y, x);
					}
				}
			}
		}

		StringBuilder buf = new StringBuilder();
		if (intensityText != null) {
			buf.append(intensityText);
		}
		buf.append("\n[");
		buf.append(Double.isNaN(xCoordinate) ? "--" : xAxis.format(xCoordinate, EXTRA_DECIMAL_PLACES));
		buf.append(", ");
		buf.append(Double.isNaN(yCoordinate) ? "--" : yAxis.format(yCoordinate, EXTRA_DECIMAL_PLACES));
		buf.append("]");

		return createCursor(Display.getDefault(), buf.toString());
	}

	private static Cursor createCursor(Display display, String text) {
		Dimension size = FigureUtilities.getTextExtents(text, display.getSystemFont());
		int width = size.width + CURSOR_SIZE;
		int height = size.height + CURSOR_SIZE;
		Image image;

		if (TRANSPARENT_COLOR == null) {
			ImageData initImageData = new ImageData(width, height, 32, new PaletteData(0xFF0000, 0xFF00, 0xFF));
			initImageData.alphaData = new byte[width * height];
			image = new Image(display, initImageData);
		} else {
			image = new Image(display, width, height);
		}
		GC gc = new GC(image);
		try {
			gc.setAdvanced(true);
			gc.setTextAntialias(SWT.ON);
			gc.setAntialias(SWT.ON);

			// create transparent background
			if (TRANSPARENT_COLOR == null) {
				gc.setBackground(WHITE_COLOR);
				gc.setAlpha(0);
				gc.fillRectangle(0, 0, width, height);
				gc.setAlpha(255);
			} else {
				gc.setBackground(TRANSPARENT_COLOR);
				gc.fillRectangle(0, 0, width, height);
				gc.setBackground(WHITE_COLOR);
			}

			// Draw dotted cross
			gc.setForeground(BLACK_COLOR);
			final PointList x = new PointList(CURSOR_SIZE);
			final PointList y = new PointList(CURSOR_SIZE);
			for (int i = 0; i < CURSOR_SIZE; i++) {
				x.addPoint(new Point(i, CURSOR_SIZE/2));
				y.addPoint(new Point(CURSOR_SIZE/2, i));
			}
			drawPointList(x, gc, false);
			drawPointList(y, gc, true);

			// Draw text with background
			gc.setForeground(WHITE_COLOR);
			gc.fillRectangle(CURSOR_SIZE, CURSOR_SIZE, size.width, size.height);
			gc.setForeground(BLACK_COLOR);
			gc.drawText(text, CURSOR_SIZE, CURSOR_SIZE);

			ImageData imageData = image.getImageData();
			if (TRANSPARENT_COLOR != null) {
				// this triggers a mask created from pixels with this color for the cursor
				imageData.transparentPixel = imageData.palette.getPixel(TRANSPARENT_COLOR.getRGB());
			}
			return new Cursor(display, imageData, CURSOR_SIZE/2 ,CURSOR_SIZE/2);
		} finally {
			gc.dispose();
			image.dispose();
		}
	}

	private static void drawPointList(PointList pl, GC gc, boolean isY) {
		int xOff = isY ? 0 : 1;
		int yOff = isY ? 1 : 0;
		for (int i = 0; i < pl.size(); i++) {
			gc.setForeground((i % 2 == 0) ? BLACK_COLOR : WHITE_COLOR);
			Point p = pl.getPoint(i);
			gc.drawLine(p.x, p.y, p.x + xOff, p.y + yOff);
		}
	}

	public static void main(String[] args) {
		final int width = 300;
		final int height = 300;

		Display display = Display.getDefault();

		Shell shell = new Shell(display, SWT.SHELL_TRIM | SWT.DOUBLE_BUFFERED);
		shell.setText("Cursor Test");
		Cursor cursor = createCursor(display, "Hello");
		shell.addListener(SWT.Paint, e -> {
			e.gc.setBackground(BLACK_COLOR);
			e.gc.fillRectangle(0, 0, width, height);
			e.gc.setBackground(WHITE_COLOR);
			e.gc.fillRectangle(0, height, width, height);
		});
		shell.setCursor(cursor);
		shell.setSize (width, 2*height);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		cursor.dispose();
		display.dispose();
	}
}
