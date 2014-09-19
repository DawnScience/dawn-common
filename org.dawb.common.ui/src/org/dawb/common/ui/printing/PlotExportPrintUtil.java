/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawb.common.ui.printing;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.SimpleDoc;
import javax.print.StreamPrintService;
import javax.print.StreamPrintServiceFactory;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dawb.common.ui.util.DisplayUtils;
import org.eclipse.gmf.runtime.draw2d.ui.render.awt.internal.svg.export.GraphicsSVG;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Utility class for exporting any active plotting area as an image file
 * 
 * @author Baha El Kassaby
 */
@SuppressWarnings("restriction")
public class PlotExportPrintUtil {

	public static final String[] FILE_TYPES = new String[] { "PNG/JPEG File", "Postscript File", "SVG File" };
	public static final String[] FILE_FORMATS = new String[] { "png", "jpg", "jpeg", "ps", "eps", "svg" };

	private static final Logger logger = LoggerFactory.getLogger(PlotExportPrintUtil.class);
	private static final String tempDirectory = System.getProperty("java.io.tmpdir");

	private static void savePostScript(File imageFile, Image image)
			throws FileNotFoundException {

		ByteArrayOutputStream os = new ByteArrayOutputStream();

		//convert SWT to AWT
		RenderedImage awtImage = convertToAWT(image.getImageData());
		try {
			ImageIO.write(awtImage, "png", os);
		} catch (IOException e) {
			logger.error("Could not write to OutputStream",e);
		}

		try {
			// Open the image file
			ByteArrayInputStream inputStream = new ByteArrayInputStream(os.toByteArray());
			InputStream is = new BufferedInputStream(inputStream);

			// Prepare the output file to receive the postscript
			OutputStream fos = new BufferedOutputStream(new FileOutputStream(
					imageFile.getAbsolutePath()));

			// Find a factory that can do the conversion
			DocFlavor flavor = DocFlavor.INPUT_STREAM.GIF;
			StreamPrintServiceFactory[] factories = StreamPrintServiceFactory.lookupStreamPrintServiceFactories
					(flavor, DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType());

			if (factories.length > 0) {
				StreamPrintService service = factories[0].getPrintService(fos);
				// Create the print job
				DocPrintJob job = service.createPrintJob();
				Doc doc = new SimpleDoc(is, flavor, null);
				// Monitor print job events;
				PrintJobWatcher pjDone = new PrintJobWatcher(job);
				// Print it
				job.print(doc, null);
				// Wait for the print job to be done
				pjDone.waitUntilDone();
				// It is now safe to close the streams
			}
			is.close();
			fos.close();
		} catch (PrintException e) {
			MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Warning", "The Postscript conversion failed: "+e.getMessage());
			logger.error("Could not print to PostScript", e);
		} catch (IOException e) {
			logger.error("IO error", e);
		}
	}

