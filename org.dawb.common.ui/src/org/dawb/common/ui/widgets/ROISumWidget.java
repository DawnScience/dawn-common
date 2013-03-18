package org.dawb.common.ui.widgets;

import java.util.Collection;
import java.util.Iterator;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * Widget that displays a ROI sum using FontExtenderWidget and min and max values of the ROI in Text fields.
 * @author wqk87977
 *
 */
public class ROISumWidget implements IROIListener {

	private final static Logger logger = LoggerFactory.getLogger(ROISumWidget.class);
	private IRegionListener regionListener;
	private AbstractPlottingSystem plottingSystem;

	private Text minText;

	private Text maxText;

	private UpdateJob updateSumMinMax;

	private FontExtenderWidget sumDisplay;

	/**
	 * Constructor
	 * @param parent
	 * @param plottingSystem
	 */
	public ROISumWidget(Composite parent, AbstractPlottingSystem plottingSystem) {
		this.plottingSystem = plottingSystem;
		this.regionListener = getRegionListener();
		this.plottingSystem.addRegionListener(regionListener);

		this.updateSumMinMax = new UpdateJob("Update Sum, Min and Max Values");

		Collection<IRegion> regions = plottingSystem.getRegions();
		if(regions.size()>0){
			IRegion region = (IRegion)regions.toArray()[0];
			createRegionComposite(parent, region.getRegionType());
			region.addROIListener(ROISumWidget.this);
		}else{
			createRegionComposite(parent, RegionType.PERIMETERBOX);
		}
		
		logger.debug("widget created");
	}

	private void createRegionComposite(Composite regionComposite, RegionType regionType){
		
		sumDisplay = new FontExtenderWidget(regionComposite, SWT.BORDER, "Sum");
		sumDisplay.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		sumDisplay.setToolTipText("Shows the SUM of the Region Of Interest");
		
		Composite minmaxComposite = new Composite(regionComposite, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.LEFT, true, true);
		minmaxComposite.setLayout(new GridLayout(2, false));
		minmaxComposite.setLayoutData(gridData);
		
		Label minLabel = new Label(minmaxComposite, SWT.NONE);
		minLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		minLabel.setText("Min");

		minText = new Text(minmaxComposite, SWT.BORDER);
		minText.setEditable(false);
		minText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label maxLabel = new Label(minmaxComposite, SWT.NONE);
		maxLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		maxLabel.setText("Max");

		maxText = new Text(minmaxComposite, SWT.BORDER);
		maxText.setEditable(false);
		maxText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		

	}

	/**
	 * Update the widget with the correct roi information
	 */
	public void update(){
		if(plottingSystem != null){
			Collection<IRegion> regions = plottingSystem.getRegions();
			if(regions.size()>0){
				IRegion region = (IRegion)regions.toArray()[0];
				if(region.getROI() instanceof RectangularROI)
					updateSumMinMax((RectangularROI)region.getROI());
				region.addROIListener(ROISumWidget.this);
			}
		}
	}

	@Override
	public void roiDragged(ROIEvent evt) {
		IRegion region = (IRegion) evt.getSource();
		if(region!=null && region.getROI() instanceof RectangularROI)
			updateSumMinMax((RectangularROI)region.getROI());
	}

	@Override
	public void roiSelected(ROIEvent evt) {
		//TODO
	}

	@Override
	public void roiChanged(ROIEvent evt) {
		// if change occurs on the plot view
		IRegion region = (IRegion) evt.getSource();
		if(region!=null && region.getROI() instanceof RectangularROI)
			updateSumMinMax((RectangularROI)region.getROI());
	}

