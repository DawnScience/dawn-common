package org.dawnsci.macro.console;

import org.dawnsci.macro.Activator;
import org.eclipse.dawnsci.macro.api.IMacroEventListener;
import org.eclipse.dawnsci.macro.api.IMacroService;
import org.eclipse.dawnsci.macro.api.MacroEventObject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsolePageParticipant;
import org.eclipse.ui.part.IPageBookViewPage;
import org.python.pydev.core.IInterpreterInfo;
import org.python.pydev.debug.newconsole.PydevConsole;

public class ConsoleParticipant implements IConsolePageParticipant, IMacroEventListener {

	private static IMacroService mservice;
	public static void setMacroService(IMacroService s) {
		mservice = s;
	}
	
	private PydevConsole console;

	@Override
	public void init(IPageBookViewPage page, IConsole c) {
		
		console = (PydevConsole)c;
		
		final IAction recordMacro = new Action("Record Macro", IAction.AS_CHECK_BOX) {
			public void run() {
				if (isChecked()) {
				    mservice.addMacroListener(ConsoleParticipant.this);
				} else {
					mservice.removeMacroListener(ConsoleParticipant.this);
				}
			}
		};
		recordMacro.setImageDescriptor(Activator.getImage("icons/recordMacro.png"));
		
		page.getSite().getActionBars().getToolBarManager().add(new Separator());
		page.getSite().getActionBars().getToolBarManager().add(recordMacro);
	}

	@Override
	public Object getAdapter(Class adapter) {
		if (IMacroService.class == adapter) return mservice;
		return null;
	}

	private boolean isDisposed = false;
	@Override
	public void dispose() {
		mservice.removeMacroListener(this);
		isDisposed = true;
	}

	@Override
	public void activated() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deactivated() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void macroChangePerformed(MacroEventObject mevt) {
		
        Object oinfo = console.getInterpreterInfo();
        if (oinfo instanceof IInterpreterInfo) {
        	IInterpreterInfo info = (IInterpreterInfo)oinfo;
        	int type = info.getInterpreterType(); 
        	// 0 for python, 1 for jython
        	
            String cmd = type==0 ? mevt.getPythonCommand() : mevt.getJythonCommand();
            
    		IDocument document = console.getDocument();
    		try {
				document.replace(document.getLength(), 0, cmd+"\n");
			} catch (BadLocationException e) {
				e.printStackTrace(); 
			}
        }
	}

	@Override
	public boolean isDisposed() {
		return isDisposed;
	}


}
