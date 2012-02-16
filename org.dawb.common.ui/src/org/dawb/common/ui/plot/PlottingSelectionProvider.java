package org.dawb.common.ui.plot;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;


public class PlottingSelectionProvider implements ISelectionProvider {

	private Set<ISelectionChangedListener> listeners;
	private ISelection currentSelection;
	
	public PlottingSelectionProvider() {
		listeners = new HashSet<ISelectionChangedListener>(11);
	}
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.add(listener);
	}

	@Override
	public ISelection getSelection() {
		return currentSelection;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void setSelection(ISelection selection) {
		this.currentSelection = selection;
		SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
		for (ISelectionChangedListener s : listeners) s.selectionChanged(e);
	}

	public void clear() {
		if (listeners!=null) listeners.clear();
		currentSelection = null;
	}

}
