/*-
 * Copyright 2013 Diamond Light Source Ltd.
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

package org.dawb.common.ui.hyper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.Activator;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.region.IROIListener;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.IRegionListener;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.region.RegionEvent;
import org.dawnsci.plotting.api.region.RegionUtils;
import org.dawnsci.plotting.api.trace.ColorOption;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.TraceUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.ASCIIDataWithHeadingSaver;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.ROISliceUtils;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.diamond.scisoft.analysis.roi.XAxisBoxROI;

/**
 * Display a 3D dataset across two plots with ROI slicing
 */
public class HyperWindow {
	
	private IPlottingSystem mainSystem;
	private IPlottingSystem sideSystem;
	private ILazyDataset lazy;
	private List<ILazyDataset> daxes;
	private IRegionListener regionListenerLeft;
	private IROIListener roiListenerLeft;
	private IROIListener roiListenerRight;
	private AbstractHyperJob leftJob;
	private AbstractHyperJob rightJob;
	private IAction reselect;
	private IAction baseline;
	private int traceDim;
	private IRegion windowRegion;
	private Composite mainComposite;
	

	public void createControl(Composite parent) {
		parent.setLayout(new FillLayout());
		SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,2,1));
		sashForm.setBackground(new Color(parent.getDisplay(), 192, 192, 192));
		
		createPlottingSystems(sashForm);
	}
	
	public void setData(ILazyDataset lazy, List<ILazyDataset> daxes, int traceDim) {
		
		this.lazy = lazy;
		this.daxes = daxes;
		//this.leftJob = new HyperJob();
		this.leftJob = new HyperTraceJob(sideSystem);
		//this.rightJob = new HyperSideJob();
		this.rightJob = new HyperImageJob(mainSystem);
		this.traceDim = traceDim;
		
		mainSystem.clear();
		
		for (IRegion region : mainSystem.getRegions()) {
			mainSystem.removeRegion(region);
		}
		
		sideSystem.clear();
		
		for (IRegion region : sideSystem.getRegions()) {
			sideSystem.removeRegion(region);
		}

		int[] axPos = getImageAxis();
		int[] imageSize = new int[]{lazy.getShape()[axPos[1]],lazy.getShape()[axPos[0]]};
		
		try {
			IRegion region = mainSystem.createRegion("Image Region 1", RegionType.BOX);
			
			mainSystem.addRegion(region);
			//TODO make roi positioning a bit more clever
			RectangularROI rroi = new RectangularROI(imageSize[1]/10, imageSize[0]/10, imageSize[1]/10, imageSize[0]/10, 0);
			region.setROI(rroi);
			//region.setUserRegion(false);
			region.addROIListener(this.roiListenerLeft);
			
			updateRight(region, rroi);
			
			windowRegion = sideSystem.createRegion("Trace Region 1", RegionType.XAXIS);
			
			sideSystem.addRegion(windowRegion);
			
			double min = daxes.get(traceDim).getSlice().min().doubleValue();
			double max = daxes.get(traceDim).getSlice().max().doubleValue();
			
			XAxisBoxROI broi = new XAxisBoxROI(min,0,(max-min)/10, 0, 0);
			windowRegion.setROI(broi);
			windowRegion.setUserRegion(false);
			windowRegion.addROIListener(this.roiListenerRight);
			updateLeft(windowRegion,broi);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void saveLineTracesAsAscii(String filename) {
		
		Collection<ITrace> traces = sideSystem.getTraces(ILineTrace.class);
		
		boolean firstTrace = true;
		List<IDataset> datasets = new ArrayList<IDataset>();
		List<String> headings = new ArrayList<String>();
		IDataset data;
		
		int dtype = 0;
		
		int i = 0;
		
		for (ITrace trace : traces ) {
			
			if (firstTrace) {
				int ddtype = AbstractDataset.getDType(((ILineTrace)trace).getData());
				data = ((ILineTrace)trace).getXData();
				int axdtype = AbstractDataset.getDType(data);
				
				if (ddtype == axdtype) {
					dtype = ddtype;
				} else if (ddtype > axdtype) {
					data = DatasetUtils.cast((AbstractDataset)data, ddtype);
					dtype = ddtype;
				} else {
					dtype = axdtype;
				}
				
				data.setShape(data.getShape()[0],1);
				datasets.add(data);
				headings.add("x");
				firstTrace = false;
			}
			
			data = ((ILineTrace)trace).getData();
			
			if (dtype != AbstractDataset.getDType(data)) {
				data = DatasetUtils.cast((AbstractDataset)data, dtype);
			}
			
			data.setShape(data.getShape()[0],1);
			datasets.add(data);
			headings.add("dataset_" + i);
			i++;
		}
		
		AbstractDataset allTraces = DatasetUtils.concatenate(datasets.toArray(new IDataset[datasets.size()]), 1);
		
		ASCIIDataWithHeadingSaver saver = new ASCIIDataWithHeadingSaver(filename);
		DataHolder dh = new DataHolder();
		dh.addDataset("AllTraces", allTraces);
		saver.setHeader("#Traces extracted from Hyperview");
		saver.setHeadings(headings);
		
		try {
			saver.saveFile(dh);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void setFocus() {
		mainComposite.setFocus();
	}
	
	private void createPlottingSystems(SashForm sashForm) {
		try {
			mainSystem = PlottingFactory.createPlottingSystem();
			mainSystem.setColorOption(ColorOption.NONE);
			mainComposite = new Composite(sashForm, SWT.NONE);
			mainComposite.setLayout(new GridLayout(1, false));
			GridUtils.removeMargins(mainComposite);

			ActionBarWrapper actionBarWrapper = ActionBarWrapper.createActionBars(mainComposite, null);

			reselect = new Action("Create new profile", SWT.TOGGLE) {
				@Override
				public void run() {
					if (reselect.isChecked()) {
						createNewRegion();
					} else {
						IContributionItem item = mainSystem.getActionBars().getToolBarManager().find("org.csstudio.swt.xygraph.undo.ZoomType.NONE");
						if (item != null && item instanceof ActionContributionItem) {
							((ActionContributionItem)item).getAction().run();
						}
					}
				}
			};
			
			reselect.setImageDescriptor(Activator.getImageDescriptor("icons/ProfileBox2.png"));
			
			actionBarWrapper.getToolBarManager().add(new Separator("uk.ac.diamond.scisoft.analysis.rcp.views.HyperPlotView.newProfileGroup"));
			actionBarWrapper.getToolBarManager().add(reselect);
			actionBarWrapper.getToolBarManager().add(new Separator("uk.ac.diamond.scisoft.analysis.rcp.views.HyperPlotView.newProfileGroupAfter"));
			
			Composite displayPlotComp  = new Composite(mainComposite, SWT.BORDER);
			displayPlotComp.setLayout(new FillLayout());
			displayPlotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			mainSystem.createPlotPart(displayPlotComp, 
													 "HyperImage", 
													 actionBarWrapper, 
													 PlotType.IMAGE, 
													 null);
			
			mainSystem.repaint();
			
			sideSystem = PlottingFactory.createPlottingSystem();
			sideSystem.setColorOption(ColorOption.NONE);
			Composite sideComp = new Composite(sashForm, SWT.NONE);
			sideComp.setLayout(new GridLayout(1, false));
			GridUtils.removeMargins(sideComp);
			ActionBarWrapper actionBarWrapper1 = ActionBarWrapper.createActionBars(sideComp, null);
			Composite sidePlotComp  = new Composite(sideComp, SWT.BORDER);
			sidePlotComp.setLayout(new FillLayout());
			sidePlotComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			baseline = new Action("Linear baseline", SWT.TOGGLE) {
				@Override
				public void run() {
					IROI roi = windowRegion.getROI();
					updateLeft(windowRegion,roi);
				}
			};
			
			IAction export = new Action("Export...") {
				@Override
				public void run() {
					FileDialog fd = new FileDialog(mainComposite.getShell(),SWT.SAVE);
					fd.setFileName("export.dat");
					final String path = fd.open();
					
					if (path == null) return;
					
					File file = new File(path);
					//TODO throw error
					if (file.exists()) return;
					
					Job exportJob = new Job("Export Traces") {
						
						@Override
						protected IStatus run(IProgressMonitor monitor) {
							saveLineTracesAsAscii(path);
							return Status.OK_STATUS;
						}
					};
					exportJob.schedule();
				}
			};
			
			baseline.setImageDescriptor(Activator.getImageDescriptor("icons/LinearBase.png"));
			actionBarWrapper1.getToolBarManager().add(new Separator("uk.ac.diamond.scisoft.analysis.rcp.views.HyperPlotView.newBaselineGroup"));
			actionBarWrapper1.getToolBarManager().add(baseline);
			actionBarWrapper1.getToolBarManager().add(export);
			actionBarWrapper1.getToolBarManager().add(new Separator("uk.ac.diamond.scisoft.analysis.rcp.views.HyperPlotView.newBaselineGroup"));
			
			sideSystem.createPlotPart(sidePlotComp, 
													 "HyperTrace", 
													 actionBarWrapper1, 
													 PlotType.XY, 
													 null);
			sideSystem.repaint();
			
			regionListenerLeft = getRegionListenerToLeft();
			mainSystem.addRegionListener(regionListenerLeft);
			roiListenerLeft = getROIListenerToRight();
			roiListenerRight = getROIListenerLeft();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected final void createNewRegion() {
		// Start with a selection of the right type
		try {
			IRegion region = mainSystem.createRegion(RegionUtils.getUniqueName("Image Region", mainSystem), RegionType.BOX);
			region.addROIListener(roiListenerLeft);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private IRegionListener getRegionListenerToLeft() {
		return new IRegionListener() {
			
			@Override
			public void regionsRemoved(RegionEvent evt) {
				
				for(ITrace trace : sideSystem.getTraces(ILineTrace.class)) {
					if (trace.getUserObject() instanceof IRegion) {
						if (((IRegion)trace.getUserObject()).isUserRegion()) {
							sideSystem.removeTrace(trace);
						}
					}
				}
			}
			
			@Override
			public void regionRemoved(RegionEvent evt) {
				
				for(ITrace trace : sideSystem.getTraces(ILineTrace.class)) {
					if (trace.getUserObject() == evt.getSource()) {
						sideSystem.removeTrace(trace);
					}
				}
			}
			
			@Override
			public void regionCreated(RegionEvent evt) {
				
			}
			
			@Override
			public void regionCancelled(RegionEvent evt) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void regionAdded(RegionEvent evt) {
				if (evt.getRegion() != null) {
					evt.getRegion().setUserRegion(true);
					evt.getRegion().addROIListener(roiListenerLeft);
					
					if (reselect.isChecked()) {
						createNewRegion();
					}
					
				}
				
			}
		};
	}
	
	private IROIListener getROIListenerToRight() {
		return new IROIListener() {
			
			@Override
			public void roiSelected(ROIEvent evt) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void roiDragged(ROIEvent evt) {
				updateRight((IRegion)evt.getSource(), evt.getROI());
				
			}
			
			@Override
			public void roiChanged(ROIEvent evt) {
				updateRight((IRegion)evt.getSource(), evt.getROI());
			}
		};
	}
	
	private IROIListener getROIListenerLeft() {
		return new IROIListener() {
			
			@Override
			public void roiSelected(ROIEvent evt) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void roiDragged(ROIEvent evt) {
				updateLeft((IRegion)evt.getSource(),evt.getROI());
				
			}
			
			@Override
			public void roiChanged(ROIEvent evt) {
				updateLeft((IRegion)evt.getSource(),evt.getROI());
				
			}
		};
	}
	
	private int[] getImageAxis() {
		int[] allDims = new int[]{2,1,0};
		int[] dims = new int[2];
		
		int i =0;
		for(int j : allDims) {
			if (j != traceDim) {
				dims[i] = j;
				i++;
			}
		}
		
		return dims;
	}
	
	protected void updateRight(IRegion r, IROI rb) {
		
		leftJob.profile(r, rb);
	}
	
	protected void updateLeft(IRegion r, IROI rb) {

		rightJob.profile(r,rb);
	}
	
	//TODO consider abstracting out plotting and data reducting from Jobs
//	private void updateImage(final IPlottingSystem plot, final IDataset image) {
//		
//		Display.getDefault().syncExec(new Runnable() {
//			@Override
//			public void run() {
//				plot.updatePlot2D(image, null, null);
//			}
//		});
//	}
//	
//	private void updateTrace(final IPlottingSystem plot, final IDataset axis, final IDataset data, final boolean update, final IRegion region) {
//		Display.getDefault().syncExec(new Runnable() {
//			@Override
//			public void run() {
//				
//				if (update) {
//					plot.updatePlot1D(axis,Arrays.asList(new IDataset[] {data}), null);
//					plot.repaint();	
//				} else {
//					List<ITrace> traceOut = plot.createPlot1D(axis,Arrays.asList(new IDataset[] {data}), null);
//					
//					for (ITrace trace : traceOut) {
//						trace.setUserObject(region);
//						if (trace instanceof ILineTrace){
//							region.setRegionColor(((ILineTrace)trace).getTraceColor());
//						}
//					}
//				}
//			}
//		});
//	}
	
	private final class HyperImageJob extends AbstractHyperJob {

		HyperImageJob(IPlottingSystem plot) {
			super("Update Image",plot);
			this.plot = plot;
			setSystem(false);
			setUser(false);
			setPriority(Job.INTERACTIVE);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				if (currentROI instanceof RectangularROI) {
					final IDataset image = ROISliceUtils.getAxisDatasetTrapzSumBaselined(daxes.get(traceDim).getSlice(),
							lazy,
							(RectangularROI)currentROI,
							traceDim,
							baseline.isChecked());
					
					image.setName("IntegatedImage");
					
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							plot.updatePlot2D(image, null, null);
						}
					});
				}
			} catch (Throwable ne) {
				ne.printStackTrace();
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}
	}
	
	private final class HyperTraceJob extends AbstractHyperJob {

		HyperTraceJob(IPlottingSystem plot) {
			super("Update Trace",plot);
			setSystem(false);
			setUser(false);
			setPriority(Job.INTERACTIVE);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			try {
				if (currentROI instanceof RectangularROI) {
					int[] dims = getImageAxis();
					
					//TODO check the dims used in the mean are sensible
					Collection<ITrace> traces = sideSystem.getTraces();
					for (ITrace trace : traces) {
						Object uo = trace.getUserObject();
						if (uo == currentRegion) {
							final IDataset dataset1 = ((AbstractDataset)ROISliceUtils.getDataset(lazy, (RectangularROI)currentROI, dims)).mean(dims[0]).mean(dims[1]);
							dataset1.setName(trace.getName());
							
							Display.getDefault().syncExec(new Runnable() {
								@Override
								public void run() {
									plot.updatePlot1D(daxes.get(traceDim).getSlice(),Arrays.asList(new IDataset[] {dataset1}), null);
									plot.repaint();						
								}
							});
							
							return Status.OK_STATUS;
						}
					}
					final IDataset dataset1 = ((AbstractDataset)ROISliceUtils.getDataset(lazy, (RectangularROI)currentROI, dims)).mean(dims[0]).mean(dims[1]);
					String name = TraceUtils.getUniqueTrace("trace", sideSystem, (String[])null);
					dataset1.setName(name);
					
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							List<ITrace> traceOut = sideSystem.createPlot1D(daxes.get(traceDim).getSlice(),Arrays.asList(new IDataset[] {dataset1}), null);
							
							for (ITrace trace : traceOut) {
								trace.setUserObject(currentRegion);
								if (trace instanceof ILineTrace){
									currentRegion.setRegionColor(((ILineTrace)trace).getTraceColor());
								}
							}					
						}
					});
				}
			} catch (Throwable ne) {
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}
	}
	
	private abstract class AbstractHyperJob extends Job {
		
		protected IRegion currentRegion;
		protected IROI currentROI;
		protected IPlottingSystem plot;
		
		public AbstractHyperJob(String name, IPlottingSystem plot) {
			super(name);
			this.plot = plot;
		}
		
		public void profile(IRegion r, IROI rb) {
			this.currentRegion = r;
			this.currentROI    = rb;
	        
          	schedule();		
		}
	}
	
}
