package org.dawb.common.ui.plot.region;

import java.util.EventObject;

public class IRegionSelectionEvent extends EventObject {

	private IRegionSelection selection;
	
	public IRegionSelectionEvent(Object source) {
		super(source);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5633107732864475490L;

	public IRegionSelection getSelection() {
		return selection;
	}

	public void setSelection(IRegionSelection selection) {
		this.selection = selection;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((selection == null) ? 0 : selection.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IRegionSelectionEvent other = (IRegionSelectionEvent) obj;
		if (selection == null) {
			if (other.selection != null)
				return false;
		} else if (!selection.equals(other.selection))
			return false;
		return true;
	}

}
