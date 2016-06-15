package org.dawnsci.conversion.ui.pages;

import java.io.File;
import java.util.Arrays;

import org.dawb.common.ui.util.GridUtils;
import org.eclipse.dawnsci.analysis.api.conversion.IConversionContext;
import org.eclipse.swt.widgets.Composite;

public class B18ReprocessAsciiConversionPage extends AbstractDatasetChoosePage {

	/**
	 * Create the wizard.
	 */
	public B18ReprocessAsciiConversionPage() {
		super("wizardPage", "Reprocess B18 datasets by removing detector element specific datasets", null);
		setTitle("Reprocess B18 datasets: remove element datasets");
		dataSetNames = new String[]{"Loading..."};
		setDirectory(false);
		setOverwriteVisible(false);
		setNewFile(true);
		setPathEditable(true);
    	setFileLabel("Output file");
    }

	/**
	 * Ensures that both text fields are set.
	 */
	protected void pathChanged() {

        final String p = getAbsoluteFilePath();
		if (p==null || p.length() == 0) {
			setErrorMessage("Please select a file to export to.");
			setPageComplete(false);
			return;
		}
		final File path = new File(p);
		if (path.exists() && !path.canWrite()) {
			setErrorMessage("Please choose another location to export to; this one is read only.");
			setPageComplete(false);
			return;
		}
		if (context.getFilePaths().size()<2) {
			if (path.exists() && !overwrite) {
				setErrorMessage("Please confirm overwrite of the file.");
				setPageComplete(false);
				return;
			}
			if (!path.getName().toLowerCase().endsWith("."+getExtension())) {
				setErrorMessage("Please set the file name to export as a file with the extension '"+getExtension()+"'.");
				setPageComplete(false);
				return;
			}
		} else {
			if (!path.exists()) {
				setErrorMessage("Please choose an existing folder to export to.");
				setPageComplete(false);
				return;
			}
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
        	if (source.isFile()) {
        	    final String strName = source.getName().substring(0, source.getName().indexOf("."))+"."+getExtension();
	        	setPath((new File(source.getParentFile(), strName)).getAbsolutePath());
	        	setDirectory(false);
	        	setFileLabel("Output file");
	        	GridUtils.setVisible(multiFileMessage, false);
	        	this.overwriteButton.setEnabled(true);
	        	this.openButton.setEnabled(true);
        	} 
        }

	}	
	
	@Override
	public IConversionContext getContext() {
		if (context==null) return null;
		//final AsciiConvert1D.ConversionInfoBean bean = new AsciiConvert1D.ConversionInfoBean();
		//bean.setConversionType(getExtension());
		//context.setUserObject(bean);
		context.setOutputPath(getAbsoluteFilePath()); // cvs or dat file.
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
