package org.dawnsci.nexus;

import static org.eclipse.dawnsci.nexus.builder.NexusDataBuilder.ATTR_NAME_TARGET;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.ILazyDataset;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.analysis.dataset.impl.AbstractDataset;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.PositionIterator;
import org.eclipse.dawnsci.nexus.NXdata;
import org.eclipse.dawnsci.nexus.NXobject;
import org.eclipse.dawnsci.nexus.NXroot;

public class NexusAssert {

	public static void assertNexusTreesEqual(final TreeFile expectedTree, final TreeFile actualTree) throws Exception {
		assertGroupNodesEqual(expectedTree.getGroupNode(), actualTree.getGroupNode());
	}

	public static void assertGroupNodesEqual(final GroupNode expectedGroup, final GroupNode actualGroup) throws Exception {
		if (expectedGroup == actualGroup) {
			return;
		}

		if (expectedGroup instanceof NXobject) {
			assertTrue(actualGroup instanceof NXobject);
			assertEquals(((NXobject) expectedGroup).getNXclass(), ((NXobject) actualGroup).getNXclass());
		}

		// check attributes same
		Iterator<String> attributeNameIterator = expectedGroup.getAttributeNameIterator();
		while (attributeNameIterator.hasNext()) {
			String attributeName = attributeNameIterator.next();
			Attribute expectedAttr = expectedGroup.getAttribute(attributeName);
			Attribute actualAttr = actualGroup.getAttribute(attributeName);
			if (!expectedAttr.getName().equals("target")) {
				assertNotNull(actualAttr);
				assertAttributesEquals(expectedAttr, actualAttr);
			}
		}
		// check number of attributes same (i.e. actualGroup has no additional attributes)
		// additional attribute "target" is allowed. This is added when loading a file with >1 hard link to same node
		int expectedNumAttributes = expectedGroup.getNumberOfAttributes();
		if (expectedGroup.containsAttribute("target")) {
			expectedNumAttributes--;
		}
		assertEquals(expectedNumAttributes, actualGroup.getNumberOfAttributes());

		// check child nodes same
		final Iterator<String> nodeNameIterator = expectedGroup.getNodeNameIterator();
		while (nodeNameIterator.hasNext()) {
			String nodeName = nodeNameIterator.next();
			// node is either a group node or data node
			if (expectedGroup.containsGroupNode(nodeName)) {
				assertTrue(actualGroup.containsGroupNode(nodeName));
				assertGroupNodesEqual(expectedGroup.getGroupNode(nodeName), actualGroup.getGroupNode(nodeName));
			} else {
				// node is a data node
				assertTrue(expectedGroup.containsDataNode(nodeName));
				assertTrue(actualGroup.containsDataNode(nodeName));
				assertDataNodesEqual(expectedGroup.getDataNode(nodeName), actualGroup.getDataNode(nodeName));
			}
		}

		assertEquals(expectedGroup.getNumberOfDataNodes(), actualGroup.getNumberOfDataNodes());
		assertEquals(expectedGroup.getNumberOfGroupNodes(), actualGroup.getNumberOfGroupNodes());
	}

	public static void assertAttributesEquals(final Attribute expectedAttr, final Attribute actualAttr) {
		assertEquals(expectedAttr.getName(), actualAttr.getName());
		assertEquals(expectedAttr.getTypeName(), actualAttr.getTypeName());
		assertEquals(expectedAttr.getFirstElement(), actualAttr.getFirstElement());
		assertEquals(expectedAttr.getSize(), actualAttr.getSize());
		assertEquals(expectedAttr.getRank(), actualAttr.getRank());
		assertArrayEquals(expectedAttr.getShape(), actualAttr.getShape());
		assertDatasetsEqual(expectedAttr.getValue(), actualAttr.getValue());
	}

