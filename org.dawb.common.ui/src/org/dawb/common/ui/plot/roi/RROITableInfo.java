/*-
 * Copyright 2012 Diamond Light Source Ltd.
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

package org.dawb.common.ui.plot.roi;

import java.util.Collection;
import java.util.Iterator;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * Custom widget to display ROI information
 */
public class RROITableInfo implements IROIListener {

	private Logger logger = LoggerFactory.getLogger(RROITableInfo.class);

	private Text xStartText;
	private Text xEndText;
	private AbstractPlottingSystem plottingSystem;
	private Text sumText;
	private Text maxText;
	private Text minText;
	private boolean isProfile;
	private Button withSum;
	private boolean showSum = false;
	private String sumStr = "";
	private String minStr = "";
	private String maxStr = "";

	private UpdateJob updateSumMinMax;

	public RROITableInfo(final Composite parent, String labelName, AbstractPlottingSystem plottingSystem, boolean isProfile) {

		Group roiGroup = new Group(parent, SWT.NONE);
		roiGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		roiGroup.setLayout(new GridLayout(2, false));
		roiGroup.setText(labelName);
		
		Label xStartLabel = new Label(roiGroup, SWT.NONE);
		xStartLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		xStartLabel.setText("x Start");
		
		xStartText = new Text(roiGroup, SWT.BORDER);
		xStartText.setEditable(false);
		xStartText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label xEndLabel = new Label(roiGroup, SWT.NONE);
		xEndLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		xEndLabel.setText("x End");
		
		xEndText = new Text(roiGroup, SWT.BORDER);
		xEndText.setEditable(false);
		xEndText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		if(!isProfile){
			
			withSum = new Button(roiGroup, SWT.CHECK);
			withSum.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			withSum.setText("Show");
			withSum.setSelection(false);
			withSum.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if(withSum.getSelection())
						showSum = true;
					else
						showSum = false;
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});
			Composite sumComposite = new Composite(roiGroup, SWT.NONE);
			sumComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			sumComposite.setLayout(new GridLayout(2, false));
			Label sumLabel = new Label(sumComposite, SWT.NONE);
			sumLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			sumLabel.setText("Sum");
			
			sumText = new Text(sumComposite, SWT.BORDER);
			sumText.setEditable(false);
			sumText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Label maxLabel = new Label(roiGroup, SWT.NONE);
			maxLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			maxLabel.setText("Min");
			
			maxText = new Text(roiGroup, SWT.BORDER);
			maxText.setEditable(false);
			maxText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Label minLabel = new Label(roiGroup, SWT.NONE);
			minLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			minLabel.setText("Max");
			
			minText = new Text(roiGroup, SWT.BORDER);
			minText.setEditable(false);
			minText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		}
		this.isProfile = isProfile;
		this.plottingSystem = plottingSystem;
		this.plottingSystem.addRegionListener(regionListener);
		
		updateSumMinMax = new UpdateJob("Update Sum, Min and Max Values");
	}

	public void dispose(){
		this.plottingSystem.removeRegionListener(regionListener);
	}

	private void setEditingRegion(final RectangularROI rroi) {
		double xStart = rroi.getPointX();
	//	double yStart = rroi.getPointY();
		double xEnd = rroi.getEndPoint()[0];
	//	double yEnd = rroi.getEndPoint()[1];
		xStartText.setText(String.valueOf(xStart));
		xEndText.setText(String.valueOf(xEnd));
		if(!isProfile){
			int xStartPt = (int) rroi.getPoint()[0];
			int yStartPt = (int) rroi.getPoint()[1];
			int xStopPt = (int) rroi.getEndPoint()[0];
			int yStopPt = (int) rroi.getEndPoint()[1];
			int xInc = rroi.getPoint()[0]<rroi.getEndPoint()[0] ? 1 : -1;
			int yInc = rroi.getPoint()[1]<rroi.getEndPoint()[1] ? 1 : -1;
			
			updateSumMinMax.update(plottingSystem, xStartPt, xStopPt, yStartPt, yStopPt, xInc, yInc);

			sumText.setText(sumStr);
			minText.setText(minStr);
			maxText.setText(maxStr);
		}
	}

	private final class UpdateJob extends Job {

		private AbstractPlottingSystem plottingSystem;
		private int xStart;
		private int xStop;
		private int yStart;
		private int yStop;
		private int xInc;
		private int yInc;

		UpdateJob(String name) {
			super(name);
			setSystem(true);
			setUser(false);
			setPriority(Job.INTERACTIVE);
		}

		public void update(AbstractPlottingSystem plottingSystem, 
				int xStart, int xStop, int yStart, int yStop, int xInc, int yInc) {
			this.plottingSystem = plottingSystem;
			this.xStart = xStart;
			this.xStop = xStop;
			this.yStart = yStart;
			this.yStop = yStop;
			this.xInc = xInc;
			this.yInc = yInc;
			
			schedule();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			Collection<ITrace> traces = plottingSystem.getTraces();
			
			Iterator<ITrace> it = traces.iterator();
			if (monitor.isCanceled()) return Status.CANCEL_STATUS;
			while (it.hasNext()) {
				ITrace iTrace = (ITrace) it.next();
				if(iTrace instanceof IImageTrace){
					IImageTrace image = (IImageTrace)iTrace;
					
					AbstractDataset dataRegion = image.getData();
					try {
						dataRegion = dataRegion.getSlice(
								new int[] { yStart, xStart },
								new int[] { yStop, xStop },
								new int[] {yInc, xInc});
						if (monitor.isCanceled()) return Status.CANCEL_STATUS;
					} catch (IllegalArgumentException e) {
						logger.debug("Error getting region data:"+ e);
					}
					
					if(showSum)
						sumStr = dataRegion.sum(true).toString();
					if (monitor.isCanceled()) return Status.CANCEL_STATUS;
					minStr = dataRegion.min().toString();
					maxStr = dataRegion.max().toString();
					
				}
			}
			
			return Status.OK_STATUS;
		}
	}

	@Override
	public void roiDragged(ROIEvent evt) {
		ROIBase roi = evt.getROI();
		if(roi != null && roi instanceof RectangularROI){
			setEditingRegion((RectangularROI)roi);
		}
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		ROIBase roi = evt.getROI();
		if(roi != null && roi instanceof RectangularROI){
			setEditingRegion((RectangularROI)roi);
		}
	}

	private IRegionListener regionListener = new IRegionListener.Stub() {
		@Override
		public void regionRemoved(RegionEvent evt) {
			if (evt.getRegion()!=null) {
				evt.getRegion().removeROIListener(RROITableInfo.this);
			}
		}
		@Override
		public void regionAdded(RegionEvent evt) {

		}
		
		@Override
		public void regionCreated(RegionEvent evt) {
			if (evt.getRegion()!=null) {
				evt.getRegion().addROIListener(RROITableInfo.this);
			}
		}
	};
}

