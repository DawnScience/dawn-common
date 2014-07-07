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

import java.util.List;
import java.util.Random;

import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ByteDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Dataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.RGBDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
import boofcv.alg.filter.binary.Contour;
import boofcv.core.image.GeneralizedImageOps;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageFloat64;
import boofcv.struct.image.ImageInt16;
import boofcv.struct.image.ImageSInt32;
import boofcv.struct.image.ImageSingleBand;
import boofcv.struct.image.ImageUInt8;
import boofcv.struct.image.MultiSpectral;

/**
 * Functions for converting to and from {@link IDataset}.
 *
 */
public class Converter {

	public Converter() {
	}

	/**
	 * Converts a IDataset into an image of the specified type.
	 * 
	 * @param src Input IDataset which is to be converted
	 * @param dst The image which it is being converted into
	 * @param orderRgb If applicable, should it adjust the ordering of each color band to maintain color consistency
	 */
//	public static <T extends ImageBase> T convertFrom(IDataset src, boolean orderRgb) {
//		if( dst instanceof ImageSingleBand ) {
//			ImageSingleBand sb = (ImageSingleBand)dst;
//		return convertFromSingle(src);
//		} else if( dst instanceof MultiSpectral ) {
//			MultiSpectral ms = (MultiSpectral)dst;
//			convertFromMulti(src,ms,orderRgb,ms.getType());
//		} else {
//			throw new IllegalArgumentException("Unknown type " + dst.getClass().getSimpleName());
//		}
//	}

	/**
	 * Converts the IDataset into an {@link boofcv.struct.image.MultiSpectral} image of the specified
	 * type. 
	 *
	 * @param src Input image. Not modified.
	 * @param dst Output. The converted image is written to.  If null a new unsigned image is created.
	 * @param orderRgb If applicable, should it adjust the ordering of each color band to maintain color consistency.
	 *                 Most of the time you want this to be true.
	 * @param type Which type of data structure is each band. (ImageUInt8 or ImageFloat32)
	 * @return Converted image.
	 */
	/*public static <T extends ImageSingleBand> MultiSpectral<T> convertFromMulti(
			IDataset src, MultiSpectral<T> dst, boolean orderRgb,
			Class<T> type) {
		if (src == null)
			throw new IllegalArgumentException("src is null!");

		if (dst != null) {
			if (src.getShape()[0] != dst.getWidth() || src.getShape()[1] != dst.getHeight()) {
				throw new IllegalArgumentException("image dimension are different");
			}
		}
		src.elementClass().;
		try {
			WritableRaster raster = src.getRaster();

			int numBands;
			if( src.getType() == BufferedImage.TYPE_BYTE_INDEXED )
				numBands = 3;
			else
				numBands = raster.getNumBands();

			if( dst == null)
				dst = new MultiSpectral<T>(type,src.getWidth(),src.getHeight(),numBands);
			else if( dst.getNumBands() != numBands )
				throw new IllegalArgumentException("Expected "+numBands+" bands in dst not "+dst.getNumBands());

			if( type == ImageUInt8.class ) {
				if (src.getRaster() instanceof ByteInterleavedRaster &&
						src.getType() != BufferedImage.TYPE_BYTE_INDEXED ) {
					if( src.getType() == BufferedImage.TYPE_BYTE_GRAY)  {
						for( int i = 0; i < dst.getNumBands(); i++ )
							ConvertRaster.bufferedToGray(src, ((MultiSpectral<ImageUInt8>) dst).getBand(i));
					} else {
						ConvertRaster.bufferedToMulti_U8((ByteInterleavedRaster) src.getRaster(), (MultiSpectral<ImageUInt8>)dst);
					}
				} else if (src.getRaster() instanceof IntegerInterleavedRaster) {
					ConvertRaster.bufferedToMulti_U8((IntegerInterleavedRaster) src.getRaster(), (MultiSpectral<ImageUInt8>)dst);
				} else {
					ConvertRaster.bufferedToMulti_U8(src, (MultiSpectral<ImageUInt8>)dst);
				}
			} else if( type == ImageFloat32.class ) {
				if (src.getRaster() instanceof ByteInterleavedRaster &&
						src.getType() != BufferedImage.TYPE_BYTE_INDEXED  ) {
					if( src.getType() == BufferedImage.TYPE_BYTE_GRAY)  {
						for( int i = 0; i < dst.getNumBands(); i++ )
							ConvertRaster.bufferedToGray(src,((MultiSpectral<ImageFloat32>)dst).getBand(i));
					} else {
						ConvertRaster.bufferedToMulti_F32((ByteInterleavedRaster) src.getRaster(), (MultiSpectral<ImageFloat32>)dst);
					}
				} else if (src.getRaster() instanceof IntegerInterleavedRaster) {
					ConvertRaster.bufferedToMulti_F32((IntegerInterleavedRaster) src.getRaster(), (MultiSpectral<ImageFloat32>)dst);
				} else {
					ConvertRaster.bufferedToMulti_F32(src, (MultiSpectral<ImageFloat32>)dst);
				}
			} else {
				throw new IllegalArgumentException("Band type not supported yet");
			}

		} catch( java.security.AccessControlException e) {
			// Applets don't allow access to the raster()
			if( dst == null )
				dst = new MultiSpectral<T>(type,src.getWidth(),src.getHeight(),3);

			if( type == ImageUInt8.class ) {
				ConvertRaster.bufferedToMulti_U8(src, (MultiSpectral<ImageUInt8>)dst);
			} else if( type == ImageFloat32.class ) {
				ConvertRaster.bufferedToMulti_F32(src, (MultiSpectral<ImageFloat32>)dst);
			}
		}

		// if requested, ensure the ordering of the bands
		if( orderRgb ) {
			orderBandsIntoRGB(dst,src);
		}

		return dst;
	}
*/

