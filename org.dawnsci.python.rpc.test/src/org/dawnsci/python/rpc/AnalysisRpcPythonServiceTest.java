/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.python.rpc;

import java.io.File;

import org.dawnsci.python.rpc.AnalysisRpcPythonService;
import org.junit.Assert;
import org.junit.Test;

public class AnalysisRpcPythonServiceTest {

	/**
	 * This test may fail is ports are in use somewhere else (another running
	 * test/Eclipse/etc)
	 */
	@Test
	public void testAssignPort() {
		Assert.assertNotEquals(AnalysisRpcPythonService.assignPort(),
				AnalysisRpcPythonService.assignPort());
	}

	@Test
	public void testCreateParameters() {
		String[] parameters = AnalysisRpcPythonService.createParameters(
				new File("python"), 1234);
		// make sure that createParameters does not mess with my exe name
		Assert.assertEquals(parameters[0], "python");
		// we need to be unbuffered so that the capture of output works properly
		Assert.assertEquals(parameters[1], "-u");
		Assert.assertTrue(parameters[2]
				.endsWith(AnalysisRpcPythonService.SCISOFT_PY_RPC_MAIN_MODULE));
		Assert.assertEquals(parameters[3], "" + 1234);
	}

}
