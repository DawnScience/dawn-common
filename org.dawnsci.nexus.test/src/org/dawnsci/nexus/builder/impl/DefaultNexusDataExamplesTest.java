package org.dawnsci.nexus.builder.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.dataset.impl.FloatDataset;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.builder.AbstractNexusProvider;
import org.eclipse.dawnsci.nexus.builder.NexusDataBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusEntryBuilder;
import org.eclipse.dawnsci.nexus.builder.NexusFileBuilder;
import org.junit.Before;
import org.junit.Test;

/**
 * This test class tests that the {@link DefaultNexusDataBuilder}
 * can construct the example {@link NXdata} structures from the
 * document http://wiki.nexusformat.org/2014_axes_and_uncertainties
 */
public class DefaultNexusDataExamplesTest {
	
	public static class TestDetector extends AbstractNexusProvider<NXdetector> {
	
		private final int[] shape;
		
		public TestDetector(int... shape) {
			this("testDetector", shape);
		}
		
		public TestDetector(String name, int... shape) {
			super(name, NexusBaseClass.NX_DETECTOR);
			useDeviceNameAsAxisName(true);
			this.shape = shape;
		}
		
		@Override
		protected NXdetector doCreateNexusObject(NexusNodeFactory nodeFactory) {
			NXdetector detector = nodeFactory.createNXdetector();
			detector.setData(new FloatDataset(shape));
			return detector;
		}
		
	}

	public static class TestPositioner extends AbstractNexusProvider<NXpositioner> {
		
		private final int[] shape;
		
		public TestPositioner(String name, int... shape) {
			super(name, NexusBaseClass.NX_POSITIONER, NXpositioner.NX_VALUE);
			useDeviceNameAsAxisName(true);
			this.shape = shape;
		}
		
		@Override
		protected NXpositioner doCreateNexusObject(NexusNodeFactory nodeFactory) {
			NXpositioner positioner = nodeFactory.createNXpositioner();
			positioner.setValue(new FloatDataset(shape));
			return positioner;
		}
		
	}
	
	private NexusDataBuilder dataBuilder;
	
	private NXdata nxData;
	
	@Before
	public void setUp() throws Exception {
		NexusFileBuilder fileBuilder = new DefaultNexusFileBuilder("test");
		NexusEntryBuilder entryBuilder = fileBuilder.newEntry();
		dataBuilder = entryBuilder.createDefaultData();
		nxData = dataBuilder.getNxData();
	}
	
	private void assertAxes(String... expectedValues) {
		Attribute axesAttr = nxData.getAttribute("axes");
		assertThat(axesAttr, is(notNullValue()));
		assertThat(axesAttr.getRank(), is(1));
		assertThat(axesAttr.getShape()[0], is(expectedValues.length));
		IDataset value = axesAttr.getValue();
		for (int i = 0; i < expectedValues.length; i++) {
			assertThat(value.getString(i), is(equalTo(expectedValues[i])));
		}
	}
	
	private void assertShape(String fieldName, int... expectedShape) {
		DataNode dataNode = nxData.getDataNode(fieldName);
		assertThat(fieldName, is(notNullValue()));
		int[] actualShape = dataNode.getDataset().getShape();
		assertArrayEquals(expectedShape, actualShape);
	}

	private void assertIndices(String axisName, int... indices) {
		Attribute indicesAttr = nxData.getAttribute(axisName + "_indices");
		assertThat(indicesAttr, is(notNullValue()));
		assertThat(indicesAttr.getRank(), is(1));
		assertThat(indicesAttr.getShape()[0], is(indices.length));
		IDataset value = indicesAttr.getValue();
		for (int i = 0; i < indices.length; i++) {
			assertThat(value.getInt(i), is(equalTo(indices[i])));
		}
	}