	/**
	 * Converts the IDataset image into an {@link boofcv.struct.image.ImageUInt8}.  If the IDataset
	 * has multiple channels the intensities of each channel are averaged together.
	 *
	 * @param src Input IDataset.
	 * @param dst Where the converted image is written to.  If null a new unsigned image is created.
	 * @return Converted image.
	 */
	public <T extends ImageBase<?>> T convertFrom(IDataset src) {
		if(src.getShape().length != 2)
			throw new IllegalArgumentException("The dataset has to be a 2 dimensionnal array");
		Class<?> type = src.getClass();
		int width = src.getShape()[0];
		int height = src.getShape()[1];

		if (src instanceof RGBDataset || src instanceof IntegerDataset) {
			ImageFloat32 dst = new ImageFloat32(width, height);
			datasetToImage(src, dst);
			return (T) dst;
		} else if (src instanceof ByteDataset) {
			ImageUInt8 dst = new ImageUInt8(width, height);
			datasetToImage(src, dst);
			return (T) dst;
		} else if(src instanceof ShortDataset) {
			ImageInt16 dst = GeneralizedImageOps.createSingleBand(ImageInt16.class, width, height);
			datasetToImage(src, dst);
			return (T) dst;
		} else if (src instanceof FloatDataset) {
			ImageFloat32 dst = new ImageFloat32(width, height);
			datasetToImage(src, dst);
			return (T) dst;
		} else if (src instanceof DoubleDataset) {
			ImageFloat64 dst = new ImageFloat64(width, height);
			datasetToImage(src, dst);
			return (T) dst;
		} else if (src instanceof BooleanDataset) {
			return null;
		} else {
			throw new IllegalArgumentException("Unknown type " + type);
		}
	}

	/**
	 * <p>
	 * Converts an IDataset image into an 8bit intensity image using the
	 * BufferedImage's RGB interface.
	 * </p>
	 * <p>
	 * This is much slower than working
	 * directly with the BufferedImage's internal raster and should be
	 * avoided if possible.
	 * </p>
	 *
	 * @param src Input image.
	 * @param dst Output image.
	 */
	public void datasetToImage(IDataset src, ImageUInt8 dst) {
		if(src.getShape().length != 2)
			throw new IllegalArgumentException("The dataset has to be a 2 dimensionnal array");
		final int width = src.getShape()[0];
		final int height = src.getShape()[1];
		byte[] data = dst.data;

		for (int y = 0; y < height; y++) {
			int index = dst.startIndex + y * dst.stride;
			for (int x = 0; x < width; x++) {
				data[index++] = src.getByte(x, y);
			}
		}
//		else {
//			for (int y = 0; y < height; y++) {
//				int index = dst.startIndex + y * dst.stride;
//				for (int x = 0; x < width; x++) {
//					int argb = src.getRGB(x, y);
//
//					data[index++] = (byte) ((((argb >>> 16) & 0xFF) + ((argb >>> 8) & 0xFF) + (argb & 0xFF)) / 3);
//				}
//			}
//		}
	}

