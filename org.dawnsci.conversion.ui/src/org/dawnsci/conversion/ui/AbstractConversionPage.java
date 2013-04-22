package org.dawnsci.conversion.ui;

import java.util.ArrayList;
import java.util.List;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

/**
 * 
 * Class to be used to add custom wizard pages to the conversion wizard.
 * 
 * @author fcp94556
 *
 */
public abstract class AbstractConversionPage extends WizardPage implements IConversionWizardPage {

	protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(AbstractConversionPage.class);

	protected IConversionContext context;
	protected AbstractConversionPage(String pageName) {
		super(pageName);
	}
	
	@Override
	public boolean isOpen() {
		return false;
	}

	public IConversionContext getContext() {
		return context;
	}

	public void setContext(IConversionContext context) {
		this.context = context;
	}
	
	protected String getSourcePath() {
		if (context!=null) return context.getFilePath();
		
		try {
			ISelection selection = EclipseUtils.getActivePage().getSelection();
			StructuredSelection s = (StructuredSelection)selection;
			final Object        o = s.getFirstElement();
			if (o instanceof IFile) {
				IFile source = (IFile)o;
				return source.getLocation().toOSString();
			}
		} catch (Throwable ignored) {
			// default ""
		}
		return "";
	        
	}
	
	/**
	 * All datasets of the right rank in the conversion file.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected List<String> getActiveDatasets(IProgressMonitor monitor) throws Exception {
		final String source = getSourcePath();
		if (source==null || "".equals(source)) return null;

		final ConversionScheme scheme = context.getConversionScheme();
		final IMetaData        meta   = LoaderFactory.getMetaData(source, new ProgressMonitorWrapper(monitor));
        final List<String>     names  = new ArrayList<String>(7);
        for (String name : meta.getDataShapes().keySet()) {
			final int[] shape = meta.getDataShapes().get(name);
			if (scheme.isRankSupported(shape.length)) {
				names.add(name);
			}
		}
        
        return names;

	}

}
