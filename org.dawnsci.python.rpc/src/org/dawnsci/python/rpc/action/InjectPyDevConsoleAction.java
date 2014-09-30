package org.dawnsci.python.rpc.action;

import java.util.HashMap;
import java.util.Map;

import org.dawnsci.python.rpc.Activator;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.jface.action.Action;

public class InjectPyDevConsoleAction extends Action {

	private Map<String, String> params;
	private InjectPyDevConsole injector;
	private String   name;
	private IDataset data;

	public InjectPyDevConsoleAction(String label) {
		
		super(label, Activator.getImageDescriptor("icons/application_osx_terminal.png"));
		this.params = new HashMap<String,String>(7);
	}

	public void run() {
		try {
			
			// Console may have been closed, see if we can get the active
			// one that they are using.
			if (injector != null && !injector.isConsoleAvailable()) {
				injector = null;
			}
			
			// Otherwise open one.
			if (injector==null) {
				injector = new InjectPyDevConsole(params);
				// Opens the console if required, including if it was closed.
				injector.inject(name, data);
				injector.open(true);
			} else {
				injector.inject(name, data);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Call this method to manually set the dataset which we should use to 
	 * send to the console. This data is currently sent using flattening.
	 * 
	 * @param name
	 * @param data
	 */
	public void setData(String name, IDataset data) {
		this.name = name;
		this.data = data;
	}
	
	/**
	 * Set a connection parameter
	 * @param name
	 * @param value
	 */
	public void setParameter(String name, String value) {
		params.put(name, value);
	}

}
