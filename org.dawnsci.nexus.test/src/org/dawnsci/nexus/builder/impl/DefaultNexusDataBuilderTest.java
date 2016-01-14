package org.dawnsci.nexus.builder.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
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

public class DefaultNexusDataBuilderTest {
	
	public static class TestPositioner extends AbstractNexusProvider<NXpositioner> {
		
		public TestPositioner() {
			super("positioner", NexusBaseClass.NX_POSITIONER, NXpositioner.NX_VALUE);
			useDeviceNameAsAxisName(true);
		}
		
		public TestPositioner(String name) {
			super(name, NexusBaseClass.NX_POSITIONER);
			useDeviceNameAsAxisName(true);
		}
		
		@Override
		protected NXpositioner doCreateNexusObject(NexusNodeFactory nodeFactory) {
			NXpositioner positioner = nodeFactory.createNXpositioner();
			positioner.initializeLazyDataset(NXpositioner.NX_VALUE, 1, Dataset.FLOAT64);
			positioner.initializeLazyDataset("source", 1, Dataset.FLOAT64);
			return positioner;
		}
		
	}
	
	public static class TestDetector extends AbstractNexusProvider<NXdetector> {

		public TestDetector() {
			super("testDetector", NexusBaseClass.NX_DETECTOR);
		}
		
		@Override
		protected NXdetector doCreateNexusObject(NexusNodeFactory nodeFactory) {
			NXdetector detector = nodeFactory.createNXdetector();
			detector.initializeLazyDataset(NXdetector.NX_DATA, 3, Dataset.FLOAT64);
			return detector;
		}
		
	}
	
	public static class MultipleFieldTestPositioner extends AbstractNexusProvider<NXpositioner> {
		
		public MultipleFieldTestPositioner() {
			super("ss1", NexusBaseClass.NX_POSITIONER, NXpositioner.NX_VALUE);
			useDeviceNameAsAxisName(true);
		}
		
		@Override
		protected NXpositioner doCreateNexusObject(NexusNodeFactory nodeFactory) {
			NXpositioner positioner = nodeFactory.createNXpositioner();
			positioner.initializeLazyDataset("field1", 1, Dataset.FLOAT64);
			positioner.initializeLazyDataset("field2", 1, Dataset.FLOAT64);
			positioner.initializeLazyDataset("field3", 1, Dataset.FLOAT64);
			positioner.initializeLazyDataset("field4", 1, Dataset.FLOAT64);
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
	public void testGetNxData() {
		assertThat(dataBuilder.getNxData(), notNullValue());
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
	public void testSetDataDevice() throws NexusException {
		assertThat(nxData.getNumberOfAttributes(), is(1));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(0));
		
		TestDetector detector = new TestDetector();
		dataBuilder.setDataDevice(detector);
		
		assertThat(nxData.getNumberOfAttributes(), is(3));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(1));
		
		assertThat(nxData.getAttribute("signal"), is(notNullValue()));
		assertThat(nxData.getAttribute("signal").getFirstElement(), is(equalTo("testDetector")));
		assertAxes(".", ".", ".");
		assertThat(nxData.getDataNode("testDetector"), is(sameInstance(
				detector.getNexusObject().getDataNode(NXdetector.NX_DATA))));
	}
	
	@Test
	public void testSetDataDevice_fieldName() throws NexusException {
		assertThat(nxData.getNumberOfAttributes(), is(1));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(0));
		
		TestDetector detector = new TestDetector();
		dataBuilder.setDataDevice(detector, "foo");
		
		assertThat(nxData.getNumberOfAttributes(), is(3));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(1));
		
