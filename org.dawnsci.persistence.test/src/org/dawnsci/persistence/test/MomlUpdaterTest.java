package org.dawnsci.persistence.test;

import static org.junit.Assert.*;

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
