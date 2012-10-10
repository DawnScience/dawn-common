/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.common.ui.slicing;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.ImageServiceBean.ImageOrigin;
import org.dawb.common.ui.Activator;
import org.dawb.common.ui.DawbUtils;
import org.dawb.common.ui.components.cell.ScaleCellEditor;
import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.IPaletteListener;
import org.dawb.common.ui.plot.trace.ITraceListener;
import org.dawb.common.ui.plot.trace.ITraceListener.Stub;
import org.dawb.common.ui.plot.trace.PaletteEvent;
import org.dawb.common.ui.plot.trace.TraceEvent;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.hdf5.nexus.NexusUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.SliceObject;
import uk.ac.gda.richbeans.components.cell.CComboCellEditor;
import uk.ac.gda.richbeans.components.cell.SpinnerCellEditorWithPlayButton;
import uk.ac.gda.richbeans.components.scalebox.RangeBox;
import uk.ac.gda.richbeans.event.ValueAdapter;
import uk.ac.gda.richbeans.event.ValueEvent;


/**
 * Dialog to slice multi-dimensional data to images and 1D plots.
 * 
 * Copied from nexus tree viewer but in a simpler to use UI.
 *  
 * TODO Perhaps move this dialog to top level GUI
 */
public class SliceComponent {
	
	private static final Logger logger = LoggerFactory.getLogger(SliceComponent.class);

	private static final List<String> COLUMN_PROPERTIES = Arrays.asList(new String[]{"Dimension","Axis","Slice"});
	
	private SliceObject     sliceObject;
	private int[]           dataShape;
	private IPlottingSystem plottingSystem;
	private boolean         autoUpdate=true;

	private TableViewer     viewer;
	private DimsDataList    dimsDataList;

	private CLabel          errorLabel, explain;
	private Button          updateAutomatically;
	private Composite       area;
	private boolean         isErrorCondition=false;
    private SliceJob        sliceJob;
    private String          sliceReceiverId;
    private CCombo          editorCombo;

	private PlotType imagePlotType = PlotType.IMAGE; // Could also be PlotType.PT1D_MULTI
	
	/**
	 * 0=x, 1=y, 2=z
	 */
	private Map<Integer,Label>  dimensionLabels;
	/**
	 * 0=x, 1=y, 2=z
	 */
	private Map<Integer,CCombo> dimensionChoice;

	private Stub traceListener;

	
	public SliceComponent(final String sliceReceiverId) {
		this.sliceReceiverId = sliceReceiverId;
		this.sliceJob        = new SliceJob();
	}
	
	/**
	 * Please call setPlottingSystem(...) before createPartControl(...) if
	 * you would like the part to show controls for images.
	 * 
	 * @param parent
	 * @return
	 */
	public Control createPartControl(Composite parent) {
		
		this.area = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout(1, false));
		
		this.explain = new CLabel(area, SWT.WRAP);
		final GridData eData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		eData.heightHint=44;
		explain.setLayoutData(eData);

		final Composite top = new Composite(area, SWT.NONE);
		top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		top.setLayout(new GridLayout(2, false));
	
