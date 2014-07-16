/*-
 * Copyright (c) 2014 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.boofcv.converter;

import georegression.struct.point.Point2D_I32;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ByteDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
import uk.ac.diamond.scisoft.analysis.dataset.RGBDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.misc.GImageStatistics;
import boofcv.alg.misc.ImageStatistics;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageDataType;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageFloat64;
import boofcv.struct.image.ImageInt16;
import boofcv.struct.image.ImageInteger;
import boofcv.struct.image.ImageSInt16;
import boofcv.struct.image.ImageSInt32;
import boofcv.struct.image.ImageSInt64;
import boofcv.struct.image.ImageSInt8;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt16;
import boofcv.struct.image.ImageUInt8;
import boofcv.struct.image.InterleavedS16;
import boofcv.struct.image.MultiSpectral;

/**
 * Functions for converting to and from {@link IDataset}.
 *
 */
public class ConvertIDataset {

	/**
	 * Converts the IDataset image into an {@link ImageBase}.
	 *
	 * @param src Input IDataset.
	 * @param shareData if true, share where possible (i.e. not a boolean dataset) 
	 * @return Converted image.
	 */
	public static ImageBase<?> convertFrom(IDataset src, boolean shareData) {
		if(src.getShape().length != 2)
			throw new IllegalArgumentException("The dataset has to be a 2 dimensionnal array");
		Class<?> type = src.getClass();

		Dataset sd;
		if (src instanceof Dataset) {
			if (!shareData && !(src instanceof BooleanDataset)) {
				sd = ((Dataset) src).clone();
			} else {
				sd = (Dataset) src;
			}
		} else {
			sd = DatasetUtils.convertToDataset(src);
		}

		int height = sd.getShapeRef()[0];
		int width = sd.getShapeRef()[1];

		if (sd instanceof BooleanDataset) {
			ImageSInt8 dst = new ImageSInt8(width, height);
			dst.data = ((ByteDataset) sd.cast(Dataset.INT8)).getData();
			return dst;
		} else if (sd instanceof ByteDataset) {
			ImageSInt8 dst = new ImageSInt8(width, height);
			dst.data = ((ByteDataset) sd).getData();
			return dst;
		} else if(sd instanceof ShortDataset) {
			ImageSInt16 dst = new ImageSInt16(width, height);
			dst.data = ((ShortDataset) sd).getData();
			return dst;
		} else if (sd instanceof IntegerDataset) {
			ImageSInt32 dst = new ImageSInt32(width, height);
			dst.data = ((IntegerDataset) sd).getData();
			return dst;
		} else if (sd instanceof LongDataset) {
			ImageSInt64 dst = new ImageSInt64(width, height);
			dst.data = ((LongDataset) sd).getData();
			return dst;
		} else if (sd instanceof FloatDataset) {
			ImageFloat32 dst = new ImageFloat32(width, height);
			dst.data = ((FloatDataset) sd).getData();
			return dst;
		} else if (sd instanceof DoubleDataset) {
			ImageFloat64 dst = new ImageFloat64(width, height);
			dst.data = ((DoubleDataset) sd).getData();
			return dst;
		} else if (sd instanceof RGBDataset) {
			InterleavedS16 dst = new InterleavedS16(width, height, 3);
			dst.data = ((RGBDataset) sd).getData();
			return dst;
		} else {
			throw new IllegalArgumentException("Unknown type " + type);
		}
	}

	private static Map<Class <? extends ImageBase<?>>, Class<?>> imageToElementClass;

