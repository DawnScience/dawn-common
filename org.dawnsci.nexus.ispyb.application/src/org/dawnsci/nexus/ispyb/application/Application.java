package org.dawnsci.nexus.ispyb.application;

import java.util.Map;

import org.dawnsci.nexus.ispyb.NexusToISPyB;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {

	private static final String PATH = "-file";
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		System.out.println("Nexus to ISpyB converter");
		
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
			throw new IllegalArgumentException("There must be a -file argument in the command line arguments");
		}
		
		System.out.println("Running for file: " + path);
		
		NexusToISPyB.insertFile(path);
		
		return IApplication.EXIT_OK;
	}

	@Override
	public void stop() {
		// nothing to do
	}
}
