package org.dawb.common.ui.wizard.persistence;

import java.io.File;
import java.util.List;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.wizard.CheckWizardPage;
import org.dawb.common.ui.wizard.ExternalFileChoosePage;
import org.dawb.common.util.io.FileUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

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
		options.setDescription("Please choose things to import.");
		addPage(options);
		
	}
	
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		// Set last import path from static
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
		// TODO Auto-generated method stub
		return false;
	}

}