	static {
		imageToElementClass = new HashMap<Class<? extends ImageBase<?>>, Class<?>>();
		for (ImageDataType i : ImageDataType.values()) {
			imageToElementClass.put(ImageDataType.typeToClass(i), i.getDataType());
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends ImageBase<?>> T convertFrom(IDataset src, Class<T> clazz, int bands) {
		Dataset ds;
		int ddt = AbstractDataset.getDTypeFromClass(imageToElementClass.get(clazz), bands);
		if (AbstractDataset.getDType(src) != ddt) {
			ds = DatasetUtils.cast(src, ddt);
		} else {
			ds = DatasetUtils.convertToDataset(src);
		}

		return (T) convertFrom(ds, true);
	}

	/**
	 * Converts a {@link ImageBase} into an IDataset
	 *
	 * @param src Input image.
	 * @param isBinary if true will convert to a binary image
	 * @return Converted image.
	 */
	public static <T extends ImageBase<?>> IDataset convertTo(T src, boolean isBinary) {
		if (src instanceof ImageUInt8 || src instanceof ImageSInt8) {
			Dataset dst = new ByteDataset(((ImageUInt8) src).data, src.height, src.width);
			if (isBinary) {
				dst = dst.cast(Dataset.BOOL);
			}
			return dst;
		} else if (src instanceof ImageUInt16 || src instanceof ImageSInt16) {
			Dataset dst = new ShortDataset(((ImageInt16<?>) src).data, src.height, src.width);
			return dst;
		} else if (src instanceof ImageSInt32) {
			Dataset dst = new IntegerDataset(((ImageSInt32) src).data, src.height, src.width);
			return dst;
		} else if (src instanceof ImageSInt64) {
			Dataset dst = new LongDataset(((ImageSInt64) src).data, src.height, src.width);
			return dst;
		} else if (src instanceof ImageFloat32) {
			Dataset dst = new FloatDataset(((ImageFloat32) src).data, src.height, src.width);
			return dst;
		} else if (src instanceof ImageFloat64) {
			Dataset dst = new DoubleDataset(((ImageFloat64) src).data, src.height, src.width);
			return dst;
		} else if (src instanceof MultiSpectral) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			MultiSpectral<ImageSingleBand> msrc = (MultiSpectral<ImageSingleBand>) src;
			int n = msrc.getNumBands();
			Dataset[] datasets = new Dataset[n];
			for (int i = 0; i < n; i++) {
				datasets[i] = (Dataset) convertTo(msrc.getBand(i), isBinary);
			}
			return DatasetUtils.createCompoundDataset(datasets);
		}
		throw new IllegalArgumentException("Image type is not yet supported: "+src.getClass().getSimpleName());
	}

	/**
	 * Converts a {@link boofcv.struct.image.ImageSInt32} into an IDataset.  If the buffered image
	 * has multiple channels the intensities of each channel are averaged together.
	 *
	 * @param src Input image.
	 * @param numColors
	 * @return dst Where the converted image is written to
	 */
	public static IDataset imageToIDataset(ImageSInt32 src, int numColors) {
		final int width = src.getWidth();
		final int height = src.getHeight();
		int[] data = src.data;

		if (numColors == 0)
			return new IntegerDataset(data, width, height);

		else {
			int colors[] = new int[numColors + 1];

			Random rand = new Random(123); // FIXME WTF?
			for( int i = 0; i < colors.length; i++ ) {
				colors[i] = rand.nextInt();
			}
			colors[0] = 0;
			RGBDataset dst = new RGBDataset(width, height);
			for( int y = 0; y < height; y++ ) {
				int indexSrc = src.startIndex + y*src.stride;
				for( int x = 0; x < width; x++ ) {
					int value = colors[src.data[indexSrc++]];
					dst.set(value, x, y);
				}
			}
			return dst;
		}
	}

	/**
	 * Draws contours. Internal and external contours are different user specified colors.
	 *
	 * @param contours List of contours
	 * @param colorExternal RGB color
	 * @param colorInternal RGB color
	 * @param width Image width
	 * @param height Image height
	 * @param out (Optional) storage for output image
	 * @return Rendered contours
	 */
	public static IDataset contourImageToIDataset(List<Contour> contours,
			int colorExternal, int colorInternal, int width, int height) {

		RGBDataset out = new RGBDataset(width, height);
		for( Contour c : contours ) {
			for(Point2D_I32 p : c.external ) {
//				out.setRGB(p.x,p.y,colorExternal);
				out.set(colorExternal, p.x, p.y);
			}
			for( List<Point2D_I32> l : c.internal ) {
				for( Point2D_I32 p : l ) {
//					out.setRGB(p.x,p.y,colorInternal);
					out.set(colorInternal, p.x, p.y);
				}
			}
		}
		return out;
	}

	/**
	 * <p>
	 * Renders a colored image where the color indicates the sign and intensity its magnitude.   The input is divided
	 * by normalize to render it in the appropriate scale.
	 * </p>
	 *
	 * @param src       Input single band image.
	 * @param dst       Where the image is rendered into.  If null a new BufferedImage will be created and return.
	 * @param normalize Used to normalize the input image. If <= 0 then the max value will be used
	 * @return Rendered image.
	 */
	public static IDataset colorizeSign(ImageSingleBand<?> src, double normalize) {
		// TODO (use RGBDataset for colors support...)
		IDataset dst = new RGBDataset(src.getWidth(), src.getHeight());

		if (normalize <= 0) {
			normalize = GImageStatistics.maxAbs(src);
		}

		if (normalize == 0) {
			// sets the output to black
			convertTo(src, true);
			return dst;
		}

		if (src.getClass().isAssignableFrom(ImageFloat32.class)) {
			return colorizeSign((ImageFloat32) src, dst, (float) normalize);
		} else {
			return colorizeSign((ImageInteger<?>) src, dst, (int) normalize);
		}
	}

	private static IDataset colorizeSign(ImageInteger<?> src, IDataset dst, int normalize) {
		for (int y = 0; y < src.height; y++) {
			for (int x = 0; x < src.width; x++) {
				int v = src.get(x, y);

				int rgb;
				if (v > 0) {
					rgb = ((255 * v / normalize) << 16);
				} else {
					rgb = -((255 * v / normalize) << 8);
				}
				dst.set(rgb,  x, y);
			}
		}
		return dst;
	}

	private static IDataset colorizeSign(ImageFloat32 src, IDataset dst, float maxAbsValue) {
		for (int y = 0; y < src.height; y++) {
			for (int x = 0; x < src.width; x++) {
				float v = src.get(x, y);

				int rgb;
				if (v > 0) {
					rgb = (int) (255 * v / maxAbsValue) << 16;
				} else {
					rgb = (int) (-255 * v / maxAbsValue) << 8;
				}
				dst.set(rgb, x, y);
			}
		}
		return dst;
	}

	public static IDataset graySign(ImageFloat32 src, float maxAbsValue) {
		IDataset dst = new IntegerDataset(src.getWidth(), src.getHeight());

		if (maxAbsValue < 0)
			maxAbsValue = ImageStatistics.maxAbs(src);

		for (int y = 0; y < src.height; y++) {
			for (int x = 0; x < src.width; x++) {
				float v = src.get(x, y);

				int rgb = 127 + (int) (127 * v / maxAbsValue);

				dst.set(rgb << 16 | rgb << 8 | rgb, x, y);
			}
		}

		return dst;
	}

	public static IDataset grayMagnitude(ImageFloat32 src, IDataset dst, float maxAbsValue) {
		for (int y = 0; y < src.height; y++) {
			for (int x = 0; x < src.width; x++) {
				float v = Math.abs(src.get(x, y));

				int rgb = (int) (255 * v / maxAbsValue);

				dst.set(rgb << 16 | rgb << 8 | rgb, x, y);
			}
		}

		return dst;
	}

}
