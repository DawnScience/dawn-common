/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.hdf5;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

public class Hdf5TestUtils {

	/**
	 *
	 * @param relPath = to plugin
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static File getBundleFile(final String relPath) throws URISyntaxException, IOException {
		if (Activator.getContext()==null) { // Tests not running in eclipse
			return new File("org.dawb.hdf5.test/src/"+relPath);
		}
		URL[] findEntries = FileLocator.findEntries(Activator.getContext().getBundle(), new Path("src"));
		URL found = FileLocator.find(Activator.getContext().getBundle(), new Path(relPath), null);
		if (found == null) {
			found = FileLocator.find(Activator.getContext().getBundle(), new Path("src/" + relPath), null);
		}
		assertNotNull(found);
		URL fileURL = FileLocator.toFileURL(found);
		assertNotNull(fileURL);
		return new File(fileURL.toURI());
	}
}
