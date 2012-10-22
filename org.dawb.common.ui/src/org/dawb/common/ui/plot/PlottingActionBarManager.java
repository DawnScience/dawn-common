package org.dawb.common.ui.plot;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.dawb.common.ui.Activator;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.tool.IToolChangeListener;
import org.dawb.common.ui.plot.tool.IToolPage;
import org.dawb.common.ui.plot.tool.IToolPage.ToolPageRole;
import org.dawb.common.ui.plot.tool.ToolChangeEvent;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class to deal with the extra actions we need in the plotting system.
 * 
 * Some actions are provided by the plotting system implementation, others are dealt with here.
 * 
 * You may override this class to add additional actions.
 * 
 * @author fcp94556
 *
 */
public abstract class PlottingActionBarManager implements IPlotActionSystem {

	private static final Logger logger = LoggerFactory.getLogger(PlottingActionBarManager.class);
	
	// Extrac actions for 1D and image viewing
	protected Map<String, IToolPage> toolPages;
	protected AbstractPlottingSystem system;
	
	public PlottingActionBarManager(AbstractPlottingSystem system) {
		this.system = system;
	}
	
	
	public void dispose() {
		
		if (toolPages!=null) toolPages.clear();
		toolPages = null;
	}
	
	private boolean isToolsRequired = true;
	
	public void setToolsRequired(boolean isToolsRequired) {
		this.isToolsRequired = isToolsRequired;
	}
	

	/**
	 * Return a MenuAction which can be attached to the part using the plotting system.
	 * 
	 * 
	 * @return
	 */
	protected MenuAction createToolActions(final ToolPageRole role, final String viewId) throws Exception {
		
		if (!isToolsRequired) return null;
		
		final IWorkbenchPart part = system.getPart();
		if (part==null)  return null;
		
		final MenuAction toolActions = new MenuAction(role.getLabel());
		toolActions.setToolTipText(role.getTooltip());
		toolActions.setImageDescriptor(Activator.getImageDescriptor(role.getImagePath()));
		toolActions.setId(role.getId());
	       	
		// This list will not be large so we loop over it more than once for each ToolPageRole type
	    final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.toolPage");
	    boolean foundSomeActions = false;
	    for (final IConfigurationElement e : configs) {
	    	
	    	foundSomeActions = true;

	    	final IToolPage tool = createToolPage(e, role);
	    	if (tool==null) continue;
	    	
	    	final Action    action= new Action(tool.getTitle()) {
	    		public void run() {		
	    			
	    			IToolPage registeredTool = toolPages.get(tool.getToolId());
	    			if (registeredTool==null || registeredTool.isDisposed()) registeredTool = createToolPage(e, role);
	    			 
	    			// If we have a dedicated tool for this tool, we do not open another
	    			String toolId  = registeredTool.getToolId();
	    			if (toolId!=null) {
	    				IViewReference ref = EclipseUtils.getPage().findViewReference("org.dawb.workbench.plotting.views.toolPageView.fixed", toolId);
	    			    if (ref!=null) {
	    			    	final IViewPart view = ref.getView(true);
	    			    	EclipseUtils.getPage().activate(view);
	    			    	return;
	    			    }
	    			    
	    			    if (tool.isAlwaysSeparateView()) {
	    					try {
								final IViewPart view = EclipseUtils.getPage().showView("org.dawb.workbench.plotting.views.toolPageView.fixed",
														    							tool.getToolId(),
																						IWorkbenchPage.VIEW_ACTIVATE);
		    			    	EclipseUtils.getPage().activate(view);
								return;
							} catch (PartInitException e1) {
								logger.error("Cannot open fixed view for "+tool.getToolId(), e1);
							}
                            
	    			    }
	    			}
	    			
	    			IViewPart viewPart=null;
	    			try {
	    				viewPart = EclipseUtils.getActivePage().showView(viewId);
						
						if (viewPart!=null && viewPart instanceof IToolChangeListener) {
							system.addToolChangeListener((IToolChangeListener)viewPart);
						}
					} catch (PartInitException e) {
						logger.error("Cannot find a view with id org.dawb.workbench.plotting.views.ToolPageView", e);
					}
	    			
	    			final IToolPage old = system.getCurrentToolPage(role);
	    			system.setCurrentToolPage(registeredTool);
	    			system.clearRegionTool();
	    			system.fireToolChangeListeners(new ToolChangeEvent(this, old, registeredTool, system.getPart()));
	    			
	    			toolActions.setSelectedAction(this);
	    			
	    		}
	    	};
	    	
	    	action.setId(e.getAttribute("id"));
	    	final String   icon  = e.getAttribute("icon");
	    	if (icon!=null) {
		    	final String   id    = e.getContributor().getName();
		    	final Bundle   bundle= Platform.getBundle(id);
		    	final URL      entry = bundle.getEntry(icon);
		    	final ImageDescriptor des = ImageDescriptor.createFromURL(entry);
		    	action.setImageDescriptor(des);
		    	tool.setImageDescriptor(des);
	    	}
	    	
	    	final String    tooltip = e.getAttribute("tooltip");
	    	if (tooltip!=null) action.setToolTipText(tooltip);
	    	
	    	toolActions.add(action);
		}
	
	    if (!foundSomeActions) return null;
	    
	    if (toolActions.size()<1) return null; // Nothing to show!
	    
     	final Action    clear = new Action("Clear tool") {

			public void run() {		
    			clearTool(role);
    			toolActions.setSelectedAction(this);		
			}
    	};
    	clear.setImageDescriptor(Activator.getImageDescriptor("icons/axis.png"));
    	clear.setToolTipText("Clear tool previously used if any.");
	    toolActions.add(clear);

	    return toolActions;
	}
	
