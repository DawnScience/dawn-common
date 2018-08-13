package org.dawnsci.macro.console;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.macro.api.IMacroEventListener;
import org.eclipse.dawnsci.macro.api.IMacroService;
import org.eclipse.dawnsci.macro.api.MacroEventObject;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to link any IDocument to recording macros.
 * 
 * @author Matthew Gerring
 *
 */
public class DocumentInserter implements IMacroEventListener, IPartListener {
	
	private static Logger logger = LoggerFactory.getLogger(DocumentInserter.class);

	private static IMacroService mservice;
	public void setMacroService(IMacroService s) {
		mservice = s;
	}
	public static IMacroService getMacroService() {
		return mservice;
	}
	
	private ISourceViewer  viewer;
	private InsertionType  type;
	private DocumentInsertionJob job;
		
	public void init(ISourceViewer viewer, InsertionType type) {
		this.viewer   = viewer;
		this.type     = type;
		this.job      = new DocumentInsertionJob(this, viewer);
		
		viewer.getTextWidget().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (job!=null) job.stop();
			}
		});
	}

	@Override
	public void macroChangePerformed(MacroEventObject mevt) {
		
		if (viewer==null || viewer.getDocument()==null) return;

		String cmd = type==InsertionType.PYTHON ? mevt.getPythonCommand() : mevt.getJythonCommand();
		String contents = viewer.getDocument().get().trim();
		if (contents.endsWith(cmd.trim()) || contents.endsWith(cmd.trim()+"\n>>>") || contents.endsWith(cmd.trim()+"\r\n>>>")) {
			System.out.println("Command already made : "+cmd);
			return; // Avoid recursion, do nothing
		}
		if (mevt.isImmediate()) {
			try {
				IDocument document = viewer.getDocument();
				document.replace(document.getLength(), 0, cmd);
				document.replace(document.getLength(), 0, "\n");

			} catch (BadLocationException e) {
				e.printStackTrace(); 
			}

		} else {
		    job.add(cmd);
		}
	}

	@Override
	public boolean isDisposed() {
		if (viewer==null) return true;
		if (viewer!=null && viewer.getTextWidget()!=null) {
			return viewer.getTextWidget().isDisposed();
		}
		return false;
	}

	private boolean isConnected = false;
	public void connect() {
		
		boolean ok = checkPy4jServer();
		if (!ok) return;
		
		mservice.addMacroListener(this);
		isConnected = true;
		EclipseUtils.getPage().addPartListener(this);
	}
	public void disconnect() {
		mservice.removeMacroListener(this);
		isConnected = false;
		if (EclipseUtils.getPage()!=null) EclipseUtils.getPage().removePartListener(this);
	}

	public boolean isConnected() {
		return isConnected;
	}
	public void toggleConnected() {
		if (isConnected()) {
			disconnect();
		} else {
			connect();
		}
	}
	
	/**
	 * User friendly stuff to remind them how to set up py4j
	 * @return
	 */
	private boolean checkPy4jServer() {
		
        final IPreferenceStore pref = new ScopedPreferenceStore(InstanceScope.INSTANCE, "net.sf.py4j.defaultserver");
        boolean isActive = pref.getBoolean("PREF_PY4J_ACTIVE");
		boolean override = Boolean.getBoolean("PREF_PY4J_ACTIVE"); // They can override the default using -DPREF_PY4J_ACTIVE=...
        if (!isActive && !override) {
        	boolean yes = MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "Cannot connect to DAWN",
        			       "The macro server is not going. Would you like to turn it on now and restart DAWN?\n\n"+
        			       "Choosing yes, will restart the workbench.");
        	if (yes) {
        		pref.setValue("PREF_PY4J_ACTIVE", true);
        		try {
        			ICommandService service = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        			service.getCommand(IWorkbenchCommandConstants.FILE_RESTART).executeWithChecks(new ExecutionEvent());
        		} catch (Exception ne) {
        			logger.error("Cannot restart", ne);
        			ErrorDialog.openError(Display.getDefault().getActiveShell(), "Restart Failed", "The restart failed, please do so manually.",
        					new Status(IStatus.ERROR, "org.dawnsci.macro", "Failed restart", ne));
        		}
        	}
        }

        // Recheck
        isActive = pref.getBoolean("PREF_PY4J_ACTIVE");
		override = Boolean.getBoolean("PREF_PY4J_ACTIVE"); // They can override the default using -DPREF_PY4J_ACTIVE=...

        return isActive || override;
	}
	
	@Override
	public void partActivated(IWorkbenchPart part) {
        try {
        	IPlottingSystem<Composite> sys = (IPlottingSystem<Composite>)part.getAdapter(IPlottingSystem.class);
        	if (sys!=null && mservice!=null) {
    			mservice.publish(new MacroEventObject(sys));
    		}

        } catch (Exception ne) {
        	// Ignored, it's just for setting the macros when parts are selected
        }
	}
	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void partClosed(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void partDeactivated(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void partOpened(IWorkbenchPart part) {
		// TODO Auto-generated method stub
		
	}
	public void dispose() {
		disconnect();
		if (job!=null) job.stop();
	}
	public InsertionType getType() {
		return type;
	}
}
