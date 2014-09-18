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
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.wizard.ResourceChoosePage;
import org.dawnsci.conversion.converters.Convert1DtoND.Convert1DInfoBean;
import org.dawnsci.conversion.ui.Activator;
import org.dawnsci.conversion.ui.IConversionWizardPage;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.mihalis.opal.checkBoxGroup.CheckBoxGroup;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class H5From1DConvertPage extends ResourceChoosePage implements
IConversionWizardPage {


	private CheckboxTableViewer checkboxTableViewer;
	private String[]            dataSetNames;
	private IMetadata          imeta;
	private IDataHolder        holder;
	private IConversionContext context;
	private Spinner fastSpinner;
	private Spinner slowSpinner;
	private CheckBoxGroup checkBoxGroup;
	private Label sizeOkLabel;
	protected CCombo         nameChoice;
	protected String         datasetName;

	public H5From1DConvertPage() {
		super("wizardPage", "Page for converting 1D data to a HDF5 file", null);
		setTitle("Convert 1D to HDF5");
		dataSetNames = new String[]{"Loading..."};
		setDirectory(false);
		setNewFile(true);
		setPathEditable(true);
	}


	public void createContentAfterFileChoose(Composite container) {

		Composite main = new Composite(container, SWT.NONE);
		main.setLayout(new GridLayout(2, false));
		main.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

		final Label chooseData = new Label(main, SWT.LEFT);
		chooseData.setText("Please tick data to export:");

		final ToolBarManager toolMan = new ToolBarManager(SWT.RIGHT|SWT.FLAT|SWT.WRAP);
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
		
		CheckBoxGroup axisCheckBox = new CheckBoxGroup(main, SWT.None);
		axisCheckBox.setLayout(new GridLayout(2, false));
		axisCheckBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		axisCheckBox.setText("Save axis dataset");
		
		final Composite content = axisCheckBox.getContent();
		
		nameChoice = new CCombo(content, SWT.READ_ONLY|SWT.BORDER);
		nameChoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		nameChoice.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				datasetName = nameChoice.getItem(nameChoice.getSelectionIndex());
			}
		});
		
		nameChoice.setItems(dataSetNames);
		
		Label label = new Label(content, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		label.setText("Select a dataset to be used as an axis. Nexus signal/axis tags will be added to appropriately sized datasets.");
		axisCheckBox.deactivate();
		
		createAdvanced(container);
		
		setPageComplete(false);
	}

	@Override
	public IConversionContext getContext() {
		if (context == null) return null;
		context.setDatasetNames(Arrays.asList(getSelected()));
		context.setOutputPath(getAbsoluteFilePath());
		
		if (checkBoxGroup.isActivated()) {
			int val = fastSpinner.getSelection()*slowSpinner.getSelection();
			
			if (context.getFilePaths() != null & val == context.getFilePaths().size()) {
				Convert1DInfoBean bean  = new Convert1DInfoBean();
				bean.fastAxis = fastSpinner.getSelection();
				bean.slowAxis = slowSpinner.getSelection();
				
				context.setUserObject(bean);
			}
		}
		
		if (nameChoice.isEnabled()) {
			context.setAxisDatasetName(nameChoice.getText());
		}
		
		return context;
	}

	@Override
	public void setContext(IConversionContext context) {
		if (context!=null && context.equals(this.context)) return;

		this.context = context;
		setErrorMessage(null);
		if (context==null) { // new context being prepared.
			this.imeta  = null;
			this.holder = null;
			setPageComplete(false);
			return;
		}
		// We populate the names later using a wizard task.
		try {
			getDataSetNames();
		} catch (Exception e) {
			//logger.error("Cannot extract data sets!", e);
		}
		
		final File source = new File(getSourcePath(context));
		setPath(source.getParent()+File.separator+"output.nxs");
		
		testSpinnerValues();
		setPageComplete(true);

	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
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

	protected void getDataSetNames() throws Exception {

		getContainer().run(true, true, new IRunnableWithProgress() {

			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

				try {

					final String source = getSourcePath(context);
					if (source==null || "".equals(source)) return;
					// Attempt to use meta data, save memory
					final IMetadata    meta = LoaderFactory.getMetadata(source, new ProgressMonitorWrapper(monitor));
					if (meta != null) {
						final Collection<String> names = meta.getDataNames();
						if (names !=null) {
							setDataNames(names.toArray(new String[names.size()]), meta, null);
							return;
						}
					}

					IDataHolder holder = LoaderFactory.getData(source, new ProgressMonitorWrapper(monitor));
					final List<String> names = new ArrayList<String>(holder.toLazyMap().keySet());
					Collections.sort(names);
					setDataNames(names.toArray(new String[names.size()]), null, holder);
					return;

				} catch (Exception ne) {
					throw new InvocationTargetException(ne);
				}

			}

		});
	}
	protected void setDataNames(String[] array, final IMetadata imeta, final IDataHolder holder) {
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
				
				nameChoice.setItems(dataSetNames);
				nameChoice.select(0);
			}
		});
	}
	
	private void createAdvanced(final Composite parent) {
		
		final ExpandableComposite advancedComposite = new ExpandableComposite(parent, SWT.NONE);
		advancedComposite.setExpanded(false);
		advancedComposite.setText("Advanced");
		advancedComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		final Composite advanced = new Composite(parent, SWT.NONE);
		advanced.setLayout(new GridLayout(1, false));
		advanced.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		
		checkBoxGroup = new CheckBoxGroup(advanced, SWT.None);
		checkBoxGroup.setLayout(new GridLayout(2, false));
		checkBoxGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		checkBoxGroup.setText("Fold 1D to 3D");
		
		final Composite content = checkBoxGroup.getContent();

		
		Label label = new Label(content, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Fast axis: ");
		
		fastSpinner = new Spinner(content, SWT.None);
		fastSpinner.setMinimum(1);
		fastSpinner.setIncrement(1);
		fastSpinner.setMaximum(Integer.MAX_VALUE);
		fastSpinner.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				testSpinnerValues();
			}
		});
		
		label = new Label(content, SWT.NULL);
		label.setLayoutData(new GridData());
		label.setText("Slow axis: ");
		
		slowSpinner = new Spinner(content, SWT.None);
		slowSpinner.setMinimum(1);
		slowSpinner.setIncrement(1);
		slowSpinner.setMaximum(Integer.MAX_VALUE);
		
		slowSpinner.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				testSpinnerValues();
			}
		});

		sizeOkLabel = new Label(content, SWT.NULL);
		sizeOkLabel.setLayoutData(new GridData());
		sizeOkLabel.setText("Are axis dimensions ok?");
		
		checkBoxGroup.deactivate();
		GridUtils.setVisible(advanced, false);
		ExpansionAdapter expansionListener = new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				GridUtils.setVisible(advanced, !advanced.isVisible());
				parent.layout(new Control[]{advanced, advancedComposite});
				parent.layout();
				parent.getParent().layout();
			}
		};
		advancedComposite.addExpansionListener(expansionListener);
		
	}
	
	public String[] getSelected() {
		Object[] elements = checkboxTableViewer.getCheckedElements();
		final String[] ret= new String[elements.length];
		for (int i = 0; i < elements.length; i++) {
			ret[i]= elements[i]!=null ? elements[i].toString() : null;
		}
		return ret;
	}
	
	private void testSpinnerValues() {
		Integer val = fastSpinner.getSelection()*slowSpinner.getSelection();
		
		if (context.getFilePaths() == null) return;
		Display display = Display.getCurrent();
		
		sizeOkLabel.setText("Product must equal " + context.getFilePaths().size());
		
		if (val != context.getFilePaths().size()) {
			Color red = display.getSystemColor(SWT.COLOR_RED);
			sizeOkLabel.setForeground(red);
		} else {
			Color blue = display.getSystemColor(SWT.COLOR_BLUE);
			sizeOkLabel.setForeground(blue);
		}
	}
	
	/**
	 * Checks the path is ok.
	 */
	@Override
	protected void pathChanged() {

		super.pathChanged();
		final String path = getAbsoluteFilePath();
		if (path!=null) {
			final File output = new File(path);
			try {
				if (output.exists() && !output.isFile()) {
					setErrorMessage("The file '"+output+"' is not a valid file.");
					return;			
				} else if (output.exists() && overwrite != null && !overwrite.getSelection()){
					setErrorMessage("The file '"+output+"' exists, please set to overwrite.");
					return;
				}
			} catch (Exception ne) {
				setErrorMessage(ne.getMessage()); // Not very friendly...
				return;			
			}
			setErrorMessage(null);
			return;
		}
	}

}
