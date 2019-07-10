package org.dawnsci.io.test;

import static org.junit.Assert.assertEquals;

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
		Dataset d = dh.getDataset("Scan 2/mu");
		assertEquals(11, d.getSize());
		assertEquals(30.83387, d.getDouble(-1), 1e-6);

		Dataset e = dh.getDataset("Scan 11/path");
		assertEquals(11, e.getSize());
		assertEquals(3539658.0, e.getDouble(-1), 1e-6);
	}
}
