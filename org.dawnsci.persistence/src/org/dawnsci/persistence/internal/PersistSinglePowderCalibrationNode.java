package org.dawnsci.persistence.internal;

import org.eclipse.dawnsci.analysis.api.diffraction.IPowderCalibrationInfo;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.dataset.IDataset;

import uk.ac.diamond.scisoft.analysis.io.NexusTreeUtils;

public class PersistSinglePowderCalibrationNode {

	public static GroupNode persistSingleCalibration(IDataset calibrationImage,
			IDiffractionMetadata metadata, IPowderCalibrationInfo info) {
		
		GroupNode entry = NexusUtils.createNXclass(NexusConstants.ENTRY);
		
		GroupNode instrument = NexusUtils.createNXclass(NexusConstants.INSTRUMENT);
		
		entry.addGroupNode("instrument", instrument);
		
		GroupNode detector = NexusTreeUtils.createNXDetector(metadata.getDetector2DProperties());
		instrument.addGroupNode("detector", detector);
		
		if (info != null) {
			detector.addGroupNode("calibration_method", createCalibrationMethod(info));
		}
		
		entry.addGroupNode("calibration_sample", createCalibrationSample(info, metadata.getDiffractionCrystalEnvironment().getWavelength()));
		if (calibrationImage != null) {
			DataNode data = NexusTreeUtils.createDataNode("data", calibrationImage, null);
			
			detector.addDataNode("data", data);
			
			GroupNode nxData = NexusUtils.createNXclass(NexusConstants.DATA);
			
			nxData.addDataNode("data", data);
			entry.addGroupNode("calibration_data", nxData);
		}
		return entry;
	}
	
	private static GroupNode createCalibrationMethod(IPowderCalibrationInfo info) {
		GroupNode note = NexusUtils.createNXclass(NexusConstants.NOTE);
		note.addDataNode("author",NexusTreeUtils.createDataNode("author", "DAWNScience", null));
		note.addDataNode("description",NexusTreeUtils.createDataNode("description", info.getMethodDescription(), null));
		note.addDataNode("d_space_index",NexusTreeUtils.createDataNode("description", info.getUsedDSpaceIndexValues(), null));
		note.addDataNode("residual",NexusTreeUtils.createDataNode("residual", info.getResidual(), null));
		
		if (info.getCitationInformation() == null || info.getCitationInformation().length == 0) return note;
		
		GroupNode cite = NexusUtils.createNXclass(NexusConstants.CITE);
		note.addGroupNode("reference", cite);
		cite.addDataNode("description",NexusTreeUtils.createDataNode("description", info.getCitationInformation()[0], null));
		cite.addDataNode("doi",NexusTreeUtils.createDataNode("doi", info.getCitationInformation()[1], null));
		cite.addDataNode("endnote",NexusTreeUtils.createDataNode("endnote", info.getCitationInformation()[2], null));
		cite.addDataNode("bibtex",NexusTreeUtils.createDataNode("bibtex", info.getCitationInformation()[2], null));

		return note;
	}
	
	private static GroupNode createCalibrationSample(IPowderCalibrationInfo info, double wavelength) {
		GroupNode sample = NexusUtils.createNXclass(NexusConstants.SAMPLE);
		
		GroupNode beam = NexusUtils.createNXclass(NexusConstants.BEAM);
		beam.addDataNode("incident_wavelength",NexusTreeUtils.createDataNode("incident_wavelength", wavelength, "angstrom"));
		
		sample.addGroupNode("beam", beam);
		
		if (info != null) {
			sample.addDataNode("d_space",NexusTreeUtils.createDataNode("d_space", info.getCalibrantDSpaceValues(), null));
			sample.addDataNode("name",NexusTreeUtils.createDataNode("name", info.getCalibrantName(), null));
			sample.addDataNode("type",NexusTreeUtils.createDataNode("type", "calibration sample", null));
		}
		
		return sample;
	}
}