		this.viewer = new TableViewer(area, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.getTable().addListener(SWT.MouseDoubleClick, new Listener() {
			public void handleEvent(Event event) {
				event.doit=false;
				// Do nothing disabled
			}
		});		

		
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().addListener(SWT.MeasureItem, new Listener() {
			public void handleEvent(Event event) {
				event.height = 45;
			}
		});

		createColumns(viewer);
		viewer.setUseHashlookup(true);
		viewer.setColumnProperties(COLUMN_PROPERTIES.toArray(new String[COLUMN_PROPERTIES.size()]));
		viewer.setCellEditors(createCellEditors(viewer));
		viewer.setCellModifier(createModifier(viewer));
			
		
		this.errorLabel = new CLabel(area, SWT.NONE);
		errorLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		errorLabel.setImage(Activator.getImageDescriptor("icons/error.png").createImage());
		GridUtils.setVisible(errorLabel,         false);
		
		final Composite bottom = new Composite(area, SWT.NONE);
		bottom.setLayout(new GridLayout(2, false));
		bottom.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, true));

		final Composite bRight = new Composite(bottom, SWT.NONE);
		bRight.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		bRight.setLayout(new GridLayout(1, false));
		
		final Composite editorComp = new Composite(bRight, SWT.NONE);
		editorComp.setLayout(new GridLayout(2, false));
		editorComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		final Label label = new Label(editorComp, SWT.NONE);
		label.setText("Edit slice with");
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		
		this.editorCombo = new CCombo(editorComp, SWT.READ_ONLY|SWT.BORDER);
		editorCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		editorCombo.setText("Slice editor");
		editorCombo.setItems(new String[]{"Scale", "Enter slice index"});// Later "Range"
		editorCombo.select(0);
		editorCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				final boolean editing = viewer.isCellEditorActive();
				final Object edit = ((StructuredSelection)viewer.getSelection()).getFirstElement();
				
				final CellEditor[] editors = viewer.getCellEditors();
				if (editorCombo.getSelectionIndex()==0) {
					editors[2] = scaleEditor;
				} else if (editorCombo.getSelectionIndex()==1) {
					editors[2] = spinnerEditor;
				}
				if (editing) {
					viewer.cancelEditing();
					viewer.editElement(edit, 2);
				}
			}
		});

		this.updateAutomatically = new Button(bRight, SWT.CHECK);
		updateAutomatically.setText("Automatic update");
		updateAutomatically.setToolTipText("Update plot when slice changes");
		updateAutomatically.setSelection(true);
		updateAutomatically.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		updateAutomatically.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				autoUpdate = updateAutomatically.getSelection();
				slice(false);
			}
		});
		
      		
		final Composite bLeft = new Composite(bottom, SWT.NONE);
		bLeft.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));
		bLeft.setLayout(new GridLayout(1, false));
		
		Button openGallery = new Button(bLeft, SWT.NONE);
		openGallery.setToolTipText("Open data set in a gallery.");
		openGallery.setImage(Activator.getImageDescriptor("icons/imageStack.png").createImage());
		openGallery.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				openGallery();
			}
		});
				
		// Axes choice is available for nexus data formats
		Group axes = new Group(bottom, SWT.NONE);
		axes.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		axes.setLayout(new GridLayout(2, false));
		axes.setText("Axes");
		dimensionLabels = new HashMap<Integer, Label>(3);
		dimensionChoice  = new HashMap<Integer, CCombo>(3);
		
		Label dim1Label = new Label(axes, SWT.NONE);
		dim1Label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		dim1Label.setText("Dimension 1 ");
		dimensionLabels.put(0, dim1Label);
		
		CCombo dim1Choice = new CCombo(axes, SWT.READ_ONLY|SWT.BORDER);
		dim1Choice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		dim1Choice.setItems(new String[]{"indices"});
		dim1Choice.select(0);
		dim1Choice.addSelectionListener(new AxisListener(3));
		dimensionChoice.put(0, dim1Choice);
		
		Label dim2Label = new Label(axes, SWT.NONE);
		dim2Label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		dim2Label.setText("Dimension 2 ");
		dimensionLabels.put(1, dim2Label);
		
		CCombo dim2Choice = new CCombo(axes, SWT.READ_ONLY|SWT.BORDER);
		dim2Choice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		dim2Choice.setItems(new String[]{"indices"});
		dim2Choice.select(0);
		dim2Choice.addSelectionListener(new AxisListener(1));
		dimensionChoice.put(1, dim2Choice);
		
		Label dim3Label = new Label(axes, SWT.NONE);
		dim3Label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		dim3Label.setText("Dimension 3 ");
		dimensionLabels.put(2, dim3Label);
	
		CCombo dim3Choice = new CCombo(axes, SWT.READ_ONLY|SWT.BORDER);
		dim3Choice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		dim3Choice.setItems(new String[]{"indices"});
		dim3Choice.select(0);
		dim3Choice.addSelectionListener(new AxisListener(2));
		dimensionChoice.put(2, dim3Choice);

		// Something to tell them their image orientation (X and Y may be mixed up!)
		if (plottingSystem!=null) {
			final Label imageOrientation = new Label(bottom, SWT.NONE);
			imageOrientation.setText("Image Orientation:  "+getImageOrientation());
			imageOrientation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
			GridUtils.setVisible(imageOrientation, plottingSystem.is2D());
			
			addImageOrientationListener(imageOrientation);
			
			this.traceListener = new ITraceListener.Stub() {
				protected void update(TraceEvent evt) {
					imageOrientation.setText("Image Orientation:  "+getImageOrientation());
					GridUtils.setVisible(imageOrientation, plottingSystem.is2D());
					area.layout();
					addImageOrientationListener(imageOrientation);
				}
			};
			imageOrientation.setToolTipText("The image orientation may mean that X and Y are reversed.");
			plottingSystem.addTraceListener(traceListener);
		}

		// Same action on slice table
		final MenuManager man = new MenuManager();
		final Action openGal  = new Action("Open data in gallery", Activator.getImageDescriptor("icons/imageStack.png")) {
			public void run() {openGallery();}
		};
		man.add(openGal);
		final Menu menu = man.createContextMenu(viewer.getTable());
		viewer.getTable().setMenu(menu);

		viewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {
				sliceJob.cancel();
			}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

			@Override
			public Object[] getElements(Object inputElement) {
				if (dimsDataList==null) return DimsDataList.getDefault();
				return dimsDataList.getElements();
			}
		});
		viewer.setInput(new Object());
    	
		return area;
	}
	
	private String getImageOrientation() {
		try {
			final IImageTrace trace  = (IImageTrace)plottingSystem.getTraces(IImageTrace.class).iterator().next();
            final ImageOrigin io     = trace.getImageOrigin();
            StringBuffer buf = new StringBuffer(io.getLabel());
            if (io==ImageOrigin.TOP_LEFT || io==ImageOrigin.BOTTOM_RIGHT) {
            	buf.append("    (X and Y are reversed)");
            }
            return buf.toString();
		} catch (Exception ne) {
			return "";
		}
	}
	
	private IPaletteListener orientationListener;
	private void addImageOrientationListener(final Label imageOrientation) {
		try {
			final IImageTrace trace  = (IImageTrace)plottingSystem.getTraces(IImageTrace.class).iterator().next();
            if (orientationListener == null) {
            	orientationListener = new IPaletteListener.Stub() {
            		@Override
            		public void imageOriginChanged(PaletteEvent evt) {
    					imageOrientation.setText("Image Orientation:  "+getImageOrientation());
           		    }      
            	};
            }
            // PaletteListeners are cleared when traces are removed.
            trace.addPaletteListener(orientationListener);

		} catch (Exception ne) {
			return;
		}
	}


	public void updateAxesVisibility() {
		if (dimsDataList!=null) {
			final int iaxes = dimsDataList.getAxisCount();
			boolean vis = iaxes>0;
			GridUtils.setVisible(dimensionLabels.get(0), vis);
			GridUtils.setVisible(dimensionChoice.get(0),  vis);
			
			vis = iaxes>1;
			GridUtils.setVisible(dimensionLabels.get(1), vis);
			GridUtils.setVisible(dimensionChoice.get(1),  vis);
			
			vis = iaxes>2;
			GridUtils.setVisible(dimensionLabels.get(2), vis);
			GridUtils.setVisible(dimensionChoice.get(2),  vis);
			
			this.area.layout();
		}
	}
	
	/**
	 * Extracts axes from nexus data.
	 */
	private void updateAxesChoices(boolean clearOldSelection) {

		for (CCombo combo : dimensionChoice.values()) {
			combo.setItems(new String[]{"indices"});
		}
		// We now match the dim in the table with the axes choices for that dim.
		// Dim 1,2 and 3 can be assigned by the user 
		int index = 0;
		int[] nexusIndices = new int[]{3,1,2};
		for (DimsData dd : dimsDataList.getDimsData()) {
			if (dd.getAxis()<0) continue;
			updateAxis(dimensionChoice.get(dd.getAxis()), nexusIndices[index], clearOldSelection);
			++index;
		}
		area.layout();
	}

	
	private void updateAxis(CCombo axis, int iaxis, boolean clearOldSelection) {
		try {    	
			final List<String> names = NexusUtils.getAxisNames(sliceObject.getPath(), sliceObject.getName(), iaxis);
			final String[] items = names!=null ? names.toArray(new String[names.size()+1]) : new String[1];
			items[items.length-1]= "indices";
			int iselection = !clearOldSelection ? axis.getSelectionIndex() : 0;
			axis.setItems(items);
			try {
				axis.select(iselection);
				sliceObject.setNexusAxis(iaxis, items[iselection]);
			} catch (Throwable ne) {
				axis.select(0);
				sliceObject.setNexusAxis(iaxis, items[0]);
			}
		} catch (Exception e) {
			logger.info("Cannot assign axes!", e);
			axis.setItems(new String[]{"indices"});
			axis.select(0);
			sliceObject.setNexusAxis(iaxis, "indices");
		}		
	}
	
	private class AxisListener extends SelectionAdapter {
		
		private int iaxis;
		AxisListener(int iaxis) {
			this.iaxis = iaxis;
		}
		public void widgetSelected(SelectionEvent e) {
			final CCombo combo = (CCombo)e.getSource();
			final String axis  = combo.getItem(combo.getSelectionIndex());
			sliceObject.setNexusAxis(iaxis, axis);
			slice(true);
		}
	}

	protected void openGallery() {
		
		if (sliceReceiverId==null) return;
		final SliceObject cs = SliceUtils.createSliceObject(dimsDataList, dataShape, sliceObject);
		
		IViewPart view;
		try {
			view = EclipseUtils.getActivePage().showView(sliceReceiverId);
		} catch (PartInitException e) {
			logger.error("Cannot find view "+sliceReceiverId);
			return;
		}
		if (view instanceof ISliceReceiver) {
			((ISliceReceiver)view).updateSlice(dataShape, cs);
		}
		
	}

	private void createDimsData() {
		
		final int dims = dataShape.length;
		
		if (plottingSystem!=null) {
			final File dataFile     = new File(sliceObject.getPath());
			final File lastSettings = new File(DawbUtils.getDawnHome()+dataFile.getName()+"."+sliceObject.getName()+".xml");
			if (lastSettings.exists()) {
				XMLDecoder decoder = null;
				try {
					this.dimsDataList = new DimsDataList();
					decoder = new XMLDecoder(new FileInputStream(lastSettings));
					for (int i = 0; i < dims; i++) {
						dimsDataList.add((DimsData)decoder.readObject());
					}
					
				} catch (Exception ne) {
					// This might not always be an error.
					logger.debug("Cannot load slice data from last settings!");
				} finally {
					if (decoder!=null) decoder.close();
				}
			}
		}
		
		if (dimsDataList==null || dimsDataList.size()!=dataShape.length) {
			try {
				this.dimsDataList = new DimsDataList(dataShape, sliceObject);
			} catch (Exception e) {
				logger.error("Cannot make new dims data list!", e);
			}
			
		}
	}

	private LabelJob labelJob;
	/**
	 * Method ensures that one x and on y are defined.
	 * @param data
	 */
	protected boolean synchronizeSliceData(final DimsData data) {
				
		final int usedAxis = data!=null ? data.getAxis() : -2;
		
		for (int i = 0; i < dimsDataList.size(); i++) {
			if (dimsDataList.getDimsData(i).equals(data)) continue;
			if (dimsDataList.getDimsData(i).getAxis()==usedAxis) dimsDataList.getDimsData(i).setAxis(-1);
		}
		
		boolean isX = false;
		for (int i = 0; i < dimsDataList.size(); i++) {
			if (dimsDataList.getDimsData(i).getAxis()==0) isX = true;
		}

        if (labelJob == null) labelJob = new LabelJob();
		labelJob.update(isX);
		
		updateAxesVisibility();

		return isX;
	}
	
	private class LabelJob extends UIJob {

		private boolean isX;

		public LabelJob() {
			super("");
		}
		
		public void update(boolean isX) {
			cancel();
			this.isX = isX;
			schedule();
		}

		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (!isX) {
				errorLabel.setText("Please set a X axis.");
			}
			GridUtils.setVisible(errorLabel,         !(isX));
			isErrorCondition = errorLabel.isVisible();
			GridUtils.setVisible(updateAutomatically, (isX&&plottingSystem!=null));
			errorLabel.getParent().layout(new Control[]{errorLabel,updateAutomatically});
			return Status.OK_STATUS;
		}
		
	}

	private ICellModifier createModifier(final TableViewer viewer) {
		return new ICellModifier() {
			
			@Override
			public boolean canModify(Object element, String property) {
				final DimsData data = (DimsData)element;
				final int       col  = COLUMN_PROPERTIES.indexOf(property);
				if (col==0) return false;
				if (col==1) return true;
				if (col==2) {
					if (dataShape[data.getDimension()]<2) return false;
					return data.getAxis()<0;
				}
				return false;
			}

			@Override
			public void modify(Object item, String property, Object value) {

				final DimsData data  = (DimsData)((IStructuredSelection)viewer.getSelection()).getFirstElement();
				final int       col   = COLUMN_PROPERTIES.indexOf(property);
				if (col==0) return;
				if (col==1) {
					data.setAxis((Integer)value);
					updateAxesChoices(true);
				}
				if (col==2) {
					if (value instanceof Integer) {
						data.setSlice((Integer)value);
					} else {
						data.setSliceRange((String)value);
					}
				}
				final boolean isValidData = synchronizeSliceData(data);
				viewer.cancelEditing();
				viewer.refresh();
				
				if (isValidData) {
					slice(false);
				}
			}
			
			@Override
			public Object getValue(Object element, String property) {
				final DimsData data = (DimsData)element;
				final int       col  = COLUMN_PROPERTIES.indexOf(property);
				if (col==0) return data.getDimension();
				if (col==1) return data.getAxis();
				if (col==2) {
					// Set the bounds
					if (viewer.getCellEditors()[2] instanceof SpinnerCellEditorWithPlayButton) {
						final SpinnerCellEditorWithPlayButton editor = (SpinnerCellEditorWithPlayButton)viewer.getCellEditors()[2];
						editor.setMaximum(dataShape[data.getDimension()]-1);
					} else if (viewer.getCellEditors()[2] instanceof ScaleCellEditor) {
						final Scale scale = (Scale)((ScaleCellEditor)viewer.getCellEditors()[2]).getControl();
						scale.setMaximum(dataShape[data.getDimension()]-1);
						scale.setPageIncrement(scale.getMaximum()/10);

						scale.setToolTipText(getScaleTooltip(scale.getMinimum(), scale.getMaximum()));

					}
					return data.getSliceRange() != null ? data.getSliceRange() : data.getSlice();
				}
				return null;
			}
		};
	}

	private ScaleCellEditor                 scaleEditor;
	private SpinnerCellEditorWithPlayButton spinnerEditor;
	
	private CellEditor[] createCellEditors(final TableViewer viewer) {
		
		final CellEditor[] editors  = new CellEditor[3];
		editors[0] = null;
		editors[1] = new CComboCellEditor(viewer.getTable(), new String[]{"X","Y","(Slice)"}, SWT.READ_ONLY) {
			protected int getDoubleClickTimeout() {
				return 0;
			}			
		};
		final CCombo combo = ((CComboCellEditor)editors[1]).getCombo();
		combo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				
				final CComboCellEditor editor = (CComboCellEditor)editors[1];
				if (!editor.isActivated()) return;
				final String   value = combo.getText();
				if ("".equals(value) || "(Slice)".equals(value)) {
					editor.applyEditorValueAndDeactivate(-1);
					return; // Bit of a bodge
				}
				final String[] items = editor.getItems();
				if (items!=null) for (int i = 0; i < items.length; i++) {
					if (items[i].equalsIgnoreCase(value)) {
						editor.applyEditorValueAndDeactivate(i);
						return;
					}
				}
			}
		});

		this.scaleEditor = new ScaleCellEditor((Composite)viewer.getControl(), SWT.NO_FOCUS);
		final Scale scale = (Scale)scaleEditor.getControl();
		scale.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		scaleEditor.setMinimum(0);
		scale.setIncrement(1);
		scaleEditor.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				final DimsData data  = (DimsData)((IStructuredSelection)viewer.getSelection()).getFirstElement();
				final int value = scale.getSelection();
				data.setSlice(value);
				data.setSliceRange(null);
				if (synchronizeSliceData(data)) slice(false);
				scale.setToolTipText(getScaleTooltip(scale.getMinimum(), scale.getMaximum()));
			}
		});
		
		final ScopedPreferenceStore store = new ScopedPreferenceStore(InstanceScope.INSTANCE, "org.dawb.workbench.ui");
		this.spinnerEditor = new SpinnerCellEditorWithPlayButton(viewer, "Play through slices", store.getInt("data.format.slice.play.speed"));
		spinnerEditor.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		spinnerEditor.addValueListener(new ValueAdapter() {
			@Override
			public void valueChangePerformed(ValueEvent e) {
                final DimsData data  = (DimsData)((IStructuredSelection)viewer.getSelection()).getFirstElement();
                if (e.getValue() instanceof Number) {
                	data.setSlice(((Number)e.getValue()).intValue());
                	data.setSliceRange(null);
                } else {
                	if (((RangeBox)e.getSource()).isError()) return;
                	data.setSliceRange((String)e.getValue());
                }
         		if (synchronizeSliceData(data)) slice(false);
			}
			
		});

		editors[2] = scaleEditor;
		
		return editors;
	}

	protected String getScaleTooltip(int minimum, int maximum) {
		
		final DimsData data  = (DimsData)((IStructuredSelection)viewer.getSelection()).getFirstElement();
		int value = data.getSlice();
        final StringBuffer buf = new StringBuffer();
        buf.append(minimum);
        buf.append(" <= ");
        buf.append(value);
        buf.append(" < ");
        buf.append(maximum+1);
        return buf.toString();
	}

	private void createColumns(final TableViewer viewer) {
		
		final TableViewerColumn dim   = new TableViewerColumn(viewer, SWT.CENTER, 0);
		dim.getColumn().setText("Dim");
		dim.getColumn().setWidth(48);
		dim.setLabelProvider(new DelegatingStyledCellLabelProvider(new SliceColumnLabelProvider(0)));
		
		final TableViewerColumn axis   = new TableViewerColumn(viewer, SWT.CENTER, 1);
		axis.getColumn().setText("Axis");
		axis.getColumn().setWidth(120);
		axis.setLabelProvider(new DelegatingStyledCellLabelProvider(new SliceColumnLabelProvider(1)));

		final TableViewerColumn slice   = new TableViewerColumn(viewer, SWT.LEFT, 2);
		slice.getColumn().setText("Slice Index");
		slice.getColumn().setWidth(200);
		slice.setLabelProvider(new DelegatingStyledCellLabelProvider(new SliceColumnLabelProvider(2)));
		
	}

	private class SliceColumnLabelProvider extends ColumnLabelProvider implements IStyledLabelProvider {

		private int col;
		public SliceColumnLabelProvider(int i) {
			this.col = i;
		}
		@Override
		public StyledString getStyledText(Object element) {
			final DimsData data = (DimsData)element;
			final StyledString ret = new StyledString();
			switch (col) {
			case 0:
				ret.append((data.getDimension()+1)+"");
				break;
			case 1:
				final int axis = data.getAxis();
				ret.append( axis==0 ? "X" : axis==1 ? "Y" : "(Slice)" );
				break;
			case 2:
				if (data.getSliceRange()!=null) {
					ret.append( data.getSliceRange() );
				} else {
					final int slice = data.getSlice();
					ret.append( slice>-1 ? slice+"" : "" );
				}
				if (data.getAxis()<0 && !errorLabel.isVisible()) {
					ret.append(new StyledString("        (click to change)", StyledString.QUALIFIER_STYLER));
				}
				break;
			default:
				ret.append( "" );
				break;
			}
			
			return ret;
		}
				
	}
	
	/**
	 * Call this method to show the slice dialog.
	 * 
	 * This non-modal dialog allows the user to slice
	 * data out of n-D data sets into a 2D plot.
	 * 
	 * This method is not thread safe, please call in the UI thread.
	 */
	public void setData(final String     name,
				        final String     filePath,
				        final int[]      dataShape) {
		
		if (Display.getDefault().getThread()!=Thread.currentThread()) {
			throw new SWTError("Please call setData(...) in slice component from the UI thread only!");
		}
		sliceJob.cancel();
		saveSettings();

		final SliceObject object = new SliceObject();
		object.setPath(filePath);
		object.setName(name);
		setSliceObject(object);
		setDataShape(dataShape);
		
		explain.setText("Create a slice of "+sliceObject.getName()+".\nIt has the shape "+Arrays.toString(dataShape));
		if (viewer.getCellEditors()[2] instanceof SpinnerCellEditorWithPlayButton) {
			((SpinnerCellEditorWithPlayButton)viewer.getCellEditors()[2]).setRangeDialogTitle("Range for slice in '"+sliceObject.getName()+"'");
			((SpinnerCellEditorWithPlayButton)viewer.getCellEditors()[2]).setPlayButtonVisible(false);
		}

		
		createDimsData();
    	viewer.refresh();
    	
		updateAxesChoices(false);
		synchronizeSliceData(null);
		slice(true);
		
		if (plottingSystem==null) {
			GridUtils.setVisible(updateAutomatically, false);
			viewer.getTable().getColumns()[2].setText("Start Index or Slice Range");
		}
	}
	
	/**
	 * Does slice in monitored job
	 */
	public void slice(final boolean force) {
		
		if (!force) {
		    if (!autoUpdate) return;
		}

		final SliceObject cs = SliceUtils.createSliceObject(dimsDataList, dataShape, sliceObject);
		sliceJob.schedule(cs);
	}
	
	public void dispose() {
		if (plottingSystem!=null && traceListener!=null) {
			plottingSystem.removeTraceListener(traceListener);	
		}
		sliceJob.cancel();
		saveSettings();
	}
	
	private void saveSettings() {
		
		if (sliceObject == null || isErrorCondition) return;
		
		final File dataFile     = new File(sliceObject.getPath());
		final File lastSettings = new File(DawbUtils.getDawbHome()+dataFile.getName()+"."+sliceObject.getName()+".xml");
		if (!lastSettings.getParentFile().exists()) lastSettings.getParentFile().mkdirs();
	
		XMLEncoder encoder=null;
		try {
			encoder = new XMLEncoder(new FileOutputStream(lastSettings));
			for (int i = 0; i < dimsDataList.size(); i++) encoder.writeObject(dimsDataList.getDimsData(i));
		} catch (Exception ne) {
			logger.error("Cannot save slice data from last settings!", ne);
		} finally  {
			if (encoder!=null) encoder.close();
		}
	}
	
	public void setSliceObject(SliceObject sliceObject) {
		this.sliceObject = sliceObject;
	}

	public void setDataShape(int[] shape) {
		this.dataShape = shape;
	}

	/**
	 * Normally call before createPartControl(...)
	 * @param plotWindow
	 */
	public void setPlottingSystem(IPlottingSystem plotWindow) {
		this.plottingSystem = plotWindow;
	}

	/**
	 * Throws exception if GUI disposed.
	 * @param vis
	 */
	public void setVisible(final boolean vis) {
		area.setVisible(vis);
		area.getParent().layout(new Control[]{area});
		
		if (!vis) {
			sliceJob.cancel();
			saveSettings();
		}
	}

	public void setSliceIndex(int dimension, int index, boolean doSlice) {
		viewer.cancelEditing();
		this.dimsDataList.getDimsData(dimension).setSlice(index);
		viewer.refresh();
		if (doSlice) slice(true);
	}
	
	public DimsDataList getDimsDataList() {
		return dimsDataList;
	}
	public Map<Integer,String> getNexusAxes() {
		return sliceObject.getNexusAxes();
	}

	public void setDimsDataList(DimsDataList dimsDataList) {
		this.dimsDataList = dimsDataList;
		viewer.refresh();
	}

	public void setImagePlotType(PlotType pt) {
		this.imagePlotType  = pt;
	}

	private class SliceJob extends Job {
		
		private SliceObject slice;
		public SliceJob() {
			super("Slice");
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			
			if (slice==null) return Status.CANCEL_STATUS;
			monitor.beginTask("Slice "+slice.getName(), 10);
			try {
				monitor.worked(1);
				if (monitor.isCanceled()) return Status.CANCEL_STATUS;
				
				PlotType type = PlotType.PT1D;
				if (slice.getAxes().size()==1) {
					type = PlotType.PT1D;
				} else  if (slice.getAxes().size()==2) {
					type = imagePlotType;
				} else {
					throw new Exception("Only 1D and images supported currently!");
				}
				
				SliceUtils.plotSlice(slice, 
						             dataShape, 
						             type, 
						             plottingSystem, 
						             monitor);
			} catch (Exception e) {
				logger.error("Cannot slice "+slice.getName(), e);
			} finally {
				monitor.done();
			}	
			
			return Status.OK_STATUS;
		}

		public void schedule(SliceObject cs) {
			if (slice!=null && slice.equals(cs)) return;
			cancel();
			this.slice = cs;
			schedule();
		}	
	}
}
