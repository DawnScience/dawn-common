package org.dawb.common.ui.image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.draw2d.ColorConstants;

public class IconUtils {

	public static ImageDescriptor createIconDescriptor(String iconText) {

		final ImageData data  = new ImageData(16, 16, 8, PaletteFactory.makeGrayScalePalette());
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

}