		assertThat(nxData.getAttribute("signal"), is(notNullValue()));
		assertThat(nxData.getAttribute("signal").getFirstElement(), is(equalTo("foo")));
		assertAxes(".", ".", ".");
		assertThat(nxData.getDataNode("foo"), is(sameInstance(
				detector.getNexusObject().getDataNode(NXdetector.NX_DATA))));
	}
	
	@Test
	public void testAddAxisDevice() throws NexusException {
		TestDetector detector = new TestDetector();
		dataBuilder.setDataDevice(detector);
		assertThat(nxData.getNumberOfAttributes(), is(3));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(1));
		
		TestPositioner positioner = new TestPositioner();
		dataBuilder.addAxisDevice(positioner, new int[] { 0 });
		
		assertThat(nxData.getNumberOfAttributes(), is(4));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(2));
		
		assertAxes(".", ".", ".");
		assertIndices("positioner", 0);
		assertThat(nxData.getDataNode("positioner"), is(sameInstance(
				positioner.getNexusObject().getDataNode(NXpositioner.NX_VALUE))));
	}
	
	@Test
	public void testAddAxisDevice_defaultAxisForDimension() throws NexusException {
		TestDetector detector = new TestDetector();
		dataBuilder.setDataDevice(detector);
		assertThat(nxData.getNumberOfAttributes(), is(3));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(1));
		
		TestPositioner positioner = new TestPositioner();
		dataBuilder.addAxisDevice(positioner, new int[] { 0 }, 0);
		
		assertThat(nxData.getNumberOfAttributes(), is(4));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(2));
		
		assertAxes("positioner", ".", ".");
		assertIndices("positioner", 0);
		assertThat(nxData.getDataNode("positioner"), is(sameInstance(
				positioner.getNexusObject().getDataNode(NXpositioner.NX_VALUE))));
	}
	
	@Test
	public void testAddAxisDevice_sourceFieldName() throws NexusException {
		TestDetector detector = new TestDetector();
		dataBuilder.setDataDevice(detector);
		assertThat(nxData.getNumberOfAttributes(), is(3));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(1));
		
		TestPositioner positioner = new TestPositioner();
		dataBuilder.addAxisDevice(positioner, "source", new int[] { 0 });
		
		assertThat(nxData.getNumberOfAttributes(), is(4));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(2));
		
		assertAxes(".", ".", ".");
		assertIndices("source", 0);
		assertThat(nxData.getDataNode("source"), is(sameInstance(
				positioner.getNexusObject().getDataNode("source"))));
	}
	
	@Test
	public void testAddAxisDevice_sourceFieldName_defaultAxisForDimension() throws NexusException {
		TestDetector detector = new TestDetector();
		dataBuilder.setDataDevice(detector);
		assertThat(nxData.getNumberOfAttributes(), is(3));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(1));
		
		TestPositioner positioner = new TestPositioner();
		dataBuilder.addAxisDevice(positioner, "source", new int[] { 0 }, 0);
		
		assertThat(nxData.getNumberOfAttributes(), is(4));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(2));
		
		assertAxes("source", ".", ".");
		assertIndices("source", 0);
		assertThat(nxData.getDataNode("source"), is(sameInstance(
				positioner.getNexusObject().getDataNode("source"))));
	}
	
	@Test
	public void testAddAxisDevice_sourceAndDestinationFieldNames() throws NexusException {
		TestDetector detector = new TestDetector();
		dataBuilder.setDataDevice(detector);
		assertThat(nxData.getNumberOfAttributes(), is(3));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(1));
		
		TestPositioner positioner = new TestPositioner();
		dataBuilder.addAxisDevice(positioner, "source", "dest", new int[] { 0 });
		
		assertThat(nxData.getNumberOfAttributes(), is(4));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(2));
		
		assertAxes(".", ".", ".");
		assertIndices("dest", 0);
		assertThat(nxData.getDataNode("dest"), is(sameInstance(
				positioner.getNexusObject().getDataNode("source"))));
	}
	
	@Test
	public void testAddAxisDevice_sourceAndDestinationFieldNames_defaultAxisForDimension() throws NexusException {
		TestDetector detector = new TestDetector();
		dataBuilder.setDataDevice(detector);
		assertThat(nxData.getNumberOfAttributes(), is(3));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(1));
		
		TestPositioner positioner = new TestPositioner();
		dataBuilder.addAxisDevice(positioner, "source", "dest", new int[] { 1 }, 1);
		
		assertThat(nxData.getNumberOfAttributes(), is(4));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(2));
		
		assertAxes(".", "dest", ".");
		assertIndices("dest", 1);
		assertThat(nxData.getDataNode("dest"), is(sameInstance(
				positioner.getNexusObject().getDataNode("source"))));
	}
	
	@Test
	public void testAddAxisDevice_multipleFields() throws NexusException {
		TestDetector detector = new TestDetector();
		dataBuilder.setDataDevice(detector);
		assertThat(nxData.getNumberOfAttributes(), is(3));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(1));
		
		MultipleFieldTestPositioner positioner = new MultipleFieldTestPositioner();
		String[] sourceFieldNames = { "field1", "field2", "field3", "field4" };
		dataBuilder.addAxisDevice(positioner, sourceFieldNames, new int[] { 0 });
		
		assertThat(nxData.getNumberOfAttributes(), is(7));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(5));
		
		assertAxes(".", ".", ".");
		for (String sourceFieldName : sourceFieldNames) {
			assertIndices(sourceFieldName, 0);
			assertThat(nxData.getDataNode(sourceFieldName), is(sameInstance(
					positioner.getNexusObject().getDataNode(sourceFieldName))));
			
		}
	}
	
	@Test
	public void testAddAxisDevice_multipleFields_defaultAxisForDimension() throws NexusException {
		TestDetector detector = new TestDetector();
		dataBuilder.setDataDevice(detector);
		assertThat(nxData.getNumberOfAttributes(), is(3));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(1));
		
		MultipleFieldTestPositioner positioner = new MultipleFieldTestPositioner();
		String[] sourceFieldNames = { "field1", "field2", "field3", "field4" };
		dataBuilder.addAxisDevice(positioner, sourceFieldNames, new int[] { 0 }, "field3", 2);
		
		assertThat(nxData.getNumberOfAttributes(), is(7));
		assertThat(nxData.getNumberOfGroupNodes(), is(0));
		assertThat(nxData.getNumberOfDataNodes(), is(5));
		
		assertAxes(".", ".", "field3");
		for (String sourceFieldName : sourceFieldNames) {
			assertIndices(sourceFieldName, 0);
			assertThat(nxData.getDataNode(sourceFieldName), is(sameInstance(
					positioner.getNexusObject().getDataNode(sourceFieldName))));
			
		}
	}
	

}
