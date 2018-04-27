/*
 * Copyright (c) 2012-2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dawnsci.persistence.ServiceLoader;
import org.dawnsci.persistence.json.JacksonMarshaller;
import org.eclipse.dawnsci.analysis.api.diffraction.IPowderCalibrationInfo;
import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.persistence.IJSonMarshaller;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.tree.TreeFactory;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.dawnsci.nexus.NexusUtils;
import org.eclipse.january.DatasetException;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.BooleanDataset;
import org.eclipse.january.dataset.Comparisons;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.january.dataset.RGBDataset;
import org.eclipse.january.dataset.ShortDataset;
import org.eclipse.january.dataset.Slice;
import org.eclipse.january.metadata.OriginMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

/**
 * Implementation of IPersistentFile<br>
 * 
 * This class is internal and not supposed to be used out of this bundle. 
 * 
 * @author Baha El-Kassaby
 *
 */
class PersistentFileImpl implements IPersistentFile {

	private static final Logger logger = LoggerFactory.getLogger(PersistentFileImpl.class);
	// Used for the persistence of operations and calibration
	private NexusFile file;
	private String filePath;

	/**
	 * 
	 * @param file
	 */
	public PersistentFileImpl(NexusFile file) throws Exception {
		this.file = file;
		this.filePath = file.getFilePath();

		init();
	}

	private void init() throws Exception {
		String currentSite = System.getenv("DAWN_SITE");
		if (currentSite == null || "".equals(currentSite)) {
			logger.debug("DAWN_SITE is not set, persistence layer defaulting to DLS in file meta information.");
			currentSite = PersistenceConstants.SITE;
		}
		// open file
		if (file == null)
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);
		try {
			file.openToWrite(true);
		} catch (IllegalStateException ie) {
			// do nothing if file is already open
			String msg = ie.getMessage();
			if (!msg.startsWith("File ") && !msg.endsWith(" is already open"))
				throw ie;
		}