	@Test
	public void testExample1() throws NexusException {
		dataBuilder.setDataDevice(new TestDetector("data", 100));
		dataBuilder.addAxisDevice(new TestPositioner("x", 100), new int[] { 0 }, 0);
		
		assertThat(nxData.getAttribute("signal").getFirstElement(), is(equalTo("data")));
		assertShape("data", 100);
		assertAxes("x");
		assertIndices("x", 0);
		assertShape("x", 100);
	}
	
	@Test
	public void testExample2() throws NexusException {
		dataBuilder.setDataDevice(new TestDetector("data", 1000, 20));
		dataBuilder.addAxisDevice(new TestDetector("pressure", 20), new int[] { 1 }, 1);
		dataBuilder.addAxisDevice(new TestDetector("temperature", 20), new int[] { 1 });
		dataBuilder.addAxisDevice(new TestDetector("time", 1000), new int[] { 0 }, 0);
		
		assertThat(nxData.getAttribute("signal").getFirstElement(), is(equalTo("data")));
		assertShape("data", 1000, 20);
		assertAxes("time", "pressure");
		assertIndices("pressure", 1);
		assertShape("pressure", 20);
		assertIndices("temperature", 1);
		assertShape("temperature", 20);
		assertIndices("time", 0);
		assertShape("time", 1000);
	}
	
	@Test
	public void testExample3() throws NexusException {
		dataBuilder.setDataDevice(new TestDetector("det", 100, 100000));
		dataBuilder.addAxisDevice(new TestDetector("pressure", 100), new int[] { 0 }, 0);
		dataBuilder.addAxisDevice(new TestDetector("tof", 100000), new int[] { 1 }, 1);
		
		assertThat(nxData.getAttribute("signal").getFirstElement(), is(equalTo("det")));
		assertShape("det", 100, 100000);
		assertAxes("pressure", "tof");
		assertIndices("pressure", 0);
		assertShape("pressure", 100);
		assertIndices("tof", 1);
		assertShape("tof", 100000);
	}
	
	@Test
	public void testExample4() throws NexusException {
		// note, wiki page example has 100x512x100000 but this is too large to allocate
		dataBuilder.setDataDevice(new TestDetector("det", 100, 512, 1000));
		dataBuilder.addAxisDevice(new TestPositioner("tof", 1000), new int[] { 2 }, 2);
		dataBuilder.addAxisDevice(new TestPositioner("x", 100, 512), new int[] { 0, 1 }, 0);
		dataBuilder.addAxisDevice(new TestPositioner("y", 100, 512), new int[] { 0, 1 }, 1);
		
		assertThat(nxData.getAttribute("signal").getFirstElement(), is(equalTo("det")));
		assertShape("det", 100, 512, 1000);
		assertAxes("x", "y", "tof");
		assertIndices("x", 0, 1);
		assertShape("x", 100, 512);
		assertIndices("y", 0, 1);
		assertShape("y", 100, 512);
	}
	
	@Test
	public void testExample5() throws NexusException {
		dataBuilder.setDataDevice(new TestDetector("det1", 50, 5, 1024));
		dataBuilder.addAxisDevice(new TestPositioner("polar_angle_demand", 50), new int[] { 0 }, 0);
		dataBuilder.addAxisDevice(new TestPositioner("polar_angle_rbv", 50, 5), new int[] { 0, 1 });
		dataBuilder.addAxisDevice(new TestPositioner("frame_number", 5), new int[] { 1 }, 1);
		dataBuilder.addAxisDevice(new TestPositioner("time", 50, 5), new int[] { 0, 1 });
		
		assertThat(nxData.getAttribute("signal").getFirstElement(), is(equalTo("det1")));
		assertShape("det1", 50, 5, 1024);
		assertAxes("polar_angle_demand", "frame_number", ".");
		assertIndices("polar_angle_demand", 0);
		assertShape("polar_angle_demand", 50);
		assertIndices("polar_angle_rbv", 0, 1);
		assertShape("polar_angle_rbv", 50, 5);
		assertIndices("frame_number", 1);
		assertShape("frame_number", 5);
		assertIndices("time", 0, 1);
		assertShape("time", 50, 5);
	}

}
