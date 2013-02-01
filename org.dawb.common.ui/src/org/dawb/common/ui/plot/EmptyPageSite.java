package org.dawb.common.ui.plot;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPageSite;

public class EmptyPageSite implements IPageSite {

	private Shell          shell;
	private IActionBars    actionBars;

	public EmptyPageSite(Shell shell, IActionBars actionBars) {
		this.shell      = shell;
		this.actionBars = actionBars;
	}

	@Override
	public IWorkbenchPage getPage() {
		return EclipseUtils.getActivePage();
	}

	@Override
	public Shell getShell() {
		return shell;
	}

	@Override
	public IWorkbenchWindow getWorkbenchWindow() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
	}

	@Override
	public void setSelectionProvider(ISelectionProvider provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public Object getService(Class api) {
		return PlatformUI.getWorkbench().getService(api);
	}

	@Override
	public boolean hasService(Class api) {
		return  PlatformUI.getWorkbench().hasService(api);
	}

	@Override
	public void registerContextMenu(String menuId, MenuManager menuManager,
			ISelectionProvider selectionProvider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IActionBars getActionBars() {
		return actionBars;
	}
	
	@Override
	public ISelectionProvider getSelectionProvider() {
		return new ISelectionProvider() {

			@Override
			public void addSelectionChangedListener(
					ISelectionChangedListener listener) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public ISelection getSelection() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void removeSelectionChangedListener(
					ISelectionChangedListener listener) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void setSelection(ISelection selection) {
				// TODO Auto-generated method stub
				
			}
			
		};
	}

}