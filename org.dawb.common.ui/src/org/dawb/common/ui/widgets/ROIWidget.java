package org.dawb.common.ui.widgets;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IROIListener;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.roi.AxisPixelROIEditTable;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

/**
 * Class to create an {@link AxisPixelROIEditTable} which implements an {@link}IROIListener
 * and shows the sum, minimum and maximum of a Rectangular ROI
 * @author wqk87977
 *
 */
public class ROIWidget implements IROIListener {

	private final static Logger logger = LoggerFactory.getLogger(ROIWidget.class);

	private Composite parent;

	public String viewName;

	private boolean roiChanged;
	private Composite regionComposite;
	private IRegionListener regionListener;
	private AbstractPlottingSystem plottingSystem;

	private AxisPixelROIEditTable roiViewer;

	private IRegion region;

	private Text nameText;

	private Text sumText;

	private Text minText;

	private Text maxText;
	
	private String sumStr = "";
	private String minStr = "";
	private String maxStr = "";

	private UpdateJob updateSumMinMax;

	private boolean sumMinMaxIsShown = true;

	private boolean isProfile = false;

	/**
	 * Constructor
	 * @param parent
	 * @param viewName the name of the plottingSystem
	 */
	public ROIWidget(Composite parent, AbstractPlottingSystem plottingSystem) {

		this.parent = parent;
		this.plottingSystem = plottingSystem;

		this.regionListener = getRegionListener(plottingSystem);
		this.plottingSystem.addRegionListener(regionListener);

		this.updateSumMinMax = new UpdateJob("Update Sum, Min and Max Values");

		logger.debug("widget created");
	}

	/**
	 * Creates the widget and its controls
	 */
	public void createWidget(){
		regionComposite = new Composite(parent, SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.LEFT, true, true);
		regionComposite.setLayout(new GridLayout(1, false));
		regionComposite.setLayoutData(gridData);

		Collection<IRegion> regions = plottingSystem.getRegions();
		if(regions.size()>0){
			IRegion region = (IRegion)regions.toArray()[0];
			createRegionComposite(regionComposite, region.getRegionType());
			region.addROIListener(ROIWidget.this);
		}else{
			createRegionComposite(regionComposite, RegionType.COLORBOX);
		}
	}

