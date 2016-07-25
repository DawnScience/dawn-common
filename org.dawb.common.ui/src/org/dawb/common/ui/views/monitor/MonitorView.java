/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.views.monitor;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.dawb.common.ui.Activator;
import org.dawb.common.ui.preferences.CommonUIPreferenceConstants;
import org.dawb.common.ui.widgets.DoubleClickModifier;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Additions: Use TreeViewer to display the scannables in their scannable groups. An action to add all scannables.
 */
public final class MonitorView extends ViewPart implements HardwareObjectListener {

	private static final Logger logger = LoggerFactory.getLogger(MonitorView.class);

	/**
	 * We have a thread which monitors the events to avoid thousands of
	 * updates swamping the gui.
	 */
	private transient BlockingQueue<HardwareObject> updateQueue;
	
	/**
	 * Thread used to process queue.
	 */
	private Thread notifyQueueThread;

	public MonitorView() {
		this.updateQueue = new LinkedBlockingQueue<HardwareObject>(5);
	}
	/**
	 * 
	 */
	public static final String ID = "org.dawb.common.ui.views.dashboardView"; //$NON-NLS-1$

	private TableViewer serverViewer;

	private TableViewerColumn maxColumn, minColumn, desColumn;

	/**
	 * Create contents of the view part
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {

		final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		Composite container = new Composite(scrolledComposite, SWT.NONE);
		container.setLayout(new FillLayout());
		scrolledComposite.setContent(container);

		this.serverViewer = new TableViewer(container, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		serverViewer.getTable().setLinesVisible(true);
		serverViewer.getTable().setHeaderVisible(true);

		ColumnViewerToolTipSupport.enableFor(serverViewer, ToolTip.NO_RECREATE);

		final TableViewerColumn name = new TableViewerColumn(serverViewer, SWT.NONE);
		name.getColumn().setText("Name");
		name.getColumn().setWidth(150);
		name.setLabelProvider(new TableLabelProvider(0));

		final TableViewerColumn value = new TableViewerColumn(serverViewer, SWT.NONE);
		value.getColumn().setText("Value");
		value.getColumn().setWidth(150);
		value.setLabelProvider(new TableLabelProvider(1));

		this.minColumn = new TableViewerColumn(serverViewer, SWT.NONE);
		minColumn.getColumn().setText("Minimum");
		minColumn.getColumn().setWidth(150);
		minColumn.setLabelProvider(new TableLabelProvider(2));

		this.maxColumn = new TableViewerColumn(serverViewer, SWT.NONE);
		maxColumn.getColumn().setText("Maximum");
		maxColumn.getColumn().setWidth(150);
		maxColumn.setLabelProvider(new TableLabelProvider(3));

		this.desColumn = new TableViewerColumn(serverViewer, SWT.NONE);
		desColumn.getColumn().setText("Description");
		desColumn.getColumn().setWidth(150);
		desColumn.setLabelProvider(new TableLabelProvider(4));

		serverViewer.setColumnProperties(new String[] { "Object Name", "Object Value" });
		serverViewer.setCellEditors(createCellEditors(serverViewer));
		serverViewer.setCellModifier(createModifier(serverViewer));
		createContentProvider();
		serverViewer.setInput(new Object());

		getSite().setSelectionProvider(serverViewer);
		createRightClickMenu();

		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {

			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(CommonUIPreferenceConstants.DASHBOARD_FORMAT)) {
					serverViewer.refresh();
				} else if (event.getProperty().equals(CommonUIPreferenceConstants.DASHBOARD_BOUNDS)) {
					updateBoundsColumns();
				} else if (event.getProperty().equals(CommonUIPreferenceConstants.DASHBOARD_DESCRIPTION)) {
					updateDummyColumn();
				} else if (event.getProperty().equals(CommonUIPreferenceConstants.MOCK_SESSION)) {
					refresh();
				}
			}
		});

		updateBoundsColumns();
		updateDummyColumn();
	}

	private void updateBoundsColumns() {
		final boolean isVis = Activator.getDefault().getPreferenceStore()
				.getBoolean(CommonUIPreferenceConstants.DASHBOARD_BOUNDS);
		if (!isVis) {
			maxColumn.getColumn().setWidth(0);
			maxColumn.getColumn().setResizable(false);
			minColumn.getColumn().setWidth(0);
			minColumn.getColumn().setResizable(false);
		} else {
			maxColumn.getColumn().setWidth(150);
			maxColumn.getColumn().setResizable(true);
			minColumn.getColumn().setWidth(150);
			minColumn.getColumn().setResizable(true);
		}
	}

	private void updateDummyColumn() {
		final boolean isVis = Activator.getDefault().getPreferenceStore()
				.getBoolean(CommonUIPreferenceConstants.DASHBOARD_DESCRIPTION);
		if (!isVis) {
			desColumn.getColumn().setWidth(0);
			desColumn.getColumn().setResizable(false);
		} else {
			desColumn.getColumn().setWidth(150);
			desColumn.getColumn().setResizable(true);
		}
	}

	private void createRightClickMenu() {
		final MenuManager menuManager = new MenuManager();
		serverViewer.getControl().setMenu(menuManager.createContextMenu(serverViewer.getControl()));
		getSite().registerContextMenu(menuManager, serverViewer);
	}

	private CellEditor[] createCellEditors(final TableViewer tableViewer) {
		CellEditor[] editors = new CellEditor[1];
		TextCellEditor nameEd = new TextCellEditor(tableViewer.getTable());
		((Text) nameEd.getControl()).setTextLimit(60);
		// NOTE Must not add verify listener - it breaks things.
		editors[0] = nameEd;

		return editors;
	}

	private ICellModifier createModifier(final TableViewer tableViewer) {
		return new DoubleClickModifier(tableViewer) {
			
			@Override
			public boolean canModify(Object element, String property) {
				if (!enabled)
					return false;
				return (element instanceof HardwareObject) && "Object Name".equalsIgnoreCase(property);
			}

			@Override
			public Object getValue(Object element, String property) {
				// NOTE: Only works for scannables right now which have one name
				final String name = ((HardwareObject) element).getHardwareName();
				return name != null ? name : "";
			}

			@Override
			public void modify(Object item, String property, Object value) {
				try {
					final HardwareObject ob = (HardwareObject) ((IStructuredSelection) serverViewer
							.getSelection()).getFirstElement();
					ob.setHardwareName((String) value);
					ob.connect();

				} catch (Exception e) {
					logger.error("Cannot set " + property, e);

				} finally {
					setEnabled(false);
				}
				serverViewer.refresh();
			}
		};
	}

	private List<HardwareObject> data;

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site);

		try {
			if (memento != null)
				this.data = getDataFromXML(memento.getTextData());
			if (data == null)
				this.data = getDefaultServerObjects();
			connect();
		} catch (Exception ne) {
			throw new PartInitException(ne.getMessage());
		}
	}

	private void connect() {
		// connect to objects in a separate thread
		Job job = new Job("Connecting dashboard to objects...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				for (HardwareObject serverObject : data) {
					try {
						serverObject.connect();
					} catch (Exception e) {
						logger.debug("Dashboard view error while trying to connect", e);
					} finally {
						serverObject.addServerObjectListener(MonitorView.this);
					}
				}
				
				if (serverViewer!=null && !serverViewer.getTable().isDisposed()) {
					getSite().getShell().getDisplay().asyncExec(new Runnable() {
						public void run() {
							serverViewer.refresh();
						}
					});
				}
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.BUILD);
		job.setUser(false);
		job.schedule();
	}

	private void disconnect() {
		for (HardwareObject serverObject : data) serverObject.disconnect();
		for (HardwareObject o : data) {
			o.removeServerObjectListener(this);
		}
	}

	@Override
	public void saveState(IMemento memento) {
		try {
			memento.putTextData(getXMLFromData(data));
		} catch (Exception e) {
			logger.error("Cannot save plot bean", e);
		}
	}

	@SuppressWarnings("unchecked")
	private List<HardwareObject> getDataFromXML(String textData) throws Exception {

		if (textData == null) return null;
		final ByteArrayInputStream stream = new ByteArrayInputStream(textData.getBytes("UTF-8"));
		final ClassLoader original = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(MonitorUtils.createHardwareObject().getClass().getClassLoader());
			XMLDecoder d = new XMLDecoder(new BufferedInputStream(stream));
			final List<HardwareObject> data = (List<HardwareObject>) d.readObject();
			d.close();
			return data;
		} finally {
			Thread.currentThread().setContextClassLoader(original);
		}
	}

	private String getXMLFromData(final List<HardwareObject> data) throws Exception {

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		
		final ClassLoader original = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(MonitorUtils.createHardwareObject().getClass().getClassLoader());
			XMLEncoder e = new XMLEncoder(new BufferedOutputStream(stream));	
			e.writeObject(data);
			e.close();
			return stream.toString("UTF-8");
		} finally {
			Thread.currentThread().setContextClassLoader(original);
		}

	}

	@Override
	public void hardwareObjectChangePerformed(HardwareObjectEvent evt) {
		
		if (serverViewer.getTable().isDisposed()) return;// Important can be called from timer thread.
		if (serverViewer.isCellEditorActive())    return;
		createNotifyQueue();
		
		final HardwareObject ob = (HardwareObject) evt.getSource();
		for (Iterator<HardwareObject> it = updateQueue.iterator(); it.hasNext();) {
			if (it.next() == ob) it.remove();
		}
		
		updateQueue.add(ob);
	}
	
	private void createNotifyQueue() {
		
		if (notifyQueueThread!=null) return;
		
		this.notifyQueueThread = new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					while(true) {
						
						final HardwareObject ob = updateQueue.take();
						if (ob == HardwareObject.NULL) break;
						
						getSite().getShell().getDisplay().asyncExec(new Runnable() {
							@Override
							public void run() {
								if (serverViewer==null || serverViewer.getControl().isDisposed()) return;
								serverViewer.update(ob, null);
							}
						});
						// Maximum refresh rate that user can deal with
						//System.getProperty("org.dawb.")
						Thread.sleep(200);
					}
				} catch (InterruptedException e) {
					logger.error("Cannot monitor events!", e);
				}
				logger.debug("Stopping thread '"+Thread.currentThread().getName()+"'");
			}
		}, getPartName()+" Thread");
		
		notifyQueueThread.start();
	}

	public void dispose() {
		
		super.dispose();
		
		disconnect();
		data.clear();
		
		this.updateQueue.clear();
		updateQueue.add(HardwareObject.NULL);
		
		this.serverViewer.getTable().dispose();
		this.serverViewer = null;
	}

	/**
	 * Add an object to listen to.
	 * 
	 * @param toAdd
	 */
	public void addServerObject(final HardwareObject toAdd) {
		try {
			toAdd.connect();
			data.add(toAdd);
			toAdd.addServerObjectListener(this);
			serverViewer.refresh();
			((DoubleClickModifier) serverViewer.getCellModifier()).setEnabled(true);
			serverViewer.editElement(toAdd, 0);

		} catch (Exception ne) {
			logger.error("Cannot add object", ne);
		}
	}

