package org.dawnsci.nexus.builder.impl;

import static org.dawnsci.nexus.NexusAssert.assertAxes;
import static org.dawnsci.nexus.NexusAssert.assertIndices;
import static org.dawnsci.nexus.NexusAssert.assertShape;
import static org.dawnsci.nexus.NexusAssert.assertSignal;

import org.eclipse.dawnsci.analysis.dataset.impl.FloatDataset;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXpositioner;
import org.eclipse.dawnsci.nexus.NexusBaseClass;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;
import org.eclipse.dawnsci.nexus.builder.AbstractNexusProvider;
import org.eclipse.dawnsci.nexus.builder.DataDevice;
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
			super(name, NexusBaseClass.NX_DETECTOR, NXdetector.NX_DATA);
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
			this.shape = shape;
		}
		
		@Override
		protected NXpositioner doCreateNexusObject(NexusNodeFactory nodeFactory) {
			NXpositioner positioner = nodeFactory.createNXpositioner();
			positioner.setValue(new FloatDataset(shape));
			return positioner;
		}
		
	}
	
	public static class PolarAnglePositioner extends AbstractNexusProvider<NXpositioner> {
		
		private final int dimensionIndex; 
		
		private final int[] scanShape;
		
		public PolarAnglePositioner(String name, int dimensionIndex, int[] scanShape) {
			super(name, NexusBaseClass.NX_POSITIONER);
			this.dimensionIndex = dimensionIndex;
			this.scanShape = scanShape;
			setDataFieldNames("rbv", "demand");
			setDemandDataFieldName("demand");
		}
		
		@Override
		protected NXpositioner doCreateNexusObject(NexusNodeFactory nodeFactory) {
			NXpositioner positioner = nodeFactory.createNXpositioner();
			positioner.setField("rbv", new FloatDataset(scanShape));
			positioner.setField("demand", new FloatDataset(scanShape[dimensionIndex]));
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
	
	@Test
	public void testExample1() throws NexusException {
		dataBuilder.setPrimaryDevice(new TestDetector("data", 100));
		dataBuilder.addDataDevice(new TestPositioner("x", 100), 0);
		
		assertSignal(nxData, "data");
		assertShape(nxData, "data", 100);
		assertAxes(nxData, "x");
		assertIndices(nxData, "x", 0);
		assertShape(nxData, "x", 100);
	}
	
	@Test
	public void testExample2() throws NexusException {
		dataBuilder.setPrimaryDevice(new TestDetector("data", 1000, 20));
		dataBuilder.addDataDevice(new TestDetector("pressure", 20), 1);
		dataBuilder.addDataDevice(new TestDetector("temperature", 20), null, 1);
		dataBuilder.addDataDevice(new TestDetector("time", 1000), 0);
		
		assertSignal(nxData, "data");
		assertShape(nxData, "data", 1000, 20);
		assertAxes(nxData, "time", "pressure");
		assertIndices(nxData, "pressure", 1);
		assertShape(nxData, "pressure", 20);
		assertIndices(nxData, "temperature", 1);
		assertShape(nxData, "temperature", 20);
		assertIndices(nxData, "time", 0);
		assertShape(nxData, "time", 1000);
	}
	
	@Test
	public void testExample3() throws NexusException {
		dataBuilder.setPrimaryDevice(new DataDevice<>(new TestDetector("det", 100, 100000), true));
		dataBuilder.addDataDevice(new TestDetector("pressure", 100), 0);
		dataBuilder.addDataDevice(new TestDetector("tof", 100000), 1);
		
		assertSignal(nxData, "det");
		assertShape(nxData, "det", 100, 100000);
		assertAxes(nxData, "pressure", "tof");
		assertIndices(nxData, "pressure", 0);
		assertShape(nxData, "pressure", 100);
		assertIndices(nxData, "tof", 1);
		assertShape(nxData, "tof", 100000);
	}
	
	@Test
	public void testExample4() throws NexusException {
		// note, wiki page example has 100x512x100000 but this is too large to allocate
		dataBuilder.setPrimaryDevice(new DataDevice<>(new TestDetector("det", 100, 512, 1000)));
		dataBuilder.addDataDevice(new TestPositioner("tof", 1000), 2);
		dataBuilder.addDataDevice(new TestPositioner("x", 100, 512), 0, 0, 1);
		dataBuilder.addDataDevice(new TestPositioner("y", 100, 512), 1, 0, 1);
		
		assertSignal(nxData, "det");
		assertShape(nxData, "det", 100, 512, 1000);
		assertAxes(nxData, "x", "y", "tof");
		assertIndices(nxData, "x", 0, 1);
		assertShape(nxData, "x", 100, 512);
		assertIndices(nxData, "y", 0, 1);
		assertShape(nxData, "y", 100, 512);
	}
	
	@Test
	public void testExample5() throws NexusException {
		dataBuilder.setPrimaryDevice(new DataDevice<>(new TestDetector("det1", 50, 5, 1024)));
		PolarAnglePositioner polarAnglePositioner = new PolarAnglePositioner(
				"polar_angle", 0, new int[] { 50, 5 });
		dataBuilder.addDataDevice(polarAnglePositioner, 0, 0, 1);
		dataBuilder.addDataDevice(new TestPositioner("frame_number", 5), 1);
		dataBuilder.addDataDevice(new TestPositioner("time", 50, 5), null, 0, 1);
		
		assertSignal(nxData, "det1");
		assertShape(nxData, "det1", 50, 5, 1024);
		assertAxes(nxData, "polar_angle_demand", "frame_number", ".");
		assertIndices(nxData, "polar_angle_demand", 0);
		assertShape(nxData, "polar_angle_demand", 50);
		assertIndices(nxData, "polar_angle_rbv", 0, 1);
		assertShape(nxData, "polar_angle_rbv", 50, 5);
		assertIndices(nxData, "frame_number", 1);
		assertShape(nxData, "frame_number", 5);
		assertIndices(nxData, "time", 0, 1);
		assertShape(nxData, "time", 50, 5);
	}

}