	/**
	 * Save a Draw2D figure as Scalable Vector Graphics to an output stream.
	 * Should be run in a UI thread
	 * @param root the figure to draw
	 * @param file the file to write the SVG DOM to
	 * @param monitor
	 * @throws Exception
	 */
	public static IStatus saveSVG(IFigure root, File file, IProgressMonitor monitor)
			throws IOException {
		GraphicsSVG graphics = null;
		OutputStream out = null;
		try {
			out = new FileOutputStream(file);
			final Rectangle viewBox = root.getBounds().getCopy();
			graphics = GraphicsSVG.getInstance(viewBox);
			// paint figure
			root.paint(graphics);
			Element svgRoot = graphics.getRoot();

			if (monitor != null && monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			// Define the view box
			svgRoot.setAttributeNS(null, "viewBox",
					String.valueOf(viewBox.x) + " " + String.valueOf(viewBox.y)
							+ " " + String.valueOf(viewBox.width) + " "
							+ String.valueOf(viewBox.height));

			if (monitor != null && monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			// Write the document to the stream
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");

			if (monitor != null && monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}

			DOMSource source = new DOMSource(svgRoot);
			StreamResult result = new StreamResult(out);

			transformer.transform(source, result);
			return Status.OK_STATUS;
		} catch (Exception e) {
			logger.debug(e.getMessage());
			MessageDialog.openWarning(Display.getDefault().getActiveShell(), "Warning", "The SVG conversion failed: "+e.getMessage());
			return Status.CANCEL_STATUS;
		} finally {
			if (graphics != null)
				graphics.dispose();
			if (out != null) {
				out.close();
			}
		}
	}

	private static BufferedImage convertToAWT(ImageData data) {
		ColorModel colorModel = null;
		PaletteData palette = data.palette;
		if (palette.isDirect) {
			colorModel = new DirectColorModel(data.depth, palette.redMask, palette.greenMask, palette.blueMask);
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					RGB rgb = palette.getRGB(pixel);
					pixelArray[0] = rgb.red;
					pixelArray[1] = rgb.green;
					pixelArray[2] = rgb.blue;
					raster.setPixels(x, y, 1, 1, pixelArray);
				}
			}
			return bufferedImage;
		} else {
			RGB[] rgbs = palette.getRGBs();
			byte[] red = new byte[rgbs.length];
			byte[] green = new byte[rgbs.length];
			byte[] blue = new byte[rgbs.length];
			for (int i = 0; i < rgbs.length; i++) {
				RGB rgb = rgbs[i];
				red[i] = (byte) rgb.red;
				green[i] = (byte) rgb.green;
				blue[i] = (byte) rgb.blue;
			}
			if (data.transparentPixel != -1) {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red,
						green, blue, data.transparentPixel);
			} else {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue);
			}
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(data.width,
							data.height), false, null);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					pixelArray[0] = pixel;
					raster.setPixel(x, y, pixelArray);
				}
			}
			return bufferedImage;
		}
	}

	/**
	 * Save the graph with the given filename. If the file name ends with a
	 * known extension, this is used as the file type otherwise it is the string
	 * passed in which is read from the save as dialog form normally.
	 * 
	 * @param filename
	 *            the name under which the graph should be saved
	 * @param fileType
	 *            type of the file
	 * @param image
	 * @param printableFigure
	 *          IFigure used to export to svg
	 * @throws Exception
	 */
	public synchronized static void saveGraph(String filename, String fileType, Image image, final IFigure printableFigure) 
			throws Exception {
		// test for all possible file types
		if (!Arrays.asList(FILE_FORMATS).contains(fileType.toLowerCase())
				&& !Arrays.asList(FILE_TYPES).contains(fileType))
			throw new RuntimeException("Cannot deal with file type " + fileType);
		// If they have specified the file type in the file name, use that.
		String lname = filename.toLowerCase();
		if (lname.endsWith(".png") || lname.endsWith(".jpg") || lname.endsWith(".jpeg"))
			fileType = FILE_TYPES[0];
		if (lname.endsWith(".ps") || lname.endsWith(".eps"))
			fileType = FILE_TYPES[1];
		if (lname.endsWith(".svg"))
			fileType = FILE_TYPES[2];
		if (fileType.equals(FILE_TYPES[0])) {
			if (!lname.endsWith(".png") && !lname.endsWith(".jpg") && !lname.endsWith(".jpeg"))
				filename = filename + ".png";
			ImageLoader loader = new ImageLoader();
			loader.data = new ImageData[] { image.getImageData() };
			loader.save(filename, SWT.IMAGE_PNG);
		} else if (fileType.equals(FILE_TYPES[1])) {
			if (!lname.endsWith(".ps") && !lname.endsWith(".eps"))
				filename = filename + ".ps";
			savePostScript(new File(filename), image);
		} else if (fileType.equals(FILE_TYPES[2])) {
			if (!lname.endsWith(".svg"))
				filename = filename + ".svg";
			final File file = new File(filename);
			// save to SVG process in a Job
			Job svgJob = new Job("Exporting to SVG") {
				IStatus result = null;
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					DisplayUtils.runInDisplayThread(true, false, null, new Runnable() {
						@Override
						public void run() {
							try {
								result = saveSVG(printableFigure, file, monitor);
							} catch (IOException e) {
								result = Status.CANCEL_STATUS;
								logger.debug("Error writing to file:"+e.toString());
							}
						}
					});
					if (result == null)
						result = Status.CANCEL_STATUS;
					return result;
				}
			};
			svgJob.setUser(true);
			svgJob.schedule();
		} else {
			throw new RuntimeException("Cannot process " + fileType);
		}
	}

	private static void copytoClipboard(Image image) {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IWorkbenchPart active = page.getActivePart();

		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { image.getImageData() };
		loader.save(tempDirectory + "/" + active.getTitle() + ".png", SWT.IMAGE_PNG);

		// we read the new image created
		File imageFile = new File(tempDirectory + "/" + active.getTitle() + ".png");

		// copy temp file created to clipboard
		Display display = Display.getCurrent();
		Clipboard clipboard = new Clipboard(display);
		String[] data = { imageFile.getAbsolutePath() };
		clipboard.setContents(new Object[] { data }, new Transfer[] { FileTransfer.getInstance() });
		clipboard.dispose();
		logger.debug("Plot copied to clip-board");
	}

	/**
	 * Copy the graph to the clip board.
	 * 
	 * @param image
	 */
	public static synchronized void copyGraph(Image image) {
		copytoClipboard(image);
	}
}

/**
 * Class used to monitor print job events
 *
 */
class PrintJobWatcher {
	// true if it is safe to close the print job's input stream
	boolean done = false;

	public PrintJobWatcher(DocPrintJob job) {
		// Add a listener to the print job
		job.addPrintJobListener(new PrintJobAdapter() {
			public void printJobCanceled(PrintJobEvent pje) {
				allDone();
			}

			public void printJobCompleted(PrintJobEvent pje) {
				allDone();
			}

			public void printJobFailed(PrintJobEvent pje) {
				allDone();
			}

			public void printJobNoMoreEvents(PrintJobEvent pje) {
				allDone();
			}

			void allDone() {
				synchronized (PrintJobWatcher.this) {
					done = true;
					PrintJobWatcher.this.notify();
				}
			}
		});
	}

	public synchronized void waitUntilDone() {
		try {
			while (!done) {
				wait();
			}
		} catch (InterruptedException e) {
		}
	}
}
