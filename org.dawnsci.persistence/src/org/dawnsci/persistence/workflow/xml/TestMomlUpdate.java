/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.workflow.xml;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class TestMomlUpdate {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String result = MomlUpdater.updateMoml("/scratch/MomlUpdate/before.moml");
		// create the new xml file
		Writer writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("/scratch/MomlUpdate/result.xml"), "utf-8"));
			writer.write(result);
			writer.close();
		} catch (IOException ex){
			ex.printStackTrace();
		}
	}

}