	private void createRegionComposite(Composite regionComposite, RegionType regionType){

		Composite nameComp = new Composite(regionComposite, SWT.NONE);
		nameComp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		nameComp.setLayout(new GridLayout(2, false));

		final Label nameLabel = new Label(nameComp, SWT.NONE);
		nameLabel.setText("Region Name  ");
		nameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false));

		nameText = new Text(nameComp, SWT.BORDER | SWT.SINGLE);
		nameText.setToolTipText("Region name");
		nameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		nameText.setEditable(false);

		Group regionTableGroup = new Group (regionComposite, SWT.NONE);
		GridData gridData = new GridData(SWT.FILL, SWT.LEFT, true, true);
		regionTableGroup.setLayout(new GridLayout(1, false));
		regionTableGroup.setLayoutData(gridData);
		regionTableGroup.setText("Region Editing Table");
		roiViewer = new AxisPixelROIEditTable(regionTableGroup, plottingSystem);
		roiViewer.setIsProfileTable(isProfile);
		roiViewer.createControl();
		// sumMinMax group
		if(sumMinMaxIsShown){
			Group sumMinMaxGroup = new Group (regionComposite, SWT.NONE);
			sumMinMaxGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			sumMinMaxGroup.setLayout(new GridLayout(2, false));
			sumMinMaxGroup.setText("Region Information");
			
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
		}

		// Should be last
		nameText.setText(getDefaultName(regionType.getIndex()));
	}

	private static Map<Integer, Integer> countMap;

	private String getDefaultName(int sel) {
		if (countMap == null)
			countMap = new HashMap<Integer, Integer>(5);
		if (!countMap.containsKey(sel)) {
			countMap.put(sel, 0);
		}
		int count = countMap.get(sel);
		count++;
		LinkedList<String> regionTypeList = new LinkedList<String> (); 
		for (RegionType type : RegionType.ALL_TYPES) {
			regionTypeList.add(type.getName());
		}
		return regionTypeList.get(sel) + " " + count;
	}

	/**
	 * Update the widget with the correct roi information
	 */
	public void update(){
		this.plottingSystem = (AbstractPlottingSystem)PlottingFactory.getPlottingSystem(viewName);
		if(plottingSystem != null){
			Collection<IRegion> regions = plottingSystem.getRegions();
			if(regions.size()>0){
				IRegion region = (IRegion)regions.toArray()[0];
				if(roiViewer == null)
					createRegionComposite(regionComposite, region.getRegionType());
				roiViewer.setTableValues(region.getROI());
				if(sumMinMaxIsShown && region.getROI() instanceof RectangularROI)
					updateSumMinMax((RectangularROI)region.getROI());

				nameText.setText(region.getName());
				this.region = region;
				region.addROIListener(ROIWidget.this);
			}
		}
	}

	@Override
	public void roiDragged(ROIEvent evt) {}

	@Override
	public void roiSelected(ROIEvent evt) {}

	@Override
	public void roiChanged(ROIEvent evt) {
		// if change occurs on the plot view
		IRegion region = (IRegion) evt.getSource();
		if(region!=null){
			setEditingRegion(region);
		}
		roiChanged = true;
		this.region = region;
	}

	/**
	 * Method to set the input of this widget given an IRegion
	 * @param region
	 */
	public void setEditingRegion(IRegion region){
		roiViewer.setTableValues(region.getROI());
		if(sumMinMaxIsShown && region.getROI() instanceof RectangularROI)
			updateSumMinMax((RectangularROI)region.getROI());
		if(nameText != null && !nameText.isDisposed())
			nameText.setText(region.getName());
	}

	/**
	 * Method to either show or hide the Sum, Min, and Max text fields<br>
	 * TRUE by default.
	 * @param b
	 */
	public void showSumMinMax(boolean b){
		this.sumMinMaxIsShown  = b;
	}

	/**
	 * Method to build a Table Viewer for a main plottingSystem or for a profile plotting System<br>
	 * FALSE by default.
	 * @param isProfile
	 */
	public void setIsProfile(boolean isProfile){
		this.isProfile = isProfile;
	}

	/**
	 * 
	 * @return IRegion
	 */
	public IRegion getRegion(){
		return region;
	}

	public boolean getRoiChanged(){
		return roiChanged;
	}

	public void setRoiChanged(boolean value){
		this.roiChanged = value;
	}

	public Composite getRegionComposite(){
		return regionComposite;
	}

	private IRegionListener getRegionListener(final AbstractPlottingSystem plottingSystem){
		return new IRegionListener.Stub() {
			@Override
			public void regionRemoved(RegionEvent evt) {
				System.out.println("arpes_region removed");
				IRegion region = evt.getRegion();
				if (region!=null) {
//						roiViewer.disposeRegion((AbstractSelectionRegion) region);
						if(plottingSystem.getRegions().size()>0){
							IRegion lastRegion = (IRegion)plottingSystem.getRegions().toArray()[0];
							roiViewer.setTableValues(lastRegion.getROI());
							if(sumMinMaxIsShown && region.getROI() instanceof RectangularROI)
								updateSumMinMax((RectangularROI)region.getROI());

						}
					
					region.removeROIListener(ROIWidget.this);
					parent.layout();
					parent.redraw();
				}
			}

			@Override
			public void regionAdded(RegionEvent evt) {
				System.out.println("arpes_region added");
				
				IRegion region = evt.getRegion();
				if (region!=null) {
						roiViewer.setTableValues(region.getROI());
						if(sumMinMaxIsShown && region.getROI() instanceof RectangularROI)
							updateSumMinMax((RectangularROI)region.getROI());

					parent.layout();
					parent.redraw();
				}
			}

			@Override
			public void regionCreated(RegionEvent evt) {
				System.out.println("arpes_region created");
				IRegion region = evt.getRegion();
				if (region!=null) {
					region.addROIListener(ROIWidget.this);
					if(roiViewer==null){
						createRegionComposite(regionComposite, region.getRegionType());
					roiViewer.setTableValues(region.getROI());
					if(sumMinMaxIsShown && region.getROI() instanceof RectangularROI)
						updateSumMinMax((RectangularROI)region.getROI());
					}
				}
			}

			@Override
			public void regionsRemoved(RegionEvent evt) {
				IWorkbenchPage page =  PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				if(page != null){
					String id = page.getActivePart().getSite().getId();
					if(!id.equals("org.dawb.workbench.editor.H5Editor")){
						Iterator<IRegion> it = plottingSystem.getRegions().iterator();
						while(it.hasNext()){
							IRegion region = it.next();
							region.removeROIListener(ROIWidget.this);
						}
					}
				}
			}
		};
	}

	private void clearListeners(AbstractPlottingSystem plotSystem, IRegionListener listener) {
		if (plotSystem==null) return;
		Collection<IRegion> regions = plotSystem.getRegions();
		if(regions != null && regions.size() > 0){
			Iterator<IRegion> it = regions.iterator();
			while(it.hasNext()){
				IRegion region = it.next();
				region.removeROIListener(this);
			}
		}
		plotSystem.removeRegionListener(listener);
	}

	/**
	 * This method needs to be called to clear the region listeners
	 */
	public void dispose(){
		clearListeners(plottingSystem, regionListener);
	}

	/**
	 * Method to add a SelectionChangedListener to the table viewer
	 * @param listener
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		roiViewer.addSelectionChangedListener(listener);
	}

	/**
	 * Method to remove a SelectionChangedListener from the table viewer
	 * @param listener
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		roiViewer.removeSelectionChangedListener(listener);
	}

	/**
	 * Method that returns the current ROI
	 * @return ROIBase
	 */
	public ROIBase getROI(){
		return roiViewer.getROI();
	}

	/**
	 * Method that returns the region name
	 * @return String
	 */
	public String getRegionName(){
		return nameText.getText();
	}

	private void updateSumMinMax(RectangularROI rroi){
		if(plottingSystem != null && sumText != null && !sumText.isDisposed()
				&& minText != null && !minText.isDisposed()
				&& maxText != null && !maxText.isDisposed()){
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
							sumStr = dataRegion.sum(true).toString();
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
					}
				}
				return Status.OK_STATUS;
			}else{
				return Status.CANCEL_STATUS;
			}
			
		}
	}
}
