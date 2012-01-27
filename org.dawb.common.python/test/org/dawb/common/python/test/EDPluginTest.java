/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.python.test;

import org.dawb.common.python.EDPlugin;
import org.junit.Test;


/**
 * Class to test Python class, requires further tests to be written.
 * 
 * The current tests here could also be done smarter and improve coverage.
 * 
 * Please add more tests...
 * 
 * @author gerring
 *
 */
public class EDPluginTest {

	
	@Test
	public void testCreatePlugin() throws Exception {
		
		EDPlugin ednaPythonPlugin = new EDPlugin("EDPluginTestPluginFactory");
		String inputXML = "<?xml version=\"1.0\" ?><XSDataString><value>Test string value.</value></XSDataString>";
		ednaPythonPlugin.setDataInput("value1", inputXML);
		ednaPythonPlugin.executePlugin();
		System.out.println(ednaPythonPlugin.getDataOutput("value1"));
	}
	
	
}