	private IRegionListener getRegionListener(){
		return new IRegionListener.Stub() {
			@Override
			public void regionRemoved(RegionEvent evt) {
				IRegion region = evt.getRegion();
				logger.debug("Region removed");
				if (region!=null) {
					if(region.getROI() instanceof RectangularROI)
						updateSumMinMax((RectangularROI)region.getROI());
					region.removeROIListener(ROISumWidget.this);
				}
			}

			@Override
			public void regionAdded(RegionEvent evt) {
				logger.debug("Region added");
				
				IRegion region = evt.getRegion();
				if (region!=null) {
					
					if(region.getROI() instanceof RectangularROI)
						updateSumMinMax((RectangularROI)region.getROI());
					region.addROIListener(ROISumWidget.this);
				}
			}

			@Override
			public void regionCreated(RegionEvent evt) {
				logger.debug("Region created");
				IRegion region = evt.getRegion();
				if (region!=null) {
					region.addROIListener(ROISumWidget.this);
					if(region.getROI() instanceof RectangularROI)
						updateSumMinMax((RectangularROI)region.getROI());
				}
			}

			@Override
			public void regionsRemoved(RegionEvent evt) {
				Iterator<IRegion> it = plottingSystem.getRegions().iterator();
				while(it.hasNext()){
					IRegion region = it.next();
					region.removeROIListener(ROISumWidget.this);
				}
			}
		};
	}

	/**
	 * This method needs to be called to clear the region listeners
	 */
	public void dispose(){
	}

	private void updateSumMinMax(RectangularROI rroi){
		if(plottingSystem != null && sumDisplay != null
				&& minText != null && !minText.isDisposed()
				&& maxText != null && !maxText.isDisposed()){
			int xStartPt = (int) rroi.getPoint()[0];
			int yStartPt = (int) rroi.getPoint()[1];
			int xStopPt = (int) rroi.getEndPoint()[0];
			int yStopPt = (int) rroi.getEndPoint()[1];
			int xInc = rroi.getPoint()[0]<rroi.getEndPoint()[0] ? 1 : -1;
			int yInc = rroi.getPoint()[1]<rroi.getEndPoint()[1] ? 1 : -1;
			
			updateSumMinMax.update(xStartPt, xStopPt, yStartPt, yStopPt, xInc, yInc);

		}
	}

	private int precision = 5;
	/**
	 * Method that rounds a value to the n precision decimals
	 * @param value
	 * @param precision
	 * @return double
	 */
	private double roundDouble(double value, int precision){
		int rounder = (int)Math.pow(10, precision);
		return (double)Math.round(value * rounder) / rounder;
	}

	private class UpdateJob extends Job {

		private int xStart;
		private int xStop;
		private int yStart;
		private int yStop;
		private int xInc;
		private int yInc;
		private String sumStr = "";
		private String minStr = "";
		private String maxStr = "";

		UpdateJob(String name) {
			super(name);
			setSystem(true);
			setUser(false);
			setPriority(Job.INTERACTIVE);
		}

		public void update(int xStart, int xStop, int yStart, int yStop, int xInc, int yInc) {
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
			
			if(plottingSystem != null){
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
						//round the Sum value(scientific notation)
						String[] str = dataRegion.sum(true).toString().split("E");
						String val1 = str[0];
						if(str.length>1){
							String val2 = str[1];
							val1 = val1.substring(0, precision+2);
							sumStr = val1+"e"+val2;
						} else {
							sumStr = String.valueOf(roundDouble((Double)(dataRegion.sum(true)), precision));
						}

						if (monitor.isCanceled()) return Status.CANCEL_STATUS;
						if (dataRegion.min() instanceof Double){
							minStr = String.valueOf(roundDouble((Double)dataRegion.min(), precision));
						} else if(dataRegion.min() instanceof Integer){
							minStr = String.valueOf(dataRegion.min());
						}
						if(dataRegion.max() instanceof Double){
							maxStr = String.valueOf(roundDouble((Double)dataRegion.max(), precision));
						} else if(dataRegion.max() instanceof Integer){
							maxStr = String.valueOf(dataRegion.max());
						}

						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								sumDisplay.update(sumStr);
//								sumText.setText(sumStr);
								minText.setText(minStr);
								maxText.setText(maxStr);
							}
						});
						
					}
				}
				return Status.OK_STATUS;
			}else{
				return Status.CANCEL_STATUS;
			}
			
		}
	}
}
