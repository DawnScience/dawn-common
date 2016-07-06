/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
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

import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.ByteDataset;
import org.eclipse.january.dataset.CompoundByteDataset;
import org.eclipse.january.dataset.CompoundDataset;
import org.eclipse.january.dataset.CompoundDoubleDataset;
import org.eclipse.january.dataset.CompoundFloatDataset;
import org.eclipse.january.dataset.CompoundIntegerDataset;
import org.eclipse.january.dataset.CompoundShortDataset;
import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.DoubleDataset;
import org.eclipse.january.dataset.FloatDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.LongDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.dataset.ShortDataset;

import boofcv.alg.filter.binary.Contour;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageDataType;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.ImageFloat64;
import boofcv.struct.image.ImageInt16;
import boofcv.struct.image.ImageInt8;
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
	 * @param clazz class of data returned, can be null
	 * @param shareData if true, share where possible (i.e. not a boolean dataset) 
	 * @return Converted image.
	 */
	public static <T extends ImageBase<?>> ImageBase<?> convertFrom(IDataset src, Class<T> clazz, boolean shareData) {
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
			if (clazz == ImageUInt8.class) {
				ImageUInt8 dst = new ImageUInt8(width, height);
				dst.data = ((ByteDataset) sd).getData();
				return dst;
			} else {
				ImageSInt8 dst = new ImageSInt8(width, height);
				dst.data = ((ByteDataset) sd).getData();
				return dst;
			}
		} else if(sd instanceof ShortDataset) {
			if (clazz == ImageUInt16.class) {
				ImageUInt16 dst = new ImageUInt16(width, height);
				dst.data = ((ShortDataset) sd).getData();
				return dst;
			} else {
				ImageSInt16 dst = new ImageSInt16(width, height);
				dst.data = ((ShortDataset) sd).getData();
				return dst;
			}
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
		} else if (sd instanceof CompoundDataset) {
			CompoundDataset cd = (CompoundDataset) sd;
			int elements = cd.getElementsPerItem();
			MultiSpectral<ImageSingleBand<?>> msrc = null;
			if (cd instanceof CompoundByteDataset) {
				CompoundByteDataset cbd = (CompoundByteDataset) cd;
				msrc = new MultiSpectral(ImageUInt8.class, width, height, elements);
				for (int i = 0; i < elements; i++) {
					msrc.bands[i] = new ImageUInt8(width, height);
					ByteDataset d = cbd.getElements(i);
					((ImageUInt8)msrc.bands[i]).data = d.getData();
				}
			} else if (cd instanceof CompoundShortDataset) {
				CompoundShortDataset csd = (CompoundShortDataset) cd;
				msrc = new MultiSpectral(ImageUInt16.class, width, height, elements);
				for (int i = 0; i < elements; i++) {
					msrc.bands[i] = new ImageUInt16(width, height);
					ShortDataset d = csd.getElements(i);
					((ImageUInt16)msrc.bands[i]).data = d.getData();
				}
			} else if (cd instanceof CompoundIntegerDataset) {
				CompoundIntegerDataset cid = (CompoundIntegerDataset) cd;
				msrc = new MultiSpectral(ImageSInt32.class, width, height, elements);
				for (int i = 0; i < elements; i++) {
					msrc.bands[i] = new ImageSInt32(width, height);
					IntegerDataset d = cid.getElements(i);
					((ImageSInt32)msrc.bands[i]).data = d.getData();
				}
			} else if (cd instanceof CompoundFloatDataset) {
				CompoundFloatDataset cfd = (CompoundFloatDataset) cd;
				msrc = new MultiSpectral(ImageFloat32.class, width, height, elements);
				for (int i = 0; i < elements; i++) {
					msrc.bands[i] = new ImageFloat32(width, height);
					FloatDataset d = cfd.getElements(i);
					((ImageFloat32)msrc.bands[i]).data = d.getData();
				}
			} else if (cd instanceof CompoundDoubleDataset) {
				CompoundDoubleDataset cdd = (CompoundDoubleDataset) cd;
				msrc = new MultiSpectral(ImageFloat64.class, width, height, elements);
				for (int i = 0; i < elements; i++) {
					msrc.bands[i] = new ImageFloat32(width, height);
					DoubleDataset d = cdd.getElements(i);
					((ImageFloat64)msrc.bands[i]).data = d.getData();
				}
			}
			if (elements == 1)
				return msrc.bands[0];
			return msrc;
		} else {
			throw new IllegalArgumentException("Unknown type " + type);
		}
	}

	private static Map<Class <? extends ImageBase<?>>, Class<?>> imageToElementClass;

	static {
		imageToElementClass = new HashMap<Class<? extends ImageBase<?>>, Class<?>>();
		for (ImageDataType i : ImageDataType.values()) {
			imageToElementClass.put(ImageDataType.typeToSingleClass(i), i.getDataType());
		}
	}

	public static <T extends ImageBase<?>> T convertFrom(IDataset src, Class<T> clazz, int bands) {
		Dataset ds;
		int ddt = DTypeUtils.getDTypeFromClass(imageToElementClass.get(clazz), bands);
		if (DTypeUtils.getDType(src) != ddt) {
			ds = DatasetUtils.cast(src, ddt);
		} else {
			ds = DatasetUtils.convertToDataset(src);
		}

		return (T) convertFrom(ds, clazz, true);
	}

	/**
	 * Converts a {@link ImageBase} into an IDataset
	 *
	 * @param src Input image.
	 * @param isBinary if true will convert to a binary image
	 * @return Converted image.
	 */
	public static <T extends ImageBase<?>> Dataset convertTo(T src, boolean isBinary) {
		if (src instanceof ImageUInt8 || src instanceof ImageSInt8) {
			Dataset dst = DatasetFactory.createFromObject(((ImageInt8<?>) src).data, src.height, src.width);
			if (isBinary) {
				dst = dst.cast(Dataset.BOOL);
			}
			return dst;
		} else if (src instanceof ImageUInt16 || src instanceof ImageSInt16) {
			Dataset dst = DatasetFactory.createFromObject(((ImageInt16<?>) src).data, src.height, src.width);
			return dst;
		} else if (src instanceof ImageSInt32) {
			Dataset dst = DatasetFactory.createFromObject(((ImageSInt32) src).data, src.height, src.width);
			return dst;
		} else if (src instanceof ImageSInt64) {
			Dataset dst = DatasetFactory.createFromObject(((ImageSInt64) src).data, src.height, src.width);
			return dst;
		} else if (src instanceof ImageFloat32) {
			Dataset dst = DatasetFactory.createFromObject(((ImageFloat32) src).data, src.height, src.width);
			return dst;
		} else if (src instanceof ImageFloat64) {
			Dataset dst = DatasetFactory.createFromObject(((ImageFloat64) src).data, src.height, src.width);
			return dst;
		} else if (src instanceof MultiSpectral) {
			@SuppressWarnings({ "rawtypes", "unchecked" })
			MultiSpectral<ImageSingleBand> msrc = (MultiSpectral<ImageSingleBand>) src;
			int n = msrc.getNumBands();
			Dataset[] datasets = new Dataset[n];
			for (int i = 0; i < n; i++) {
				datasets[i] = convertTo(msrc.getBand(i), isBinary);
			}
			if (n == 3) {
				RGBDataset rgb = DatasetFactory.createCompoundDataset(RGBDataset.class, datasets[0], datasets[1], datasets[2]);
				return rgb;
			}
			return DatasetUtils.createCompoundDataset(datasets);
		}
		throw new IllegalArgumentException("Image type is not yet supported: "+src.getClass().getSimpleName());
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
	public static Dataset contourImageToIDataset(List<Contour> contours,
			int colorExternal, int colorInternal, int width, int height) {

		RGBDataset out = DatasetFactory.zeros(RGBDataset.class, width, height);
		for( Contour c : contours ) {
			for(Point2D_I32 p : c.external ) {
				out.set(colorExternal, p.x, p.y);
			}
			for( List<Point2D_I32> l : c.internal ) {
				for( Point2D_I32 p : l ) {
					out.set(colorInternal, p.x, p.y);
				}
			}
		}
		return out;
	}
}
