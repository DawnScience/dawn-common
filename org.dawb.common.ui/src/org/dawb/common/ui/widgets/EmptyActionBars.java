package org.dawb.common.ui.widgets;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.services.IServiceLocator;

public class EmptyActionBars implements IActionBars {

	private IToolBarManager toolBarManager;
	private IMenuManager menuManager;
	private IStatusLineManager statusLineManager;

	public EmptyActionBars(IToolBarManager toolBarManager,
			IMenuManager menuManager, IStatusLineManager statusLineManager) {
		this.toolBarManager = toolBarManager;
		this.menuManager    = menuManager;
		this.statusLineManager = statusLineManager;
	}

	@Override
	public void clearGlobalActionHandlers() {
		// TODO Auto-generated method stub

	}

	@Override
	public IAction getGlobalActionHandler(String actionId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IMenuManager getMenuManager() {
		// TODO Auto-generated method stub
		return menuManager;
	}

	@Override
	public IServiceLocator getServiceLocator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IStatusLineManager getStatusLineManager() {
		// TODO Auto-generated method stub
		return statusLineManager;
	}

	@Override
	public IToolBarManager getToolBarManager() {
		// TODO Auto-generated method stub
		return toolBarManager;
	}

	@Override
	public void setGlobalActionHandler(String actionId, IAction handler) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateActionBars() {
		// TODO Auto-generated method stub

	}

}
