package org.dawnsci.macro;


import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.dawnsci.analysis.api.ClassLoaderExtensionPoint;
import org.eclipse.dawnsci.macro.api.IMacroRunner;
import org.python.util.PythonInterpreter;

import com.thoughtworks.xstream.core.util.CompositeClassLoader;

import uk.ac.diamond.scisoft.python.JythonInterpreterUtils;

/**
 * Lazily creates an interpreter the fist time that a command
 * is run.
 * 
 * @see IMacroRunner
 * 
 * @author fcp94556
 *
 */
public class MacroRunnerImpl implements IMacroRunner {
		
	static {
		System.out.println("Started MacroRunner Service");
	}

	private static PythonInterpreter interpreter;
	
	public MacroRunnerImpl() {
		super();
	}
	
	@Override
	public void exec(String s) throws Exception {
		createInterpreterIfRequired();
		System.out.println(">> "+s);
		interpreter.exec(s);
	}
	
	private synchronized void createInterpreterIfRequired() throws Exception {
		
		if (interpreter!=null) return;
		
		IExtensionRegistry reg = RegistryFactory.getRegistry();
		if (reg == null) return; // not running within OSGi?

		CompositeClassLoader loader = new CompositeClassLoader();
		for (IConfigurationElement i : reg.getConfigurationElementsFor(ClassLoaderExtensionPoint.ID)) {
			try {
				Object e = i.createExecutableExtension(ClassLoaderExtensionPoint.ATTR);
				loader.add(e.getClass().getClassLoader());
			} catch (Exception ne) {
				// do nothing
			}
		}
		loader.add(JythonInterpreterUtils.class.getClassLoader());

		interpreter = JythonInterpreterUtils.getFullInterpreter(loader);
	}
}
