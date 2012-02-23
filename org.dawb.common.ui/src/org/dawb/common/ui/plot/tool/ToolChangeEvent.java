package org.dawb.common.ui.plot.tool;

import java.util.EventObject;

public class ToolChangeEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8805347445621628990L;

	private IToolPage oldPage;
	private IToolPage newPage;

	public ToolChangeEvent(Object source, IToolPage oldPage, IToolPage newPage) {
		super(source);
		this.oldPage = oldPage;
		this.newPage = newPage;
	}

	public IToolPage getOldPage() {
		return oldPage;
	}

	public IToolPage getNewPage() {
		return newPage;
	}

}
