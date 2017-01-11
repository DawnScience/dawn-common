/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.wizard.persistence;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dawb.common.ui.ServiceLoader;
import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.wizard.CheckWizardPage;
import org.dawb.common.ui.wizard.PlotDataConversionPage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunctionService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentNodeFactory;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileHDF5;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.region.IRegion;
import org.eclipse.dawnsci.plotting.api.tool.IToolPage;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.ILineTrace;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;

public class PersistenceExportWizard extends AbstractPersistenceWizard implements IExportWizard {
	
	public static final String ID = "org.dawnsci.plotting.exportMask";
	
	PlotDataConversionPage page;

	private CheckWizardPage options;
	
	private static IPath  containerFullPath;
	private static String staticFileName;
 
	public PersistenceExportWizard() {
		
		setWindowTitle("Export");
		setNeedsProgressMonitor(true);
		
		this.page = new PlotDataConversionPage();
		page.setDescription("Please choose the location of the file to export. (This file will be a nexus HDF5 file.)");
		addPage(page);
		
		this.options = new CheckWizardPage("Export Options", createDefaultOptions());
		Map<String,String> stringOptions = new HashMap<String, String>(1);
		stringOptions.put(PersistWizardConstants.MASK, "");
		options.setStringValues(stringOptions);
		options.setDescription("Please choose things to export.");
		
		addPage(options);
		
	}
	
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);
		
		if (containerFullPath==null && staticFileName==null) {
			try {
			    final IFile file = EclipseUtils.getSelectedFile();
			    if (file!=null) {
			    	containerFullPath = file.getParent().getFullPath();
			    	staticFileName    = file.getName();
			    }
			} catch (Throwable ne) {
				// Nowt
			}
		}
		
		if (containerFullPath!=null) page.setPath(containerFullPath.toString());
		if (staticFileName!=null)    page.setFileLabel(staticFileName);
	}

    public boolean canFinish() {
    	if (page.isPageComplete() && getContainer().getCurrentPage()==page) {
    		options.setDescription("Please choose the things to save in '"+page.getPath()+"'.");
    		options.setOptionEnabled(PersistWizardConstants.ORIGINAL_DATA,   false);
    		options.setOptionEnabled(PersistWizardConstants.IMAGE_HIST,      false);
    		options.setOptionEnabled(PersistWizardConstants.MASK,            false);
    		options.setOptionEnabled(PersistWizardConstants.REGIONS,         false);
    		options.setOptionEnabled(PersistWizardConstants.DIFF_META,       false);
    		options.setOptionEnabled(PersistWizardConstants.FUNCTIONS,       false);

    		File                file=null;
    		IPersistentFile     pf=null;
    		
    		PERSIST_BLOCK: try {
        		IPersistenceService service = ServiceLoader.getPersistenceService();
        		file = new File(page.getAbsoluteFilePath());
        		if (!file.exists()) break PERSIST_BLOCK;
    		    pf    = service.getPersistentFile(file.getAbsolutePath());
    		    final List<String>  names = pf.getMaskNames(null);
    		    if (names!=null && !names.isEmpty()) {
    		    	options.setStringValue(PersistWizardConstants.MASK, names.get(0));
    		    }
    		        		    
    		} catch (Throwable ne) {
    			logger.error("Cannot read persistence file at "+file);
    		} finally {
    			if (pf!=null) pf.close();
    		}

    		final IPlottingSystem<Composite>  system   = getPlottingSystem();
    		if (system!=null) {
    			if (system != null) {
    				ITrace trace  = system.getTraces().iterator().next();
    				if (trace!=null) {
    					
    					options.setOptionEnabled(PersistWizardConstants.ORIGINAL_DATA, true);
    					
    					if (trace instanceof IImageTrace && ((IImageTrace)trace).getMask()!=null) {
    						options.setOptionEnabled(PersistWizardConstants.MASK, true);
    					}
    				}
    				
    				boolean requireHistory = false;
    				final IToolPageSystem tsystem = (IToolPageSystem)system.getAdapter(IToolPageSystem.class);
    				final IToolPage       tool    = tsystem.getActiveTool();
    				if (tool != null && tool.getToolId().equals("org.dawb.workbench.plotting.tools.imageCompareTool")) {
    					final Map<String, IDataset> data = (Map<String, IDataset>)tool.getToolData();
    					if (data!=null && !data.isEmpty()) requireHistory = true;
    				}
      				options.setOptionEnabled(PersistWizardConstants.IMAGE_HIST, requireHistory);
    				
    				final Collection<IRegion> regions = system.getRegions();
    				if (regions != null && !regions.isEmpty()) {
    					options.setOptionEnabled(PersistWizardConstants.REGIONS, true);
    				}
    				
    				if (trace!=null && trace instanceof IImageTrace && trace.getData() != null) {
    					IMetadata meta = trace.getData().getMetadata();
    					if (meta != null && (meta instanceof IDiffractionMetadata)) {
    						options.setOptionEnabled(PersistWizardConstants.DIFF_META, true);
    					}
    				}
    				
    				final IWorkbenchPart   part   = EclipseUtils.getPage().getActivePart();
    				if (part!=null) {
    					final IFunctionService funcService = (IFunctionService)part.getAdapter(IFunctionService.class);
    					if (funcService != null) {
    						options.setOptionEnabled(PersistWizardConstants.FUNCTIONS, true);
    					}
    				}

    			}
    		}
    	}
        return super.canFinish();
    }

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		      
 	}

	@Override
	public boolean performFinish() {
		
		 IFile file = null;
		 try {
			 IWorkspace workspace= ResourcesPlugin.getWorkspace();
			 File afile = new File(page.getAbsoluteFilePath());
			 IPath location= Path.fromOSString(afile.getAbsolutePath());
			 file= workspace.getRoot().getFileForLocation(location); 
			 			 			 
			 final IWorkbenchPart  part   = EclipseUtils.getPage().getActivePart();
			 final IPlottingSystem<Composite> system = getPlottingSystem();
			 final IFunctionService funcService = (IFunctionService)part.getAdapter(IFunctionService.class);

			 final IFile finalFile = file;
			 
			 getContainer().run(true, true, new IRunnableWithProgress() {

				 @Override
				 public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					 
					 IPersistentFile file = null;
					 IPersistenceService service = null;
					 String savePath = null;;
					 try {
						  service = ServiceLoader.getPersistenceService();
						 if(finalFile != null)
							 savePath = finalFile.getLocation().toOSString();
						 else
							 savePath = page.getAbsoluteFilePath();
						 final File ioFile = new File(savePath);
						 if (ioFile.exists()) ioFile.delete();
						 file    = service.createPersistentFile(savePath);
						 
						 final int length = getTotalWork(options, system, funcService);
						 monitor.beginTask("Export", length);
						 final IMonitor mon = new ProgressMonitorWrapper(monitor);

						 // Save things.
						 if (options.is(PersistWizardConstants.ORIGINAL_DATA)) {
							 Collection<ITrace> traces  = system.getTraces();
							 for (ITrace trace : traces) {
								 monitor.worked(1);
								 IDataset data = trace.getData();
								 if (trace.getName()!=null) data.setName(trace.getName());
								 file.setData(data);
								 if (trace instanceof IImageTrace) {
									 final List<IDataset> iaxes = ((IImageTrace)trace).getAxes();
									 if (iaxes!=null) file.setAxes(iaxes);
								 } else if (trace instanceof ILineTrace) {
									 IDataset xData = ((ILineTrace)trace).getXData();
									 if (xData != null) file.setAxes(Arrays.asList(new IDataset[]{xData,null}));
								 }
							 }
						 }
						 if (options.is(PersistWizardConstants.IMAGE_HIST)) {
			    				final IToolPageSystem tsystem = (IToolPageSystem)system.getAdapter(IToolPageSystem.class);
			    				final IToolPage       tool    = tsystem.getActiveTool();
			    				if (tool != null && tool.getToolId().equals("org.dawb.workbench.plotting.tools.imageCompareTool")) {
			    					final Map<String, IDataset> data = (Map<String, IDataset>)tool.getToolData();
			    					if (data!=null && !data.isEmpty()) {
			    						file.setHistory(data.values().toArray(new IDataset[data.size()]));
			    						monitor.worked(1);
			    					}
			    				}
						 }
						 
						 final ITrace trace = system.getTraces().iterator().next();
						 if (options.is(PersistWizardConstants.MASK) && trace instanceof IImageTrace) {
							 IImageTrace image = (IImageTrace)trace;
							 final String name = options.getString(PersistWizardConstants.MASK);
							 if (image.getMask()!=null) {
								 file.addMask(name, (BooleanDataset)image.getMask(), mon);
								 monitor.worked(1);
							 }
						 }
						 
						 final Collection<IRegion> regions = system.getRegions();
						 if (options.is(PersistWizardConstants.REGIONS) && regions!=null && !regions.isEmpty()) {
							 for (IRegion iRegion : regions) {
								 monitor.worked(1);
								 if (!file.isRegionSupported(iRegion.getROI())) {
									logger.debug("Region "+ iRegion.getName() + " of type "
											+ iRegion.getClass().getName() + " is not supported");
									continue;
								 }
								 file.addROI(iRegion.getName(), iRegion.getROI());
								 file.setRegionAttribute(iRegion.getName(), "Region Type", iRegion.getRegionType().getName());
								 if (iRegion.getUserObject()!=null) {
									 file.setRegionAttribute(iRegion.getName(), "User Object", iRegion.getUserObject().toString()); 
								 }
							 }
						 }
						 
						 

						 if (options.is(PersistWizardConstants.FUNCTIONS)) {
							 if (funcService != null) {
								 Map<String, IFunction> functions = funcService.getFunctions();
								 if (functions != null) {
									 monitor.worked(functions.size());
									 file.setFunctions(functions);
								 }
							 }
						 }
						 
						 
					 } catch (Exception e) {
						 throw new InvocationTargetException(e);
					 } finally {
						 if (file!=null) file.close();
						 NexusFile nexusFile = null;
						 try {
							 final ITrace trace = system.getTraces().iterator().next();
							 if (options.is(PersistWizardConstants.DIFF_META)) {
								 if (trace!=null && trace instanceof IImageTrace && trace.getData() != null) {
									 IMetadata meta = trace.getData().getMetadata();
									 if (meta == null || meta instanceof IDiffractionMetadata) {
										 IPersistentNodeFactory pnf = service.getPersistentNodeFactory();
										 GroupNode node = pnf.writePowderCalibrationToFile((IDiffractionMetadata)meta, null, null);
										 nexusFile = NexusFileHDF5.openNexusFile(savePath);
										 nexusFile.addNode("/entry",node);
									 }
								 }
							 }
						 }catch (Exception e) {
							 
						 } finally {
							 if (nexusFile != null)
								try {
									nexusFile.close();
								} catch (NexusException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						 }
					 }
				 }
			 });
		 } catch (Throwable ne) {
			 if (ne instanceof InvocationTargetException && ((InvocationTargetException)ne).getCause()!=null){
				 ne = ((InvocationTargetException)ne).getCause();
			 }
			 String message = null;
			 if (file!=null) {
				 message = "Cannot export '"+file.getName()+"' ";
			 } else {
				 message = "Cannot export file.";
			 }
			 logger.error("Cannot export file!", ne);
		     ErrorDialog.openError(Display.getDefault().getActiveShell(), "Export failure", message, new Status(IStatus.WARNING, "org.dawb.common.ui", ne.getMessage(), ne));
		     return true;
		 } finally {
			 try {
				 if (file != null)
					 file.getParent().refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
			 } catch (CoreException e) {
				 logger.error("Cannot refresh dir "+file, e);
			 }
		 }
		 
		 containerFullPath = Path.fromOSString(page.getAbsoluteFilePath());
		 staticFileName    = page.getFileLabel();
		 
		 return true;
	}

	private int getTotalWork(CheckWizardPage options, IPlottingSystem<Composite> system,  final IFunctionService funcService) {

		try {
			int ret = 1;
			if (options.is(PersistWizardConstants.ORIGINAL_DATA)) {
				Collection<ITrace> traces  = system.getTraces();
				ret+=traces!=null?traces.size():0;
			}
			if (options.is(PersistWizardConstants.IMAGE_HIST)) {
				final IToolPageSystem tsystem = (IToolPageSystem)system.getAdapter(IToolPageSystem.class);
				final IToolPage       tool    = tsystem.getActiveTool();
				if (tool != null && tool.getToolId().equals("org.dawb.workbench.plotting.tools.imageCompareTool")) {
					final Map<String, IDataset> data = (Map<String, IDataset>)tool.getToolData();
					if (data!=null && !data.isEmpty()) {
						ret+=data.size();
					}
				}
			}

			final ITrace trace = system.getTraces().iterator().next();
			if (options.is(PersistWizardConstants.MASK) && trace instanceof IImageTrace) {
				IImageTrace image = (IImageTrace)trace;
				final String name = options.getString(PersistWizardConstants.MASK);
				if (image.getMask()!=null) {
					ret+=1;
				}
			}

			final Collection<IRegion> regions = system.getRegions();
			if (options.is(PersistWizardConstants.REGIONS) && regions!=null && !regions.isEmpty()) {
				ret+=regions.size();
			}

			if (options.is(PersistWizardConstants.DIFF_META)) {
				if (trace!=null && trace instanceof IImageTrace && trace.getData() != null) {
					ret+=1;
				}
			}

			if (options.is(PersistWizardConstants.FUNCTIONS)) {
				if (funcService != null) {
					Map<String, IFunction> functions = funcService.getFunctions();
					if (functions != null) ret+=functions.size();
				}
			}

			return ret;
		} catch (Exception ne) {
			logger.error("Cannot estimate work, assuming 100 things to do!", ne);
			return 100;
		}
	}

	private IPlottingSystem<Composite> getPlottingSystem() {
		
		
		// Perhaps the plotting system is on a dialog
		final Shell[] shells = Display.getDefault().getShells();
		if (shells!=null) for (Shell shell : shells) {
			final Object o = shell.getData();
			if (o!=null && o instanceof IAdaptable) {
				IPlottingSystem<Composite> s = (IPlottingSystem<Composite>)((IAdaptable)o).getAdapter(IPlottingSystem.class);
				if (s!=null) return s;
			} 
		}
		
		final IWorkbenchPart  part   = EclipseUtils.getPage().getActivePart();
		if (part!=null) {
			
			//First test if part is a tool page which might have its own plotting system
			Object ob = part.getAdapter(IToolPageSystem.class);
			
			if (ob != null && ob instanceof IPlottingSystem) return (IPlottingSystem<Composite>)ob;
			
			return (IPlottingSystem<Composite>)part.getAdapter(IPlottingSystem.class);
		}
		
		return null;
	}

}
