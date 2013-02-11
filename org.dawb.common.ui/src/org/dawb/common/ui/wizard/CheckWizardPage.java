package org.dawb.common.ui.wizard;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * A page for editing a list of booleans.
 * @author fcp94556
 *
 */
public class CheckWizardPage extends WizardPage implements SelectionListener{


	private Map<String, Boolean> values;
	private Map<String, String>  stringValues;
	private Map<String, Text>    textCache;
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


	public void setStringValues(Map<String,String> values) {
		this.stringValues = values;
	}

	public void setStringValue(String label, String stringValue) {
		stringValues.put(label, stringValue);
		textCache.get(label).setText(stringValue);
	}

	@Override
	public void createControl(Composite parent) {
		final Composite content = new Composite(parent, SWT.NONE);
		content.setLayout(new GridLayout(1, false));
		
		for (final String label : values.keySet()) {
			Button button = new Button(content, SWT.CHECK);
			button.setText(label);
			button.setSelection(values.get(label));
			if (stringValues!=null && stringValues.containsKey(label)) {
			    button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
			    final Text text = new Text(content, SWT.BORDER);
			    text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			    text.setEnabled(values.get(label));
			    text.setText(stringValues.get(label));
			    text.addModifyListener(new ModifyListener() {					
					@Override
					public void modifyText(ModifyEvent e) {
						stringValues.put(label, text.getText());
						validate();
					}
				});
			    if (textCache==null) textCache = new HashMap<String, Text>(7);
			    textCache.put(label, text);
			    
			} else {
			    button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			}
			button.addSelectionListener(this);
		}
		setControl(content);
		validate();
	}

	private void validate() {
		for (String label : values.keySet()) {
			if (stringValues!=null&&stringValues.containsKey(label) && values.get(label)) {
				String value = stringValues.get(label);
				if (value==null || value.equals("")) {
					setErrorMessage("Please set a value for '"+label+"'.");
					return;
				}
			}
		}
		setErrorMessage(null);
	}


	@Override
	public void widgetSelected(SelectionEvent e) {
		if (isDisposed) return;
		final Button button = (Button)e.getSource();
		final String label  = button.getText();
		values.put(label, button.getSelection());
		if (textCache!=null && textCache.get(label)!=null) {
			textCache.get(label).setEnabled(button.getSelection());
		}
		validate();
	}
	@Override
	public void dispose() {
		isDisposed = true;
		if (textCache!=null) textCache.clear();
		super.dispose();
	}
	
	public boolean is(String propertyName) {
		return values.get(propertyName);
	}

	public String getString(String propertyName) {
		return stringValues.get(propertyName);
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// TODO Auto-generated method stub
		
	}

}
