package org.dawnsci.nexus.ispyb.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.SQLException;
import java.util.Optional;

import org.dawnsci.nexus.ispyb.NexusToISPyB;
import org.dawnsci.nexus.ispyb.ServiceHolder;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.junit.Test;

import uk.ac.diamond.ispyb.api.IspybDataCollectionApi;
import uk.ac.diamond.ispyb.api.IspybDataCollectionFactoryService;
import uk.ac.diamond.ispyb.api.Schema;

public class NexusToISPyBTest {

	@Test
	public void test() throws SQLException {
		ServiceHolder.setNexusFactory(new NexusFileFactoryHDF5());
		
		
		IspybDataCollectionFactoryService mockDCFS = mock(IspybDataCollectionFactoryService.class);
		ServiceHolder.setIspybDataCollectionFactory(mockDCFS);
		
		IspybDataCollectionApi mockDCA = mock(IspybDataCollectionApi.class);
		
		when(mockDCFS.buildIspybApi("test_host:test_port", Optional.of("test_user"), Optional.of("test_password"), Optional.of(Schema.ISPYB.toString()))).thenReturn(mockDCA);
		
		NexusToISPyB.insertFile("testfiles/test_ispyb_nexus.nxs", "testfiles/test.properties");
		
		verify(mockDCFS).buildIspybApi("test_host:test_port", Optional.of("test_user"), Optional.of("test_password"), Optional.of(Schema.ISPYB.toString()));

	}

}
