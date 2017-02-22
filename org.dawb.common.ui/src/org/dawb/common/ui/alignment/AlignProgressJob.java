/*-
 * Copyright (c) 2015-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.alignment;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.image.HessianRegParameters;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.dawnsci.analysis.api.image.DetectionAlgoParameters;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.image.AlignImages;
import uk.ac.diamond.scisoft.analysis.image.AlignMethod;

/**
 * Job that performs alignment the original stack of images
 */
public class AlignProgressJob implements IRunnableWithProgress {

	private static final Logger logger = LoggerFactory.getLogger(AlignProgressJob.class);

	// loaded data
	private ILazyDataset data;
	private List<IDataset> nonLazyData;
	private int mode;

	private List<List<double[]>> shifts;
	private ILazyDataset shiftedImages;
	private List<IDataset> nonLazyShiftedImages;

	private AlignMethod alignState;

	private RectangularROI roi;

	private boolean isLazy;

	private DetectionAlgoParameters detectionParams;
	private HessianRegParameters hessianParams;

	private static IImageTransform transformer;

	/**
	 * Use {@code AlignProgressJob(boolean)} instead 
	 */
	public AlignProgressJob() {
		this(false);
	}

	public AlignProgressJob(boolean isLazy) {
		super();
		this.isLazy = isLazy;
	}

	/**
	 * Injected by OSGI
	 * @param it
	 */
	public static void setImageTransform(IImageTransform it) {
		transformer = it;
	}

	@Override
	public void run(IProgressMonitor monitor) {
		if (isLazy) {
			if (shiftedImages != null) {
				shiftedImages = null;
			}
			if (data == null)
				return;
		} else {
			if (nonLazyShiftedImages != null && !nonLazyShiftedImages.isEmpty())
				nonLazyShiftedImages.clear();
			if (nonLazyData == null)
				return;
		}
		int n = isLazy ? data.getShape()[0] : nonLazyData.size();

		if (monitor != null)
			monitor.beginTask("Aligning images...", alignState == AlignMethod.WITH_ROI ? 5*n : n);
		if (alignState == AlignMethod.WITH_ROI && n % mode != 0) {
			final String msg = "Missing file? Could not load multiple of " + mode + " images";
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Alignment error", msg);
				}
			});
			logger.warn(msg);
			return;
		}
		final IMonitor mon = new ProgressMonitorWrapper(monitor);
		try {
			if (alignState == AlignMethod.WITH_ROI) {
				if (shifts == null)
					shifts = new ArrayList<List<double[]>>();
				if (!shifts.isEmpty())
					shifts.clear();
				if (isLazy)
					shiftedImages = AlignImages.alignLazyWithROI(data, shifts, roi, mode, mon);
				else
					nonLazyShiftedImages = AlignImages.alignWithROI(nonLazyData, shifts, roi, mode, mon);
			} else if (alignState == AlignMethod.HESSIAN_REGISTRATION) {
				// align with boofcv
				if (isLazy) {
					shiftedImages = transformer.align(data, detectionParams, hessianParams, mon);
				} else {
					nonLazyShiftedImages = transformer.align(nonLazyData, mon);
				}
			}
		} catch (final Exception e) {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Alignment error",
							"An error occured while aligning images:" + e);
				}
			});
			logger.error("Error aligning images:", e);
		}
		if (monitor != null) {
			monitor.done();
		}

	}

	/**
	 * 
	 * @return shifts
	 */
	public List<List<double[]>> getShifts() {
		return shifts;
	}

	/**
	 * 
	 * @param data
	 *          original stack of images
	 */
	public void setData(List<IDataset> data) {
		this.nonLazyData = data;
	}

	/**
	 * 
	 * @param data
	 *          original stack of images
	 */
	public void setData(ILazyDataset data) {
		this.data = data;
	}

	/**
	 * Sets the roi for alignment with Roi
	 * 
	 * @param roi
	 */
	public void setRectangularROI(RectangularROI roi) {
		this.roi = roi;
	}

	/**
	 * 
	 * @return
	 *      Corrected stack of images
	 */
	public ILazyDataset getLazyShiftedImages() {
		return shiftedImages;
	}

	/**
	 * 
	 * @return
	 *      Corrected stack of images
	 */
	public List<IDataset> getShiftedImages() {
		return nonLazyShiftedImages;
	}

	/**
	 * Set the number of column used for ROI alignment method (2 or 4)
	 * @param mode
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

	public void setAlignMethod(AlignMethod alignState) {
		this.alignState = alignState;
	}

	public void setRansacParameters(DetectionAlgoParameters ransacParams) {
		this.detectionParams = ransacParams;
	}

	public void setHessianParameters(HessianRegParameters hessianParams) {
		this.hessianParams = hessianParams;
	}

}