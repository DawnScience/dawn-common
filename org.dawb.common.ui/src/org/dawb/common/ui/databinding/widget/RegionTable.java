package org.dawb.common.ui.databinding.widget;

import org.dawb.common.ui.databinding.model.RegionRowDataModel;
import org.dawb.common.ui.databinding.model.RegionTableViewModel;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.jface.databinding.viewers.ViewerSupport;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import org.dawnsci.common.widgets.celleditor.FloatSpinnerCellEditor;

/**
 * A region table which is binded to its corresponding view model
 * @author wqk87977
 *
 */
public class RegionTable extends Composite {

	private static final String NAME_PROPERTY = "name";

	private static final String XSTART_PROPERTY = "xStart";

	private static final String XEND_PROPERTY = "xEnd";

	private static final String WIDTH_PROPERTY = "width";

	private static final String YSTART_PROPERTY = "yStart";

	private static final String YEND_PROPERTY = "yEnd";

	private static final String HEIGHT_PROPERTY = "height";

	private TableViewer m_regionViewer;

	private RegionTableViewModel viewModel;

	public RegionTable(Composite parent, RegionTableViewModel viewModel) {
		super(parent, SWT.NONE);
		this.viewModel = viewModel;
		buildControls(parent);
	}

	protected void buildControls(Composite parent) {

		final Table table = new Table(parent, SWT.FULL_SELECTION);
		m_regionViewer = buildAndLayoutTable(table);

		// celleditors
		attachCellEditors(m_regionViewer, table);

		// Data binding
		// ViewerSupport.bind takes care of the TableViewer input, 
		// the Label and Content providers and the databinding
		ViewerSupport.bind(m_regionViewer, viewModel.getValues(),
				BeanProperties.values(new String[] { "name", "xStart", "xEnd", "width", "yStart", "yEnd", "height" }));

	}

	private TableViewer buildAndLayoutTable(final Table table) {

		TableViewer tableViewer = new TableViewer(table);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TableViewerColumn columnTableType = new TableViewerColumn(tableViewer, SWT.NONE, 0); 
		columnTableType.getColumn().setText("Type");
		columnTableType.getColumn().setWidth(80);

		final TableViewerColumn columnTableXStart = new TableViewerColumn(tableViewer, SWT.NONE, 1); 
		columnTableXStart.getColumn().setText("X Start");
		columnTableXStart.getColumn().setWidth(100);

		final TableViewerColumn columnTableXEnd = new TableViewerColumn(tableViewer, SWT.NONE, 2); 
		columnTableXEnd.getColumn().setText("X End");
		columnTableXEnd.getColumn().setWidth(100);

		final TableViewerColumn columnTableWidth = new TableViewerColumn(tableViewer, SWT.NONE, 3); 
		columnTableWidth.getColumn().setText("Width");
		columnTableWidth.getColumn().setWidth(100);

		final TableViewerColumn columnTableYStart = new TableViewerColumn(tableViewer, SWT.NONE, 4); 
		columnTableYStart.getColumn().setText("Y Start");
		columnTableYStart.getColumn().setWidth(100);

		final TableViewerColumn columnTableYEnd = new TableViewerColumn(tableViewer, SWT.NONE, 5); 
		columnTableYEnd.getColumn().setText("Y End");
		columnTableYEnd.getColumn().setWidth(100);

		final TableViewerColumn columnTableHeight = new TableViewerColumn(tableViewer, SWT.NONE, 6); 
		columnTableHeight.getColumn().setText("Height");
		columnTableHeight.getColumn().setWidth(100);

		return tableViewer;
	}

	private void attachCellEditors(final TableViewer viewer, Composite parent) {
		viewer.setCellModifier(new ICellModifier() {
			public boolean canModify(Object element, String property) {
				if(property.equals(NAME_PROPERTY))
					return false;
				return true;
			}

			public Object getValue(Object element, String property) {
				RegionRowDataModel row = ((RegionRowDataModel) element);
				if(property.equals(NAME_PROPERTY))
					return row.getName();
				else if (property.equals(XSTART_PROPERTY))
					return row.getxStart();
				else if (property.equals(XEND_PROPERTY))
					return row.getxEnd();
				else if (property.equals(WIDTH_PROPERTY))
					return row.getWidth();
				else if (property.equals(YSTART_PROPERTY))
					return row.getyStart();
				else if (property.equals(YEND_PROPERTY))
					return row.getyEnd();
				else if (property.equals(HEIGHT_PROPERTY))
					return row.getHeight();
				else
					return null;
			}

			public void modify(Object element, String property, Object value) {
				TableItem tableItem = (TableItem) element;
				RegionRowDataModel row = (RegionRowDataModel) tableItem.getData();
				if (property.equals(NAME_PROPERTY))
					row.setName((String) value);
				else if (property.equals(XSTART_PROPERTY))
					row.setxStart((Double) value);
				else if (property.equals(XEND_PROPERTY))
					row.setxEnd((Double) value);
				else if (property.equals(WIDTH_PROPERTY))
					row.setWidth((Double) value);
				else if (property.equals(YSTART_PROPERTY))
					row.setyStart((Double) value);
				else if (property.equals(YEND_PROPERTY))
					row.setyEnd((Double) value);
				else if (property.equals(HEIGHT_PROPERTY))
					row.setHeight((Double) value);
				viewer.refresh(row);
			}
		});

		FloatSpinnerCellEditor xStartSpinner = new FloatSpinnerCellEditor(parent);
		xStartSpinner.setMaximum(Double.MAX_VALUE);
		xStartSpinner.setMinimum(-Double.MAX_VALUE);

		FloatSpinnerCellEditor xEndSpinner = new FloatSpinnerCellEditor(parent);
		xEndSpinner.setMaximum(Double.MAX_VALUE);
		xEndSpinner.setMinimum(-Double.MAX_VALUE);

		FloatSpinnerCellEditor widthSpinner = new FloatSpinnerCellEditor(parent);
		widthSpinner.setMaximum(Double.MAX_VALUE);
		widthSpinner.setMinimum(-Double.MAX_VALUE);

		FloatSpinnerCellEditor yStartSpinner = new FloatSpinnerCellEditor(parent);
		yStartSpinner.setMaximum(Double.MAX_VALUE);
		yStartSpinner.setMinimum(-Double.MAX_VALUE);

		FloatSpinnerCellEditor yEndSpinner = new FloatSpinnerCellEditor(parent);
		yEndSpinner.setMaximum(Double.MAX_VALUE);
		yEndSpinner.setMinimum(-Double.MAX_VALUE);

		FloatSpinnerCellEditor heightSpinner = new FloatSpinnerCellEditor(parent);
		heightSpinner.setMaximum(Double.MAX_VALUE);
		heightSpinner.setMinimum(-Double.MAX_VALUE);
		viewer.setCellEditors(new CellEditor[] { new TextCellEditor(parent),
				xStartSpinner, xEndSpinner, widthSpinner, 
				yStartSpinner, yEndSpinner, heightSpinner });

		viewer.setColumnProperties(new String[] { NAME_PROPERTY,
				XSTART_PROPERTY, XEND_PROPERTY, WIDTH_PROPERTY,
				YSTART_PROPERTY, YEND_PROPERTY, HEIGHT_PROPERTY });
	}

	public IStructuredSelection getSelection() {
		return (IStructuredSelection) m_regionViewer.getSelection();
	}

	public void setSelection(IStructuredSelection selection) {
		m_regionViewer.setSelection(selection, true);
	}

	public TableViewer getTableViewer(){
		return m_regionViewer;
	}
}