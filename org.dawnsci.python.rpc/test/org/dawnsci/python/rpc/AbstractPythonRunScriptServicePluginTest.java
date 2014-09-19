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
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.dawb.common.util.eclipse.BundleUtils;
import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcException;
import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcRemoteException;

abstract public class AbstractPythonRunScriptServicePluginTest implements
		IPythonRunScript {

	private String getScript(String script) throws IOException {
		final String scriptPath = BundleUtils.getBundleLocation(
				"org.dawnsci.python.rpc").getAbsolutePath()
				+ "/test/org/dawnsci/python/rpc/" + script;
		// We need to keep all the files in the same directory so we can reuse the same
		// launched service. So load the file and put it in the temp directory.
		// See the exception raised in python_service_runscript.py if sys.path[0] is
		// changed
		String scriptContents = FileUtils.readFileToString(new File(scriptPath));
		return getTemp(scriptContents);
	}

	/** Make a temporary file out of a script */
	private String getTemp(String pycode) throws IOException {
		File temp = File.createTempFile("PythonRunScriptServiceTest", ".py");
		FileWriter writer = new FileWriter(temp);
		try {
			writer.write(pycode);
		} finally {
			writer.close();
		}
		// Comment out this line if you want to keep the temp python file
		temp.deleteOnExit();
		return temp.getAbsolutePath();
	}

	@Test
	public void testEmptyResult() throws AnalysisRpcException, IOException,
			CoreException {
		Map<String, Object> result = runScript(
				getTemp("def run(**kwargs): return {}"),
				Collections.<String, Object> emptyMap());
		Assert.assertEquals(Collections.emptyMap(), result);
	}

	@Test
	public void testNullResult() throws AnalysisRpcException, IOException,
			CoreException {
		// Remember, no return in Python is the same as return None
		Map<String, Object> result = runScript(
				getTemp("def run(**kwargs): pass"),
				Collections.<String, Object> emptyMap());
		Assert.assertEquals(null, result);
	}

	@Test
	public void testExceptionFromPython() throws IOException {
		String pycode = "def run(**kwargs): assert False, 'assertion failed'";
		try {
			runScript(getTemp(pycode),
					Collections.<String, Object> emptyMap());
		} catch (AnalysisRpcException e) {
			AnalysisRpcRemoteException cause = (AnalysisRpcRemoteException)e.getCause();
			Assert.assertTrue(cause.toString()
					.contains("AssertionError"));
			Assert.assertTrue(cause.toString().contains("assertion failed"));
			Assert.assertTrue(cause.getPythonFormattedStackTrace(null).contains(pycode));
		}
	}

	@Test
	public void testBasicOutput() throws AnalysisRpcException, IOException,
			CoreException {
		Map<String, Object> result = runScript(
				getTemp("def run(**kwargs): return {'var1': 23}"),
				Collections.<String, Object> emptyMap());
		Assert.assertTrue(result.size() == 1);
		Assert.assertEquals(result.get("var1"), 23);
	}

	@Test
	public void testMultipleOutput() throws AnalysisRpcException, IOException,
			CoreException {
		Map<String, Object> result = runScript(
				getTemp("def run(**kwargs): return {'var1': 23, 'var2': 'abc'}"),
				Collections.<String, Object> emptyMap());
		Assert.assertTrue(result.size() == 2);
		Assert.assertEquals(result.get("var1"), 23);
		Assert.assertEquals(result.get("var2"), "abc");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testComplexOutput() throws AnalysisRpcException, IOException,
			CoreException {
		Map<String, Object> result = runScript(
				getTemp("def run(**kwargs): return {'var1': {1 : 2, 'b' : 3}}"),
				Collections.<String, Object> emptyMap());
		Assert.assertTrue(result.size() == 1);
		Map<Object, Object> var1 = (Map<Object, Object>) result.get("var1");
		Assert.assertEquals(var1.get(1), 2);
		Assert.assertEquals(var1.get("b"), 3);
	}

	@Test
	public void testBasicInput() throws AnalysisRpcException, IOException {
		Map<String, Object> input = new HashMap<String, Object>();
		input.put("var1", 23);
		runScript(getTemp("def run(var1, **kwargs): assert var1 == 23"), input);
	}

	@Test
	public void testMultipleInput() throws AnalysisRpcException, IOException {
		Map<String, Object> input = new HashMap<String, Object>();
		input.put("var1", 23);
		input.put("var2", "abc");
		runScript(
				getTemp("def run(var1, var2, **kwargs): assert var1 == 23; assert var2 == 'abc'"),
				input);
	}

	@Test
	public void testComplexInput() throws AnalysisRpcException, IOException {
		Map<String, Object> input = new HashMap<String, Object>();
		Map<Object, Object> var1 = new HashMap<Object, Object>();
		var1.put(1, 2);
		var1.put("b", 3);
		input.put("var1", var1);
		runScript(
				getTemp("def run(var1, **kwargs): assert var1 == {1 : 2, 'b' : 3}"),
				input);
	}

	/** Regression test (of sorts) for DAWNSCI-317 */
	@Test
	public void testBugDawnSci317() throws AnalysisRpcException, IOException {
		runScript(getScript("runscript_bug_dawnsci317.py"),
				Collections.<String, Object> emptyMap());
	}

	@Test
	public void testImport() throws AnalysisRpcException, IOException {
		runScript(getTemp("import math\ndef run(**kwargs): pass"),
				Collections.<String, Object> emptyMap());
	}

	@Test
	public void testAlternativeMethodName() throws AnalysisRpcException,
			IOException {
		Map<String, Object> result = runScript(
				getTemp("def altrun(**kwargs): return {'var1': 23}"),
				Collections.<String, Object> emptyMap(), "altrun");
		Assert.assertTrue(result.size() == 1);
		Assert.assertEquals(result.get("var1"), 23);
	}

	@Test
	public void testVariableName() throws AnalysisRpcException, IOException {
		Map<String, Object> result = runScript(
				getTemp("run=lambda **kwargs: {'var1': 23}"),
				Collections.<String, Object> emptyMap());
		Assert.assertTrue(result.size() == 1);
		Assert.assertEquals(result.get("var1"), 23);
	}

	@Test
	public void testClassCall() throws AnalysisRpcException, IOException {
		Map<String, Object> result = runScript(
				getTemp("class x:\n\tdef __call__(self, **kwargs): return {'var1': 23}\nrun=x()"),
				Collections.<String, Object> emptyMap());
		Assert.assertTrue(result.size() == 1);
		Assert.assertEquals(result.get("var1"), 23);
	}

}