	private static void assertDataNodesEqual(final DataNode expectedDataNode, final DataNode actualDataNode) {
		// check attributes same
		Iterator<String> attributeNameIterator = expectedDataNode.getAttributeNameIterator();
		while (attributeNameIterator.hasNext()) {
			String attributeName = attributeNameIterator.next();
			Attribute expectedAttr = expectedDataNode.getAttribute(attributeName);
			Attribute actualAttr = actualDataNode.getAttribute(attributeName);
			if (!expectedAttr.getName().equals("target")) {
				assertNotNull(expectedAttr);
				assertAttributesEquals(expectedAttr, actualAttr);
			}
		}
		// check number of attributes same (i.e. actualDataNode has no additional attributes)
		// additional attribute "target" is allowed. This is added when loading a file with >1 hard link to same node
		int expectedNumAttributes = expectedDataNode.getNumberOfAttributes();
//		if (expectedDataNode.containsAttribute("target")) {
//			expectedNumAttributes--;
//		}
		assertEquals(expectedNumAttributes, actualDataNode.getNumberOfAttributes());

		assertEquals(expectedDataNode.getTypeName(), actualDataNode.getTypeName());
		assertEquals(expectedDataNode.isAugmented(), actualDataNode.isAugmented());
		assertEquals(expectedDataNode.isString(), actualDataNode.isString());
		assertEquals(expectedDataNode.isSupported(), actualDataNode.isSupported());
		assertEquals(expectedDataNode.isUnsigned(), actualDataNode.isUnsigned());
		assertEquals(expectedDataNode.getMaxStringLength(), actualDataNode.getMaxStringLength());
		// TODO reinstate lines below and check why they break - dataNode2 is null
//		assertArrayEquals(dataNode1.getMaxShape(), dataNode2.getMaxShape());
//		assertArrayEquals(dataNode1.getChunkShape(), dataNode2.getChunkShape());
		assertEquals(expectedDataNode.getString(), actualDataNode.getString());
		assertDatasetsEqual(expectedDataNode.getDataset(), actualDataNode.getDataset());
	}

	private static void assertDatasetsEqual(final ILazyDataset expectedDataset, final ILazyDataset actualDataset) {
		// Note: dataset names can be different, as long as the containing data node names are the same
		// assertEquals(dataset1.getName(), dataset2.getName());
		// assertEquals(dataset1.getClass(), dataset2.getClass());
		assertEquals(expectedDataset.elementClass(), actualDataset.elementClass());
		assertEquals(expectedDataset.getElementsPerItem(), actualDataset.getElementsPerItem());
		assertEquals(expectedDataset.getSize(), actualDataset.getSize());
		if (actualDataset.getRank() == 0) {
			// TODO: special case for scalar datasets. This could be fixed in future by marking the
			// dataset as scalar in the HDF5 file
			assertEquals(1, expectedDataset.getRank());
			assertArrayEquals(new int[] { 1 }, expectedDataset.getShape());
		} else {
			assertEquals(expectedDataset.getRank(), actualDataset.getRank());
			assertArrayEquals(expectedDataset.getShape(), actualDataset.getShape());
		}

		assertDatasetDataEqual(expectedDataset, actualDataset);

		// TODO: in future also check metadata
	}

