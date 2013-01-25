package org.dawb.common.ui.image;

import org.dawb.common.ui.image.IconUtils.ShapeType;
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

		final ImageData data  = new ImageData(16, 16, 8, makeGrayScalePalette());
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

		final ImageData data  = new ImageData(16, 16, 8, makeGrayScalePalette());
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

	public static ImageDescriptor getBrushIcon(int size, ShapeType shape) {
		
		final ImageData data  = new ImageData(16, 16, 8, makeGrayScalePalette());
        final Image     image = new Image(Display.getCurrent(), data);
        
        final GC gc = new GC(image);
       
        gc.setBackground(Display.getDefault().getActiveShell().getBackground());
        gc.fillRectangle(new Rectangle(0,0,16,16));
        gc.setForeground(ColorConstants.gray);
        gc.setLineWidth(2);
        switch (shape) {
        case SQUARE:
            gc.drawRectangle(2,2,size,size);
            break;
        case TRIANGLE:
        	final PointList pl = new PointList();
        	pl.addPoint(2+(size/2), 2);
           	pl.addPoint(2+size, 2+size);
           	pl.addPoint(2, 2+size);
            gc.drawPolygon(pl.toIntArray());
            break;
        case CIRCLE:
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

	
	public static PaletteData makeGrayScalePalette() {
		RGB grayscale[] = new RGB[256];
		for (int i = 0; i < 256; i++) {
			grayscale[i] = new RGB(i, i, i);
		}
		return new PaletteData(grayscale);
	}

}
