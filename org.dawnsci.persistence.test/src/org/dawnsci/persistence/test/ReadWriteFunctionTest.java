/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.dawnsci.jexl.internal.ExpressionServiceImpl;
import org.dawnsci.persistence.PersistenceServiceCreator;
import org.dawnsci.persistence.ServiceLoader;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Fermi;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.JexlExpressionFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Parameter;

public class ReadWriteFunctionTest {

	@Before
	public void init() {
		// Set factory for test
		ServiceLoader.setNexusFactory(new NexusFileFactoryHDF5());
		ServiceLoader.setExpressionService(new ExpressionServiceImpl());
	}

	// Do not put the annotation as the files needs to be created and closed
	// after each test
	// so it can run with the thread tests
	// Passes value by array
	public IPersistentFile before(File[] tmp) throws Exception {
		// Set factory for test
		ServiceLoader.setNexusFactory(new NexusFileFactoryHDF5());

		tmp[0] = File.createTempFile("TestFunction", ".txt");
		tmp[0].createNewFile();

		// create the PersistentService
		IPersistenceService persist = PersistenceServiceCreator.createPersistenceService();
		IPersistentFile file = persist.createPersistentFile(tmp[0].getAbsolutePath());
		return file;
	}

	public void after(File tmp, IPersistentFile file){
		if (tmp != null)
			tmp.deleteOnExit();
		if(file != null)
			file.close();
	}

	@Test
	public void testWriteFunction() throws Exception {
		// create and init files
		File[] tmp = new File[1];
		IPersistentFile file = before(tmp);

		Gaussian gaussian = new Gaussian();
		Map<String, IFunction> functions = new HashMap<String, IFunction>();
		functions.put("MyFunction", gaussian);
		//write function to JSON
		file.setFunctions(functions);
		//read function from JSON
		Map<String, IFunction> functionsRead = file.getFunctions(null);

		assertEquals(gaussian, functionsRead.get("MyFunction"));

		// close files
		after(tmp[0], file);
	}

	@Test
	public void testWriteFunctionWithParam() throws Exception {
		// create and init files
		File[] tmp = new File[1];
		IPersistentFile file = before(tmp);

		Gaussian gaussian = new Gaussian(1,2,3);
		Map<String, IFunction> functions = new HashMap<String, IFunction>();
		functions.put("MyFunction", gaussian);
		//write function to JSON
		file.setFunctions(functions);
		//read function from JSON
		Map<String, IFunction> functionsRead = file.getFunctions(null);
		assertEquals(gaussian, functionsRead.get("MyFunction"));

		// close files
		after(tmp[0], file);
	}

	@Test
	public void testWriteFermiFunctionWithParam() throws Exception {
		// create and init files
		File[] tmp = new File[1];
		IPersistentFile file = before(tmp);

		Fermi fermi = new Fermi(1,2,3,4);
		Map<String, IFunction> functions = new HashMap<String, IFunction>();
		functions.put("MyFunction", fermi);
		//write function to JSON
		file.setFunctions(functions);
		//read function from JSON
		Map<String, IFunction> functionsRead = file.getFunctions(null);

		assertEquals(fermi, functionsRead.get("MyFunction"));

		// close files
		after(tmp[0], file);
	}
	
	@Test
	public void testWriteCompositeFunction() throws Exception {
		// create and init files
		File[] tmp = new File[1];
		IPersistentFile file = before(tmp);

		CompositeFunction compoundFunction = new CompositeFunction();
		Gaussian gaussian = new Gaussian(1,2,3);
		Fermi fermi = new Fermi(1,2,3,4);
		compoundFunction.addFunction(gaussian);
		compoundFunction.addFunction(fermi);

		Map<String, IFunction> functions = new HashMap<String, IFunction>();
		functions.put("MyFunction", compoundFunction);
		//write function to JSON
		file.setFunctions(functions);
		//read function from JSON
		Map<String, IFunction> functionsRead = file.getFunctions(null);

		assertEquals(compoundFunction, functionsRead.get("MyFunction"));

		// close files
		after(tmp[0], file);
	}

	@Test
	public void testWriteJexlExpressionFunction() throws Exception {
		// create and init files
		File[] tmp = new File[1];
		IPersistentFile file = before(tmp);

		String expression = "func:Gaussian(x,pos,fwhm,area)+func:Gaussian(x,pos+offset,fwhm,area/proportion)";
		JexlExpressionFunction jexl = new JexlExpressionFunction(new ExpressionServiceImpl(), expression);
		for (int i = 0; i < jexl.getNoOfParameters(); i++) {
			jexl.setParameter(i, new Parameter(i+1));
		}
		Map<String, IFunction> functions = new HashMap<String, IFunction>();
		functions.put("MyFunction", jexl);
		//write function to JSON
		file.setFunctions(functions);
		// read function from JSON
		Map<String, IFunction> functionsRead = file.getFunctions(null);

		assertEquals(jexl, functionsRead.get("MyFunction"));

		// close files
		after(tmp[0], file);
	}
}
