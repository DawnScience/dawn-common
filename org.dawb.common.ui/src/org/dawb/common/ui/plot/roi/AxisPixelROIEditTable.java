package org.dawb.common.ui.plot.roi;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.dawb.common.ui.databinding.AbstractModelObject;
import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.util.number.DoubleUtils;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
import uk.ac.gda.richbeans.components.cell.FieldComponentCellEditor;
import uk.ac.gda.richbeans.components.wrappers.FloatSpinnerWrapper;
import uk.ac.gda.richbeans.components.wrappers.SpinnerWrapper;

/**
 * Class to create a TableViewer that shows ROI information<br> 
 * both with real (axis) and pixel values.<br>
 * This table uses JFace data binding to update its content.
 * TODO make it work with all ROIs (only working for RectangularROI currently)
 * @author wqk87977
 *
 */
public class AxisPixelROIEditTable {

	private Composite parent;
	private TableViewer regionViewer;

	private AxisPixelTableViewModel viewModel;

	private AbstractPlottingSystem plottingSystem;

	private Logger logger = LoggerFactory.getLogger(AxisPixelROIEditTable.class);

	private ROIBase roi;

	private IObservableList values;

	private int precision = 5;

	private boolean isProfile = false;

	private AxisPixelProfileTableViewModel profileViewModel;

	/**
	 * 
	 * @param parent
	 * @param plottingSystem
	 */
	public AxisPixelROIEditTable(Composite parent, AbstractPlottingSystem plottingSystem) {
		this.parent = parent;
		this.plottingSystem = plottingSystem;
	}

	/**
	 * Method to create the Control
	 */
	public void createControl(){
		// if we listen to the main plottingSystem
		if(!isProfile){
			this.viewModel = new AxisPixelTableViewModel();
			final Table table = new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
			regionViewer = buildAndLayoutTable(table);
//			final Label clickToEdit = new Label(parent, SWT.WRAP);
//			clickToEdit.setText("* Click to change");
//			clickToEdit.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));

			// Data binding
			// ViewerSupport.bind takes care of the TableViewer input, 
			// the Label and Content providers and the databinding
			ViewerSupport.bind(regionViewer, viewModel.getValues(),
					BeanProperties.values(new String[] { "name", "start", "end", "diff" }));

		}else{
			this.profileViewModel = new AxisPixelProfileTableViewModel();
			final Table table = new Table(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
			regionViewer = buildAndLayoutTable(table);
//			final Label clickToEdit = new Label(parent, SWT.WRAP);
//			clickToEdit.setText("* Click to change");
//			clickToEdit.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));

			ViewerSupport.bind(regionViewer, profileViewModel.getValues(),
					BeanProperties.values(new String[] { "name", "start", "end", "diff" }));
		}
	}

	private TableViewer buildAndLayoutTable(final Table table) {

		TableViewer tableViewer = new TableViewer(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, true, false, 2, 2));

		TableViewerColumn viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, 0); 
		viewerColumn.getColumn().setText("Name");
		viewerColumn.getColumn().setWidth(80);
		RegionEditingSupport regionEditor = new RegionEditingSupport(tableViewer, 0);
		viewerColumn.setEditingSupport(regionEditor);
		
		viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, 1); 
		viewerColumn.getColumn().setText("Start");
		viewerColumn.getColumn().setWidth(120);
		regionEditor = new RegionEditingSupport(tableViewer, 1);
		viewerColumn.setEditingSupport(regionEditor);

		viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, 2); 
		viewerColumn.getColumn().setText("End");
		viewerColumn.getColumn().setWidth(120);
		regionEditor = new RegionEditingSupport(tableViewer, 2);
		viewerColumn.setEditingSupport(regionEditor);

		viewerColumn = new TableViewerColumn(tableViewer, SWT.NONE, 3);
		if(!isProfile)
			viewerColumn.getColumn().setText("Width(X), Height(Y)");
		else
			viewerColumn.getColumn().setText("Width");
		viewerColumn.getColumn().setWidth(120);
		regionEditor = new RegionEditingSupport(tableViewer, 3);
		viewerColumn.setEditingSupport(regionEditor);

		return tableViewer;
	}

	/**
	 * EditingSupport Class
	 *
	 */
	private class RegionEditingSupport extends EditingSupport {

		private int column;

		public RegionEditingSupport(ColumnViewer viewer, int col) {
			super(viewer);
			this.column = col;
		}
		@Override
		protected CellEditor getCellEditor(final Object element) {
			
			FieldComponentCellEditor ed = null;
			try {
				ed = new FieldComponentCellEditor(((TableViewer)getViewer()).getTable(), 
						                     FloatSpinnerWrapper.class.getName(), SWT.RIGHT);
			} catch (ClassNotFoundException e) {
				logger.error("Cannot get FieldComponentCellEditor for "+SpinnerWrapper.class.getName(), e);
				return null;
			}
			
			final FloatSpinnerWrapper   rb = (FloatSpinnerWrapper)ed.getFieldWidget();
			if (rb.getPrecision() < 3)
				rb.setFormat(rb.getWidth(), 3);
			
			rb.setMaximum(Double.MAX_VALUE);
			rb.setMinimum(-Double.MAX_VALUE);

			rb.setButtonVisible(false);
			rb.setActive(true);
			
			((Spinner) rb.getControl())
					.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							setValue(element, rb.getValue(), false);
						}
					});	
			return ed;
		}

		@Override
		protected boolean canEdit(Object element) {
			if (column==0) return false;
			else return true;
		}

		@Override
		protected Object getValue(Object element) {
			final AxisPixelRowDataModel row = (AxisPixelRowDataModel)element;
			switch (column){
			case 0:
				return row.getName();
			case 1:
				return row.getStart();
			case 2:
				return row.getEnd();
			case 3:
				return row.getDiff();
			default:
				return null;
			}
		}

		@Override
		protected void setValue(Object element, Object value) {
			this.setValue(element, value, true);
		}
		
		//@SuppressWarnings("unchecked")
		protected void setValue(Object element, Object value, boolean tableRefresh) {

			final AxisPixelRowDataModel row = (AxisPixelRowDataModel) element;
			
			switch (column){
			case 0:
				row.setName((String)value);
				break;
			case 1:
				row.setStart((Double)value);
				row.setDiff(row.getEnd() - row.getStart());
				break;
			case 2:
				row.setEnd((Double)value);
				// set new diff
				row.setDiff(row.getEnd() - row.getStart());
				break;
			case 3:
				row.setDiff((Double)value);
				// set new end
				row.setEnd(row.getStart() + row.getDiff());
				break;
			default:
				break;
			}

			if (tableRefresh) {
				getViewer().refresh();
			}
			if(!isProfile)
				roi = createRoi(viewModel.getValues());
			else
				roi = createRoi(profileViewModel.getValues());
			setTableValues(roi);
		}

	}

	/**
	 * Method that creates a ROI using the input of the table viewer
	 * @param rows
	 * @return ROIBase
	 */
	private ROIBase createRoi(IObservableList rows) {
		
		double ptx = 0, pty = 0, width = 0, height = 0, angle = 0;
		ROIBase ret = null; 
		if (roi == null)
			roi = plottingSystem.getRegions().iterator().next().getROI();
		if (roi instanceof RectangularROI) {
			if(!isProfile){
//				if(rows.get(0) instanceof AxisPixelRowDataModel){
//					//Convert from Axis to Pixel values
//					AxisPixelRowDataModel xAxisRow = (AxisPixelRowDataModel) rows.get(0);
//					
//					// We get the axes data to convert from the axis to pixel values
//					Collection<ITrace> traces = plottingSystem.getTraces();
//					Iterator<ITrace> it = traces.iterator();
//					while(it.hasNext()){
//						ITrace trace = it.next();
//						if(trace instanceof IImageTrace){
//							IImageTrace image = (IImageTrace)trace;
//							List<AbstractDataset> axes = image.getAxes();
//							// x axis and width
//							ptx = axes.get(0).getDouble((int)Math.round(xAxisRow.getStart()));
//							double ptxEnd =axes.get(0).getDouble((int)Math.round(xAxisRow.getEnd()));
//							width = ptxEnd - ptx;
//						}
//					}
//				}
//				if(rows.get(1) instanceof AxisPixelRowDataModel){
//					//Convert from Axis to Pixel values
//					AxisPixelRowDataModel yAxisRow = (AxisPixelRowDataModel) rows.get(1);
//					// We get the axes data to convert from the axis to pixel values
//					Collection<ITrace> traces = plottingSystem.getTraces();
//					Iterator<ITrace> it = traces.iterator();
//					while(it.hasNext()){
//						ITrace trace = it.next();
//						if(trace instanceof IImageTrace){
//							IImageTrace image = (IImageTrace)trace;
//							List<AbstractDataset> axes = image.getAxes();
//							// x axis and width
//							pty = axes.get(1).getDouble((int)Math.round(yAxisRow.getStart()));
//							double ptyEnd =axes.get(1).getDouble((int)Math.round(yAxisRow.getEnd()));
//							height = ptyEnd - pty;
//						}
//					}
//				}
				if(rows.get(2) instanceof AxisPixelRowDataModel){
					AxisPixelRowDataModel xPixelRow = (AxisPixelRowDataModel) rows.get(2);
					ptx = xPixelRow.getStart();
					width = xPixelRow.getDiff();
				}
				if(rows.get(3) instanceof AxisPixelRowDataModel){
					AxisPixelRowDataModel yPixelRow = (AxisPixelRowDataModel) rows.get(3);
					pty = yPixelRow.getStart();
					height = yPixelRow.getDiff();
				}
			} else {
				if(rows.get(0) instanceof AxisPixelRowDataModel){
					//Convert from Axis to Pixel values
					AxisPixelRowDataModel xAxisRow = (AxisPixelRowDataModel) rows.get(0);
					ptx = xAxisRow.getStart();
					double ptxEnd = xAxisRow.getEnd();
					width = ptxEnd - ptx;
				}
				pty = roi.getPointY();
				height = ((RectangularROI) roi).getEndPoint()[1] - pty;
			}
			RectangularROI rr = new RectangularROI(ptx, pty, width, height, angle);
			ret = rr;
		}
		return ret;
	}

	public IStructuredSelection getSelection() {
		return (IStructuredSelection) regionViewer.getSelection();
	}

	public void setSelection(IStructuredSelection selection) {
		regionViewer.setSelection(selection, true);
	}

	/**
	 * Method that returns the TableViewer
	 * @return TableViewer
	 */
	public TableViewer getTableViewer(){
		return regionViewer;
	}

	/**
	 * Method used to set the {@link}AxisPixelROIEditTable<br>
	 * to listen to the main plottingSystem or to a profile plottingSystems<br>
	 * By default this class will create a table viewer used to listen to a<br>
	 * main plottingSystem.
	 * 
	 * @param isProfileTable
	 */
	public void setIsProfileTable(boolean isProfileTable){
		this.isProfile  = isProfileTable;
	}

	/**
	 * Method that sets the table viewer values given a Region of Interest
	 * @param region
	 */
	public void setTableValues(ROIBase region) {
		roi = region;

		RectangularROI rroi = (RectangularROI)roi;
		double xStart = roi.getPointX();
		double yStart = roi.getPointY();
		double xEnd = rroi.getEndPoint()[0];
		double yEnd = rroi.getEndPoint()[1];

		if(!isProfile){
			values = viewModel.getValues();
			AxisPixelRowDataModel xAxisRow = (AxisPixelRowDataModel)values.get(0);
			AxisPixelRowDataModel yAxisRow = (AxisPixelRowDataModel)values.get(1);
			AxisPixelRowDataModel xPixelRow = (AxisPixelRowDataModel)values.get(2);
			AxisPixelRowDataModel yPixelRow = (AxisPixelRowDataModel)values.get(3);
			try{
				// We get the axes data to convert from the pixel to axis values
				Collection<ITrace> traces = plottingSystem.getTraces();
				Iterator<ITrace> it = traces.iterator();
				while(it.hasNext()){
					ITrace trace = it.next();
					if(trace instanceof IImageTrace){
						IImageTrace image = (IImageTrace)trace;
						List<AbstractDataset> axes = image.getAxes();
						if(axes != null){
							// x axis and width
							double xAxisStart = axes.get(0).getElementDoubleAbs((int)Math.round(xStart));
							double xAxisEnd =axes.get(0).getElementDoubleAbs((int)(int)Math.round(xEnd));
							xAxisRow.setStart(DoubleUtils.roundDouble(xAxisStart, precision));
							xAxisRow.setEnd(DoubleUtils.roundDouble(xAxisEnd, precision));
							xAxisRow.setDiff(DoubleUtils.roundDouble(xAxisEnd-xAxisStart, precision));
							// yaxis and height
							double yAxisStart = axes.get(1).getElementDoubleAbs((int)Math.round(yStart));
							double yAxisEnd =axes.get(1).getElementDoubleAbs((int)(int)Math.round(yEnd));
							yAxisRow.setStart(DoubleUtils.roundDouble(yAxisStart, precision));
							yAxisRow.setEnd(DoubleUtils.roundDouble(yAxisEnd, precision));
							yAxisRow.setDiff(DoubleUtils.roundDouble(yAxisEnd-yAxisStart, precision));
							
						} else { //if no axes we set them manually according to the data shape
							int[] shapes = image.getData().getShape();
					
							int[] xAxis = new int[shapes[0]];
							for(int i = 0; i < xAxis.length; i ++){
								xAxis[i] = i;
							}
							AbstractDataset xData = new IntegerDataset(xAxis, shapes[0]);
							
							int[] yAxis = new int[shapes[1]];
							for(int i = 0; i < yAxis.length; i ++){
								yAxis[i] = i;
							}
							AbstractDataset yData = new IntegerDataset(yAxis, shapes[1]);

							// x axis and width
							double xAxisStart = xData.getElementDoubleAbs((int)Math.round(xStart));
							double xAxisEnd = xData.getElementDoubleAbs((int)(int)Math.round(xEnd));
							xAxisRow.setStart(DoubleUtils.roundDouble(xAxisStart, precision));
							xAxisRow.setEnd(DoubleUtils.roundDouble(xAxisEnd, precision));
							xAxisRow.setDiff(DoubleUtils.roundDouble(xAxisEnd-xAxisStart, precision));
							// yaxis and height
							double yAxisStart = yData.getElementDoubleAbs((int)Math.round(yStart));
							double yAxisEnd = yData.getElementDoubleAbs((int)(int)Math.round(yEnd));
							yAxisRow.setStart(DoubleUtils.roundDouble(yAxisStart, precision));
							yAxisRow.setEnd(DoubleUtils.roundDouble(yAxisEnd, precision));
							yAxisRow.setDiff(DoubleUtils.roundDouble(yAxisEnd-yAxisStart, precision));
						}

					}
				}

				xPixelRow.setStart(DoubleUtils.roundDouble(xStart, precision));
				xPixelRow.setEnd(DoubleUtils.roundDouble(xEnd, precision));
				xPixelRow.setDiff(DoubleUtils.roundDouble(xEnd-xStart, precision));
				yPixelRow.setStart(DoubleUtils.roundDouble(yStart, precision));
				yPixelRow.setEnd(DoubleUtils.roundDouble(yEnd, precision));
				yPixelRow.setDiff(DoubleUtils.roundDouble(yEnd-yStart, precision));
			} catch (ArrayIndexOutOfBoundsException ae) {
				// do nothing
			} catch (Exception e) {
				logger .debug("Error while updating the AxisPixelEditTable:"+ e);
			}
		} else {
			values = profileViewModel.getValues();
			AxisPixelRowDataModel xAxisRow = (AxisPixelRowDataModel)values.get(0);
//			AxisPixelRowDataModel xPixelRow = (AxisPixelRowDataModel)values.get(1);
			xAxisRow.setStart(DoubleUtils.roundDouble(xStart, precision));
			xAxisRow.setEnd(DoubleUtils.roundDouble(xEnd, precision));
			xAxisRow.setDiff(DoubleUtils.roundDouble(xEnd-xStart, precision));
			
//			xPixelRow.setStart(roundDouble(xStart, precision));
//			xPixelRow.setEnd(roundDouble(xEnd, precision));
//			xPixelRow.setDiff(roundDouble(xEnd-xStart, precision));
		}
	}

	/**
	 * Methods that returns the current ROI
	 * @return ROIBase
	 */
	public ROIBase getROI(){
		return roi;
	}

	public void setValues(IObservableList values) {
		this.values = values;
	}

	/**
	 * Method to add a SelectionChangedListener to the TableViewer
	 * @param listener
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		regionViewer.addSelectionChangedListener(listener);
	}

	/**
	 * Method to remove a SelectionChangedListener from the TableViewer
	 * @param listener
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener){
		regionViewer.removeSelectionChangedListener(listener);
	}

	/**
	 * View Model of  AxisPixel Table: main one
	 *
	 */
	private class AxisPixelTableViewModel {

		private IObservableList rows = new WritableList();

		private AxisPixelRowDataModel xAxisRow;
		private AxisPixelRowDataModel yAxisRow;
		private AxisPixelRowDataModel xPixelRow;
		private AxisPixelRowDataModel yPixelRow;

		final private String description0 = "X-Axis values (Not yet editable)";
		final private String description1 = "Y-Axis values (Not yet editable)"; 
		final private String description2 = "X values as pixels (Editable)"; 
		final private String description3 = "X values as pixels (Editable)"; 

		{
			xAxisRow = new AxisPixelRowDataModel(new String("X Axis"), new Double(0), new Double(0), new Double(0), description0);
			yAxisRow = new AxisPixelRowDataModel(new String("Y Axis"), new Double(0), new Double(0), new Double(0), description1); 
			xPixelRow = new AxisPixelRowDataModel(new String("X Pixel"), new Double(0), new Double(0), new Double(0), description2); 
			yPixelRow = new AxisPixelRowDataModel(new String("Y Pixel"), new Double(0), new Double(0), new Double(0), description3); 

			rows.add(xAxisRow);
			rows.add(yAxisRow);
			rows.add(xPixelRow);
			rows.add(yPixelRow);
		}

		public IObservableList getValues() {
			return rows;
		}
	}

	/**
	 * View Model of  AxisPixel Table: profile one
	 *
	 */
	private class AxisPixelProfileTableViewModel {

		private IObservableList rows = new WritableList();

		final private String description = "X-Axis values"; 
		private AxisPixelRowDataModel xAxisRow;
//		private AxisPixelRowDataModel xPixelRow;

		{
			xAxisRow = new AxisPixelRowDataModel(new String("X Axis"), new Double(0), new Double(0), new Double(0), description);
//			xPixelRow = new AxisPixelRowDataModel(new String("X Pixel"), new Double(0), new Double(0), new Double(0)); 

			rows.add(xAxisRow);
//			rows.add(xPixelRow);
		}

		public IObservableList getValues() {
			return rows;
		}
	}

	/**
	 * Model object for a Region Of Interest row used in an AxisPixel Table
	 * @author wqk87977
	 *
	 */
	private class AxisPixelRowDataModel extends AbstractModelObject {
		private String name;
		private double start;
		private double end;
		private double diff;
		private String description;

		public AxisPixelRowDataModel(String name, double start, double end, double diff, String description) {
			this.name = name;
			this.start = start;
			this.end = end;
			this.diff = diff;
			this.description = description;
		}

		public String getName() {
			return name;
		}

		public double getStart() {
			return start;
		}

		public double getEnd() {
			return end;
		}

		public double getDiff() {
			return diff;
		}

		@SuppressWarnings("unused")
		/**
		 * TODO add a description in a tool tip<br>
		 * but this can only be done by providing our own LabelProvider<br>
		 * which is currently done by the ViewerSupport.bind mechanism.<br>
		 * 
		 * @return string
		 */
		public String getDescription(){
			return description;
		}

		public void setName(String name){
			String oldValue = this.name;
			this.name = name;
			firePropertyChange("name", oldValue, this.name);
		}

		public void setStart(double start) {
			double oldValue = this.start;
			this.start = start;
			firePropertyChange("start", oldValue, this.start);
		}

		public void setEnd(double end) {
			double oldValue = this.end;
			this.end = end;
			firePropertyChange("end", oldValue, this.end);
		}

		public void setDiff(double diff) {
			double oldValue = this.diff;
			this.diff = diff;
			firePropertyChange("diff", oldValue, this.diff);
		}
	}
}