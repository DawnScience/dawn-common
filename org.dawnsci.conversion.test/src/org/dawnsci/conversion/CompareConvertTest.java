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

import java.io.File;
import java.util.Arrays;

import org.dawb.common.services.conversion.IConversionContext;
import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
import org.junit.Test;

public class CompareConvertTest {

	@Test
	public void testImageSimple() throws Exception {
							
		ConversionServiceImpl service = new ConversionServiceImpl();

		final IConversionContext context = service.open("C:/Work/runtime-uk.ac.diamond.dawn.product/data_big/i20/.*nxs");
		final File output = new File("C:/Work/runtime-uk.ac.diamond.dawn.product/data_big/i20/compare_convert_test.h5");
		context.setOutputPath(output.getAbsolutePath());
		context.setDatasetNames(Arrays.asList("/entry1/instrument/cold_head_temp/cold_head_temp", 
										      "/entry1/instrument/xas_scannable/Energy", 
											  "/entry1/instrument/FFI0/FFI0"));
		context.setConversionScheme(ConversionScheme.COMPARE);

		service.process(context);
					
	}
}
