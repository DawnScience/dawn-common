package org.dawnsci.io.spec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.dawnsci.io.spec.MultiScanDataParser;
import org.dawnsci.io.spec.SpecLoader;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.january.dataset.Dataset;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;

public class SpecLoaderTest {

	@Test
	public void loadI16File() throws ScanFileHolderException {
		String n = "testfiles/DIAMOND_I16_Lucian.spec";
		DataHolder dh = new SpecLoader(n).loadFile();

		assertEquals(84, dh.size());
		assertEquals(6, countScanItems(dh, 1)); // 0 - 5
		Dataset d = dh.getDataset("Scan 2/mu");
		assertEquals(11, d.getSize());
		assertEquals(30.83387, d.getDouble(-1), 1e-6);

		assertEquals(6, countScanItems(dh, 11)); // 60 - 65
		Dataset e = dh.getDataset("Scan 11/path");
		assertEquals(11, e.getSize());
		assertEquals(3539658.0, e.getDouble(-1), 1e-6);
	}

	@Test
	public void loadID32File() throws ScanFileHolderException {
		String n = "testfiles/fourc_20161029_long_night_00.spec";
		DataHolder dh = new SpecLoader(n).loadFile();

		assertEquals(2079, dh.size());
		assertEquals(60, countScanItems(dh, 1)); // 0 - 59
		Dataset d = dh.getDataset("Scan 1/xpos");
		assertEquals(61, d.getSize());
		assertEquals(-10294.5, d.getDouble(10), 1e-6);
		assertTrue(dh.contains("Scan 1/gcell2"));

		assertEquals(57, countScanItems(dh, 36)); // 2022 - 2078
		Dataset e = dh.getDataset("Scan 36/diode_r");
		assertEquals(90, e.getSize());
		assertEquals(281881.0, e.getDouble(-1), 1e-6);
		assertTrue(dh.contains("Scan 36/gcell2"));
	}

	private int countScanItems(DataHolder dh, int s) {
		String scan = "Scan " + s + "/";
		int c = 0;
		for (String name :dh.getNames()) {
			if (name.startsWith(scan)) {
				c++;
			}
		}
		return c;
	}

	@Test
	public void createGoodNames() {
		List<String> names = new ArrayList<>();
		names.add("good");
		names.add("bad");

		String n = MultiScanDataParser.createGoodName(names, null, 2);
		assertEquals("Column 2", n);
		names.add(n);
		assertEquals("Column 22", MultiScanDataParser.createGoodName(names, "  ", 2));
		assertEquals("ugly", MultiScanDataParser.createGoodName(names, "ugly", 2));
		assertEquals("bad2", MultiScanDataParser.createGoodName(names, "bad", 2));
		names.add("bad2");
		assertEquals("bad3", MultiScanDataParser.createGoodName(names, "bad", 2));
		assertEquals("bad22", MultiScanDataParser.createGoodName(names, "bad2", 2));
	}
}
