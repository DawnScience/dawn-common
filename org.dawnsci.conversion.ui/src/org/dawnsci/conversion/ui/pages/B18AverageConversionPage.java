package org.dawnsci.conversion.ui.pages;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.conversion.converters.B18AverageConverter;
import org.dawnsci.conversion.converters.B18AverageConverter.B18DataType;
import org.dawnsci.conversion.converters.B18AverageConverter.B18InterpolationType;
import org.dawnsci.conversion.ui.LoaderServiceHolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.slicing.api.util.SliceUtils;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Event;

public class B18AverageConversionPage extends AbstractDatasetChoosePage {

	// some Java 8 lambda/stream magic
	private final static String[] B18DATATYPE_OPTIONS = Arrays.stream(B18AverageConverter.B18DataType.values()).map(e -> e.toString()).toArray(String[]::new);
	private final static String[] B18INTERPOLATION_OPTIONS = Arrays.stream(B18AverageConverter.B18InterpolationType.values()).map(e -> e.toString()).toArray(String[]::new);
	
	protected int dataTypeSelection;
	protected int interpolationSelection;
	protected String[] parseableMetadata;
	protected Table parseableMetadataTable;
	private IMetadata md;
	private boolean useMetadata;
	private double metadataDelta;
	private Button parseableMetadataButton;
	
