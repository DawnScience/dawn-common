package org.dawnsci.macro.console;

import java.lang.ref.SoftReference;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.python.pydev.editor.PyEdit;

/**
 * 
 * This action exists once for all python file editors, which is how eclipse does it.
 * 
 * @author Matthew Gerring
 *
 */
public class RecordMacroAction extends AbstractHandler implements IEditorActionDelegate {
	

	private Map<String, SoftReference<DocumentInserter>> inserterMap;
	
	public RecordMacroAction() {
		inserterMap = new IdentityHashMap<String, SoftReference<DocumentInserter>>();
	}

	@Override
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		
		if (targetEditor==null) return;
		
		if (targetEditor instanceof PyEdit) {
						
			DocumentInserter inserter = getInserter(targetEditor);
			if (inserter==null) return;
			action.setChecked(inserter.isConnected());
		}
	}
	
	private DocumentInserter getInserter(IEditorPart targetEditor) {
		
		if (targetEditor==null) return null;
		
		final String name = targetEditor.getEditorInput().getName();
		if (inserterMap.containsKey(name)) {
			SoftReference<DocumentInserter> ref = inserterMap.get(name);
			if (ref.get()!=null) return ref.get();
		}
		
		DocumentInserter inserter = new DocumentInserter();
		PyEdit text = (PyEdit)targetEditor;

		inserter.init(text.getISourceViewer(), InsertionType.PYTHON);
		
		inserterMap.put(name, new SoftReference<DocumentInserter>(inserter));
		return inserter;
	}

	private void toggle() {
		// Get the editor from the active part, which this editor must be
		// if someone is clicking it.
		final IEditorPart part = getActiveEditor();
		if (part==null) return;

		final String name = part.getEditorInput().getName();
		if (!inserterMap.containsKey(name)) return;
		
		SoftReference<DocumentInserter> ref = inserterMap.get(name);
		DocumentInserter           inserter = ref.get();
		if (inserter!=null) inserter.toggleConnected();
		
		removeDisposed();
	}


	private void removeDisposed() {
	    // Map is small so loop ok.
		for (Iterator<String> it = inserterMap.keySet().iterator(); it.hasNext();) {

			SoftReference<DocumentInserter> ref = inserterMap.get(it.next());
			DocumentInserter           inserter = ref.get();
			if (inserter==null) it.remove();
			if (inserter.isDisposed()) it.remove();
		}
	}

	@Override
	public void run(IAction action) {
		toggle();
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		toggle();
		return Boolean.TRUE;
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	
	/**
	 * @return IEditorPart
	 */
	private static IEditorPart getActiveEditor() {
		final IWorkbenchPage page = getPage();
		return page.getActiveEditor();
	}
	/**
	 * Gets the page, even during startup.
	 * @return the page
	 */
	private static IWorkbenchPage getPage() {
		IWorkbenchPage activePage = getActivePage();
		if (activePage!=null) return activePage;
		return getDefaultPage();
	}
	
	/**
	 * @return IWorkbenchPage
	 */
	private static IWorkbenchPage getActivePage() {
		final IWorkbench bench = PlatformUI.getWorkbench();
		if (bench==null) return null;
		final IWorkbenchWindow window = bench.getActiveWorkbenchWindow();
		if (window==null) return null;
		return window.getActivePage();
	}
	private static IWorkbenchPage getDefaultPage() {
		final IWorkbench bench = PlatformUI.getWorkbench();
		if (bench==null) return null;
		final IWorkbenchWindow[] windows = bench.getWorkbenchWindows();
		if (windows==null) return null;
		
		return windows[0].getActivePage();
	}

}