	private static void assertDatasetDataEqual(final ILazyDataset expectedDataset, final ILazyDataset actualDataset) {
		if (expectedDataset instanceof Dataset && actualDataset instanceof Dataset) {
			assertEquals(expectedDataset, actualDataset); // uses Dataset.equals() method
		} else {
			assertEquals(expectedDataset.getSize(), actualDataset.getSize());
			if (expectedDataset.getSize() == 0) {
				return;
			}
			
			// getSlice() with no args loads whole dataset if a lazy dataset
			IDataset expectedSlice = expectedDataset.getSlice();
			IDataset actualSlice = actualDataset.getSlice();

			final int datatype = AbstractDataset.getDType(actualDataset);
			PositionIterator positionIterator = new PositionIterator(actualDataset.getShape());
			while (positionIterator.hasNext()) {
				int[] position = positionIterator.getPos();
				switch (datatype) {
				case Dataset.BOOL:
					assertEquals(expectedSlice.getBoolean(position), actualSlice.getBoolean(position));
					break;
				case Dataset.INT8:
					assertEquals(expectedSlice.getByte(position), actualSlice.getByte(position));
					break;
				case Dataset.INT32:
					assertEquals(expectedSlice.getInt(position), actualSlice.getInt(position));
					break;
				case Dataset.INT64:
					assertEquals(expectedSlice.getLong(position), actualSlice.getLong(position));
					break;
				case Dataset.FLOAT32:
					assertEquals(expectedSlice.getFloat(position), actualSlice.getFloat(position), 1e-7);
					break;
				case Dataset.FLOAT64:
					assertEquals(expectedSlice.getDouble(position), actualSlice.getDouble(position), 1e-15);
					break;
				case Dataset.STRING:
				case Dataset.DATE:
					assertEquals(expectedSlice.getString(position), actualSlice.getString(position));
					break;
				case Dataset.COMPLEX64:
				case Dataset.COMPLEX128:
				case Dataset.OBJECT:
					assertEquals(expectedSlice.getObject(position), actualSlice.getObject(position));
					break;
				}
			}
		}
	}
	
	public static void assertSignal(NXdata nxData, String expectedSignalFieldName) {
		Attribute signalAttr = nxData.getAttribute("signal");
		assertThat(signalAttr, is(notNullValue()));
		assertThat(signalAttr.getRank(), is(1));
		assertThat(signalAttr.getFirstElement(), is(equalTo(expectedSignalFieldName)));
		assertThat(nxData.getDataNode(expectedSignalFieldName), is(notNullValue()));
		
	}

	public static void assertAxes(NXdata nxData, String... expectedValues) {
		Attribute axesAttr = nxData.getAttribute("axes");
		assertThat(axesAttr, is(notNullValue()));
		assertThat(axesAttr.getRank(), is(1));
		assertThat(axesAttr.getShape()[0], is(expectedValues.length));
		IDataset value = axesAttr.getValue();
		for (int i = 0; i < expectedValues.length; i++) {
			assertThat(value.getString(i), is(equalTo(expectedValues[i])));
		}
	}

	public static void assertShape(NXdata nxData, String fieldName, int... expectedShape) {
		DataNode dataNode = nxData.getDataNode(fieldName);
		assertThat(fieldName, is(notNullValue()));
		int[] actualShape = dataNode.getDataset().getShape();
		assertArrayEquals(expectedShape, actualShape);
	}

	public static void assertIndices(NXdata nxData, String axisName, int... indices) {
		Attribute indicesAttr = nxData.getAttribute(axisName + "_indices");
		assertThat(indicesAttr, is(notNullValue()));
		assertThat(indicesAttr.getRank(), is(1));
		assertThat(indicesAttr.getShape()[0], is(indices.length));
		IDataset value = indicesAttr.getValue();
		for (int i = 0; i < indices.length; i++) {
			assertThat(value.getInt(i), is(equalTo(indices[i])));
		}
	}
	
	public static void assertTarget(NXdata nxData, String destName, NXroot nxRoot, String targetPath) {
		DataNode dataNode = nxData.getDataNode(destName);
		assertThat(dataNode, is(notNullValue()));
		Attribute targetAttr = dataNode.getAttribute(ATTR_NAME_TARGET);
		assertThat(targetAttr, is(notNullValue()));
		assertThat(targetAttr.getSize(), is(1));
		assertThat(targetAttr.getFirstElement(), is(equalTo(targetPath)));
		
		NodeLink nodeLink = nxRoot.findNodeLink(targetPath);
		assertTrue(nodeLink.isDestinationData());
		assertThat(nodeLink.getDestination(), is(sameInstance(dataNode)));
	}

}
