package org.dawnsci.conversion.ui;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawb.common.services.conversion.IConversionService;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.ExternalFileChoosePage;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;

public class ConversionChoicePage extends ExternalFileChoosePage implements IConversionWizardPage {
	
	private static final Logger logger = LoggerFactory.getLogger(ConversionChoicePage.class);

	private IConversionService service;
	private ConversionScheme chosenConversion;

	protected ConversionChoicePage(String pageName, IConversionService service) {
		super(pageName, "Please choose what you would like to convert", null);
		this.service = service;
	}
	
	@Override
	protected void createContentBeforeFileChoose(Composite container) {

		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		// 4 col grid
		final Label convLabel = new Label(container, SWT.NONE);
		convLabel.setText("Conversion Type");
		convLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		final CCombo choice = new CCombo(container, SWT.READ_ONLY);
		choice.setItems(ConversionScheme.getLabels());
		choice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		choice.select(0);
		this.chosenConversion = ConversionScheme.values()[0];
		choice.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				chosenConversion = ConversionScheme.values()[choice.getSelectionIndex()];
				pathChanged();
				getWizard().canFinish();
			}
		});
	}
	
	protected void createContentAfterFileChoose(Composite container) {
		ISelection selection = EclipseUtils.getActivePage().getSelection();
		StructuredSelection s = (StructuredSelection)selection;
		final Object        o = s.getFirstElement();
		if (o instanceof IFile) {
			IFile source = (IFile)o;
			setPath(source.getFullPath().toString());
		}
	}
	
	protected void pathChanged()  {
		final String filePath = getAbsoluteFilePath();
		if (filePath==null || "".equals(filePath)) {
			setErrorMessage("The file to convert has not been specified");
			return;
		}
		
		final File file = new File(filePath);
		if (!file.exists()) {
			setErrorMessage("The file '"+filePath+"' does not exist and cannot be converted.");
			return;
		}
		
		// Test that there are some datasets of the required rank
		try {
			
			final int ranks[] = chosenConversion.getPreferredRanks();
			if (ranks!=null) {
				final IMetaData meta = LoaderFactory.getMetaData(filePath, new IMonitor.Stub());
				final Map<String,int[]> shapes = meta.getDataShapes();
				boolean foundRequiredRank = false;
				for (String name : shapes.keySet()) {
					final int[] shape = shapes.get(name);
					for (int rank : ranks) {
						if (shape!=null && shape.length==rank) {
							foundRequiredRank = true;
							break;
						}						
					}
				}
				
				if (!foundRequiredRank) {
					setErrorMessage("Conversion '"+chosenConversion.getUiLabel()+"', requires datasets of rank(s) "+Arrays.toString(ranks)+" to be in the file.");
					return;
				}
			}
		} catch (Throwable ne) {
			setErrorMessage("Error, cannot read file '"+filePath+"'. Reason: "+ne.getMessage());
			logger.error("Cannot  '"+filePath+"'", ne);
		}
		
		setErrorMessage(null);
		return;

	}

	
	@Override
	public IConversionContext getContext() {
		final String filePath = getAbsoluteFilePath();
		IConversionContext context = service.open(filePath);
		context.setConversionScheme(chosenConversion);
		context.setOutputPath((new File(filePath)).getParent());
		return context;
	}

	@Override
	public void setContext(IConversionContext context) {
		// Does nothing, we generate the context in this class.
	}
	
    public boolean isPageComplete() {
    	boolean parentOk = super.isPageComplete();
    	
    	if (isCurrentPage()) {
        	final IWizardPage next = getNextPage();
        	if (next instanceof IConversionWizardPage) {
    	    	try {
    		    	final IConversionContext context = getContext();
    		    	final File ourConv   = new File(context.getFilePath());
    		    	final File theirConv = new File(((IConversionWizardPage)next).getContext().getFilePath());
    	    	    if (!ourConv.equals(theirConv)) {
    	    	    	((IConversionWizardPage)next).setContext(null);
    	    	    	return false;
    	    	    }
    	    	} catch (Exception ne) {
    	    		// Nowt
    	    	}
        	} 
    	}
    	if (!parentOk) return false;
    	
   	
    	return parentOk;
    }

	@Override
	public boolean isOpen() {
		return false;
	}


}