		setSite(currentSite);
		setVersion(PersistenceConstants.CURRENT_VERSION);
	}

	/**
	 * Construct read-only persistent file
	 * @param filePath
	 */
	public PersistentFileImpl(String filePath) {
		this.filePath = filePath;
		try {
			this.file = ServiceLoader.getNexusFactory().newNexusFile(filePath);
			file.openToRead();
		} catch (Exception e) {
			logger.error("Error creating Nexus file:" + e);
		}
	}

	@Override
	public void setMasks(Map<String, ? extends IDataset> masks) throws Exception {
		if (file == null) {
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);
			file.openToWrite(true);
		}

		GroupNode group = createDataNode(file, PersistenceConstants.MASK_ENTRY);

		if (masks != null) {
			Set<String> names = masks.keySet();

			Iterator<String> it = names.iterator();
			while (it.hasNext()) {
				String name = it.next();
				BooleanDataset bd = (BooleanDataset) masks.get(name);
				// Inverse the dataset
				bd = Comparisons.logicalNot(bd);

				Dataset data = DatasetUtils.cast(bd, Dataset.INT8);

				try {
					data.setName(name);
					DataNode dNode = file.createData(group, data);
					file.addAttribute(dNode, TreeFactory.createAttribute("attribute", "SDS"));
				} catch (Exception de) {
					de.printStackTrace();
				}
			}
		}
	}

	@Override
	public void addMask(String name, IDataset mask, IMonitor mon) throws Exception {
		// Inverse the dataset
		mask = Comparisons.logicalNot((BooleanDataset) mask);
		Dataset id = DatasetUtils.cast((BooleanDataset) mask, Dataset.INT8);
		GroupNode group = createDataNode(file, PersistenceConstants.MASK_ENTRY);
		try {
			file.createData(group, name, id);
		} catch (NexusException ne) {
			file.addNode(group, name, TreeFactory.createDataNode(this.hashCode()));
		}
	}

	@Override
	public void setData(IDataset data, IDataset xAxis, IDataset yAxis) throws Exception {
		setData(data, new IDataset[] {yAxis, xAxis});
	}

	@Override
	public void setData(IDataset data, IDataset[] axes) throws Exception {
		writeH5Data(data, axes);
	}

	@Override
	public void setHistory(IDataset... sets) throws Exception {
		if (file == null)
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);

		GroupNode group = createDataNode(file, PersistenceConstants.HISTORY_ENTRY);
		int index = 0;
		for (IDataset data : sets) {
			index++;
			if (data != null) {
				String dataName = !data.getName().equals("") ? data.getName() : "history" + index;
				data.setName(dataName);
				file.createData(group, data);
			}
		}
	}

	@Override
	public void setAxes(IDataset... axes) throws Exception {
		writeH5Data(null, axes);
	}

	private final static String DATA = "data";

	/**
	 * Method to write image data (and axis) to an HDF5 file given a specific
	 * path entry to save the data.
	 * 
	 * @param data
	 * @param axes
	 * @throws Exception
	 */
	private void writeH5Data(IDataset data, final IDataset... axes) {
		// create nodes in separate try/catch clauses in order to try creating the next node even if the previous one wasn't successful
		GroupNode group = createDataNode(file, PersistenceConstants.DATA_ENTRY);

		// if there are data nodes already then move them to a new NXdata group
		if (group.getNumberOfDataNodes() > 0) {
			Map<String, DataNode> dMap = group.getDataNodeMap();
			Map<String, Dataset> oDataMap = new LinkedHashMap<>();
			for (String n : dMap.keySet()) {
				try {
					Dataset d = DatasetUtils.sliceAndConvertLazyDataset(dMap.get(n).getDataset());
					d.setName(n);
					oDataMap.put(n, d);
					file.removeNode(group, n);
				} catch (NexusException | DatasetException e) {
				}
			}
			try { // make new collection instead and move old data into new subgroup
				file.removeNode(PersistenceConstants.ENTRY, DATA);
				GroupNode nGroup = file.getGroup(PersistenceConstants.DATA_ENTRY, true);
				file.addAttribute(nGroup, TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.COLLECTION));
				String signal = group.getAttribute(NexusConstants.DATA_SIGNAL).getFirstElement();
				GroupNode sGroup = file.getGroup(nGroup, signal, NexusConstants.DATA, true);
				Iterator<String> it = group.getAttributeNameIterator();
				while (it.hasNext()) {
					String an = it.next();
					Attribute a;
					if (NexusConstants.DATA_SIGNAL.equals(an)) {
						a = TreeFactory.createAttribute(an, DATA);
					} else {
						a = group.getAttribute(an);
					}
					file.addAttribute(sGroup, a);
				}
				for (String n : oDataMap.keySet()) {
					Dataset d = oDataMap.get(n);
					if (signal.equals(n)) {
						d.setName(DATA);
					}
					file.createData(sGroup, d);
				}
				group = nGroup;
			} catch (NexusException e) {
			}
		}

		if (data != null) {
			try {
				String dataName = data.getName();
				if (dataName.isEmpty()) {
					dataName = DATA;
				}
				if (dataName.contains(":")) {
					dataName = dataName.replace(":", "_");
				}
				if (group.getNumberOfGroupNodes() > 0) {
					GroupNode sGroup = file.getGroup(group, dataName, NexusConstants.DATA, true);
					dataName = NexusConstants.DATA_SIGNAL;
					group = sGroup;
				}
				data.setName(dataName);

				boolean isRGB = false;
				// we create the dataset
				DataNode dNode = null;
				if (data instanceof RGBDataset) {
					data = ((RGBDataset) data).asNonCompoundDataset(true);
					data.setName(dataName);
					isRGB = true;
				}
				
				dNode = file.createData(group, data);
				
				// add attributes according to NeXus standard for NXdata
				file.addAttribute(group, TreeFactory.createAttribute("signal", dataName));
				if (isRGB) {
					file.addAttribute(dNode,  TreeFactory.createAttribute("interpretation","rgba-image"));
				}
				
			} catch (NexusException ne) {
				logger.warn(ne.getMessage());
			}
		}

		int nAxes = axes == null ? 0 : axes.length;
		int rank = data == null ? nAxes : data.getRank();
		String[] axisNames = new String[rank];
		Arrays.fill(axisNames, ".");

		for (int n = 0; n < rank; n++) {
			IDataset a = n < nAxes ? axes[n] : null;
			if (a != null) {
				String axisName = a.getName();
				if (axisName.isEmpty()) {
					if (rank == 1) {
						axisName = "X Axis";
					} else if (rank == 2) {
						axisName = n == 0 ? "Y Axis" : "X Axis";
					} else {
						axisName = "Axis " + n;
					}
					a.setName(axisName);
				}
				try {
					file.createData(group, a);
					file.addAttribute(group, TreeFactory.createAttribute(axisName + NexusConstants.DATA_INDICES_SUFFIX, n));
				} catch (NexusException ne) {
					logger.warn(ne.getMessage());
				}
				axisNames[n] = axisName;
			}
		}
		try {
			file.addAttribute(group, TreeFactory.createAttribute("axes", axisNames));
		} catch (NexusException ne) {
			logger.warn(ne.getMessage());
		}
		try {
			file.flush();
		} catch (NexusException ne) {
			logger.warn(ne.getMessage());
		}
	}

	@Override
	public void setROIs(Map<String, IROI> rois) throws Exception {
		if (file == null)
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);

		GroupNode group = createDataNode(file, PersistenceConstants.ROI_ENTRY);
		if (rois != null) {
			Iterator<String> it = rois.keySet().iterator();
			while (it.hasNext()) {
				String name = it.next();
				IROI roi = rois.get(name);
				writeRoi(group, PersistenceConstants.ROI_ENTRY, name, roi);
			}
		}
	}

	@Override
	public void addROI(String name, IROI roiBase) throws Exception {
		if (file == null)
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);
		GroupNode group = createDataNode(file, PersistenceConstants.ROI_ENTRY);
		writeRoi(group, PersistenceConstants.ROI_ENTRY, name, roiBase);
	}

	@Override
	public void setRegionAttribute(String regionName, String attributeName, String attributeValue) throws Exception {
		if ("JSON".equals(attributeName))
			throw new Exception("Cannot override the JSON attribute!");
		final DataNode node = file.getData(PersistenceConstants.ROI_ENTRY + Node.SEPARATOR + regionName);
		file.addAttribute(node, TreeFactory.createAttribute(attributeName, attributeValue));
	}

	@Override
	public String getRegionAttribute(String regionName, String attributeName) throws Exception {
		return file.getAttributeValue(PersistenceConstants.ROI_ENTRY + Node.SEPARATOR + regionName + "@" + attributeName);
	}

	/**
	 * Used to set the version of the API
	 * 
	 * @param version
	 * @throws Exception
	 */
	private void setVersion(String version) throws Exception {
		if (file == null)
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);
		GroupNode group = file.getGroup(PersistenceConstants.ENTRY, true);
		file.addAttribute(group, TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.ENTRY));
		// group = file.getGroup("/entry/Version", true);
		file.addAttribute(group, TreeFactory.createAttribute("Version", version));
	}

	@Override
	public void setSite(String site) throws Exception {
		if (file == null)
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);
		GroupNode group = file.getGroup(PersistenceConstants.ENTRY, true);
		file.addAttribute(group, TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.ENTRY));
		// group = file.getGroup("/entry/Site", true);
		file.addAttribute(group, TreeFactory.createAttribute("Site", site));
	}

	@Override
	public ILazyDataset getData(String dataName, IMonitor mon) throws Exception {
		if (file == null)
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);
		dataName = dataName.isEmpty() ? NexusConstants.DATA_DATA : dataName;
		GroupNode group = file.getGroup(PersistenceConstants.DATA_ENTRY, false);
		if (!group.containsDataNode(dataName)) {
			if (group.containsGroupNode(dataName)) {
				group = file.getGroup(group, dataName, null, false);
				dataName = DATA;
			}
			if (!group.containsDataNode(dataName)) {
				throw new IllegalArgumentException("No such dataset found: " + dataName);
			}
		}
		return group.getDataNode(dataName).getDataset();
	}

	/**
	 * Method to set datasets which persist history
	 * 
	 * @param data
	 * @throws Exception
	 */
	@Override
	public Map<String, ILazyDataset> getHistory(IMonitor mon) throws Exception {
		List<String> names = getNames(file, PersistenceConstants.HISTORY_ENTRY, mon);
		Map<String, ILazyDataset> sets = new HashMap<String, ILazyDataset>(names.size());
		Iterator<String> it = names.iterator();
		GroupNode group = file.getGroup(PersistenceConstants.HISTORY_ENTRY, false);
		while (it.hasNext()) {
			String name = it.next();
//			if (PersistenceConstants.HISTORY_ENTRY.endsWith(name)) {
				DataNode datanode = group.getDataNode(name);
				ILazyDataset data = datanode.getDataset();
				sets.put(PersistenceConstants.HISTORY_ENTRY + Node.SEPARATOR + name, data);
//			}
		}
		return sets;
	}

	@Override
	public List<ILazyDataset> getAxes(IMonitor mon, String dataName, String... axisNames) throws Exception {
		List<ILazyDataset> axes = new ArrayList<ILazyDataset>();

		GroupNode group = file.getGroup(PersistenceConstants.DATA_ENTRY, false);
		if (dataName == null) {
			if (group.getNumberOfGroupNodes() > 0) {
				group = group.getGroupNodes().get(0);
			}
		}
		for (String a : axisNames) {
			if (a == null || a.isEmpty()) { // TODO automatically get all axes
				throw new IllegalArgumentException();
			}
			ILazyDataset axis = group.getDataNode(a).getDataset();
			if (axis != null) {
				axes.add(axis);
			}
		}
		return axes;
	}

	@Override
	public BooleanDataset getMask(String maskName, IMonitor mon) throws Exception {
		if (file == null)
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);
		DataNode datanode = file.getData(PersistenceConstants.MASK_ENTRY + Node.SEPARATOR + maskName);
		if (datanode == null)
			throw new Exception("The mask with the name " + maskName + " is null");
		ILazyDataset lazy = datanode.getDataset();
		IDataset bdataset = lazy.getSlice(new Slice(0, lazy.getShape()[0], 1)).squeeze();
		BooleanDataset bd = (BooleanDataset) DatasetUtils.cast(bdataset, Dataset.BOOL);
		bd.setName(maskName);
		if (getVersionNumber() > 1) {
			// Inverse the dataset
			bd = Comparisons.logicalNot(bd);
		}
		return bd;
	}

	@Override
	public Map<String, IDataset> getMasks(IMonitor mon) throws Exception {
		if (file == null) {
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);
		}
		Map<String, IDataset> masks = new HashMap<String, IDataset>();
		List<String> names = getMaskNames(mon);
		Iterator<String> it = names.iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			DataNode datanode = file.getData(PersistenceConstants.MASK_ENTRY + Node.SEPARATOR + name);
			ILazyDataset data = datanode.getDataset();

			IDataset bdataset = data.getSlice(new Slice(0, data.getShape()[0], 1)).squeeze();
			BooleanDataset bd = (BooleanDataset) DatasetUtils.cast(bdataset, Dataset.BOOL);
			if (getVersionNumber() > 1) {
				// Inverse the dataset
				bd = Comparisons.logicalNot(bd);
			}
			masks.put(name, bd);
		}
		return masks;
	}

	private Double getVersionNumber() throws Exception {
		String version = getVersion();
		if (version == null)
			throw new Exception("No version number could be found in the file");
		String str = version.replace("[", "").replace("]", "");
		return Double.parseDouble(str);
	}

	@Override
	public IROI getROI(String roiName) throws Exception {
		String json = file.getAttributeValue(PersistenceConstants.ROI_ENTRY + Node.SEPARATOR + roiName + "@JSON");
		if (json == null)
			throw new Exception("Reading Exception: " + PersistenceConstants.ROI_ENTRY
					+ " entry does not exist in the file " + filePath);
		// JSON deserialization
		json = json.substring(1, json.length() - 1); // this is needed as somehow, the getAttribute adds [ ] around the json string...
		IROI roi = (IROI) ServiceLoader.getJSONMarshallerService().unmarshal(json, IROI.class);
		return roi;
	}

	@Override
	public Map<String, IROI> getROIs(IMonitor mon) throws Exception {
		Map<String, IROI> rois = new HashMap<String, IROI>();
		if (file == null)
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);
		List<String> names = getROINames(mon);
		if (names == null)
			return null;
		Iterator<String> it = names.iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			String json = file.getAttributeValue(PersistenceConstants.ROI_ENTRY + Node.SEPARATOR + name + "@JSON");
			IROI roi = (IROI) ServiceLoader.getJSONMarshallerService().unmarshal(json, IROI.class);
			rois.put(name, roi);
		}
		return rois;
	}

	@Override
	public List<String> getDataNames(IMonitor mon) throws Exception {
		return getNames(file, PersistenceConstants.DATA_ENTRY, mon);
	}

	@Override
	public List<String> getMaskNames(IMonitor mon) throws Exception {
		return getNames(file, PersistenceConstants.MASK_ENTRY, mon);
	}

	@Override
	public List<String> getROINames(IMonitor mon) throws Exception {
		return getNames(file, PersistenceConstants.ROI_ENTRY, mon);
	}

	private List<String> getNames(NexusFile f, String nodepath, IMonitor mon) throws Exception {
		List<String> names = null;
		GroupNode grp = f.getGroup(nodepath, false);
		if (grp == null)
			throw new Exception("Reading Exception: " + nodepath + " entry does not exist in the file " + filePath);
		Collection<String> children = grp.getNames();
		names = new ArrayList<String>(children);
		return names;
	}

	@Override
	public void setDiffractionMetadata(IDiffractionMetadata metadata) throws Exception {
		throw new RuntimeException("DO NOT SAVE METADATA THIS WAY!");

	}

	@Override
	public IDiffractionMetadata getDiffractionMetadata(IMonitor mon) throws Exception {
		throw new RuntimeException("DO NOT READ METADATA THIS WAY!");
	}

	@Override
	public String getVersion() throws Exception {
		return file.getAttributeValue(PersistenceConstants.ENTRY + "@Version");
	}

	@Override
	public String getSite() throws Exception {
		return file.getAttributeValue(PersistenceConstants.ENTRY + "@Site");
	}

	@Override
	public boolean containsData() {
		return isEntry(PersistenceConstants.DATA_ENTRY, null);
	}

	@Override
	public boolean containsMask() {
		return isEntry(PersistenceConstants.MASK_ENTRY, null);
	}

	@Override
	public boolean containsRegion() {
		return isEntry(PersistenceConstants.ROI_ENTRY, null);
	}

	@Override
	public boolean containsDiffractionMetadata() {
		return isEntry(PersistenceConstants.DIFFRACTIONMETADATA_ENTRY, null);
	}

	@Override
	public boolean containsFunction() {
		return isEntry(PersistenceConstants.FUNCTION_ENTRY, null);
	}

	private boolean isEntry(String entryPath, IMonitor mon) {
		GroupNode groupnode = null;
		try {
			groupnode = file.getGroup(entryPath, false);
			return groupnode != null;
		} catch (NexusException e) {
			logger.warn(e.getMessage());
		}
		return false;
	}

	/**
	 * Method to write rois data to an HDF5 file given a specific path entry to
	 * save the data.<br>
	 * The rois are serialised using GSON and are saved as JSON format in the
	 * HDF5 file.
	 * 
	 * @param rois
	 * @throws Exception
	 */
	private void writeRoi(GroupNode group, String parent, String name, IROI roi) throws Exception {
		String json = ServiceLoader.getJSONMarshallerService().marshal(roi);
		// we create the dataset
		Dataset data = DatasetFactory.createFromObject(IntegerDataset.class, (Object) new int[] { 0 });
		try {
			data.setName(name);
			DataNode dNode = file.createData(group, data);
			// we set the JSON attribute
			file.addAttribute(dNode, TreeFactory.createAttribute("JSON", json));
		} catch (Exception de) {
			de.printStackTrace();
		}
	}

	private GroupNode createDataNode(NexusFile file, String path) {
		return createNode(file, path, NexusConstants.DATA);
	}

	public static GroupNode createNode(NexusFile file, String path, String nxclass) {
		GroupNode group = null;
		try {
			group = file.getGroup("/entry", true);
			file.addAttribute(group, TreeFactory.createAttribute(NexusConstants.NXCLASS, NexusConstants.ENTRY));
			group = file.getGroup(path, true);
			file.addAttribute(group, TreeFactory.createAttribute(NexusConstants.NXCLASS, nxclass));
			return group;
		} catch (NexusException e) {
			e.printStackTrace();
		}
		return group;
	}

	/**
	 * Method to read image data (axes, masks, image) from an HDF5 file and
	 * returns an ILazyDataset
	 * 
	 * @return ILazyDataset
	 * @throws Exception
	 */
	private ILazyDataset readH5Data(IDataHolder dh, String dataName, String dataEntry) throws Exception {
		ILazyDataset ld = dh.getLazyDataset(dataEntry + "/" + dataName);
		if (ld == null)
			throw new Exception("Reading Exception: " + dataEntry + " entry does not exist in the file " + filePath);
		return ld;
	}

	/**
	 * Method to read mask data from an HDF5 file
	 * 
	 * @return BooleanDataset
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private BooleanDataset readH5Mask(IDataHolder dh, String maskName) throws Exception {
		ILazyDataset ld = dh.getLazyDataset(PersistenceConstants.MASK_ENTRY + "/" + maskName);
		return (BooleanDataset) DatasetUtils.cast(dh.getDataset(PersistenceConstants.MASK_ENTRY + "/" + maskName),
				Dataset.BOOL);
	}

	@Override
	public void close() {
		try {
			if (file != null) {
				file.close();
			}
		} catch (NexusException e) {
			logger.debug("Cannot close " + filePath, e);
		}
	}

	public NexusFile getFile() {
		return file;
	}

	public String getFilePath() {
		return filePath;
	}

	@Override
	public boolean isRegionSupported(IROI roi) {
		return ServiceLoader.getJSONMarshallerService().isObjMixInSupported(roi);
	}

	@Override
	public void setFunctions(Map<String, IFunction> functions) throws Exception {
		if (file == null)
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);
		GroupNode group = createDataNode(file, PersistenceConstants.FUNCTION_ENTRY);
		if (functions != null) {
			Iterator<String> it = functions.keySet().iterator();
			while (it.hasNext()) {
				String name = it.next();
				IFunction function = functions.get(name);
				writeFunction(group, PersistenceConstants.FUNCTION_ENTRY, name, function);
			}
		}
	}

	@Override
	public void addFunction(String name, IFunction function) throws Exception {
		if (file == null)
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);
		GroupNode group = createDataNode(file, PersistenceConstants.FUNCTION_ENTRY);
		writeFunction(group, PersistenceConstants.FUNCTION_ENTRY, name, function);
	}

	@Override
	public IFunction getFunction(String functionName) throws Exception {
		String json = file.getAttributeValue(PersistenceConstants.FUNCTION_ENTRY + Node.SEPARATOR + functionName);
		if (json == null)
			throw new Exception("Reading Exception: " + PersistenceConstants.FUNCTION_ENTRY
					+ " entry does not exist in the file " + filePath);
		// Deserialize the json back to a function
		// TODO replace JacksonMarshaller by IMarshallerService
		IFunction function = (IFunction) new JacksonMarshaller().unmarshal(json);
		return function;
	}

	@Override
	public Map<String, IFunction> getFunctions(IMonitor mon) throws Exception {
		Map<String, IFunction> functions = new HashMap<String, IFunction>();
		if (file == null)
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);
		IJSonMarshaller converter = new JacksonMarshaller();
		List<String> names = getFunctionNames(mon);
		Iterator<String> it = names.iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			String json = file.getAttributeValue(PersistenceConstants.FUNCTION_ENTRY + Node.SEPARATOR + name + "@JSON");
			// Deserialize the json back to a function
			// TODO replace JacksonMarshaller by IMarshallerService
			IFunction function = (IFunction) converter.unmarshal(json);
			functions.put(name, function);
		}
		return functions;
	}

	@Override
	public List<String> getFunctionNames(IMonitor mon) throws Exception {
		return getNames(file, PersistenceConstants.FUNCTION_ENTRY, mon);
	}

	private void writeFunction(GroupNode group, String parent, String name, IFunction function) throws Exception {
		// TODO replace JacksonMarshaller by IMarshallerService
		String json = new JacksonMarshaller().marshal(function);
		// we create the dataset
		Dataset data = DatasetFactory.createFromObject(IntegerDataset.class, (Object) new int[] { 0 });
		try {
			data.setName(name);
			DataNode dNode = file.createData(group, data);
			// we set the JSON attribute
			file.addAttribute(dNode, TreeFactory.createAttribute("JSON", json));
		} catch (Exception de) {
			de.printStackTrace();
		}
	}

	@Override
	public void setPowderCalibrationInformation(IDataset calibrationImage, IDiffractionMetadata metadata,
			IPowderCalibrationInfo info) throws Exception {
		if (file == null)
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);

		PersistSinglePowderCalibration.writeCalibrationToFile(file, calibrationImage, metadata, info);
	}

	@Override
	public void setOperations(IOperation<? extends IOperationModel, ? extends OperationData>... operations)
			throws Exception {
		
		try (NexusFile nexusFile = ServiceLoader.getNexusFactory().newNexusFile(filePath)) {
			nexusFile.openToWrite(true);
			GroupNode gn = PersistJsonOperationsNode.writeOperationsToNode(operations);
			nexusFile.getGroup("/entry", true);
			nexusFile.addNode("/entry/process", gn);
		}
	}

	@Override
	public IOperation<? extends IOperationModel, ? extends OperationData>[] getOperations() throws Exception {
		PersistJsonOperationsNode helper = new PersistJsonOperationsNode();
		return helper.readOperations(LoaderFactory.getData(filePath).getTree());
	}

	@Override
	public void setOperationDataOrigin(OriginMetadata origin) throws Exception {
		if (origin == null)
			return;
		if (file == null)
			file = ServiceLoader.getNexusFactory().newNexusFile(filePath);
		PersistJsonOperationHelper helper = new PersistJsonOperationHelper();
		helper.writeOriginalDataInformation(file, origin);
	}

	@Override
	public OriginMetadata getOperationDataOrigin() throws Exception {
		return null;
	}
}
