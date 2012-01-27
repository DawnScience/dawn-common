/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
/**
 * 
 */
package org.dawb.common.ui.views;

import java.io.File;
import java.util.Collection;

import org.dawb.common.services.ILoaderService;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.IMetadataProvider;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;

/**
 * @author suchet + gerring
 * 
 */
public class HeaderTableView extends ViewPart implements ISelectionListener, IPartListener {

	public static final String ID = "fable.imageviewer.views.HeaderView";
	
	private static final Logger logger = LoggerFactory.getLogger(HeaderTableView.class);
	
	private IMetaData           meta;
	private StructuredSelection lastSelection;
	private boolean             requirePageUpdates;
	private TableViewer         table;
	
	public HeaderTableView() {
        this(true);
	}
	
	/**
	 * 
	 */
	public HeaderTableView(final boolean requirePageUpdates) {
		this.requirePageUpdates = requirePageUpdates;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(final Composite parent) {
		
		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridUtils.removeMargins(container);
		
		final Text searchText = new Text(container, SWT.BORDER | SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		searchText.setToolTipText("Search on data set name or expression value." );
				
		this.table = new TableViewer(container, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
        
		table.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		table.getTable().setLinesVisible(true);
		table.getTable().setHeaderVisible(true);
		
		final TableViewerColumn key = new TableViewerColumn(table, SWT.NONE, 0);
		key.getColumn().setText("Key");
		key.getColumn().setWidth(200);
		key.setLabelProvider(new HeaderColumnLabelProvider(0));
		
		final TableViewerColumn value = new TableViewerColumn(table, SWT.NONE, 1);
		value.getColumn().setText("Value");
		value.getColumn().setWidth(200);
		value.setLabelProvider(new HeaderColumnLabelProvider(1));

		table.setColumnProperties(new String[]{"Key","Value"});
		table.setUseHashlookup(true);		
		
		final HeaderFilter filter = new HeaderFilter();
		table.addFilter(filter);
		searchText.addModifyListener(new ModifyListener() {		
			@Override
			public void modifyText(ModifyEvent e) {
				if (parent.isDisposed()) return;
				filter.setSearchText(searchText.getText());
				table.refresh();
			}
		});
		
		if (requirePageUpdates) {
			final IWorkbenchPage page = EclipseUtils.getActivePage();
			if (page!=null) {
				final IEditorPart editor = page.getActiveEditor();
				if (editor!=null) {
					updatePath(EclipseUtils.getFilePath(editor.getEditorInput()));
				}
			}
			
			// Instead of sample controller we use the workbench
			// selection. If this is an image editor part, then we know that to do.
			getSite().getPage().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
			getSite().getPage().addPartListener(this);
		}
	}

	private void updateSelection(ISelection selection) {
		
		if (selection == null) return;
		if (selection instanceof StructuredSelection) {
			this.lastSelection = (StructuredSelection)selection;
			final Object sel = lastSelection.getFirstElement();
			
			if (sel instanceof IFile) {
				final String filePath = ((IFile)sel).getLocation().toOSString();
				updatePath(filePath);
			} else if (sel instanceof File) {
				final String filePath = ((File)sel).getAbsolutePath();
				updatePath(filePath);
			} else if( sel instanceof IMetadataProvider){
				try {
					meta = ((IMetadataProvider)sel).getMetadata();
					updatePartName();
					updateTable.schedule();
				} catch (Exception e) {
					logger.error("Could not capture metadata from selection",e);
				}
				
			}
		} 
	}
	
	
	private void updatePartName() {
		if(meta!=null){
		Collection<String> partName = meta.getDataNames();
		if ( partName!=null)
			setPartName("Metadata "+partName.toString());
		else
			setPartName("Metadata");
		}
	}

	private void getMetaData(final String filePath) throws InterruptedException {
		
		final Job metaJob = new Job("Extra Meta Data "+filePath) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				
				final ILoaderService service = (ILoaderService)PlatformUI.getWorkbench().getService(ILoaderService.class);
				try {
					meta = service.getMetaData(filePath, monitor);
				} catch (Exception e1) {
					logger.error("Cannot get meta data for "+filePath, e1);
					return Status.CANCEL_STATUS;
				}
				
				updateTable.schedule();
				
				return Status.OK_STATUS;
			}
			
		};
		
		metaJob.schedule();
	} 
	
	UIJob updateTable = new UIJob("Updating Metadata Table") {
		
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			
			if (table.getControl().isDisposed()) return Status.CANCEL_STATUS;
			table.setContentProvider(new IStructuredContentProvider() {			
				@Override
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}
				
				@Override
				public void dispose() {
				}
				@Override
				public Object[] getElements(Object inputElement) {
					try {
						return meta.getMetaNames().toArray(new Object[meta.getMetaNames().size()]);
					} catch (Exception e) {
						return new Object[]{""};
					}
				}
			});	
			
			// Maybe being the selection provider cause the left mouse problem
	        //if (getSite()!=null) getSite().setSelectionProvider(dataViewer);
			
			if (table.getControl().isDisposed()) return Status.CANCEL_STATUS;
			table.setInput(new String());
			return Status.OK_STATUS;
		}
	};
	/**
	 * May be called to set the path from which to update the meta table.
	 * @param filePath
	 */
	public void updatePath(final String filePath) {
		
	    try {
			getMetaData(filePath);
			setPartName("Header " + (new File(filePath)).getName());
		} catch (InterruptedException e) {
			logger.error("Interupted reading meta data.", e);
		}
		
	}
	
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof IMetadataProvider)
			try {
				meta = ((IMetadataProvider) part).getMetadata();
				updateTable.schedule();
			} catch (Exception e) {
				logger.error("There was a error reading the metadata from the selection", e);
			}
		else
			updateSelection(selection);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		table.getControl().setFocus();
	}

	@Override
	public void dispose() {
		if (requirePageUpdates) {
			getSite().getPage().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
			getSite().getPage().removePartListener(this);
		}
		super.dispose();
	}
	
	@Override
	public void partActivated(IWorkbenchPart part) {

		if (part instanceof IMetadataProvider) {
			updateFromMetaDataProvider(part);
		}
		if (part instanceof IEditorPart) {
			final IEditorPart  ed = (IEditorPart)part;
			final IEditorInput in = ed.getEditorInput();
			final String     path = EclipseUtils.getFilePath(in);
			if (path!=null) updatePath(path);
		}
		
	
	}



	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		updateFromMetaDataProvider(part);
	}

	@Override
	public void partClosed(IWorkbenchPart part) {
		
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
		
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		
 	} 
	
	private void updateFromMetaDataProvider(IWorkbenchPart part) {
		if (part instanceof IMetadataProvider) {
			try {
				meta = ((IMetadataProvider) part).getMetadata();
				if (meta != null && !table.getTable().isDisposed()) {
					updateTable.schedule();
					setPartName("Header " + part.getTitle());
				}
			} catch (Exception e) {
				logger.error("Cannot get meta data from " + part.getTitle(), e);
			}
		}
	}
	
	private class HeaderColumnLabelProvider extends ColumnLabelProvider {
		private int column;

		public HeaderColumnLabelProvider(int col) {
			this.column = col;
		}
		
		public String getText(final Object element) {
			if (column==0) return element.toString();
			if (column==1)
				try {
					return meta.getMetaValue(element.toString()).toString();
				} catch (Exception ignored) {
					// Null allowed
				}
			return "";
		}
	}
	
	class HeaderFilter extends ViewerFilter {

		private String searchString;

		public void setSearchText(String s) {
			if (s==null) s= "";
			this.searchString = ".*" + s.toLowerCase() + ".*";
		}
		
		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (searchString == null || searchString.length() == 0) {
				return true;
			}
			
			final String name = (String)element;
		
			if (name==null || "".equals(name)) return true;
			
			if (name.toLowerCase().matches(searchString)) {
				return true;
			}
			if (name.toLowerCase().matches(searchString)) {
				return true;
			}

			return false;
		}
	}
}
