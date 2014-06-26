package org.dawnsci.persistence.internal;

import javax.vecmath.Vector3d;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.h5.H5Datatype;

import org.dawb.hdf5.HierarchicalDataFileUtils;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.dawb.hdf5.Nexus;
import org.dawnsci.io.h5.H5Utils;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.IPowderCalibrationInfo;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;

public class PersistSinglePowderCalibration {

	public static final String DAWNCALIBRATIONID = "DAWNScience";
	public static final String DEFAULTDETECTORNAME = "detector";
	public static final String DEFAULTINSTRUMENTNAME = "/entry/instrument";
	public static final String DEFAULTDATANAME = "/entry/calibration_data";

	public static final String DATANAME = "data";
	
	public static void writeCalibrationToFile(IHierarchicalDataFile file, IDataset calibrationImage,
			IDiffractionMetadata metadata, IPowderCalibrationInfo info) throws Exception {
		
		String parent = HierarchicalDataFileUtils.createParentEntry(file, DEFAULTINSTRUMENTNAME,Nexus.INST);
		
		parent = file.group("detector", parent);
		file.setNexusAttribute(parent, Nexus.DETECT);
		
		//write in data
		final Datatype      datatype = H5Utils.getDatatype(calibrationImage);
		long[] shape = H5Utils.getLong(calibrationImage.getShape());
		final Dataset dataset = file.createDataset(DATANAME,  datatype, shape, ((AbstractDataset)calibrationImage).getBuffer(), parent);
		file.setNexusAttribute(dataset.getFullName(), Nexus.SDS);
		
		//make NXData in root as link to this data, make sure data has a link attribute back to the detector
		String group = file.group(DEFAULTDATANAME);
		file.setNexusAttribute(group, Nexus.DATA);
		file.createLink(group, dataset.getName(), dataset.getFullName());
		
		
		
		//pixel+beam centre information
		DetectorProperties detprop = metadata.getDetector2DProperties();
		
		PersistDiffractionMetadataUtils.writeDetectorProperties(file, parent, detprop);
		
		createCalibrationMethod(file,info,parent);
		DiffractionCrystalEnvironment de = metadata.getDiffractionCrystalEnvironment();
		PersistDiffractionMetadataUtils.writeWavelengthSample(file, info, de.getWavelength());
		
	}
	
	private static void createDependsOnTransformations(IHierarchicalDataFile file, DetectorProperties det, String parent) throws Exception {
		String transform = file.group("transformations",parent);
		file.setNexusAttribute(transform, "NXtransformations");
		
		DoubleDataset offset = new DoubleDataset(new double[3], new int[]{3});
		offset.setName("offset");
		
		Vector3d trans = (Vector3d) det.getOrigin().clone();
		double length = trans.length();
		trans.normalize();
		
		DoubleDataset vector = new DoubleDataset(new double[3], new int[]{3});
		trans.get(vector.getData());
		vector.setName("vector");
		//as passive, so -length
		Dataset ds = createNXtransformation("translation", "translation",vector,offset,"mm",".",length,file,transform);
		
		double[] norm = det.getNormalAnglesInDegrees();
		
		
		vector = new DoubleDataset(new double[3], new int[]{3});
		vector.set(1, 2);
		vector.setName("vector");
		
		Dataset roll = createNXtransformation("roll", "rotation",vector,offset,"degrees",ds.getFullName(),norm[2],file,transform);
		
		vector = new DoubleDataset(new double[3], new int[]{3});
		vector.set(1, 0);
		vector.setName("vector");
		
		Dataset pitch = createNXtransformation("pitch", "rotation",vector,offset,"degrees",roll.getFullName(),-norm[1],file,transform);
		
		vector = new DoubleDataset(new double[3], new int[]{3});
		vector.set(1, 1);
		vector.setName("vector");
		
		Dataset yaw = createNXtransformation("yaw", "rotation",vector,offset,"degrees",pitch.getFullName(),norm[0],file,transform);
	
		file.createDataset("depends_on", yaw.getFullName(), parent);
		
	}
	
	private static void createCalibrationMethod(IHierarchicalDataFile file, IPowderCalibrationInfo info, String parent) throws Exception {
		String note = file.group("calibration_method",parent);
		file.setNexusAttribute(note, "NXnote");
		file.createDataset("author", DAWNCALIBRATIONID, note);
		file.createDataset("description", info.getMethodDescription(), note);
		final Datatype      datatyped = H5Utils.getDatatype(info.getUsedDSpaceIndexValues());
		long[] shaped = H5Utils.getLong(info.getUsedDSpaceIndexValues().getShape());
		final Dataset datasetd = file.createDataset("d_space_index",  datatyped, shaped, ((AbstractDataset)info.getUsedDSpaceIndexValues()).getBuffer(), note);
		file.setNexusAttribute(datasetd.getFullName(), Nexus.SDS);
		
		createDoubleDataset("residual", info.getResidual(), file, note);
		
		if (info.getCitationInformation() == null || info.getCitationInformation().length == 0) return;
		
		String cite = file.group("reference", note);
		file.setNexusAttribute(cite, "NXcite");
		file.createDataset("description", info.getCitationInformation()[0], cite);
		file.createDataset("doi", info.getCitationInformation()[1], cite);
		file.createDataset("endnote", info.getCitationInformation()[2], cite);
		file.createDataset("bibtex", info.getCitationInformation()[3], cite);
		
	}
	
	private static Dataset createNXtransformation(String name, String type, AbstractDataset vector, AbstractDataset offset, String units, String depends_on, double value, IHierarchicalDataFile file, String group) throws Exception {
		
		Dataset ds = createDoubleDataset(name, value,file, group);
		setDatasetAttribute(vector, ds, file);
		setDatasetAttribute(offset, ds, file);
		file.setAttribute(ds.getFullName(), "units", units);
		file.setAttribute(ds.getFullName(), "transformation_type", type);
		file.setAttribute(ds.getFullName(), "depends_on", depends_on);
		return ds;
	}
	
	private static void setDatasetAttribute(AbstractDataset dataset, Dataset ds, IHierarchicalDataFile file) throws Exception {
		file.setDatasetAttribute(ds, dataset.getName(), H5Utils.getDatatype(dataset), H5Utils.getLong(dataset.getShape()), dataset.getBuffer());	
	}
	
	private static Dataset createDoubleDataset(String name, double val, IHierarchicalDataFile file, String group) throws Exception {
		
		return file.createDataset(name, new H5Datatype(Datatype.CLASS_FLOAT, 64/8, Datatype.NATIVE, Datatype.NATIVE),
				new long[]{1}, new double[]{val}, group);
		
	}
	
}
