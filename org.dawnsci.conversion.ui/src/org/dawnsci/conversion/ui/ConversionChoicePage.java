package org.dawnsci.conversion.ui;

import java.io.File;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawb.common.services.conversion.IConversionService;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.ExternalFileChoosePage;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;

public class ConversionChoicePage extends ExternalFileChoosePage implements IConversionWizardPage {

	private IConversionService service;

	protected ConversionChoicePage(String pageName, IConversionService service) {
		super(pageName, "Please choose what you would like to convert", null);
		this.service = service;
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		ISelection selection = EclipseUtils.getActivePage().getSelection();
		StructuredSelection s = (StructuredSelection)selection;
		final Object        o = s.getFirstElement();
		if (o instanceof IFile) {
			IFile source = (IFile)o;
			setPath(source.getFullPath().toString());
		}

 	}

	@Override
	public IConversionContext getContext() {
		final String filePath = getAbsoluteFilePath();
		IConversionContext context = service.open(filePath);
		context.setConversionScheme(ConversionScheme.ASCII_FROM_1D); // TODO Hard coded.
		return context;
	}

	@Override
	public void setContext(IConversionContext context) {
		// Does nothing, we generate the context in this class.
	}
	
    public boolean isPageComplete() {
    	boolean parentOk = super.isPageComplete();
    	if (!parentOk) return false;
    	
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
    	
    	return parentOk;
    }

	@Override
	public boolean isOpen() {
		return false;
	}


}
