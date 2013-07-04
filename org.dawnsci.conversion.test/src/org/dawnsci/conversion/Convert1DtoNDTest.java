/*-
 * Copyright © 2011 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package org.dawnsci.conversion;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.junit.Test;

import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class Convert1DtoNDTest {
	
private String testfile = "MoKedge_1_15.nxs";
	
	@Test
	public void test1DSimple() throws Exception {
		
		ConversionServiceImpl service = new ConversionServiceImpl();
		
		// Determine path to test file
		final String path = getTestFilePath(testfile);
		
		String[] paths = new String[]{path,path,path,path};
		
		final IConversionContext context = service.open(paths);
		
		final File tmp = File.createTempFile("testSimple", ".nxs");
		tmp.deleteOnExit();
        context.setOutputPath(tmp.getAbsolutePath());
        context.setConversionScheme(ConversionScheme.H5_FROM_1D);
        context.setAxisDatasetName("/entry1/counterTimer01/Energy");
        context.setDatasetName("/entry1/counterTimer01/(I0|lnI0It|It)");
        
        service.process(context);
        
        final DataHolder   dh    = LoaderFactory.getData(tmp.getAbsolutePath());
        final List<String> names = Arrays.asList("/entry1/counterTimer01/I0","/entry1/counterTimer01/lnI0It","/entry1/counterTimer01/It");
        for (String name : names) {
            ILazyDataset ds = dh.getLazyDataset(name);
            assertArrayEquals(new int[] {4,489},ds.getShape());
		}
   	}
	
private String getTestFilePath(String fileName) {
		
		final File test = new File("testfiles/"+fileName);
		return test.getAbsolutePath();
	
	}

}
