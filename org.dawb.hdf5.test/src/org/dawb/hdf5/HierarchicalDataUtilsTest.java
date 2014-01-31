package org.dawb.hdf5;

import static org.dawb.hdf5.HierarchicalDataUtils.compareScalars;
import static org.dawb.hdf5.HierarchicalDataUtils.extractScalar;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

public class HierarchicalDataUtilsTest {

	@Test
	public void testExtractScalar() {
		assertEquals(null, extractScalar(null));

		assertEquals((short) 1, extractScalar(new short[] { 1 }));
		assertEquals(1, extractScalar(new int[] { 1 }));
		assertEquals(10000000000l, extractScalar(new long[] { 10000000000l }));
		assertEquals('a', extractScalar(new char[] { 'a' }));
		assertEquals((float) 1, extractScalar(new float[] { 1 }));
		assertEquals((double) 1, extractScalar(new double[] { 1 }));
		assertEquals(true, extractScalar(new boolean[] { true }));
		assertEquals((byte) 1, extractScalar(new byte[] { 1 }));
		assertEquals("1", extractScalar(new String[] { "1" }));
		assertEquals((Integer) 1, extractScalar(new Integer[] { 1 }));

		assertEquals(null, extractScalar("Hello"));
		assertEquals(null, extractScalar(123));
		assertEquals(null, extractScalar(new int[] {}));
		assertEquals(null, extractScalar(new int[] { 1, 2 }));
		assertEquals(null, extractScalar(new int[][] { new int[] { 1 } }));
		assertEquals(null, extractScalar(new Object[] { 1 }));
	}

	@Test
	public void testCompareScalars() {
		assertTrue(compareScalars(1, 2) < 0);
		assertTrue(compareScalars(2, 1) > 0);
		assertTrue(compareScalars(1, 1) == 0);

		// fall back to .toString comparison because they are
		// different types (but both comparable)
		assertTrue(compareScalars((short) 1, (int) 1) == 0);
		assertTrue(compareScalars("1", (int) 1) == 0);

		// not both comparable
		assertTrue(compareScalars(Arrays.asList(1), "[1]") == 0);
		assertTrue(compareScalars("[1]", Arrays.asList(1)) == 0);
		assertTrue(compareScalars(Arrays.asList(1), Arrays.asList(1)) == 0);
	}
}
