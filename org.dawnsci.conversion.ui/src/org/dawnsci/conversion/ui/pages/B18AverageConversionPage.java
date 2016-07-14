package org.dawnsci.conversion.ui.pages;

import java.io.File;
import java.util.Arrays;

import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.conversion.converters.B18AverageConverter;
import org.dawnsci.conversion.converters.B18AverageConverter.B18DataType;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class B18AverageConversionPage extends AbstractDatasetChoosePage {

	// some Java 8 lambda/stream magic
	private final static String[] B18DATATYPE_OPTIONS = Arrays.stream(B18AverageConverter.B18DataType.values()).map(e -> e.toString()).toArray(String[]::new);
	
	protected int dataTypeSelection;
	
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
				
		Label convertLabel = new Label(container, SWT.NONE);
		convertLabel.setText("Datatype");
		
		final Combo combo = new Combo(container, SWT.READ_ONLY|SWT.BORDER);
		combo.setItems(B18DATATYPE_OPTIONS);
		combo.setToolTipText("Select the type of data");
		combo.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 3, 1));
		combo.select(0);
		
		dataTypeSelection = 0;
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				dataTypeSelection = combo.getSelectionIndex();
			}
		});

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
        	this.openButton.setSelection(false);
        	this.open = false;
        	this.openButton.setEnabled(false);
        } else {
        	// this cannot happen... More than one file should always be provided for this to work
        	setPageComplete(false);
        	logger.error("B18 data averaging requires either multiple files or a folder");
        }

	}	
	
	@Override
	public IConversionContext getContext() {
		if (context==null) return null;
		final B18AverageConverter.ConversionInfoBean bean = new B18AverageConverter.ConversionInfoBean();
		//bean.setConversionType(getExtension());
		bean.setDataType(B18DataType.values()[dataTypeSelection]);
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
    	main.setVisible(false);
	}
	
}
