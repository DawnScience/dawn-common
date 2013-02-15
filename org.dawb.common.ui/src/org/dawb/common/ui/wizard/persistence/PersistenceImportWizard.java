package org.dawb.common.ui.wizard.persistence;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.plot.IPlottingSystem;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.CheckWizardPage;
import org.dawb.common.ui.wizard.ExternalFileChoosePage;
import org.dawb.common.util.io.FileUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;

import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

/**
 * 
 * @author fcp94556
 *
 */
public class PersistenceImportWizard extends AbstractPerstenceWizard implements IImportWizard {

	public static final String ID = "org.dawnsci.plotting.importMask";
	private ExternalFileChoosePage fcp;
	private CheckWizardPage options;

	public PersistenceImportWizard() {
		
		setWindowTitle("Import");
		
		this.fcp = new ExternalFileChoosePage("Import File", null, null);
		fcp.setDescription("Choose the file (*.nxs or *.msk) to import.");
		addPage(fcp);
		
		this.options = new CheckWizardPage("Import Options", createDefaultOptions());
		options.setStringValues("Mask", Arrays.asList(""));
		options.setDescription("Please choose things to import.");
		addPage(options);
		
	}
	
	private static String lastStaticPath;
	
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		if (lastStaticPath!=null) {
			fcp.setPath(lastStaticPath);
		}
	}
	
    public boolean canFinish() {
    	COMPLETE_TEST: if (fcp.isPageComplete()) {
    		final String absolutePath = fcp.getAbsoluteFilePath();
    		if (absolutePath==null) break COMPLETE_TEST;
    		options.setOptionEnabled("Original Data", false);
    		options.setOptionEnabled("Mask",          true);
    		options.setOptionEnabled("Regions",       true);
    		final File   file         = new File(absolutePath);
    		if (file.exists())  {
    			final String ext = FileUtils.getFileExtension(file);
    			if (ext!=null) {
    				if ("msk".equals(ext.toLowerCase())){
    		    		options.setOptionEnabled("Regions", false);
   					
    				} else if ("nxs".equals(ext.toLowerCase())) {
    					
    		    		IPersistentFile     pf=null;
    		    		
    		    		try {
    		        		IPersistenceService service = (IPersistenceService)ServiceManager.getService(IPersistenceService.class);
    		    		    pf    = service.getPersistentFile(file.getAbsolutePath());
    		    		    final List<String>  names = pf.getMaskNames(null);
    		    		    if (names==null || names.isEmpty()) {
    		    		    	options.setOptionEnabled("Mask", false);
    		    		    } else {
    		    		    	options.setStringValues("Mask", names);
    		    		    }
    		    		    
    		    		    final List<String> regions = pf.getROINames(null);
    		    		    if (regions==null || regions.isEmpty()) {
    		    		    	options.setOptionEnabled("Regions", false);
    		    		    }
    		    		        		    
    		    		} catch (Throwable ne) {
    		    			logger.error("Cannot read persistence file at "+file);
    		    		} finally {
    		    			if (pf!=null) pf.close();
    		    		}

    				}
    			}
    				
    			
    		}
    	}
    	return super.canFinish();
    }

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean performFinish() {
		 String absolutePath = null;
		 try {
			 absolutePath   = fcp.getAbsoluteFilePath();
			 			 
			 final IWorkbenchPart  part   = EclipseUtils.getPage().getActivePart();
			 final IPlottingSystem system = (IPlottingSystem)part.getAdapter(IPlottingSystem.class);

			 final String finalPath = absolutePath;
			 getContainer().run(true, true, new IRunnableWithProgress() {

				 @Override
				 public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					 
					 IPersistentFile file = null;
					 try {
	// TODO Fit2D!
						 IPersistenceService service = (IPersistenceService)ServiceManager.getService(IPersistenceService.class);
						 file    = service.getPersistentFile(finalPath);
						 
						 final IMonitor mon = new ProgressMonitorWrapper(monitor);

						 // Save things.
						 ITrace trace  = system.getTraces().iterator().next();
						 if (options.is("Original Data")) {
							 // TODO
							 //trace.setData(file.getData("data", mon));
//							 if (trace instanceof IImageTrace) {
//								 file.getAxes(xAxisName, yAxisName, mon)
//								 final List<AbstractDataset> axes = file.getAxes(xAxisName, yAxisName, mon)
//								 if (axes!=null) ((IImageTrace)trace).setAxes(axes);
//							 }
						 }
						 
						 if (options.is("Mask") && trace instanceof IImageTrace) {
							 final IImageTrace image = (IImageTrace)trace;
							 String name = options.getString("Mask"); //TODO drop down of available masks.
							 if (name == null) name = file.getMaskNames(null).get(0);
							 final BooleanDataset mask = file.getMask(name, mon);
							 if (mask!=null)  {
								 Display.getDefault().syncExec(new Runnable() {
									 public void run() {
										 image.setMask(mask);
									 }
								 });
							 }
							 
						 }
						 
						 final IPersistentFile finalFile = file;
						 final Map<String, ROIBase> rois = file.getROIs(mon);
						 if (options.is("Regions") && rois!=null && !rois.isEmpty()) {
							 for (final String roiName : rois.keySet()) {
								 final ROIBase roi = rois.get(roiName);
								 Display.getDefault().syncExec(new Runnable() {
									 public void run() {
										 try {
											 IRegion region = null;
											 if (system.getRegion(roiName)!=null) {
												 region = system.getRegion(roiName);
												 region.setROI(roi);
											 } else {
												 region = system.createRegion(roiName, RegionType.forROI(roi));
												 system.addRegion(region);
												 region.setROI(roi);
											 }
											 if (region!=null) {
												 String uObject = finalFile.getRegionAttribute(roiName, "User Object");
												 if (uObject!=null) region.setUserObject(uObject); // Makes a string out of
												                                                   // it but gives a clue.
											 }
										 } catch (Throwable e) {
											 logger.error("Cannot create/import region "+roiName, e);
										 }
									 }
								 });
							 }
						 }
						 
					 } catch (Exception e) {
						 throw new InvocationTargetException(e);
					 } finally {
						 if (file!=null) file.close();
					 }
				 }
			 });
		 } catch (Throwable ne) {
			 if (ne instanceof InvocationTargetException && ((InvocationTargetException)ne).getCause()!=null){
				 ne = ((InvocationTargetException)ne).getCause();
			 }
			 String message = null;
			 if (absolutePath!=null) {
				 message = "Cannot import from '"+absolutePath+"' ";
			 } else {
				 message = "Cannot import file.";
			 }
			 logger.error("Cannot export mask file!", ne);
		     ErrorDialog.openError(Display.getDefault().getActiveShell(), "Export failure", message, new Status(IStatus.WARNING, "org.dawb.common.ui", ne.getMessage(), ne));
		     return true;
		 }
		 
		 lastStaticPath = absolutePath;
		 
		 return true;
	}

}
