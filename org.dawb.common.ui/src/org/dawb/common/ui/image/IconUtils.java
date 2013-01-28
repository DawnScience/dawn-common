package org.dawb.common.ui.image;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public class IconUtils {
	
	public enum ShapeType {
		SQUARE, TRIANGLE, CIRCLE;
	}

	public static ImageDescriptor createIconDescriptor(String iconText) {

		final ImageData data  = new ImageData(16, 16, 8, new PaletteData(0xFF, 0xFF00, 0xFF0000));
        final Image     image = new Image(Display.getCurrent(), data);
        
        final GC gc = new GC(image);
       
        gc.setForeground(ColorConstants.white);
        gc.fillRectangle(new Rectangle(0,0,16,16));
        gc.setForeground(ColorConstants.darkGray);
        gc.setFont(new Font(Display.getCurrent(), new FontData("Dialog", 6, SWT.BOLD)));
        gc.drawText(iconText, 4, 2);
        
        gc.dispose();
		
		return new ImageDescriptor() {			
			@Override
			public ImageData getImageData() {
				return image.getImageData();
			}
		};
	}


	public static ImageDescriptor createPenDescriptor(int penSize) {

		final ImageData data  = new ImageData(16, 16, 8, new PaletteData(0xFF, 0xFF00, 0xFF0000));
        final Image     image = new Image(Display.getCurrent(), data);
        
        final GC gc = new GC(image);
       
        gc.setBackground(Display.getDefault().getActiveShell().getBackground());
        gc.fillRectangle(new Rectangle(0,0,16,16));
        gc.setBackground(ColorConstants.gray);
        gc.fillRectangle(2, 2, penSize, penSize);
        
        gc.dispose();
		
		return new ImageDescriptor() {			
			@Override
			public ImageData getImageData() {
				return image.getImageData();
			}
		};
	}
	

	/**
	 * Icon for the cursor 
	 * @param pensize
	 * @param square
	 * @return
	 */
	public static ImageDescriptor getPenCursorIcon(int pensize, ShapeType shape) {

		final Image image = new Image(Display.getCurrent(), new Rectangle(0, 0, pensize+4, pensize+4));
		final GC    gc    = new GC(image);

		gc.setBackground(Display.getDefault().getActiveShell().getBackground());
		gc.setAlpha(0);
		gc.fillRectangle(new Rectangle(0,0,pensize+4,pensize+4));

		gc.setAlpha(255);
		gc.setForeground(ColorConstants.black);

		switch (shape) {
		case SQUARE:
			gc.drawRectangle(2,2,pensize,pensize);
			break;
		case TRIANGLE:
			final PointList pl = new PointList();
			pl.addPoint(2+(pensize/2), 2);
			pl.addPoint(2+pensize, 2+pensize);
			pl.addPoint(2, 2+pensize);
			gc.drawPolygon(pl.toIntArray());
			break;
		case CIRCLE:
			gc.drawOval(2,2,pensize,pensize);
			break;
		}


		gc.dispose();

		return new ImageDescriptor() {			
			@Override
			public ImageData getImageData() {
				return image.getImageData();
			}
		};

	}


	/**
	 * Icon for the actions to choose brush type.
	 * @param size
	 * @param shape
	 * @param maskRGB
	 * @return
	 */
	public static ImageDescriptor getBrushIcon(int size, ShapeType shape, RGB maskRGB) {
		
		final ImageData data  = new ImageData(16, 16, 16, new PaletteData(0xFF, 0xFF00, 0xFF0000));
        final Image     image = new Image(Display.getCurrent(), data);
        
        final GC gc = new GC(image);
       
        gc.setBackground(Display.getDefault().getActiveShell().getBackground());
        gc.fillRectangle(new Rectangle(0,0,16,16));
        gc.setForeground(ColorConstants.gray);
        gc.setLineWidth(2);
		if (maskRGB!=null) {
			final Color     maskColour = new Color(null, maskRGB);
	        gc.setBackground(maskColour);
		}
        switch (shape) {
        case SQUARE:
            if (maskRGB!=null) gc.fillRectangle(2,2,size,size);
            gc.drawRectangle(2,2,size,size);
            break;
        case TRIANGLE:
        	final PointList pl = new PointList();
        	pl.addPoint(2+(size/2), 2);
           	pl.addPoint(2+size, 2+size);
           	pl.addPoint(2, 2+size);
           	if (maskRGB!=null) gc.fillPolygon(pl.toIntArray());
            gc.drawPolygon(pl.toIntArray());
            break;
        case CIRCLE:
        	if (maskRGB!=null) gc.fillOval(2,2,size,size);
            gc.drawOval(2,2,size,size);
            break;
       }
            
        
        gc.dispose();
		
		return new ImageDescriptor() {			
			@Override
			public ImageData getImageData() {
				return image.getImageData();
			}
		};
	}

	
}
