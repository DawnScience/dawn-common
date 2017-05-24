/*
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.views;

import java.io.File;
import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.dawb.common.ui.Activator;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.slicing.api.system.ISliceSystem;
import org.eclipse.dawnsci.slicing.api.util.ProgressMonitorWrapper;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IMetadataProvider;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.progress.UIJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author suchet + gerring
 * @author Baha El Kassaby (migration to E4)
 * 
 */
public class HeaderTableE4Part {

	public static final String ID = "org.dawb.common.ui.views.e4.headerTableView";

	private static final Logger logger = LoggerFactory.getLogger(HeaderTableE4Part.class);

	private IMetadata meta;
	private StructuredSelection lastSelection;
	private boolean requirePageUpdates;
	private TableViewer table;
	private CLabel fileNameLabel;
	private String currentFilePath;

	@Inject
	private EPartService partService;

	public HeaderTableE4Part() {
		this(true);
	}

	/**
	 * 
	 */
	public HeaderTableE4Part(final boolean requirePageUpdates) {
		this.requirePageUpdates = requirePageUpdates;
	}

	@PostConstruct
	public void createPartControl(final Composite parent) {

		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridUtils.removeMargins(container);

		this.fileNameLabel = new CLabel(container, SWT.NONE);
		fileNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));

		final Text searchText = new Text(container, SWT.BORDER | SWT.SEARCH
				| SWT.ICON_SEARCH | SWT.ICON_CANCEL);
		searchText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_FILL));
		searchText
				.setToolTipText("Search on data set name or expression value.");

		this.table = new TableViewer(container, SWT.FULL_SELECTION | SWT.SINGLE
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

		table.getTable().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		table.getTable().setLinesVisible(true);
		table.getTable().setHeaderVisible(true);

		final TableViewerColumn key = new TableViewerColumn(table, SWT.NONE, 0);
		key.getColumn().setText("Key");
		key.getColumn().setWidth(200);
		key.setLabelProvider(new HeaderColumnLabelProvider(0));
		key.getColumn().addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				int dir = table.getTable().getSortDirection();
				dir = (dir == SWT.UP || dir == SWT.NONE) ? SWT.DOWN : SWT.UP;
				table.getTable().setSortDirection(dir);
				table.getTable().setSortColumn(key.getColumn());
				table.refresh();
			}
		});

		table.setSorter(new ViewerSorter() {
			public int compare(Viewer viewer, Object e1, Object e2) {
				String val1 = (String) e1;
				val1 = val1 != null ? val1.toLowerCase() : "";
				String val2 = (String) e2;
				val2 = val2 != null ? val2.toLowerCase() : "";

				int dir = table.getTable().getSortDirection();
				if (dir == SWT.UP) {
					return val2.compareTo(val1);
				} else if (dir == SWT.DOWN) {
					return val1.compareTo(val2);
				}
				return val1.compareTo(val2);
			}
		});

		final TableViewerColumn value = new TableViewerColumn(table, SWT.NONE,
				1);
		value.getColumn().setText("Value");
		value.getColumn().setWidth(500);
		value.setLabelProvider(new HeaderColumnLabelProvider(1));

		table.setColumnProperties(new String[] { "Key", "Value" });
		table.setUseHashlookup(true);

		final HeaderFilter filter = new HeaderFilter();
		table.addFilter(filter);
		searchText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (parent.isDisposed())
					return;
				filter.setSearchText(searchText.getText());
				table.refresh();
			}
		});

		if (requirePageUpdates) {
			final IWorkbenchPage page = EclipseUtils.getActivePage();
			if (page != null) {
				final IEditorPart editor = page.getActiveEditor();
				if (editor != null) {
					updatePath(EclipseUtils
							.getFilePath(editor.getEditorInput()));
				}
			}
		}
	}

	private UIJob updateTable = new UIJob("Updating Metadata Table") {
		@Override
		public IStatus runInUIThread(IProgressMonitor monitor) {
			if (table == null)
				return Status.CANCEL_STATUS;
			if(table.getControl().isDisposed())
				return Status.CANCEL_STATUS;
			table.setContentProvider(new IStructuredContentProvider() {
				@Override
				public void inputChanged(Viewer viewer, Object oldInput,
						Object newInput) {
				}

				@Override
				public void dispose() {
				}

				@Override
				public Object[] getElements(Object inputElement) {
					if (meta == null)
						return new Object[] { "" };
					try {
						return meta.getMetaNames().toArray(
								new Object[meta.getMetaNames().size()]);
					} catch (Exception e) {
						return new Object[] { "" };
					}
				}
			});

			// Maybe being the selection provider cause the left mouse problem
			// if (getSite()!=null) getSite().setSelectionProvider(dataViewer);
			if (table.getControl().isDisposed())
				return Status.CANCEL_STATUS;
			table.setInput(new String());
			return Status.OK_STATUS;
		}
	};

	/**
	 * May be called to set the path from which to update the meta table.
	 * 
	 * @param filePath
	 */
	public synchronized void updatePath(final String filePath) {
		try {
			if (currentFilePath != null && currentFilePath.equals(filePath))
				return;
			currentFilePath = filePath;
			getMetaData(filePath);
			setFileName((new File(filePath)).getName());
		} catch (InterruptedException e) {
			logger.error("Interupted reading meta data.", e);
		}
	}

	/**
	 * Called to programmatically send the meta which should be shown.
	 * 
	 * @param prov
	 */
	public void setMetaProvider(IMetadataProvider prov) {
		try {
			meta = prov.getFirstMetadata(IMetadata.class);
			if (meta != null)
				updateTable.schedule();
		} catch (Exception e) {
			logger.error(
					"There was a error reading the metadata from the selection",
					e);
		}
	}

	/**
	 * Selection listener
	 * 
	 * @param sel
	 */
	@Inject
	public void selectionChanged(@Optional @Named(IServiceConstants.ACTIVE_SELECTION) Object sel) {
		MPart part = partService.getActivePart();
		IEclipseContext context = part != null ? part.getContext() : null;
		IWorkbenchPart wpart = context != null ? context.get(IWorkbenchPart.class) : null;
		if (wpart != null && wpart instanceof IMetadataProvider) {
			try {
				meta = ((IMetadataProvider) part).getFirstMetadata(IMetadata.class);
				if (meta != null)
					updateTable.schedule();
			} catch (Exception e) {
				logger.error("There was a error reading the metadata from the selection", e);
			}
		} else {
			ISliceSystem slicer = wpart != null ? (ISliceSystem) wpart.getAdapter(ISliceSystem.class) : null;
			if (slicer == null && wpart != null) {
				final IAdaptable page = wpart.getAdapter(Page.class) instanceof IAdaptable
						? (IAdaptable) wpart.getAdapter(Page.class) : null;
				if (page != null)
					slicer = (ISliceSystem) page.getAdapter(ISliceSystem.class);
			}
			if (slicer != null) {
				IMetadata md = slicer.getSliceMetadata();
				if (md != null) {
					selectMetadata(md);
					return;
				}
			}
			updateSelection(sel);
		}
	}

	/**
	 * Part listener
	 * 
	 * @param part
	 */
	@Inject
	public void partActivated(@Optional @Named(IServiceConstants.ACTIVE_PART) MPart part) {
		IEclipseContext context = part != null ? part.getContext() : null;
		IWorkbenchPart wpart = context != null ? context.get(IWorkbenchPart.class) : null;
		if (wpart instanceof IMetadataProvider) {
			updateFromMetaDataProvider(wpart);
		}
		if (wpart instanceof IEditorPart) {
			final IEditorPart ed = (IEditorPart) wpart;
			final IEditorInput in = ed.getEditorInput();
			final String path = EclipseUtils.getFilePath(in);
			if (path != null)
				updatePath(path);
		}
	}

	private void updateSelection(Object selection) {
		if (selection == null)
			return;
		if (selection instanceof StructuredSelection) {
			this.lastSelection = (StructuredSelection) selection;
			final Object sel = lastSelection.getFirstElement();
			if (sel instanceof IFile) {
				final String filePath = ((IFile) sel).getLocation()
						.toOSString();
				updatePath(filePath);
			} else if (sel instanceof File) {
				if (!((File) sel).isDirectory()) {
					final String filePath = ((File) sel).getAbsolutePath();
					updatePath(filePath);
				}
			} else if (sel instanceof IMetadata) {
				selectMetadata((IMetadata) sel);
			} else if (sel instanceof IMetadataProvider) {
				try {
					selectMetadata(((IMetadataProvider) sel).getFirstMetadata(IMetadata.class));
				} catch (Exception e) {
					logger.error("Cannot get metadata", e);
				}
			}
		}
	}

	private void selectMetadata(IMetadata sel) {
		meta = sel;
		updatePartName();
		updateTable.schedule();
	}

	private void updatePartName() {
		if (meta != null) {
			if (meta.getFilePath() != null) {
				final File file = new File(meta.getFilePath());
				setFileName(file.getName());
			} else {
				setFileName("");
			}
		}
	}

	private void setFileName(String fileName) {
		if (fileNameLabel != null)
			fileNameLabel.setText(fileName);
	}

	private void getMetaData(final String filePath) throws InterruptedException {
		final Job metaJob = new Job("Extra Metadata " + filePath) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				final ILoaderService service = Activator
						.getService(ILoaderService.class);
				try {
					meta = service.getMetadata(filePath,
							new ProgressMonitorWrapper(monitor));
				} catch (Exception e1) {
					logger.error("Cannot get meta data for " + filePath, e1);
					return Status.CANCEL_STATUS;
				}
				updateTable.schedule();
				return Status.OK_STATUS;
			}
		};
		metaJob.schedule();
	}

	@Focus
	public void setFocus() {
		if (table != null)
			table.getControl().setFocus();
	}

	@PreDestroy
	public void dispose() {
		if (requirePageUpdates) {
			// E4 takes care of removing the listeners?
			// getSite().getPage().getWorkbenchWindow().getSelectionService().removeSelectionListener(this);
			// getSite().getPage().removePartListener(this);
		}
	}

	public void partBroughtToTop(IWorkbenchPart part) {
		updateFromMetaDataProvider(part);
	}

	private void updateFromMetaDataProvider(IWorkbenchPart part) {
		if (part instanceof IMetadataProvider) {
			try {
				meta = ((IMetadataProvider) part).getFirstMetadata(IMetadata.class);
				if (meta != null && !table.getTable().isDisposed()) {
					updateTable.schedule();
					setFileName(part.getTitle());
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
			if (column == 0)
				return element.toString();
			if (column == 1 && meta != null) {
				Serializable m = null;
				try {
					m = meta.getMetaValue(element.toString());
					if (m != null) {
						if (m.getClass().isArray()) { // can cope with arrays
							return DatasetFactory.createFromObject(m).toString(true);
						}
						return m.toString();
					}
				} catch (Exception ignored) {
					// Null allowed
				}
			}
			return "";
		}
	}

	class HeaderFilter extends ViewerFilter {

		private String searchString;

		public void setSearchText(String s) {
			if (s == null)
				s = "";
			this.searchString = ".*" + s.toLowerCase() + ".*";
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement, Object element) {
			if (searchString == null || searchString.length() == 0)
				return true;
			final String name = (String) element;
			if (name == null || "".equals(name))
				return true;
			if (name.toLowerCase().matches(searchString))
				return true;
			if (name.toLowerCase().matches(searchString))
				return true;
			return false;
		}
	}
}
