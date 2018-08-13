
package org.dawnsci.macro;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;
import org.python.pydev.ui.pythonpathconf.InterpreterNewCustomEntriesAdapter;

public class Py4JNewCustomEntries extends InterpreterNewCustomEntriesAdapter {
	@Override
	public Collection<String> getAdditionalLibraries() {
		if (!PlatformUI.isWorkbenchRunning()) {
			return super.getAdditionalLibraries();
		}
		try {
			File bndDir = FileLocator.getBundleFile(Platform.getBundle("py4j-python")).getAbsoluteFile();
			ArrayList<String> list = new ArrayList<String>();
			list.add(new File(bndDir, "src").getAbsolutePath());
			return list;
		} catch (IOException e) {
		}
		return super.getAdditionalLibraries();
	}
}
