/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.test;

import static org.junit.Assert.assertTrue;

import org.dawnsci.persistence.workflow.xml.MomlUpdater;
import org.junit.Before;
import org.junit.Test;

public class MomlUpdaterTest {

	private String result;
	private String xmlValidation;
	private String expressionMode;

	@Before
	public void setUp() throws Exception {
		result = MomlUpdater.updateMoml("resource/toUpdate.moml");
		xmlValidation = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
		expressionMode = "Expression Mode";
	}

	@Test
	public void testXMLDeclaration() {
		assertTrue("Test result String starts with XML declaration", result.startsWith(xmlValidation));
	}

	@Test
	public void testExpressionModeDeleted() {
		assertTrue("Test Expression Mode node is deleted", !result.contains(expressionMode));
	}

}
