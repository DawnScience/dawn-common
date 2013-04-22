/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.conversion.ui.pages;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawnsci.conversion.internal.AsciiConvert1D;
import org.dawnsci.conversion.ui.AbstractConversionPage;
import org.dawnsci.conversion.ui.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

/**
 *   AsciiConvertPage used if the context is a 1D ascii one.
 *
 *   @author gerring
 *   @date Aug 31, 2010
 *   @project org.edna.workbench.actions
 **/
public class AsciiConvertPage extends AbstractConversionPage {
	
	private CheckboxTableViewer checkboxTableViewer;
	private String[]            dataSetNames;
	private int                 conversionSelection;

	private boolean open      = true;
	private boolean overwrite = false;
	private String  path;
	private Label   txtLabel;
	private Text    txtPath;

	private IMetaData          imeta;
	private DataHolder         holder;
	
	private final static String[] CONVERT_OPTIONS = new String[] {"Tab Separated Values (*.dat)", 
		                                                          "Comma Separated Values (*.cvs)"};

	/**
	 * Create the wizard.
	 */
	public AsciiConvertPage() {
		super("wizardPage");
		setTitle("Convert Data");
		setDescription("Convert data from synchrotron formats and compressed files to common simple data formats.");
		dataSetNames = new String[]{"Loading..."};
    }

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		
		Composite container = new Composite(parent, SWT.NULL);

		setControl(container);
		container.setLayout(new GridLayout(1, false));
		
		Composite top = new Composite(container, SWT.NONE);
		top.setLayout(new GridLayout(3, false));
		top.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		
		Label convertLabel = new Label(top, SWT.NONE);
		convertLabel.setText("Convert to");
		
		final Combo combo = new Combo(top, SWT.READ_ONLY);
		combo.setItems(CONVERT_OPTIONS);
		combo.setToolTipText("Convert to file type by file extension");
		combo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		combo.select(0);
		
