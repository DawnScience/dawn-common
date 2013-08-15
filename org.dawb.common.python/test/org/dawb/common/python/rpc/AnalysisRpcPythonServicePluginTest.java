package org.dawb.common.python.rpc;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcException;
import uk.ac.diamond.scisoft.analysis.rpc.AnalysisRpcServer;
import uk.ac.diamond.scisoft.analysis.rpc.IAnalysisRpcHandler;

public class AnalysisRpcPythonServicePluginTest {

	private AnalysisRpcPythonService service;

	@Before
	public void before() throws AnalysisRpcException, IOException, CoreException {
		service = new AnalysisRpcPythonServiceManual();
	}

	@After
	public void after() {
		service.stop();
	}

	@Test
	public void testIsAlive() throws IOException, AnalysisRpcException {
		Assert.assertTrue(service.getClient().isAlive());
	}

	private interface ItestAddHandler {
		public int plus(int a, int b) throws AnalysisRpcException;
	}

	@Test
	public void testAddHandler() throws AnalysisRpcException {
		service.addHandler("def plus(a, b): return a + b", "plus");
		ItestAddHandler proxy = service.getClient().newProxyInstance(
				ItestAddHandler.class);
		Assert.assertEquals(proxy.plus(1, 2), 3);
	}

	private interface ItestAddHandlers {
		public int plus(int a, int b) throws AnalysisRpcException;

		public int minus(int a, int b) throws AnalysisRpcException;
	}

	@Test
	public void testAddHandlers() throws AnalysisRpcException {
		service.addHandlers(
				"def plus(a, b): return a + b\ndef minus(a, b): return a - b",
				new String[] { "plus", "minus" });
		ItestAddHandlers proxy = service.getClient().newProxyInstance(
				ItestAddHandlers.class);
		Assert.assertEquals(proxy.plus(1, 2), 3);
		Assert.assertEquals(proxy.minus(1, 2), -1);
	}

	@Test
	public void testTwoServices() throws AnalysisRpcException, IOException, CoreException {
		// use field service as service 1
		AnalysisRpcPythonServiceManual service2 = new AnalysisRpcPythonServiceManual();
		try {
			Assert.assertTrue(service.getClient().isAlive());
			Assert.assertTrue(service2.getClient().isAlive());
			Assert.assertTrue(service.getClient().isAlive());
			Assert.assertTrue("Assert services are indeed on different ports",
					service.getClient().getPort() != service2.getClient()
							.getPort());
		} finally {
			service2.stop();
		}
	}

	private interface ItestSetPlotPort {
		public void callPlot() throws AnalysisRpcException;
	}

	@Test
	public void testSetPlotPort() throws AnalysisRpcException {
		// Create a fake server pretending to be a plot server
		// This directs all calls to here.
		AnalysisRpcServer server = new AnalysisRpcServer(0);
		final boolean[] called = new boolean[] { false };
		server.addHandler("SDAPlotter", new IAnalysisRpcHandler() {

			@Override
			public Object run(Object[] args) throws AnalysisRpcException {
				called[0] = true;
				return null;
			}
		});
		server.start();

		// tell the Python server where the plot server is
		service.setPlottingPort(server.getPort());

		service.addHandler(
				"def callPlot(): import scisoftpy as dnp; dnp.plot.plot(dnp.arange(10))\n",
				"callPlot");
		ItestSetPlotPort proxy = service.getClient().newProxyInstance(
				ItestSetPlotPort.class, true);
		proxy.callPlot();
		Assert.assertTrue(called[0]);
	}

}
