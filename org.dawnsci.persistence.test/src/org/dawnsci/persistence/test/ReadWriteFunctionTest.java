package org.dawnsci.persistence.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawnsci.persistence.PersistenceServiceCreator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Fermi;
import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction;

public class ReadWriteFunctionTest {

	private File tmp;
	private IPersistenceService persist;
	private IPersistentFile file;

	@Before
	public void before() throws Exception {
		tmp = File.createTempFile("TestFunction", ".txt");
		tmp.createNewFile();

		// create the PersistentService
		persist = PersistenceServiceCreator.createPersistenceService();
		file = persist.createPersistentFile(tmp.getAbsolutePath());
	}

	@After
	public void after(){
		if (tmp != null)
			tmp.deleteOnExit();

		if(file != null)
			file.close();
	}


	@Test
	public void testWriteFunction() throws Exception {
		
		Gaussian gaussian = new Gaussian();
		Map<String, IFunction> functions = new HashMap<String, IFunction>();
		functions.put("MyFunction", gaussian);
		//write function to JSON
		file.setFunctions(functions);
		//read function from JSON
		Map<String, IFunction> functionsRead = file.getFunctions(null);

		assertEquals(gaussian, functionsRead.get("MyFunction"));
	}

	@Test
	public void testWriteFunctionWithParam() throws Exception {
		
		Gaussian gaussian = new Gaussian(1,2,3);
		Map<String, IFunction> functions = new HashMap<String, IFunction>();
		functions.put("MyFunction", gaussian);
		//write function to JSON
		file.setFunctions(functions);
		//read function from JSON
		Map<String, IFunction> functionsRead = file.getFunctions(null);
		assertEquals(gaussian, functionsRead.get("MyFunction"));

	}

	@Test
	public void testWriteFermiFunctionWithParam() throws Exception {
		
		Fermi fermi = new Fermi(1,2,3,4);
		Map<String, IFunction> functions = new HashMap<String, IFunction>();
		functions.put("MyFunction", fermi);
		//write function to JSON
		file.setFunctions(functions);
		//read function from JSON
		Map<String, IFunction> functionsRead = file.getFunctions(null);

		assertEquals(fermi, functionsRead.get("MyFunction"));
	}
	
	@Test
	public void testWriteCompositeFunction() throws Exception {
		CompositeFunction compoundFunction = new CompositeFunction();
		Gaussian gaussian = new Gaussian(1,2,3);
		Fermi fermi = new Fermi(1,2,3,4);
		compoundFunction.addFunction(gaussian);
		compoundFunction.addFunction(fermi);
		//TODO
//		Map<String, IFunction> functions = new HashMap<String, IFunction>();
//		functions.put("MyFunction", compoundFunction);
//		//write function to JSON
//		file.setFunctions(functions);
//		//read function from JSON
//		Map<String, IFunction> functionsRead = file.getFunctions(null);
//
//		assertEquals(compoundFunction, functionsRead.get("MyFunction"));
	}

}
