package org.dawnsci.nexus.ispyb;

import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;

import uk.ac.diamond.ispyb.api.DataCollectionExperiment;
import uk.ac.diamond.ispyb.api.DataCollectionGroup;
import uk.ac.diamond.ispyb.api.DataCollectionInfo;
import uk.ac.diamond.ispyb.api.DataCollectionMain;
import uk.ac.diamond.ispyb.api.Detector;
import uk.ac.diamond.ispyb.api.IspybDataCollectionApi;
import uk.ac.diamond.ispyb.api.IspybFactoryService;
import uk.ac.diamond.ispyb.api.Sample;

/**
 * DataCollectionGroup
 * ProposalCode - NXentry:experiment_identifier (ee12345-1)
 * ProposalNumber - NXentry:experiment_identifier (ee12345-1)
 * StartTime - NXentry:start_time
 * EndTime - NXentry:end_time
 * Comments - NXentry:title
 * 
 * DataCollectionMain
 * FileTempate - path to file?
 * StartTime - as DCG
 * EndTime - as DCG
 * DetectorID - retrieved from NXdetector:serial_number - detector entries must be added manually
 * NumberOfImages - calculate from Data shape
 * SnapShots - Extract from data
 * 
 * DataCollectionExperiment
 * setXBeam - NXdetector:beam_center_x
 * DetectorDistance - NXdetector:distance
 * Wavelength - NXMonochromator:wavelength (or convert from energy)
 * ExposureTime - NXdetector:count_time
 * Resolution - calculate from NXdetector + NXMono
 * Flux - NXbeam:flux
 * 
 * Sample
 * Name - NXsample:name
 * Comments - NXsample:description
 * 
 */
public class NexusToISPyB {
	
	public static void insertFile(String path) {
		
		INexusFileFactory nexusFactory = ServiceHolder.getNexusFactory();
		
		if (nexusFactory == null) {
			System.err.println("OSGI not working");
			return;
		}
		 
		
		try (NexusFile nexusFile = nexusFactory.newNexusFile(path)) {
			nexusFile.openToRead();
			
			TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
			
			GroupNode groupNode = nexusTree.getGroupNode();
			
			if (!(groupNode instanceof NXroot)) {
				System.err.println("Root node not NXroot!");
				return;
			}
			
			NXroot nxRoot = (NXroot)groupNode;
			Optional<NXentry> first = nxRoot.getAllEntry().values().stream().findFirst();
			
			if (!first.isPresent()) {
				System.err.println("Contains no NXentry");
				return;
			}
			
			NXentry nxEntry = first.get();
			
			List<NXdetector> detectors = nxRoot.getAllEntry().values().stream()
					.flatMap(e -> e.getAllInstrument().values().stream())
					.flatMap(i -> i.getAllDetector().values().stream()).collect(Collectors.toList());
					
					
			for (NXdetector d : detectors) {
				System.out.println(d.getLocal_nameScalar());
			}
			
			nxRoot.toString();
			
//			nxRoot.getAllEntry().getKeys().stream
			
		
		
		
		
		IspybFactoryService<IspybDataCollectionApi> factory = ServiceHolder.getIspybDataCollectionFactory();
		
			IspybDataCollectionApi dcApi = factory.buildIspybApi("somehow", null, null);
			
			DataCollectionGroup dcGroup = new DataCollectionGroup();
//			dcGroup.
			//From parsing file path?
			String visit = nxEntry.getExperiment_identifierScalar();
			dcGroup.setProposalCode("proposalCode");
			dcGroup.setProposalNumber(1);
			Long dcGroupID = dcApi.upsertDataCollectionGroup(dcGroup);
			
			Sample s = new Sample();

			//for detectors in Nexus file
			
			
			
			DataCollectionMain dcMain = new DataCollectionMain();
			dcMain.setGroupId(dcGroupID);
			Optional<Detector> detector = dcApi.retrieveDetector("serialNumber");
			detector.ifPresent(d -> dcMain.setDetectorId(d.getDetectorId().intValue()));
			dcMain.setFileTemplate(path);
			Date start = nxEntry.getStart_timeScalar();
			Date end = nxEntry.getEnd_timeScalar();
			DataCollectionExperiment dce = new DataCollectionExperiment();
			dcMain.setStartTime(new Timestamp(start.getTime()));
			dcMain.setEndTime(new Timestamp(end.getTime()));
			DataCollectionInfo dci = new DataCollectionInfo();
			
//			dcMain.set
			
			dcApi.upsertDataCollectionMain(dcMain);
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