	protected void clearTool(ToolPageRole role) {
		final IToolPage old = system.getCurrentToolPage(role);
		
		final EmptyTool empty = system.getEmptyTool(role);
		system.setCurrentToolPage(empty);
		system.clearRegionTool();
		system.fireToolChangeListeners(new ToolChangeEvent(this, old, empty, system.getPart()));		
	}


	/**
	 * 
	 * @param e
	 * @param role, may be null
	 * @return
	 */
	private IToolPage createToolPage(IConfigurationElement e, ToolPageRole role) {
    	
		IToolPage tool = null;
    	try {
    		tool  = (IToolPage)e.createExecutableExtension("class");
    	} catch (Throwable ne) {
    		logger.error("Cannot create tool page "+e.getAttribute("class"), ne);
    		return null;
    	}
    	if (role==null) role = tool.getToolPageRole();
    	if (tool.getToolPageRole()!=role) return null;
	    
    	tool.setToolSystem(system);
    	tool.setPlottingSystem(system);
    	tool.setTitle(e.getAttribute("label"));
    	tool.setPart(system.getPart());
    	tool.setToolId(e.getAttribute("id"));
    	tool.setCheatSheetId(e.getAttribute("cheat_sheet_id"));
    	
    	// Save tool page
    	if (toolPages==null) toolPages = new HashMap<String, IToolPage>(7);
    	toolPages.put(tool.getToolId(), tool);
    	
    	return tool;
	}


	protected IToolPage getToolPage(final String id) {
		if (toolPages==null) return null;
		IToolPage page = toolPages.get(id);
		if (page==null) {
		    final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.toolPage");
		    for (final IConfigurationElement e : configs) {
		    	if (id.equals(e.getAttribute("id"))) {
		    		page = createToolPage(e, null);
		    		break;
		    	}
		    }
			
		}
		return page;
	}
	
	protected void clearCachedTools() {
		if (toolPages==null) return;
		toolPages.clear();
	}
	
	/**
	 * returns false if no tool was shown
	 * @param toolId
	 * @return
	 * @throws Exception 
	 */
	public boolean setToolVisible(final String toolId, final ToolPageRole role, final String viewId) throws Exception {
	    
		final IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor("org.dawb.common.ui.toolPage");
	    for (IConfigurationElement e : configs) {
			
	    	if (!toolId.equals(e.getAttribute("id"))) continue;
	    	final IToolPage page  = (IToolPage)e.createExecutableExtension("class");
	    	if (page.getToolPageRole()!=role) continue;
		    
	    	final String    label = e.getAttribute("label");
	    	page.setToolSystem(system);
	    	page.setPlottingSystem(system);
	    	page.setTitle(label);
	    	page.setPart(system.getPart());
	    	

	    	IViewPart viewPart=null;
	    	try {
	    		viewPart = EclipseUtils.getActivePage().showView(viewId);

	    		if (viewPart!=null && viewPart instanceof IToolChangeListener) {
	    			system.addToolChangeListener((IToolChangeListener)viewPart);
	    		}
	    	} catch (PartInitException pe) {
	    		logger.error("Cannot find a view with id org.dawb.workbench.plotting.views.ToolPageView", pe);
	    	}

	    	final IToolPage old = system.getCurrentToolPage(role);
	    	system.setCurrentToolPage(page);
	    	system.clearRegionTool();
	    	system.fireToolChangeListeners(new ToolChangeEvent(this, old, page, system.getPart()));
	    			
	    	return true;
	    }
	    
	    return false;
	}


	@Override
	public void fillZoomActions(IContributionManager man) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void fillRegionActions(IContributionManager man) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void fillUndoActions(IContributionManager man) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void fillPrintActions(IContributionManager man) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void fillAnnotationActions(IContributionManager man) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void fillToolActions(IContributionManager man, ToolPageRole role) {
		// TODO Auto-generated method stub
		
	}


	public void fillTraceActions(final IContributionManager manager, final ITrace trace, final IPlottingSystem sys) {
		// TODO Override as needed
	}
}