		conversionSelection = 0;
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				conversionSelection = combo.getSelectionIndex();
				path = null;
				txtPath.setText(getPath());
			}
		});
		
		txtLabel = new Label(top, SWT.NULL);
		txtLabel.setText("Export &File  ");
		txtPath = new Text(top, SWT.BORDER);
		txtPath.setEditable(false);
		txtPath.setEnabled(false);
		txtPath.setText(getPath());
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		txtPath.setLayoutData(gd);
		txtPath.addModifyListener(new ModifyListener() {			
			@Override
			public void modifyText(ModifyEvent e) {
				pathChanged();
			}
		});

		Button button = new Button(top, SWT.PUSH);
		button.setText("...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		
		final Button over = new Button(top, SWT.CHECK);
		over.setText("Overwrite file if it exists.");
		over.setSelection(overwrite);
		over.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				overwrite = over.getSelection();
				pathChanged();
			}
		});
		over.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
		
		final Button open = new Button(top, SWT.CHECK);
		open.setText("Open file after export.");
		open.setSelection(true);
		open.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				AsciiConvertPage.this.open = open.getSelection();
				pathChanged();
			}
		});
		open.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));


		pathChanged();

		
		Composite main = new Composite(container, SWT.NONE);
		main.setLayout(new GridLayout(2, false));
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		final Label chooseData = new Label(main, SWT.LEFT);
		chooseData.setText("Please tick data to export:");
		
		final ToolBarManager toolMan = new ToolBarManager(SWT.RIGHT|SWT.FLAT);
        createActions(toolMan);
        toolMan.createControl(main);
        toolMan.getControl().setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, true, false));
		
		this.checkboxTableViewer = CheckboxTableViewer.newCheckList(main, SWT.BORDER | SWT.FULL_SELECTION);
		Table table = checkboxTableViewer.getTable();
		table.setToolTipText("Select data to export to the csv.");
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		final MenuManager man = new MenuManager();
        createActions(man);
        Menu menu = man.createContextMenu(checkboxTableViewer.getControl());
        checkboxTableViewer.getControl().setMenu(menu);
	
		checkboxTableViewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public void dispose() {}
			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

			@Override
			public Object[] getElements(Object inputElement) {
				return dataSetNames;
			}
		});
		checkboxTableViewer.setInput(new Object());
		checkboxTableViewer.setAllGrayed(true);

		
		setPageComplete(false);

	}
	
    public boolean isPageComplete() {
    	if (context==null) return false;
        return super.isPageComplete();
    }
	
	private void createActions(IContributionManager toolMan) {
		
        final Action tickNone = new Action("Select None", Activator.getImageDescriptor("icons/unticked.gif")) {
        	public void run() {
        		checkboxTableViewer.setAllChecked(false);
        	}
        };
        toolMan.add(tickNone);
        
        final Action tickAll1D = new Action("Select All 1D Data", Activator.getImageDescriptor("icons/ticked.png")) {
        	public void run() {
        		setAll1DChecked();
        	}
        };
        toolMan.add(tickAll1D);

	}
	
	private static String exportFolder = null;
	protected String getPath() {
		if (path==null) { // We make one up from the source
			String sourcePath = getSourcePath();
			final File source = new File(sourcePath);
			final String strName = source.getName().substring(0, source.getName().indexOf("."))+"."+getExtension();
			if (exportFolder == null) {
				this.path = (new File(source.getParentFile(), strName)).getAbsolutePath();
			} else {
				this.path = (new File(exportFolder, strName)).getAbsolutePath();

			}		
		}
		return path;
	}
	
	private String getExtension() {
		return conversionSelection==0?"dat":"csv";
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for the container field.
	 */

	private void handleBrowse() {
		
		final FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
		dialog.setText("Choose file");
		final String filePath = getPath();
		if (filePath!=null) {
			final File file = new File(filePath);
			if (file.isDirectory()) {
				dialog.setFilterPath(file.getAbsolutePath());
			} else {
				dialog.setFilterPath(file.getParent());
				dialog.setFileName(file.getName());
			}

		}
		final String path = dialog.open();
		if (path!=null) {
			this.path    = path;
		    txtPath.setText(this.path);
		    exportFolder = (new File(path)).getParent();
		}

		pathChanged();
	}

	/**
	 * Ensures that both text fields are set.
	 */

	private void pathChanged() {

        final String p = txtPath.getText();
        txtLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));
		if (p==null || p.length() == 0) {
			updateStatus("Please select a file to export to.");
			return;
		}
		String strPath = getPath();
		final File path = new File(strPath);
		if (path.exists() && !path.canWrite()) {
			updateStatus("Please choose another location to export to; this one is read only.");
			txtLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_RED));
			return;
		}
		if (path.exists() && !overwrite) {
			updateStatus("Please confirm overwrite of the file.");
			return;
		}
		if (!path.getName().toLowerCase().endsWith("."+getExtension())) {
			updateStatus("Please set the file name to export as a file with the extension '"+getExtension()+"'.");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public boolean isOpen() {
		return open;
	}
	
	protected void getDataSetNames() throws Exception {
		
		getContainer().run(true, true, new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				
				try {

					final String source = getSourcePath();
					if (source==null || "".equals(source)) return;
					// Attempt to use meta data, save memory
					final IMetaData    meta = LoaderFactory.getMetaData(source, new ProgressMonitorWrapper(monitor));
					if (meta != null) {
						final Collection<String> names = meta.getDataNames();
						if (names !=null) {
							setDataNames(names.toArray(new String[names.size()]), meta, null);
							return;
						}
					}

					DataHolder holder = LoaderFactory.getData(source, new ProgressMonitorWrapper(monitor));
					final List<String> names = new ArrayList<String>(holder.getMap().keySet());
					Collections.sort(names);
					setDataNames(names.toArray(new String[names.size()]), null, holder);
					return;

				} catch (Exception ne) {
					throw new InvocationTargetException(ne);
				}

			}

		});
	}

	protected void setDataNames(String[] array, final IMetaData imeta, final DataHolder holder) {
		dataSetNames = array;
		this.imeta   = imeta;
		this.holder  = holder;
		getContainer().getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				checkboxTableViewer.getTable().setEnabled(true);
				checkboxTableViewer.refresh();
				checkboxTableViewer.setAllChecked(false);
				checkboxTableViewer.setAllGrayed(false);
				setAll1DChecked();
			}
		});
	}
	
	protected void setAll1DChecked() {
		for (String name : dataSetNames) {
			int rank=-1;
			if (imeta!=null) {
				rank = imeta.getDataShapes()!=null && imeta.getDataShapes().get(name)!=null
				     ? imeta.getDataShapes().get(name).length
				     : -1;
			}
			if (rank<0 && holder!=null) {
				final ILazyDataset ld = holder.getLazyDataset(name);
				rank = ld!=null ? ld.getRank() : -1;
			}
			
			if (rank==1) {
				checkboxTableViewer.setChecked(name, true);
			}
		}		
	}

	public String[] getSelected() {
		Object[] elements = checkboxTableViewer.getCheckedElements();
		final String[] ret= new String[elements.length];
		for (int i = 0; i < elements.length; i++) {
			ret[i]= elements[i]!=null ? elements[i].toString() : null;
		}
		return ret;
	}

	public boolean isOverwrite() {
		return overwrite;
	}

	@Override
	public void setContext(IConversionContext context) {
		this.context = context;
		if (context==null) { // new context being prepared.
			this.imeta  = null;
			this.holder = null;
		}
		// We populate the names later using a wizard task.
        try {
			getDataSetNames();
		} catch (Exception e) {
			logger.error("Cannot extract data sets!", e);
		}
        
        setPageComplete(true);
	}
	
	@Override
	public IConversionContext getContext() {
		if (context==null) return null;
		final AsciiConvert1D.ConversionInfoBean bean = new AsciiConvert1D.ConversionInfoBean();
		bean.setConversionType(getExtension());
		context.setUserObject(bean);
		context.setOutputPath(getPath()); // cvs or dat file.
		context.setDatasetNames(Arrays.asList(getSelected()));
		return context;
	}


}