	/**
	 * Create the wizard.
	 */
	public B18AverageConversionPage() {
		super("wizardPage", "Average B18 datasets", null);
		setTitle("Average B18 datasets");
		dataSetNames = new String[]{"Loading..."};
		setDirectory(false);
		setOverwriteVisible(false);
		setNewFile(true);
		setPathEditable(true);
    	setFileLabel("Output file");
    }

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	@Override
	public void createContentBeforeFileChoose(Composite container) {
		Composite newContainer = new Composite(container, SWT.NONE);
		newContainer.setLayout(new GridLayout(5, false));
		final GridData newContainerGrid = new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1);
		newContainerGrid.minimumHeight = 35;
		newContainer.setLayoutData(newContainerGrid);
		{
			final Label convertLabel = new Label(newContainer, SWT.NONE);
			convertLabel.setText("Datatype");
			convertLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));

			final Combo combo = new Combo(newContainer, SWT.READ_ONLY|SWT.BORDER);
			combo.setItems(B18DATATYPE_OPTIONS);
			combo.setToolTipText("Select the type of data");
			combo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
			combo.select(0);

			dataTypeSelection = 0;
			combo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					dataTypeSelection = combo.getSelectionIndex();
					if (dataTypeSelection == B18AverageConverter.B18DataType.CUSTOM.ordinal()) {
						recursiveSetEnabled(main, true);
					} else {
						recursiveSetEnabled(main, false);
					}
				}
			});
		}
		final Label fillLabel = new Label(newContainer, SWT.NONE);
		fillLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		{
			final Label convertLabel = new Label(newContainer, SWT.NONE);
			convertLabel.setText("Interpolation");
			convertLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

			final Combo combo = new Combo(newContainer, SWT.READ_ONLY|SWT.BORDER);
			combo.setItems(B18INTERPOLATION_OPTIONS);
			combo.setToolTipText("Select the interpolation type");
			combo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
			combo.select(0);

			interpolationSelection = 0;
			combo.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					interpolationSelection = combo.getSelectionIndex();
				}
			});
		}
		
		final Group parseableMetadataGroup = new Group(container, SWT.NONE);
		parseableMetadataGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1));
		parseableMetadataGroup.setLayout(new GridLayout(3, false));
		parseableMetadataGroup.setText("Metadata");
		
		parseableMetadataButton = new Button(parseableMetadataGroup, SWT.CHECK);
		parseableMetadataButton.setText("Use metadata for grouping");
		parseableMetadataButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		final Label parseableMetadataLabel = new Label(parseableMetadataGroup, SWT.RIGHT);
		parseableMetadataLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		parseableMetadataLabel.setText("Delta");
		final Text parseableMetadataText = new Text(parseableMetadataGroup, SWT.BORDER | SWT.SINGLE);
		final GridData textGrid = new GridData(SWT.FILL, SWT.CENTER, true, false);
		//textGrid.minimumWidth = 40;
		parseableMetadataText.setLayoutData(textGrid);
		parseableMetadataText.setText("0.1");
		metadataDelta = 0.1;
		//parseableMetadataText.setSize(450, SWT.DEFAULT);
		parseableMetadataText.addListener(SWT.Modify, event -> {
			// check if delta is a double and greater than or equal to zero
			String text = parseableMetadataText.getText();
			try {
				double deltaTemp = Double.parseDouble(text);
				if (deltaTemp < 0.0)
					throw new Exception();
				parseableMetadataText.setBackground(null);
				setErrorMessage(null);
				setPageComplete(true);
				metadataDelta = deltaTemp;
			} catch (Exception e) {
				parseableMetadataText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
				setErrorMessage("Delta must be greater than or equal to zero");
				setPageComplete(false);
			}
		});
		
		// parseableMetadata table
		parseableMetadataTable = new Table(parseableMetadataGroup, SWT.BORDER | SWT.HIDE_SELECTION);
		parseableMetadataTable.setLinesVisible(true);
		final GridData tableGrid = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		tableGrid.minimumHeight = 100;
		parseableMetadataTable.setLayoutData(tableGrid);
		parseableMetadataTable.setHeaderVisible(true);
		// metadata name column
		TableColumn tempColumn = new TableColumn(parseableMetadataTable, SWT.LEFT);
		tempColumn.setWidth(300);
		tempColumn.setText("Metadata name");
		// metadata value first file
		tempColumn = new TableColumn(parseableMetadataTable, SWT.LEFT);
		tempColumn.setWidth(200);
		tempColumn.setText("Metadata value first file");
	
		parseableMetadataButton.addListener(SWT.Selection, event -> {
			parseableMetadataLabel.setEnabled(parseableMetadataButton.getSelection());
			parseableMetadataText.setEnabled(parseableMetadataButton.getSelection());
			parseableMetadataTable.setEnabled(parseableMetadataButton.getSelection());
			useMetadata = parseableMetadataButton.getSelection();
		});
		
		parseableMetadataButton.setSelection(false);
		parseableMetadataButton.notifyListeners(SWT.Selection, new Event());
		
	}
	
	/**
	 * Ensures that both text fields are set.
	 */
	protected void pathChanged() {

        final String p = getAbsoluteFilePath();
		if (p==null || p.length() == 0) {
			setErrorMessage("Please select a folder to export to.");
			setPageComplete(false);
			return;
		}
		final File path = new File(p);
		if (path.exists() && !path.canWrite()) {
			setErrorMessage("Please choose another location to export to; this one is read only.");
			setPageComplete(false);
			return;
		}
		else if (!path.exists()) {
			setErrorMessage("Please choose an existing folder to export to.");
			setPageComplete(false);
			return;
		}
		setErrorMessage(null);
		setPageComplete(true);
	}

	@Override
	protected void getDataSetNames() throws Exception {
		
		getContainer().run(true, true, new IRunnableWithProgress() {


			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				
				try {

					final String source = getSourcePath(context);
					if (source==null || "".equals(source)) return;
					// Attempt to use meta data, save memory
					IDataHolder holder = LoaderServiceHolder.getLoaderService().getData(source, new ProgressMonitorWrapper(monitor));
					List<String> names = SliceUtils.getSlicableNames(holder, getMinimumDataSize());
					// get rid of the energy -> it will always be included...
					names = names.stream().filter(name -> !name.matches("(?i).*energy.*")).collect(Collectors.toList());
					setDataNames(names.toArray(new String[names.size()]), null, holder);
					
					md = holder.getMetadata();
					parseableMetadata = md.getMetaNames().stream().filter(name -> {
						// lambda magic
						try {
							Double.parseDouble((String) md.getMetaValue(name));
							return true;
						} catch (Exception e) {
							return false;
						}
					}).toArray(String[]::new);
					
					return;

				} catch (Exception ne) {
					throw new InvocationTargetException(ne);
				}

			}

		});
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
			logger.error("Cannot extract data sets!", e);
		}
       
        // necessary to ensure the table is grayed out when opening the page
    	checkboxTableViewer.getTable().setEnabled(false);
    	
		final File source = new File(getSourcePath(context));
       
        setPageComplete(true);
        
        if (context.getFilePaths().size()>1 || source.isDirectory()) { // Multi
        	setPath(source.getParent());
        	setDirectory(true);
        	setFileLabel("Output folder");
        	GridUtils.setVisible(multiFileMessage, true);
        	this.overwriteButton.setSelection(true);
        	this.overwrite = true;
        	this.overwriteButton.setEnabled(false);
        	this.overwriteButton.setVisible(false);
        	this.openButton.setSelection(false);
        	this.open = false;
        	this.openButton.setEnabled(false);
        	this.openButton.setVisible(false);
        	//parseableMetadataTable.getParent().getParent().layout(false);
        } else {
        	// this cannot happen... More than one file should always be provided for this to work
        	setPageComplete(false);
        	logger.error("B18 data averaging requires either multiple files or a folder");
        }

    	// clear the table
    	parseableMetadataTable.removeAll();
    	// add the metadata names
    	for (String metaDataName : parseableMetadata) {
    		TableItem item = new TableItem(parseableMetadataTable, SWT.NONE);
    		
    		item.setText(0, metaDataName);
    		try {
				item.setText(1, (String) md.getMetaValue(metaDataName));
			} catch (Exception e) {
				// unlikely to happen but have to catch it...
			}
    	}
    	if (parseableMetadata == null || parseableMetadata.length == 0) {
    		parseableMetadataButton.setEnabled(false);
    		parseableMetadataButton.setSelection(false);
    		useMetadata = false;
    	}
    	else {
    		parseableMetadataTable.select(0);
    	}
	}	
	
	@Override
	public IConversionContext getContext() {
		if (context==null) return null;
		final B18AverageConverter.ConversionInfoBean bean = new B18AverageConverter.ConversionInfoBean();
		//bean.setConversionType(getExtension());
		bean.setDataType(B18DataType.values()[dataTypeSelection]);
		bean.setInterpolationType(B18InterpolationType.values()[interpolationSelection]);
		bean.setUseMetadataForGrouping(useMetadata);
		if (bean.isUseMetadataForGrouping()) {
			bean.setMetadataForGroupingDelta(metadataDelta);
			//now the name of the metadata...
			String metadataName = parseableMetadataTable.getSelection()[0].getText(0);
			bean.setMetadataForGroupingName(metadataName);
		}
		context.setUserObject(bean);
		context.setOutputPath(getAbsoluteFilePath()); // cvs or dat file.
		//getSelected will here need to correspond to the datasets we need. Their names are known
		context.setDatasetNames(Arrays.asList(getSelected()));
		return context;
	}

	protected int getMinimumDataSize() {
		return 1; // Data must be 1D or better
	}

	@Override
	protected String getDataTableTooltipText() {
		return "Select data to export to the "+getExtension();
	}

	@Override
	protected String getExtension() {
		return "dat";
	}

	@Override
	public void createContentAfterFileChoose(Composite container) {
		super.createContentAfterFileChoose(container);
    	main.setVisible(true);
    	// the default dataType is never CUSTOM
    	recursiveSetEnabled(main, false);
	}
	
	// Inspired by http://stackoverflow.com/questions/2957657/disable-and-grey-out-an-eclipse-widget
	public void recursiveSetEnabled(Control ctrl, boolean enabled) {
		   if (ctrl instanceof Composite) {
		      Composite comp = (Composite) ctrl;
		      Control[] kids = comp.getChildren();
		      for (Control c : kids)
		         recursiveSetEnabled(c, enabled);
		      if (kids == null || kids.length == 0) 
		    	  ctrl.setEnabled(enabled);
		   } else {
		      ctrl.setEnabled(enabled);
		   }
		}
}
