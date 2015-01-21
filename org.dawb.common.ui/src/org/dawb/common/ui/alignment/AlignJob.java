/*-
 * Copyright 2014 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dawb.common.ui.alignment;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROIList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.image.AlignImages;
import uk.ac.diamond.scisoft.analysis.image.AlignMethod;

/**
 * Job that performs alignment the original stack of images
 */
public class AlignJob extends Job {

	private static final Logger logger = LoggerFactory.getLogger(AlignJob.class);

	// loaded data
	private List<IDataset> data;
	private int mode;

	private List<List<double[]>> shifts = null;
	private List<IDataset> shiftedImages = new ArrayList<IDataset>();

	private AlignMethod alignState;

	private RectangularROI roi;

	private static IImageTransform transformer;

	public AlignJob() {
		super("Aligning calculation");
		setUser(true);
	}

	/**
	 * Injected by OSGI
	 * @param it
	 */
	public static void setImageTransform(IImageTransform it) {
		transformer = it;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (!shiftedImages.isEmpty())
			shiftedImages.clear();

		if (data == null)
			return new Status(IStatus.ERROR, "uk.ac.diamond.scisoft.analysis", "No data loaded!");
		int n = data.size();
		if (n % mode != 0) {
			String msg = "Missing file? Could not load multiple of " + mode + " images";
			logger.warn(msg);
			return new Status(IStatus.ERROR, "uk.ac.diamond.scisoft.analysis", msg);
		}
		if (alignState == AlignMethod.WITH_ROI) {
			super.setName("Calculating image alignment with ROI...");
			return alignWithROI(n, roi, monitor);
		} else if (alignState == AlignMethod.AFFINE_TRANSFORM) {
			super.setName("Calculating image alignment with affine transformation...");
			// align with boofcv
			try {
				shiftedImages = transformer.align(data);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("TODO put description of error here", e);
			}
		}

		if (monitor != null)
			monitor.worked(1);
		return Status.OK_STATUS;
	}

	private IStatus alignWithROI(int n, RectangularROI roi, IProgressMonitor monitor) {
		int nsets = n / mode;

		if (roi == null)
			return Status.CANCEL_STATUS;
		RectangularROIList rois = new RectangularROIList();
		rois.add(roi);

		if (monitor != null)
			monitor.worked(1);

		shifts = new ArrayList<List<double[]>>();
		int index = 0;
		int nr = rois.size();
		if (nr > 0) {
			if (nr < mode) { // clean up roi list
				if (mode == 2) {
					rois.add(rois.get(0));
				} else {
					switch (nr) {
					case 1:
						rois.add(rois.get(0));
						rois.add(rois.get(0));
						rois.add(rois.get(0));
						break;
					case 2:
					case 3:
						rois.add(2, rois.get(0));
						rois.add(3, rois.get(1));
						break;
					}
				}
			}

			IDataset[] tImages = new IDataset[nsets];
			List<IDataset> shifted = new ArrayList<IDataset>(nsets);
			boolean fromStart = false;
			// align first images across columns:
			// Example: [0,1,2]-[3,4,5]-[6,7,8]-[9,10,11] for 12 images on 4 columns
			// with images 0,3,6,9 as the top images of each column.
			List<double[]> topShifts = new ArrayList<double[]>();
			IDataset[] topImages = new IDataset[mode];
			List<IDataset> anchorList = new ArrayList<IDataset>();
			for (int i = 0; i < mode; i++) {
				topImages[i] = data.get(i * nsets);
			}
			// align top images
			topShifts = AlignImages.align(topImages, anchorList, rois.get(0), true, null);

			for (int p = 0; p < mode; p++) {
				for (int i = 0; i < nsets; i++) {
					tImages[i] = data.get(index++);
				}
				IDataset anchor = anchorList.get(p);
				shifted.clear();
				try {
					// align rest of images
					shifts.add(AlignImages.align(tImages, shifted, rois.get(p), true, topShifts.get(p)));
					shifted.remove(0); // remove unshifted anchor
					shiftedImages.add(anchor); // add shifted anchor
					shiftedImages.addAll(shifted); // add aligned images
				} catch (Exception e) {
					String msg = "Problem with alignment: " + e;
					logger.warn(msg);
					return new Status(IStatus.ERROR, "uk.ac.diamond.scisoft.analysis", msg);
				}

				fromStart = !fromStart;
				if (monitor != null)
					monitor.worked(1);
			}
		}
		if (monitor != null)
			monitor.worked(1);
		return Status.OK_STATUS;
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
	public List<IDataset> getShiftedImages() {
		return shiftedImages;
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

}