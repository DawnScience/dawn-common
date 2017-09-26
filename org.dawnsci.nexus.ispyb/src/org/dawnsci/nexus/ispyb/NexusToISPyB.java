package org.dawnsci.nexus.ispyb;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;


public class NexusToISPyB {
	
	public static void insertFile(String path) {
		
		INexusFileFactory nexusFactory = ServiceHolder.getNexusFactory();
		 
		
		try (NexusFile nexusFile = nexusFactory.newNexusFile(path)) {
			nexusFile.openToRead();
			
			TreeFile nexusTree = NexusUtils.loadNexusTree(nexusFile);
			
			GroupNode groupNode = nexusTree.getGroupNode();
			
			if (!(groupNode instanceof NXroot)) {
				System.err.println("Root node not NXroot!");
				return;
			}
			
			NXroot nxRoot = (NXroot)groupNode;
			
			List<NXdetector> detectors = nxRoot.getAllEntry().values().stream()
					.flatMap(e -> e.getAllInstrument().values().stream())
					.flatMap(i -> i.getAllDetector().values().stream()).collect(Collectors.toList());
					
					
			for (NXdetector d : detectors) {
				System.out.println(d.getLocal_nameScalar());
			}
			
			nxRoot.toString();
			
//			nxRoot.getAllEntry().getKeys().stream
			
		} catch (NexusException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		
		
		
		IspybFactoryService<IspybDataCollectionApi> factory = ServiceHolder.getIspybDataCollectionFactory();
		try {
			IspybDataCollectionApi dcApi = factory.buildIspybApi("somehow", null, null);
			
			DataCollectionGroup dcGroup = new DataCollectionGroup();
			dcGroup.setProposalCode("proposalCode");
			dcGroup.setProposalNumber(1);
			Long dcGroupID = dcApi.upsertDataCollectionGroup(dcGroup);
			
			//for detectors in Nexus file
			
			DataCollectionMain dcMain = new DataCollectionMain();
			dcMain.setGroupId(dcGroupID);
			Optional<Detector> detector = dcApi.retrieveDetector("serialNumber");
			detector.ifPresent(d -> dcMain.setDetectorId(d.getDetectorId().intValue()));
//			dcMain.setFileTemplate(fileTemplate);
			
			
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
