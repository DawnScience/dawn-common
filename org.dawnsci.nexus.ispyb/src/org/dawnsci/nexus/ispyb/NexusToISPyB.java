package org.dawnsci.nexus.ispyb;

import java.io.File;
import java.io.FileInputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.TreeFile;
import org.eclipse.dawnsci.nexus.INexusFileFactory;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXentry;
import org.eclipse.dawnsci.nexus.NXmonochromator;
import org.eclipse.dawnsci.nexus.NXroot;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.IDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.ispyb.api.DataCollectionExperiment;
import uk.ac.diamond.ispyb.api.DataCollectionGroup;
import uk.ac.diamond.ispyb.api.DataCollectionInfo;
import uk.ac.diamond.ispyb.api.DataCollectionMain;
import uk.ac.diamond.ispyb.api.Detector;
import uk.ac.diamond.ispyb.api.IspybDataCollectionApi;
import uk.ac.diamond.ispyb.api.IspybDataCollectionFactoryService;
import uk.ac.diamond.ispyb.api.Sample;
import uk.ac.diamond.ispyb.api.Schema;

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
	
	private static final String USER = "ispyb.source.connect.user";
	private static final String HOST = "ispyb.source.connect.host";
	private static final String PORT = "ispyb.source.connect.port";
	private static final String PASSWORD = "ispyb.source.connect.password";
	
	private final static Logger logger = LoggerFactory.getLogger(NexusToISPyB.class);

	public static void insertFile(String nexusFile, String configFile) {
		
		DBProps props = parsePropertiesFile(configFile);
		
		if (props == null) {
			logger.error("Could not read properties from file: " + configFile);
			return;
		}
		
		insertFile(nexusFile, props.user, props.password, props.host + ":" + props.port);
	}
	

	public static void insertFile(String path, String user, String password, String uri) {
		
		INexusFileFactory nexusFactory = ServiceHolder.getNexusFactory();
		
		if (nexusFactory == null) {
			logger.error("OSGI not working");
			return;
		}
		 
		
		try (NexusFile nexusFile = nexusFactory.newNexusFile(path)) {
			
			nexusFile.openToRead();
			
			Optional<NXentry> nxEntry = extractNXEntry(nexusFile);
			
			Optional<DataCollectionGroup> dataCollectionGroup  = nxEntry.flatMap(NexusToISPyB::buildDataCollectionGroup);
		
			if (!dataCollectionGroup.isPresent()) {
				logger.error("No data collection group constructed");
				return;
			}
			
			DataCollectionGroup dcGroup = dataCollectionGroup.get();
			
			IspybDataCollectionFactoryService factory = ServiceHolder.getIspybDataCollectionFactory();
		
			IspybDataCollectionApi dcApi = factory.buildIspybApi(uri, Optional.of(user), Optional.of(password), Optional.of(Schema.ISPYB.toString()));
		
			Long dcGroupID = dcApi.upsertDataCollectionGroup(dcGroup);
		
			Optional<NXmonochromator> mono = nxEntry.get().getAllInstrument().values().stream().flatMap(i -> i.getAllMonochromator().values().stream()).findFirst();
			
			Float wavelength = null;
			
			if (mono.isPresent()) {
				Double w = mono.get().getWavelengthScalar();
				if (w != null) wavelength = w.floatValue();
			}

			final Float fw = wavelength;
			
			nxEntry.get().getAllInstrument().values().stream()
			.flatMap(i -> i.getAllDetector().values().stream())
			.forEach(d -> buildDataCollections(d,dcApi,path,dcGroupID, fw));
			
			
			Sample s = new Sample();

			//for detectors in Nexus file
			
			
			DataCollectionExperiment dce = new DataCollectionExperiment();
			
//			dcMain.setStartTime(dcGroup.getStarttime());
//			dcMain.setEndTime(dcGroup.getEndtime());
//			DataCollectionInfo dci = new DataCollectionInfo();
			
//			dcMain.set
			
//			dcApi.upsertDataCollectionMain(dcMain);
			
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static Optional<NXentry> extractNXEntry(NexusFile nexusFile) {
		TreeFile nexusTree;
		try {
			nexusTree = NexusUtils.loadNexusTree(nexusFile);
		} catch (NexusException e) {
			logger.error("Cannot construct Nexus Tree", e);
			return Optional.empty();
		}
		
		GroupNode groupNode = nexusTree.getGroupNode();
		
		if (!(groupNode instanceof NXroot)) {
			logger.error("Root node not NXroot!");
			return Optional.empty();
		}
		
		NXroot nxRoot = (NXroot)groupNode;
		return nxRoot.getAllEntry().values().stream().findFirst();
		
	}
	
	private static void buildDataCollections(NXdetector detector, IspybDataCollectionApi dcApi, String path, long groupID, Float wavelength) {
		DataCollectionMain dcMain = new DataCollectionMain();
		
		IDataset dataset = detector.getDataset("serial_number");
		
		String serial_number = "";
		
		if (dataset != null) {
			serial_number = dataset.getString(0);
		}
		
		if (!serial_number.isEmpty()) {
			Optional<Detector> det = Optional.empty();
			try {
				det = dcApi.retrieveDetector(serial_number);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			det.ifPresent(d -> dcMain.setDetectorId(d.getDetectorId().intValue()));
		}
		
		dcMain.setFileTemplate(path);
		
		dcMain.setGroupId(groupID);
		
		
		
		Long mainID = dcApi.upsertDataCollectionMain(dcMain);
		
		DataCollectionExperiment dcExp = new DataCollectionExperiment();
		dcExp.setId(mainID);
		if (wavelength != null) dcExp.setWavelength(wavelength);
		
	}
	
	private static Optional<DataCollectionGroup> buildDataCollectionGroup(NXentry nxEntry) {
		String visit = nxEntry.getExperiment_identifierScalar();
		
		if (visit == null) {
			logger.error("NXentry contains no experiment_identifier");
			return Optional.empty();
		}
		
		String[] split = visit.split("-");
		
		String propCode = "";
		int propNumber = -1;
		
		if (split.length != 2) {
			logger.error("Malformed visit code: " + visit);
			return Optional.empty();
		}
		
		try {
			propNumber = Integer.parseInt(split[1]);
		} catch (NumberFormatException e) {
			logger.error("Malformed visit code (parse int): " + split[1]);
			return Optional.empty();
		}
		
		propCode = split[0];
		
		DataCollectionGroup dcGroup = new DataCollectionGroup();
		
		logger.debug("Proposal code seems sensible, checking for other values");
		
		Date start = nxEntry.getStart_timeScalar();
		Date end = nxEntry.getEnd_timeScalar();
		String description = nxEntry.getExperiment_descriptionScalar();
		
		if (start != null) {
			dcGroup.setStarttime(new Timestamp(start.getTime()));
		}
		
		if (end != null) {
			dcGroup.setEndtime(new Timestamp(end.getTime()));
		}
		
		if (description != null) {
			dcGroup.setComments(propCode);
		}
		
		return Optional.of(dcGroup);
	}
	
	private static DBProps parsePropertiesFile(String configFile) {
		
		File file = new File(configFile);
		try (FileInputStream fileInput = new FileInputStream(file)) {
			Properties properties = new Properties();
			properties.load(fileInput);
			String user = properties.getProperty(USER);
			String host = properties.getProperty(HOST);
			String port = properties.getProperty(PORT);
			String password = properties.getProperty(PASSWORD);
			return new DBProps(user, host, port, password);
			
		} catch (Exception e) {
			logger.error("Error reading properties file", e);
		}
		
		return null;
	}
	
	private static class DBProps {
		public String user;
		public String host;
		public String port;
		public String password;
		
		public DBProps(String user, String host, String port, String password) {
			this.user = user;
			this.host = host;
			this.port = port;
			this.password = password;
		}
	}


}
