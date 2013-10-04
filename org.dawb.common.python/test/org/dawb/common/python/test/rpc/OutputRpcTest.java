/*-
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawb.common.python.test.rpc;

import java.util.Arrays;
import java.util.HashMap;

import org.dawb.common.util.eclipse.BundleUtils;
import org.dawnsci.python.rpc.PythonService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

import com.isencia.util.commandline.ManagedCommandline;

public class OutputRpcTest {

	private static PythonService service;
	private String stdoutAsStringBefore;
	private String stderrAsStringBefore;

	@Before
	public void before() throws Exception {
		service = PythonService.openConnection("python");
		Assert.assertTrue(service.isRunning());
		service.getCommand().setStreamLogsToLogging(false);
		service.getCommand().setStreamLogsToLoggingAndSaved(true);

		stdoutAsStringBefore = service.getCommand().getStdoutAsString();
		stderrAsStringBefore = service.getCommand().getStderrAsString();
	}

	@After
	public void after() {
		if (service != null)
			service.stop();
	}

	public void runTestScript(String script) throws Exception {
		final String scriptPath = BundleUtils.getBundleLocation(
				"org.dawb.common.python").getAbsolutePath()
				+ "/test/org/dawb/common/python/test/rpc/" + script;

		// Run the script
		service.runScript(scriptPath, new HashMap<String, AbstractDataset>(),
				Arrays.asList(new String[] {})); // Calls the method 'run'
													// in the script with
													// the arguments
	}
	
	public String getTestsStdout() throws Exception {
		Thread.sleep(1000);
		String stdoutAsStringAfter = service.getCommand().getStdoutAsString();

		String newData = stdoutAsStringAfter.substring(stdoutAsStringBefore
				.length());
		return newData;
	}
	
	public String getTestsStderr() throws Exception {
		Thread.sleep(1000);
		String stderrAsStringAfter = service.getCommand().getStderrAsString();

		String newData = stderrAsStringAfter.substring(stderrAsStringBefore
				.length());
		return newData;
	}

	@Test
	public void testStdout() throws Exception {
		runTestScript("stdout.py");
		Assert.assertEquals("hello world\n", getTestsStdout());
	}


	@Test
	public void testStderr() throws Exception {
		runTestScript("stderr.py");
		Assert.assertEquals("from stderr\n", getTestsStderr());
	}

	/** 
	 * This tests fails because {@link ManagedCommandline#StreamGobbler} assumes that lines
	 * are newline terminated (ie. readLine is used in StreamGobbler. It may be that this
	 * test should never pass, or that some synchronization is put between the StreamGobbler
	 * thread and the call to getTestStdout to ensure all the data has been "gobbled".
	 * 
	 * In addition this test causes an exception to be thrown in the StreamGobbler thread that
	 * looks similar to this:
     * 08:23:19.611 [null - StdOut] ERROR c.i.u.commandline.ManagedCommandline - Error reading from stream by Gobbler null - StdOut
	 * java.io.IOException: Stream closed
	 * 	at java.io.BufferedInputStream.getBufIfOpen(BufferedInputStream.java:145) [na:1.6.0_31]
	 * 	at java.io.BufferedInputStream.read(BufferedInputStream.java:308) [na:1.6.0_31]
	 * 	at sun.nio.cs.StreamDecoder.readBytes(StreamDecoder.java:264) [na:1.6.0_31]
	 * 	at sun.nio.cs.StreamDecoder.implRead(StreamDecoder.java:306) [na:1.6.0_31]
	 * 	at sun.nio.cs.StreamDecoder.read(StreamDecoder.java:158) [na:1.6.0_31]
	 * 	at java.io.InputStreamReader.read(InputStreamReader.java:167) [na:1.6.0_31]
	 * 	at java.io.BufferedReader.fill(BufferedReader.java:136) [na:1.6.0_31]
	 * 	at java.io.BufferedReader.readLine(BufferedReader.java:299) [na:1.6.0_31]
	 * 	at java.io.BufferedReader.readLine(BufferedReader.java:362) [na:1.6.0_31]
	 * 	at com.isencia.util.commandline.ManagedCommandline$StreamGobbler.read(ManagedCommandline.java:238) [na:na]
	 * 	at com.isencia.util.commandline.ManagedCommandline$StreamGobbler.run(ManagedCommandline.java:227) [na:na]
	 * 
	 * This happens because the process terminates while readLine is active, but has received >= 1 
	 * character. 
	 * 
	 * Therefore, it may be best to simply use read() in StreamGobbler and that would also
	 * mean that readRemainder() wouldn't be necessary.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testStdoutOneChar() throws Exception {
		runTestScript("stdout_onechar.py");
		Assert.assertEquals("J", getTestsStdout());
	}


}
