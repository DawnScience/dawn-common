package org.dawnsci.conversion.ui.pages;

import java.io.File;
import java.lang.reflect.Array;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IProcessingConversionInfo;
import org.dawnsci.conversion.converters.ImageConverter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;

import uk.ac.diamond.scisoft.analysis.processing.IExecutionVisitor;
import uk.ac.diamond.scisoft.analysis.processing.IOperation;
import uk.ac.diamond.scisoft.analysis.processing.visitors.HierarchicalFileExecutionVisitor;

public class ImageProcessConvertPage extends AbstractSliceConversionPage {

	IWorkbench workbench;
	
	public ImageProcessConvertPage() {
		super("wizardPage", "Page for processing HDF5 data.", null);
		setTitle("Process");
		setDirectory(true);
		setFileLabel("Export to");
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void createAdvanced(Composite parent) {
		
		final File source = new File(getSourcePath(context));
		setPath(source.getParent()+File.separator+"output");

	}

	public void setWorkbench(IWorkbench workbench) {
		this.workbench = workbench;
	}
	
	@Override
	public IConversionContext getContext() {
		if (context == null) return null;
		IConversionContext context = super.getContext();
		
		if (workbench == null) return context;
		
		IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
		IViewPart view = page.findView("org.dawnsci.processing.ui.processingView");
		
		Object ob = view.getAdapter(IOperation.class);
		IOperation[] ops = null;
		
		if (ob.getClass().isArray() && Array.get(ob, 0) instanceof IOperation) {
			ops = (IOperation[])ob;
		}
		
		if (ops != null) {
			
			final IOperation[] fop = ops;
			
			context.setUserObject(new IProcessingConversionInfo() {

				@Override
				public IOperation[] getOperationSeries() {
					return fop;
				}

				@Override
				public IExecutionVisitor getExecutionVisitor(String fileName) {
					return new HierarchicalFileExecutionVisitor(fileName);
				}

			});
		}
		return context;
	}
	
}
