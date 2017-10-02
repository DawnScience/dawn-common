package org.dawnsci.nexus.ispyb.test;

import java.util.Date;

import org.dawnsci.nexus.ispyb.ServiceHolder;
import org.eclipse.dawnsci.hdf5.nexus.NexusFileFactoryHDF5;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXbeam;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXmonochromator;
import org.eclipse.dawnsci.nexus.NXsample;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusNodeFactory;

public class ExampleFileBuilder {

	public static void main(String[] args) {
		ServiceHolder.setNexusFactory(new NexusFileFactoryHDF5());
		
		INexusFileFactory nff = ServiceHolder.getNexusFactory();
		
		try (NexusFile nexus = nff.newNexusFile("/scratch/test_ispyb_nexus.nxs")) {
			
			NXentry nxEntry = NexusNodeFactory.createNXentry();
			populateNXEntry(nxEntry);
			
			NXinstrument nxInstrument = NexusNodeFactory.createNXinstrument();
			NXdetector nxDetector = NexusNodeFactory.createNXdetector();
			
			populateNXDetector(nxDetector);
			
			NXmonochromator nxMonochromator = NexusNodeFactory.createNXmonochromator();
			nxMonochromator.setWavelengthScalar(1.0);
			
			NXbeam nxBeam = NexusNodeFactory.createNXbeam();
			nxBeam.setFluxScalar(1.0);
			
			NXsample nxSample = NexusNodeFactory.createNXsample();
			populateNXsample(nxSample);
			
			nxInstrument.addGroupNode("detector", nxDetector);
			nxInstrument.addGroupNode("monochromator", nxMonochromator);
			nxInstrument.addGroupNode("beam", nxBeam);
			
			nxEntry.addGroupNode("instrument", nxInstrument);
			nxEntry.addGroupNode("sample", nxSample);
			
			nexus.createAndOpenToWrite();
			nexus.addNode("/entry", nxEntry);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void populateNXEntry(NXentry entry) {
		
		entry.setExperiment_identifierScalar("cm1234-5");
		entry.setStart_timeScalar(new Date());
		entry.setEnd_timeScalar(new Date());
		entry.setTitleScalar("p45 malcolm scan");
		
	}
	
	private static void populateNXDetector(NXdetector detector) {
	
		detector.setField("serial_number", "detector_serial_number_xxxxxxxxxx");
		detector.setBeam_center_xScalar(1000.0);
		detector.setBeam_center_yScalar(1001.0);
		detector.setDistanceScalar(100.0);
		detector.setCount_timeScalar(10);
		
	}
	
	private static void populateNXsample(NXsample sample) {
		sample.setNameScalar("my_sample");
		sample.setDescriptionScalar("This is my sample");
	}

}
