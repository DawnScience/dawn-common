package org.dawnsci.processing.docgenerator;

import java.util.Map;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {
	
	private static final String PATH = "-output";

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
		
		Map<?,?> map = context.getArguments();
		String[] args = (String[]) map.get("application.args");
		
		int n = args.length;
		int i = 0;
		String path = null;
		for (; i < n; i++) {
			String a = args[i];
			if (a.equals(PATH)) {
				path  = args[i+1];
				break;
			}
		}
		if (path == null) {
			throw new IllegalArgumentException("There must be a -output argument in the command line arguments");
		}
		
		ProcessingDocGenerator.writeProcessingDoc(path);
		return IApplication.EXIT_OK;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		// nothing to do
	}
}