	/**
	 * <p>
	 * Converts an IDataset image into an 16bit intensity image using the
	 * BufferedImage's RGB interface.
	 * </p>
	 * <p>
	 * This is much slower than working
	 * directly with the BufferedImage's internal raster and should be
	 * avoided if possible.
	 * </p>
	 *
	 * @param src Input image.
	 * @param dst Output image.
	 */
	public void datasetToImage(IDataset src, ImageInt16<?> dst) {
		if(src.getShape().length != 2)
			throw new IllegalArgumentException("The dataset has to be a 2 dimensionnal array");
		final int width = src.getShape()[0];
		final int height = src.getShape()[1];

		short[] data = dst.data;

		for (int y = 0; y < height; y++) {
			int index = dst.startIndex + y * dst.stride;
			for (int x = 0; x < width; x++) {
				data[index++] = src.getShort(x, y);
			}
		}
//		} else {
//			// this will be totally garbage.  just here so that some unit test will pass
//			for (int y = 0; y < height; y++) {
//				int index = dst.startIndex + y * dst.stride;
//				for (int x = 0; x < width; x++) {
//					int argb = src.getRGB(x, y);
//
//					data[index++] = (short) ((((argb >>> 16) & 0xFF) + ((argb >>> 8) & 0xFF) + (argb & 0xFF)) / 3);
//				}
//			}
//		}
	}

	/**
	 * <p>
	 * Converts an IDataset image into an 8bit intensity image using the
	 * BufferedImage's RGB interface.
	 * </p>
	 * <p>
	 * This is much slower than working
	 * directly with the BufferedImage's internal raster and should be
	 * avoided if possible.
	 * </p>
	 *
	 * @param src Input image.
	 * @param dst Output image.
	 */
	public void datasetToImage(IDataset src, ImageFloat32 dst) {
		if(src.getShape().length != 2)
			throw new IllegalArgumentException("The dataset has to be a 2 dimensionnal array");
		final int width = src.getShape()[0];
		final int height = src.getShape()[1];

		float[] data = dst.data;

		for (int y = 0; y < height; y++) {
			int index = dst.startIndex + y * dst.stride;
			for (int x = 0; x < width; x++) {
				data[index++] = src.getFloat(x, y);
			}
		}
//		} else {
//			for (int y = 0; y < height; y++) {
//				int index = dst.startIndex + y * dst.stride;
//				for (int x = 0; x < width; x++) {
//					int argb = src.getRGB(x, y);
//					int r = (argb >>> 16) & 0xFF;
//					int g = (argb >>> 8) & 0xFF;
//					int b = argb & 0xFF;
//					float ave = (r + g + b) / 3.0f;
//					data[index++] = ave;
//				}
//			}
//		}
	}

	/**
	 * <p>
	 * Converts an IDataset image into an 8bit intensity image using the
	 * BufferedImage's RGB interface.
	 * </p>
	 * <p>
	 * This is much slower than working
	 * directly with the BufferedImage's internal raster and should be
	 * avoided if possible.
	 * </p>
	 *
	 * @param src Input image.
	 * @param dst Output image.
	 */
	public void datasetToImage(IDataset src, ImageFloat64 dst) {
		if(src.getShape().length != 2)
			throw new IllegalArgumentException("The dataset has to be a 2 dimensionnal array");
		final int width = src.getShape()[0];
		final int height = src.getShape()[1];

		double[] data = dst.data;

		for (int y = 0; y < height; y++) {
			int index = dst.startIndex + y * dst.stride;
			for (int x = 0; x < width; x++) {
				data[index++] = src.getDouble(x, y);
			}
		}
	}

	/**
	 * Converts a {@link boofcv.struct.image.ImageBase} into an IDataset.  If the buffered image
	 * has multiple channels the intensities of each channel are averaged together.
	 *
	 * @param src Input image.
	 * @param isBinary if true will convert to a binary image
	 * @return Converted image.
	 */
	public <T extends ImageBase<?>> IDataset convertTo(T src, boolean isBinary) {
		if( src instanceof ImageSingleBand ) {
			if (ImageUInt8.class == src.getClass()) {
				return imageToIDataset((ImageUInt8) src, isBinary);
			} else if (ImageInt16.class.isInstance(src)) {
				return imageToIDataset((ImageInt16<?>) src);
			} else if (ImageFloat32.class == src.getClass()) {
				return imageToIDataset((ImageFloat32) src);
			} else if (ImageFloat64.class == src.getClass()) {
				return imageToIDataset((ImageFloat64) src);
			} else if (ImageSInt32.class == src.getClass()) {
				return imageToIDataset((ImageSInt32) src, 0);
			} else {
				throw new IllegalArgumentException("ImageSingleBand type is not yet supported: "+src.getClass().getSimpleName());
			}
		} else if( src instanceof MultiSpectral ) {
//			MultiSpectral ms = (MultiSpectral)src;
//
//			if( ImageUInt8.class == ms.getType() ) {
//				return convertTo_U8((MultiSpectral<ImageUInt8>) ms, dst, orderRgb);
//			} else if( ImageFloat32.class == ms.getType() ) {
//				return convertTo_F32((MultiSpectral<ImageFloat32>) ms, dst, orderRgb);
//			} else {
//				throw new IllegalArgumentException("MultiSpectral type is not yet supported: "+ ms.getType().getSimpleName());
//			}
		}
		return null;
	}

