package org.dawb.common.ui.plot;

import org.dawb.common.ui.plot.tool.AbstractToolPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class EmptyTool extends AbstractToolPage {

	private Composite composite;

	public EmptyTool() {
		setTitle("Empty Tool"); 
	}
	@Override
	public void createControl(Composite parent) {
		this.composite = new Composite(parent, SWT.NONE);
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
