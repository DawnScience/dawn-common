package org.dawb.common.ui.plot;

import java.util.Collection;
import java.util.List;

import org.dawb.common.ui.plot.annotation.IAnnotation;
import org.dawb.common.ui.plot.axis.IAxis;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.IRegionListener;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ILineTrace;
import org.dawb.common.ui.plot.trace.ISurfaceTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPart;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

/**
 * Will be a thread safe version of all the plotting system methods.
 * 
 * @author fcp94556
 *
 */
public class ThreadSafePlottingSystem implements IPlottingSystem {

	private IPlottingSystem deligate;

	public ThreadSafePlottingSystem(IPlottingSystem deligate) {
		this.deligate = deligate;
	}

	@Override
	public IImageTrace createImageTrace(String traceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILineTrace createLineTrace(String traceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISurfaceTrace createSurfaceTrace(String traceName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTrace(ITrace trace) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeTrace(ITrace trace) {
		// TODO Auto-generated method stub

	}

	@Override
	public ITrace getTrace(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ITrace> getTraces() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<ITrace> getTraces(Class<? extends ITrace> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addTraceListener(ITraceListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeTraceListener(ITraceListener l) {
		// TODO Auto-generated method stub

	}

	@Override
	public void renameTrace(ITrace trace, String name) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public IRegion createRegion(String name, RegionType regionType)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addRegion(IRegion region) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeRegion(IRegion region) {
		// TODO Auto-generated method stub

	}

	@Override
	public IRegion getRegion(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<IRegion> getRegions(RegionType type) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean addRegionListener(IRegionListener l) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeRegionListener(IRegionListener l) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void clearRegions() {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<IRegion> getRegions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void renameRegion(IRegion region, String name) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public IAxis createAxis(String title, boolean isYAxis, int side) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IAxis getSelectedYAxis() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSelectedYAxis(IAxis yAxis) {
		// TODO Auto-generated method stub

	}

	@Override
	public IAxis getSelectedXAxis() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSelectedXAxis(IAxis xAxis) {
		// TODO Auto-generated method stub

	}

	@Override
	public void autoscaleAxes() {
		// TODO Auto-generated method stub

	}

	@Override
	public IAnnotation createAnnotation(String name) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addAnnotation(IAnnotation region) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAnnotation(IAnnotation region) {
		// TODO Auto-generated method stub

	}

	@Override
	public IAnnotation getAnnotation(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearAnnotations() {
		// TODO Auto-generated method stub

	}

	@Override
	public void renameAnnotation(IAnnotation annotation, String name)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void printPlotting() {
		// TODO Auto-generated method stub

	}

	@Override
	public void copyPlotting() {
		// TODO Auto-generated method stub

	}

	@Override
	public String savePlotting(String filename) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void savePlotting(String filename, String filetype) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTitle(String title) {
		// TODO Auto-generated method stub

	}

	@Override
	public void createPlotPart(Composite parent, String plotName,
			IActionBars bars, PlotType hint, IWorkbenchPart part) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getPlotName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ITrace> createPlot1D(AbstractDataset x,
			List<AbstractDataset> ys, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ITrace> createPlot1D(AbstractDataset x,
			List<AbstractDataset> ys, String title, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ITrace> updatePlot1D(AbstractDataset x,
			List<AbstractDataset> ys, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITrace createPlot2D(AbstractDataset image,
			List<AbstractDataset> axes, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITrace updatePlot2D(AbstractDataset image,
			List<AbstractDataset> axes, IProgressMonitor monitor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPlotType(PlotType plotType) {
		// TODO Auto-generated method stub

	}

	@Override
	public void append(String dataSetName, Number xValue, Number yValue,
			IProgressMonitor monitor) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void repaint() {
		// TODO Auto-generated method stub

	}

	@Override
	public Composite getPlotComposite() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISelectionProvider getSelectionProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AbstractDataset getData(String dataSetName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PlotType getPlotType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean is2D() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IActionBars getActionBars() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IPlotActionSystem getPlotActionSystem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDefaultCursor(int cursorType) {
		// TODO Auto-generated method stub

	}

}
