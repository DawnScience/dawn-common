package org.dawnsci.persistence.internal;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.diffraction.IPowderCalibrationInfo;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.analysis.tree.impl.AttributeImpl;
import org.eclipse.dawnsci.analysis.tree.impl.GroupNodeImpl;
import org.eclipse.dawnsci.hdf.object.Nexus;

import uk.ac.diamond.scisoft.analysis.io.NexusTreeUtils;

public class PersistSinglePowderCalibrationNode {

	public static GroupNode persistSingleCalibration(IDataset calibrationImage,
			IDiffractionMetadata metadata, IPowderCalibrationInfo info) {
		
		GroupNodeImpl entry = new GroupNodeImpl(1);
		entry.addAttribute(new AttributeImpl("NX_class", Nexus.ENTRY));
		
		GroupNode instrument = new GroupNodeImpl(1);
		instrument.addAttribute(new AttributeImpl("NX_class", Nexus.INST));
		
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
			
			GroupNode nxData = TreeFactory.createGroupNode(0);
			nxData.addAttribute(TreeFactory.createAttribute("NX_class", "NXdata"));
			
			nxData.addDataNode("data", data);
			entry.addGroupNode("calibration_data", nxData);
		}
		 

		return entry;
	}
	
	
	private static GroupNode createCalibrationMethod(IPowderCalibrationInfo info) {
		GroupNode note = TreeFactory.createGroupNode(0);
		note.addAttribute(TreeFactory.createAttribute("NX_class", "NXnote"));
		note.addDataNode("author",NexusTreeUtils.createDataNode("author", "DAWNScience", null));
		note.addDataNode("description",NexusTreeUtils.createDataNode("description", info.getMethodDescription(), null));
		note.addDataNode("d_space_index",NexusTreeUtils.createDataNode("description", info.getUsedDSpaceIndexValues(), null));
		note.addDataNode("residual",NexusTreeUtils.createDataNode("residual", info.getResidual(), null));
		
		if (info.getCitationInformation() == null || info.getCitationInformation().length == 0) return note;
		
		GroupNode cite = TreeFactory.createGroupNode(0);
		cite.addAttribute(TreeFactory.createAttribute("NX_class", "NXcite"));
		note.addGroupNode("reference", cite);
		cite.addDataNode("description",NexusTreeUtils.createDataNode("description", info.getCitationInformation()[0], null));
		cite.addDataNode("doi",NexusTreeUtils.createDataNode("doi", info.getCitationInformation()[1], null));
		cite.addDataNode("endnote",NexusTreeUtils.createDataNode("endnote", info.getCitationInformation()[2], null));
		cite.addDataNode("bibtex",NexusTreeUtils.createDataNode("bibtex", info.getCitationInformation()[2], null));

		return note;
	}
	
	private static GroupNode createCalibrationSample(IPowderCalibrationInfo info, double wavelength) {
		GroupNode sample = TreeFactory.createGroupNode(0);
		sample.addAttribute(TreeFactory.createAttribute("NX_class", "NXsample"));
		
		
		GroupNode beam = TreeFactory.createGroupNode(0);
		beam.addAttribute(TreeFactory.createAttribute("NX_class", NexusTreeUtils.NX_BEAM));
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
