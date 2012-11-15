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
import java.util.List;

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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
	private Text widthText;
	private Text xBisStartText;
	private Text xBisEndText;
	private Text widthBisText;
	
	private Text yStartText;
	private Text yEndText;
	private Text heightText;
	private Text yBisStartText;
	private Text yBisEndText;
	private Text heightBisText;
	
	private Text sumText;
	private Text maxText;
	private Text minText;
	private Text sumBisText;
	private Text maxBisText;
	private Text minBisText;
	
	private AbstractPlottingSystem plottingSystem;
	
	private boolean isProfile;
	private String sumStr = "";
	private String minStr = "";
	private String maxStr = "";

	private UpdateJob updateSumMinMax;

	private Composite composite;

	public RROITableInfo(final Composite parent, String labelName, AbstractPlottingSystem plottingSystem, boolean isProfile) {
		
		composite = new Group(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		composite.setLayout(new GridLayout(2, false));
		
		// x group : xstart, xend, width
		Group xRoiGroup = new Group (composite, SWT.NONE);
		xRoiGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		xRoiGroup.setLayout(new GridLayout(2, false));
		xRoiGroup.setText("Axis Values");
		
		Label xStartLabel = new Label(xRoiGroup, SWT.NONE);
		xStartLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		xStartLabel.setText("x Start");
		
		xStartText = new Text(xRoiGroup, SWT.BORDER);
		xStartText.setEditable(false);
		xStartText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label xEndLabel = new Label(xRoiGroup, SWT.NONE);
		xEndLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		xEndLabel.setText("x End");
		
		xEndText = new Text(xRoiGroup, SWT.BORDER);
		xEndText.setEditable(false);
		xEndText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label widthLabel = new Label(xRoiGroup, SWT.NONE);
		widthLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		widthLabel.setText("Width");
		
		widthText = new Text(xRoiGroup, SWT.BORDER);
		widthText.setEditable(false);
		widthText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		// x bis group : xstart, xend, width
		Group xBisRoiGroup = new Group (composite, SWT.NONE);
		xBisRoiGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		xBisRoiGroup.setLayout(new GridLayout(2, false));
		xBisRoiGroup.setText("Pixel Values");
		
		Label xBisStartLabel = new Label(xBisRoiGroup, SWT.NONE);
		xBisStartLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		xBisStartLabel.setText("x Start");
		
		xBisStartText = new Text(xBisRoiGroup, SWT.BORDER);
		xBisStartText.setEditable(false);
		xBisStartText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label xBisEndLabel = new Label(xBisRoiGroup, SWT.NONE);
		xBisEndLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		xBisEndLabel.setText("x End");
		
		xBisEndText = new Text(xBisRoiGroup, SWT.BORDER);
		xBisEndText.setEditable(false);
		xBisEndText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label widthBisLabel = new Label(xBisRoiGroup, SWT.NONE);
		widthBisLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		widthBisLabel.setText("Width");
		
		widthBisText = new Text(xBisRoiGroup, SWT.BORDER);
		widthBisText.setEditable(false);
		widthBisText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		if(!isProfile){

			// y group : ystart, yend, height
			Group yRoiGroup = new Group (composite, SWT.NONE);
			yRoiGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			yRoiGroup.setLayout(new GridLayout(2, false));
			yRoiGroup.setText("Axis Values");
			
			Label yStartLabel = new Label(yRoiGroup, SWT.NONE);
			yStartLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			yStartLabel.setText("y Start");
			
			yStartText = new Text(yRoiGroup, SWT.BORDER);
			yStartText.setEditable(false);
			yStartText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Label yEndLabel = new Label(yRoiGroup, SWT.NONE);
			yEndLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			yEndLabel.setText("y End");
			
			yEndText = new Text(yRoiGroup, SWT.BORDER);
			yEndText.setEditable(false);
			yEndText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Label heightLabel = new Label(yRoiGroup, SWT.NONE);
			heightLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			heightLabel.setText("Height");
			
			heightText = new Text(yRoiGroup, SWT.BORDER);
			heightText.setEditable(false);
			heightText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			// y bis group : ystart, yend, height
			Group yBisRoiGroup = new Group (composite, SWT.NONE);
			yBisRoiGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			yBisRoiGroup.setLayout(new GridLayout(2, false));
			yBisRoiGroup.setText("Pixel Values");
			
			Label yBisStartLabel = new Label(yBisRoiGroup, SWT.NONE);
			yBisStartLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			yBisStartLabel.setText("y Start");
			
			yBisStartText = new Text(yBisRoiGroup, SWT.BORDER);
			yBisStartText.setEditable(false);
			yBisStartText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Label yBisEndLabel = new Label(yBisRoiGroup, SWT.NONE);
			yBisEndLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			yBisEndLabel.setText("y End");
			
			yBisEndText = new Text(yBisRoiGroup, SWT.BORDER);
			yBisEndText.setEditable(false);
			yBisEndText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Label heightBisLabel = new Label(yBisRoiGroup, SWT.NONE);
			heightBisLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			heightBisLabel.setText("Height");
			
			heightBisText = new Text(yBisRoiGroup, SWT.BORDER);
			heightBisText.setEditable(false);
			heightBisText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
			// sumMinMax group
			Group sumMinMaxGroup = new Group (composite, SWT.NONE);
			sumMinMaxGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			sumMinMaxGroup.setLayout(new GridLayout(2, false));
			
			Label sumLabel = new Label(sumMinMaxGroup, SWT.NONE);
			sumLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			sumLabel.setText("Sum");
			
			sumText = new Text(sumMinMaxGroup, SWT.BORDER);
			sumText.setEditable(false);
			sumText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Label minLabel = new Label(sumMinMaxGroup, SWT.NONE);
			minLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			minLabel.setText("Min");
			
			minText = new Text(sumMinMaxGroup, SWT.BORDER);
			minText.setEditable(false);
			minText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Label maxLabel = new Label(sumMinMaxGroup, SWT.NONE);
			maxLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			maxLabel.setText("Max");
			
			maxText = new Text(sumMinMaxGroup, SWT.BORDER);
			maxText.setEditable(false);
			maxText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			// bis sumMinMax group
			Group sumMinMaxBisGroup = new Group (composite, SWT.NONE);
			sumMinMaxBisGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			sumMinMaxBisGroup.setLayout(new GridLayout(2, false));
			
			Label sumBisLabel = new Label(sumMinMaxBisGroup, SWT.NONE);
			sumBisLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			sumBisLabel.setText("Sum");
			
			sumBisText = new Text(sumMinMaxBisGroup, SWT.BORDER);
			sumBisText.setEditable(false);
			sumBisText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Label minBisLabel = new Label(sumMinMaxBisGroup, SWT.NONE);
			minBisLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			minBisLabel.setText("Min");
			
			minBisText = new Text(sumMinMaxBisGroup, SWT.BORDER);
			minBisText.setEditable(false);
			minBisText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
			Label maxBisLabel = new Label(sumMinMaxBisGroup, SWT.NONE);
			maxBisLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			maxBisLabel.setText("Max");
			
			maxBisText = new Text(sumMinMaxBisGroup, SWT.BORDER);
			maxBisText.setEditable(false);
			maxBisText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			
		}
		this.isProfile = isProfile;
		this.plottingSystem = plottingSystem;
		this.plottingSystem.addRegionListener(regionListener);
		
		updateSumMinMax = new UpdateJob("Update Sum, Min and Max Values");
	}

	public void dispose(){
		this.plottingSystem.removeRegionListener(regionListener);
	}
	
	public Composite getComposite(){
		return this.composite;
	}

	private void setEditingRegion(final RectangularROI rroi) {
		double xStart = rroi.getPointX();
		double yStart = rroi.getPointY();
		double xEnd = rroi.getEndPoint()[0];
		double yEnd = rroi.getEndPoint()[1];
		
		// main plotting system
		if(!isProfile){
			try{
				// We get the axes data to convert from the axis pixel to data values
				Collection<ITrace> traces = plottingSystem.getTraces();
				Iterator<ITrace> it = traces.iterator();
				while(it.hasNext()){
					ITrace trace = it.next();
					if(trace instanceof IImageTrace){
						IImageTrace image = (IImageTrace)trace;
						List<AbstractDataset> axes = image.getAxes();
						// x axis and width
						double xNewStart = axes.get(0).getElementDoubleAbs((int)Math.round(xStart));
						double xNewEnd =axes.get(0).getElementDoubleAbs((int)(int)Math.round(xEnd));
						xStartText.setText(String.valueOf(xNewStart));
						xEndText.setText(String.valueOf(xNewEnd));
						widthText.setText(String.valueOf(xNewEnd-xNewStart));
						// yaxis and height
						double yNewStart = axes.get(1).getElementDoubleAbs((int)Math.round(yStart));
						double yNewEnd =axes.get(1).getElementDoubleAbs((int)(int)Math.round(yEnd));
						yStartText.setText(String.valueOf(yNewStart));
						yEndText.setText(String.valueOf(yNewEnd));
						heightText.setText(String.valueOf(yNewEnd-yNewStart));
					}
				}
				xBisStartText.setText(String.valueOf(xStart));
				xBisEndText.setText(String.valueOf(xEnd));
				widthBisText.setText(String.valueOf(xEnd-xStart));
				
				yBisStartText.setText(String.valueOf(yStart));
				yBisEndText.setText(String.valueOf(yEnd));
				heightBisText.setText(String.valueOf(yEnd-yStart));
				
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
			} catch (Exception e) {
				logger.debug("Error while updating the ROITableInfo:"+ e);
			}
		} 
		//if profile plotting system: no need to convert as the axes are already correctly set
		else {
			xStartText.setText(String.valueOf(xStart));
			xEndText.setText(String.valueOf(xEnd));
			widthText.setText(String.valueOf(xEnd-xStart));
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