	/**
	 * 
	 */
	public void deleteSelectedObject() {
		try {
			final HardwareObject ob = (HardwareObject)((IStructuredSelection) serverViewer.getSelection()).getFirstElement();
			data.remove(ob); // NOTE the equals method of ServerObject simply looks at the label.
			ob.disconnect();
			serverViewer.refresh();
		} catch (Exception ignored) {
			// Might be nothing selected.
		}
	}

	/**
	 * Used when user has too many scannables and would like to reset the view.
	 */
	public void resetSelectedObjects() {
		try {
			disconnect();
			data.clear();
			data.addAll(getDefaultServerObjects());
			connect();
			
		} catch (Exception ne) {
			logger.error("Cannot reset objects", ne);
		}
	}

	/**
	 * Called to refresh all the values in the table.
	 */
	public void refresh() {
		try {
			disconnect();
			connect();
		} catch (Exception ne) {
			logger.error("Cannot refresh objects", ne);
		}
	}

	/**
	 * Called to refresh all the values in the table.
	 * 
	 * @param moveAmount
	 */
	public void move(final int moveAmount) {

		final int sel = serverViewer.getTable().getSelectionIndex();
		final int pos = sel + moveAmount;
		if (pos < 0 || pos > this.data.size() - 1)
			return;

		final HardwareObject o = data.remove(sel);
		data.add(pos, o);

		serverViewer.refresh();
	}

