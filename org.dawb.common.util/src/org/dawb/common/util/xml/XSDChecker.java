/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.util.xml;

import java.io.IOException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

public class XSDChecker {

	public static void main(String[] args) throws Exception {
		
		System.out.println(XSDChecker.validateFile("/users/gerring/Desktop/sequence.xml"));
	}
	
	public static boolean validateFile(final String filePath) throws SAXException, IOException {

		// 1. Lookup a factory for the W3C XML Schema language
		SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

		// 2. Compile the schema. 
		// Here the schema is loaded from a java.io.File, but you could use 
		// a java.net.URL or a javax.xml.transform.Source instead.
		Schema schema = factory.newSchema(new StreamSource(XSDChecker.class.getResourceAsStream("dad.xsd")));

		// 3. Get a validator from the schema.
		Validator validator = schema.newValidator();

		// 4. Parse the document you want to check.
		Source source = new StreamSource(filePath);

		// 5. Check the document
		validator.validate(source);
		return true;

	}
}
