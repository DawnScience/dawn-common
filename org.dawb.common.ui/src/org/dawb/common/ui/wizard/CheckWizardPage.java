package org.dawb.common.ui.wizard;

import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * A page for editing a list of booleans.
 * @author fcp94556
 *
 */
public class CheckWizardPage extends WizardPage implements SelectionListener{


	private Map<String, Boolean> values;
	private boolean isDisposed=false;

	/**
	 * The map is a map of labels to values.
	 * 
	 * @param pageName
	 * @param values
	 */
	public CheckWizardPage(String pageName, Map<String,Boolean> values) {
		super(pageName);
		this.values = values;
	}

	@Override
	public void createControl(Composite parent) {
		final Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout());
		
		for (String label : values.keySet()) {
			Button button = new Button(content, SWT.CHECK);
			button.setText(label);
			button.setSelection(values.get(label));
			button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			button.addSelectionListener(this);
		}
		setControl(content);
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (isDisposed) return;
		final Button button = (Button)e.getSource();
		values.put(button.getText(), button.getSelection());
	}
	@Override
	public void dispose() {
		isDisposed = true;
		super.dispose();
	}

	
	public Map<String,Boolean> getValues() {
		return values;
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