	/**
	 * 
	 */
	public void clearSelectedObjects() {
		final boolean ok = MessageDialog.openConfirm(getSite().getShell(), "Please confirm clear",
				"Would you like to clear all monitored objects?");
		if (!ok)
			return;
		try {
			disconnect();
			data.clear();
			serverViewer.refresh();
		} catch (Exception ne) {
			logger.error("Cannot clear objects", ne);
		}
	}

	private class TableLabelProvider extends ColumnLabelProvider {
		private int column;

		TableLabelProvider(int col) {
			this.column = col;
		}

		@Override
		public String getText(Object element) {
			final HardwareObject ob = (HardwareObject) element;
			switch (column) {
			case 0:
				return ob.getLabel();
			case 1:
				return formatValue(ob.getValue(), ob.getUnit());
			case 2:
				return formatValue(ob.getMinimum(), ob.getUnit());
			case 3:
				return formatValue(ob.getMaximum(), ob.getUnit());
			case 4:
				return ob.getDescription();
			default:
				return "";
			}
		}

		private String formatValue(final Object valueOriginal, final String unit) {
			Object value = valueOriginal;
			if (value != null) {
				try {
					final double dblValue = value instanceof Double ? (Double) value : Double.parseDouble(value
							.toString());
					final String formatString = Activator.getDefault().getPreferenceStore()
							.getString(CommonUIPreferenceConstants.DASHBOARD_FORMAT);
					DecimalFormat format = new DecimalFormat(formatString);
					value = format.format(dblValue);
				} catch (Exception ignored) {
					value = valueOriginal;
				}
			}
			if (value != null && unit != null)
				return value + " " + unit;
			if (value != null)
				return value + "";
			return "";
		}

		@Override
		public String getToolTipText(Object element) {
			final HardwareObject serverOb = (HardwareObject) element;
			if (serverOb.isError())
				return "Cannot locate scannable '" + serverOb.getLabel() + "'.";
			if (column == 4) {
				return serverOb.getClassName();
			}
			return serverOb.getTooltip();
		}
	}

	private void createContentProvider() {
		serverViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return data.toArray(); // Does not happen that often and list not large
			}
		});
	}

	@Override
	public void setFocus() {
		if (this.serverViewer != null)
			serverViewer.getControl().setFocus();
	}

	/**
	 * Called to get the default list of things to monitor.
	 */
	protected List<HardwareObject> getDefaultServerObjects() throws Exception {

		final List<HardwareObject> data = new ArrayList<HardwareObject>(5);

		// TODO Maybe allow plugins to define default hardware which should be used.
//		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(
//				"org.dawb.common.ui.views.dashboard.default.objects");
//
//		for (IConfigurationElement e : config) {
//			final String name = e.getAttribute("name");
//			final HardwareObject ob = DashUtils.createHardwareObject(name);
//			ob.setTooltip(e.getAttribute("tooltip"));
//
//			data.add(ob);
//		}

		return data;
	}

}
