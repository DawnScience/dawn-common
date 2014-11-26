/*
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.printing;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.*;
import java.io.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copy BufferedImage to AWT clip-board
 *
 */
public class ImageToAWTClipboard implements ClipboardOwner {

	private static Logger logger = LoggerFactory.getLogger(ImageToAWTClipboard.class);

	public ImageToAWTClipboard(BufferedImage i) {
		TransferableImage trans = new TransferableImage(i);
		Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
		c.setContents(trans, this);
	}

	@Override
	public void lostOwnership(Clipboard clip, Transferable trans) {
		logger.debug("Lost Clipboard Ownership");
	}

	private class TransferableImage implements Transferable {

		Image image;

		public TransferableImage(Image image) {
			this.image = image;
		}

		@Override
		public Object getTransferData(DataFlavor flavor)
				throws UnsupportedFlavorException, IOException {
			if (flavor.equals(DataFlavor.imageFlavor) && image != null) {
				return image;
			} else {
				throw new UnsupportedFlavorException(flavor);
			}
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			DataFlavor[] flavors = new DataFlavor[1];
			flavors[0] = DataFlavor.imageFlavor;
			return flavors;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			DataFlavor[] flavors = getTransferDataFlavors();
			for (int i = 0; i < flavors.length; i++) {
				if (flavor.equals(flavors[i])) {
					return true;
				}
			}
			return false;
		}
	}
}