	/**
	 * Converts a {@link boofcv.struct.image.ImageUInt8} into an IDataset.  If the buffered image
	 * has multiple channels the intensities of each channel are averaged together.
	 *
	 * @param src Input image.
	 * @param isBinary if true, will convert to a Binary Dataset
	 * @return dst Where the converted image is written to
	 */
	public IDataset imageToIDataset(ImageUInt8 src, boolean isBinary) {
		final int width = src.getWidth();
		final int height = src.getHeight();
		byte[] data = src.data;
		
		if (!isBinary)
			return new ByteDataset(data, width, height);

		BooleanDataset dst = new BooleanDataset(width, height);
		for (int y = 0; y < height; y++) {
			int indexSrc = src.startIndex + src.stride * y;
			for (int x = 0; x < width; x++) {
//				int v = data[indexSrc++] & 0xFF;
//				int argb = v << 16 | v << 8 | v;
				boolean value = data[indexSrc++] > 0 ? true : false;
				dst.set(value, x, y);
//				dst.setRGB(x, y, argb);
			}
		}
		return dst;
	}

	/**
	 * Converts a {@link boofcv.struct.image.ImageInt16} into an IDataset.  If the buffered image
	 * has multiple channels the intensities of each channel are averaged together.
	 *
	 * @param src Input image.
	 * @return dst Where the converted image is written to
	 */
	public IDataset imageToIDataset(ImageInt16<?> src) {

		final int width = src.getWidth();
		final int height = src.getHeight();
		ShortDataset dst = new ShortDataset(width, height);

		short[] data = src.data;
		for (int y = 0; y < height; y++) {
			int indexSrc = src.startIndex + src.stride * y;
			for (int x = 0; x < width; x++) {
				dst.set(data[indexSrc++], x, y);
			}
		}
		return dst;
	}

	/**
	 * Converts a {@link boofcv.struct.image.ImageFloat32} into an IDataset.  If the buffered image
	 * has multiple channels the intensities of each channel are averaged together.
	 *
	 * @param src Input image.
	 * @return dst Where the converted image is written to
	 */
	public IDataset imageToIDataset(ImageFloat32 src) {
		final int width = src.getWidth();
		final int height = src.getHeight();
		FloatDataset dst = new FloatDataset(width, height);

		float[] data = src.data;
		for (int y = 0; y < height; y++) {
			int indexSrc = src.startIndex + src.stride * y;

			for (int x = 0; x < width; x++) {
//				int v = (int) data[indexSrc++];

//				int argb = v << 16 | v << 8 | v;
	
				dst.set(data[indexSrc++], x, y);
//				dst.setRGB(x, y, argb);
			}
		}
		return dst;
	}

	/**
	 * Converts a {@link boofcv.struct.image.ImageFloat64} into an IDataset.  If the buffered image
	 * has multiple channels the intensities of each channel are averaged together.
	 *
	 * @param src Input image.
	 * @return dst Where the converted image is written to
	 */
	public IDataset imageToIDataset(ImageFloat64 src) {
		final int width = src.getWidth();
		final int height = src.getHeight();
		DoubleDataset dst = new DoubleDataset(width, height);

		double[] data = src.data;
		for (int y = 0; y < height; y++) {
			int indexSrc = src.startIndex + src.stride * y;

			for (int x = 0; x < width; x++) {
//				int v = (int) data[indexSrc++];

//				int argb = v << 16 | v << 8 | v;
				dst.set(data[indexSrc++], x, y);
//				dst.setRGB(x, y, argb);
			}
		}
		return dst;
	}

	/**
	 * Converts a {@link boofcv.struct.image.ImageSInt32} into an IDataset.  If the buffered image
	 * has multiple channels the intensities of each channel are averaged together.
	 *
	 * @param src Input image.
	 * @param numColors
	 * @return dst Where the converted image is written to
	 */
	public IDataset imageToIDataset(ImageSInt32 src, int numColors) {
		final int width = src.getWidth();
		final int height = src.getHeight();
		int[] data = src.data;

		if (numColors == 0)
			return new IntegerDataset(data, width, height);

		else {
			int colors[] = new int[numColors + 1];

			Random rand = new Random(123);
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
	public IDataset contourImageToIDataset(List<Contour> contours,
			int colorExternal, int colorInternal, int width, int height) {

		RGBDataset out = new RGBDataset(width, height);
		IDataset red = out.createRedDataset(Dataset.INT16);
		IDataset green = out.createGreenDataset(Dataset.INT16);
		IDataset blue = out.createBlueDataset(Dataset.INT16);
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
}
