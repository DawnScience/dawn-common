package org.dawnsci.persistence.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Matrix3d;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5Datatype;

import org.dawb.common.services.IPersistentFile;
import org.dawb.common.util.eclipse.BundleUtils;
import org.dawb.gda.extensions.loaders.H5LazyDataset;
import org.dawb.gda.extensions.loaders.H5Utils;
import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.dawb.hdf5.Nexus;
import org.dawb.hdf5.nexus.NexusUtils;
import org.dawnsci.persistence.Activator;
import org.dawnsci.persistence.function.FunctionBean;
import org.dawnsci.persistence.function.FunctionBeanConverter;
import org.dawnsci.persistence.metadata.diffraction.NexusDiffractionMetaReader;
import org.dawnsci.persistence.roi.ROIBean;
import org.dawnsci.persistence.roi.ROIBeanConverter;
import org.dawnsci.persistence.util.PersistenceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Implementation of IPersistentFile<br>
 * 
 * This class is internal and not supposed to be used out of this bundle.
 * 
 * @author wqk87977
 *
 */
class PersistentFileImpl implements IPersistentFile{
	private static final Logger logger = LoggerFactory.getLogger(PersistentFileImpl.class);
	private IHierarchicalDataFile file;
	private String filePath;
	private final String ENTRY = "/entry";
	private final String DATA_ENTRY = "/entry/data";
	private final String MASK_ENTRY = "/entry/mask";
	private final String ROI_ENTRY = "/entry/region";
	private final String FUNCTION_ENTRY = "/entry/function";
	private final String DIFFRACTIONMETADATA_ENTRY = "/entry/diffraction_metadata";

	/**
	 * Version of the API
	 */
	private final String VERSION_FILE = "/resource/persistence-version.txt";
	/**
	 * Site where the API is used
	 */
	private final String SITE_FILE = "/resource/persistence-site.txt";

	/**
	 * For save
	 * @param file
	 */
	PersistentFileImpl(IHierarchicalDataFile file) throws Exception{
		this.file = file;
		this.filePath = file.getPath();
		// set the site and version
		String sitePath = "", versionPath = "";
		if(Activator.getContext() == null){
			sitePath = System.getProperty("user.dir")+SITE_FILE;
			versionPath = System.getProperty("user.dir")+VERSION_FILE;
		} else {
			sitePath = BundleUtils.getBundleLocation(Activator.getContext().getBundle()).getAbsolutePath()+SITE_FILE;
			versionPath = BundleUtils.getBundleLocation(Activator.getContext().getBundle()).getAbsolutePath()+VERSION_FILE;
		}
		setSite(PersistenceUtils.readFile(sitePath));
		setVersion(PersistenceUtils.readFile(versionPath));
	}

	/**
	 * For read
	 * @param filePath
	 */
	PersistentFileImpl(String filePath) {
		this.filePath = filePath;
		try {
			this.file = HierarchicalDataFactory.getReader(filePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void setMasks(Map<String, BooleanDataset> masks) throws Exception {
		writeH5Mask(masks);
	}

	@Override
	public void addMask(String name, BooleanDataset mask, IMonitor mon) throws Exception{

		AbstractDataset id = DatasetUtils.cast(mask, AbstractDataset.INT8);
		//check if parent group exists
		Group parent = (Group)file.getData(MASK_ENTRY);
		if(parent == null) parent = createParentEntry(MASK_ENTRY);
		final Datatype datatype = H5Utils.getDatatype(id);
		final long[] shape = new long[id.getShape().length];

		for (int i = 0; i < shape.length; i++) shape[i] = id.getShape()[i];
		final Dataset dataset = file.replaceDataset(name, datatype, shape, id.getBuffer(), parent);
		file.setNexusAttribute(dataset, Nexus.SDS);
	}

	@Override
	public void setData(AbstractDataset data) throws Exception {
		writeH5Data(data, null, null);
	}

	@Override
	public void setAxes(List<AbstractDataset> axes) throws Exception{
		writeH5Data(null, axes.get(0), axes.get(1));
	}

	@Override
	public void setROIs(Map<String, ROIBase> rois) throws Exception {
		writeH5Rois(rois);
	}

	@Override
	public void addROI(String name, ROIBase roiBase) throws Exception {

		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);

		Group parent = createParentEntry(ROI_ENTRY);

		// JSON serialisation
		GsonBuilder builder = new GsonBuilder();
		//TODO: serialiser to be worked on...
		//builder.registerTypeAdapter(ROIBean.class, new ROISerializer());
		Gson gson = builder.create();

		writeRoi(file, parent, name, roiBase, gson);
	}

	@Override
	public void setRegionAttribute(String regionName, String attributeName, String attributeValue) throws Exception {
		if ("JSON".equals(attributeName)) throw new Exception("Cannot override the JSON attribute!");
		final HObject node = file.getData(ROI_ENTRY+"/"+regionName);
		file.setAttribute(node, attributeName, attributeValue);
	}

	@Override
	public String getRegionAttribute(String regionName, String attributeName) throws Exception{
		return file.getAttributeValue(ROI_ENTRY+"/"+regionName+"@"+attributeName);
	}

	/**
	 * Used to set the version of the API
	 * @param version
	 * @throws Exception
	 */
	private void setVersion(String version) throws Exception {
		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
		//check if parent group exists
		Group parent = (Group)file.getData(ENTRY);
		if(parent == null) parent = createParentEntry(ENTRY);
		file.setAttribute(parent, "Version", version);
	}

	@Override
	public void setSite(String site) throws Exception {
		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);
		//check if parent group exists
		Group parent = (Group)file.getData(ENTRY);
		if(parent == null) parent = createParentEntry(ENTRY);
		file.setAttribute(parent, "Site", site);
	}

	@Override
	public ILazyDataset getData(String dataName, IMonitor mon) throws Exception{
		ILazyDataset data = null;
		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
		dataName = !dataName.equals("") ? dataName : "data";
		data = readH5Data(dh, dataName, DATA_ENTRY);

		return data;
	}

	@Override
	public List<ILazyDataset> getAxes(String xAxisName, String yAxisName, IMonitor mon) throws Exception {
		List<ILazyDataset> axes = new ArrayList<ILazyDataset>();
		ILazyDataset xaxis = null, yaxis = null;

		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
		xAxisName = !xAxisName.equals("") ? xAxisName : "X Axis";
		xaxis = readH5Data(dh, xAxisName, DATA_ENTRY);
		if(xaxis != null)
			axes.add(xaxis);
		yAxisName = !yAxisName.equals("") ? yAxisName : "Y Axis";
		yaxis = readH5Data(dh, yAxisName, DATA_ENTRY);
		if(yaxis != null)
			axes.add(yaxis);

		return axes;
	}

	@Override
	public BooleanDataset getMask(String maskName, IMonitor mon) throws Exception {
		BooleanDataset mask = null;
		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
		mask = readH5Mask(dh, maskName);
		return mask;
	}

	@Override
	public Map<String, BooleanDataset> getMasks(IMonitor mon) throws Exception {
		Map<String, BooleanDataset> masks = null;
		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
		masks = readH5Masks(dh, mon);
		return masks;
	}

	@Override
	public ROIBase getROI(String roiName) throws Exception {
		ROIBase roi = null;
		roi = readH5ROI(roiName);

		return roi;
	}

	@Override
	public Map<String, ROIBase> getROIs(IMonitor mon) throws Exception {
		Map<String, ROIBase> rois = null;
		rois = readH5ROIs(mon);

		return rois;
	}

	@Override
	public List<String> getDataNames(IMonitor mon) throws Exception{
		List<String> names = null;
		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
		names = getNames(dh, DATA_ENTRY);

		return names;
	}

	@Override
	public List<String> getMaskNames(IMonitor mon) throws Exception{
		List<String> names = null;
		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
		names = getNames(dh, MASK_ENTRY);
		return names;
	}

	@Override
	public List<String> getROINames(IMonitor mon) throws Exception{
		List<String> names = null;

		IHierarchicalDataFile file = null;
		try {
			file      = HierarchicalDataFactory.getReader(getFilePath());
			Group grp = (Group)file.getData(ROI_ENTRY);
			if (grp==null) throw new Exception("Reading Exception: " +ROI_ENTRY+ " entry does not exist in the file " + filePath);

			List<HObject> children =  grp.getMemberList();
			if (names==null) names = new ArrayList<String>(children.size());
			for (HObject hObject : children) {
				names.add(hObject.getName());
			}
        } finally {
        	if (file!=null) file.close();
        }
		return names;
	}

	@Override
	public void setDiffractionMetadata(IDiffractionMetadata metadata) throws Exception {
		writeH5DiffractionMetadata(metadata);		
	}

	@Override
	public IDiffractionMetadata getDiffractionMetadata(IMonitor mon) throws Exception {
		//Reverse of the setMetadata.  Would be nice in the future to be able to use the
		//LoaderFactory but work needs to be done on loading specific metadata from nexus
		//files first

		return readH5DiffractionMetadata(mon);	
	}

	@Override
	public String getVersion() throws Exception {
		return file.getAttributeValue(ENTRY+"@Version");
	}

	@Override
	public String getSite() throws Exception {
		return file.getAttributeValue(ENTRY+"@Site");
	}

	@Override
	public boolean isEntry(String entryPath, IMonitor mon)  {
		DataHolder dh = null;
		try {
			dh = LoaderFactory.getData(filePath, true, mon);
		} catch (Exception e) {
			logger.debug("Error while loading the file: "+ e);
			e.printStackTrace();
		}
		if(dh != null){
			String[] names = dh.getNames();
			for (int i = 0; i < names.length; i++) {
				if(names[i].startsWith(entryPath)){
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Method to write image data (and axis) to an HDF5 file given a specific path entry to save the data.
	 * 
	 * @param data
	 * @param xAxisData
	 * @param yAxisData
	 * @throws Exception
	 */
	private void writeH5Data(final AbstractDataset data, 
			final AbstractDataset xAxisData, 
			final AbstractDataset yAxisData) throws Exception {

		if(file == null)
			file = HierarchicalDataFactory.getWriter(filePath);

		Group parent = createParentEntry(DATA_ENTRY);

		if(data != null){

			String dataName = !data.getName().equals("") ? data.getName() : "data";
			final Datatype      datatype = H5Utils.getDatatype(data);
			final long[]         shape = new long[data.getShape().length];
			for (int i = 0; i < shape.length; i++) shape[i] = data.getShape()[i];

			final Dataset dataset = file.replaceDataset(dataName,  datatype, shape, data.getBuffer(), parent);
			file.setNexusAttribute(dataset, Nexus.SDS);
		}
		if(xAxisData != null){
			String xAxisName = !xAxisData.getName().equals("") ? xAxisData.getName() : "X Axis";
			final Datatype      xDatatype = H5Utils.getDatatype(xAxisData);
			final long[]         xShape = new long[xAxisData.getShape().length];
			for (int i = 0; i < xShape.length; i++) xShape[i] = xAxisData.getShape()[i];

			final Dataset xDataset = file.replaceDataset(xAxisName,  xDatatype, xShape, xAxisData.getBuffer(), parent);
			file.setNexusAttribute(xDataset, Nexus.SDS);
		}

		if(yAxisData != null){
			String yAxisName = !yAxisData.getName().equals("") ? yAxisData.getName() : "Y Axis";
			final Datatype      yDatatype = H5Utils.getDatatype(yAxisData);
			final long[]         yShape = new long[yAxisData.getShape().length];
			for (int i = 0; i < yShape.length; i++) yShape[i] = yAxisData.getShape()[i];

			final Dataset yDataset = file.replaceDataset(yAxisName,  yDatatype, yShape, yAxisData.getBuffer(), parent);
			file.setNexusAttribute(yDataset, Nexus.SDS);
		}
	}

	// delete empty strings
	private static String[] cleanArray(String[] array){

		List<String> list = new ArrayList<String>();
		for (int i = 0; i < array.length; i++) {
			if(!array[i].isEmpty()){
				list.add(array[i]);
			}
		}
		String[] result = new String[list.size()];
		return list.toArray(result);
	}

	/**
	 * Method to write mask data to an HDF5 file given a specific path entry to save the data.
	 * 
	 * @param masks
	 * @throws Exception
	 */
	private void writeH5Mask(final Map<String, BooleanDataset> masks) throws Exception {

		if(file == null)
			file = HierarchicalDataFactory.getWriter(filePath);

		Group parent = createParentEntry(MASK_ENTRY);

		if (masks != null) {
			Set<String> names = masks.keySet();

			Iterator<String> it = names.iterator();
			while(it.hasNext()){
				String name = it.next();
				BooleanDataset bd = masks.get(name);
				AbstractDataset id = DatasetUtils.cast(bd, AbstractDataset.INT8);
				final Datatype datatype = H5Utils.getDatatype(id);
				final long[] shape = new long[id.getShape().length];
				for (int i = 0; i < shape.length; i++)
					shape[i] = id.getShape()[i];

				final Dataset dataset = file.replaceDataset(name, datatype, shape, id.getBuffer(), parent);
				file.setNexusAttribute(dataset, Nexus.SDS);
			}
		}
	}

	/**
	 * Method to write rois data to an HDF5 file given a specific path entry to save the data.<br>
	 * The rois are serialised using GSON and are saved as JSON format in the HDF5 file.
	 * 
	 * @param rois
	 * @throws Exception
	 */
	private void writeH5Rois(final Map<String, ROIBase> rois) throws Exception {

		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);

		Group parent = createParentEntry(ROI_ENTRY);

		if (rois != null) {
			// JSON serialisation
			GsonBuilder builder = new GsonBuilder();
			//TODO: serialiser to be worked on...
			//builder.registerTypeAdapter(ROIBean.class, new ROISerializer());
			Gson gson = builder.create();

			Iterator<String> it = rois.keySet().iterator();
			while(it.hasNext()){
				String name = it.next();
				ROIBase roi = rois.get(name);
				writeRoi(file, parent, name, roi, gson);
			}

		}
	}

	private HObject writeRoi(IHierarchicalDataFile file, 
			Group   parent,
			String  name,
			ROIBase roi,
			Gson    gson) throws Exception {

		ROIBean roibean = ROIBeanConverter.ROIBaseToROIBean(name, roi);

		long[] dims = {1};

		String json = gson.toJson(roibean);

		// we create the dataset
		Dataset dat = file.replaceDataset(name, new H5Datatype(Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, Datatype.NATIVE), dims, new int[]{0}, parent);
		// we set the JSON attribute
		file.setAttribute(dat, "JSON", json);

		return dat;
	}


	private Group createParentEntry(String fullEntry) throws Exception {
		return createParentEntry(fullEntry, Nexus.DATA);
	}

	private Group createParentEntry(String fullEntry, String nexusEntry) throws Exception{
		String[] entries = fullEntry.split("/");
		entries = cleanArray(entries);
		Group parent = null;
		for (int i = 0; i < entries.length; i++) {
			Group entry = null;
			if(i == 0){
				entry = file.group(entries[i]);
				file.setNexusAttribute(entry, Nexus.ENTRY);
				parent = entry;
			} else if(i == entries.length-1) {
				entry = file.group(entries[i], parent);
				file.setNexusAttribute(entry, nexusEntry);
				parent = entry;
			} else {
				entry = file.group(entries[i], parent);
				file.setNexusAttribute(entry, Nexus.ENTRY);
				parent = entry;
			}
		}
		return parent;
	}

	/**
	 * Method to read image data (axes, masks, image) from an HDF5 file and returns an ILazyDataset

	 * @return ILazyDataset
	 * @throws Exception 
	 */
	private ILazyDataset readH5Data(DataHolder dh, String dataName, String dataEntry) throws Exception{
		ILazyDataset ld = dh.getLazyDataset(dataEntry+"/"+dataName);
		if(ld == null) throw new Exception("Reading Exception: " +dataEntry+ " entry does not exist in the file " + filePath);
		return ld;
	}

	/**
	 * Method to read mask data from an HDF5 file
	 * @return BooleanDataset
	 * @throws Exception 
	 */
	private BooleanDataset readH5Mask(DataHolder dh, String maskName) throws Exception{
		ILazyDataset ld = dh.getLazyDataset(MASK_ENTRY+"/"+maskName);
		if (ld instanceof H5LazyDataset) {
			return (BooleanDataset)DatasetUtils.cast(((H5LazyDataset)ld).getCompleteData(null), AbstractDataset.BOOL);
		} else {
			return (BooleanDataset)DatasetUtils.cast(dh.getDataset(MASK_ENTRY+"/"+maskName), AbstractDataset.BOOL);
		}
	}

	/**
	 * Method to read mask data from an HDF5 file
	 * @return Map<String, BooleanDataset>
	 * @throws Exception 
	 */
	private Map<String, BooleanDataset> readH5Masks(DataHolder dh, IMonitor mon) throws Exception{
		Map<String, BooleanDataset> masks = new HashMap<String, BooleanDataset>();
		List<String> names = getMaskNames(mon);

		Iterator<String> it = names.iterator();
		while (it.hasNext()) {
			String name = (String) it.next();

			ShortDataset sdata = (ShortDataset)LoaderFactory.getDataSet(filePath, MASK_ENTRY+"/"+name, mon);
			BooleanDataset bd = (BooleanDataset) DatasetUtils.cast(sdata, AbstractDataset.BOOL);
			masks.put(name, bd);
		}
		return masks;
	}

	/**
	 * Method to read roi data from an HDF5 file
	 * 
	 * @return ROIBase
	 * @throws Exception 
	 */
	private ROIBase readH5ROI(String roiName) throws Exception{

		String json = file.getAttributeValue(ROI_ENTRY+"/"+roiName);
		if(json == null) throw new Exception("Reading Exception: " +ROI_ENTRY+ " entry does not exist in the file " + filePath);
		// JSON deserialization
		GsonBuilder builder = new GsonBuilder();
		//TODO: deserialiser to be worked on...
		//builder.registerTypeAdapter(ROIBean.class, new ROIDeserializer());
		Gson gson = builder.create();
		ROIBean roibean = gson.fromJson(json, ROIBean.class);
		//convert the bean to roibase
		ROIBase roi = ROIBeanConverter.ROIBeanToROIBase(roibean);

		return roi;
	}

	/**
	 * Method to read roi data from an HDF5 file
	 * @return Map<String, ROIBase>
	 * @throws Exception 
	 */
	private Map<String, ROIBase> readH5ROIs(IMonitor mon) throws Exception{
		Map<String, ROIBase> rois = new HashMap<String, ROIBase>();
		if(file == null)
			file = HierarchicalDataFactory.getReader(filePath);
		// JSON deserialization
		GsonBuilder builder = new GsonBuilder();
		//TODO: deserialiser to be worked on...
		//builder.registerTypeAdapter(ROIBean.class, new ROIDeserializer());
		Gson gson = builder.create();

		List<String> names = getROINames(mon);
		
		if (names== null) return null;
		
		Iterator<String> it = names.iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			String json = file.getAttributeValue(ROI_ENTRY+"/"+name+"@JSON");
			json = json.substring(1, json.length()-1); // this is needed as somehow, the getAttribute adds [ ] around the json string...
			ROIBean roibean = ROIBeanConverter.getROIBeanfromJSON(gson, json);

			//convert the bean to roibase
			ROIBase roi = ROIBeanConverter.ROIBeanToROIBase(roibean);
			rois.put(name, roi);
		}

		return rois;
	}

	/**
	 * Method to retrieve all names in dataEntry
	 * @param dataEntry
	 * @param mon
	 * @return List<String>
	 * @throws Exception
	 */
	private List<String> getNames(DataHolder dh, String dataEntry) throws Exception{
		List<String> nameslist = new ArrayList<String>();
		String[] names = dh.getNames();
		for (int i = 0; i < names.length; i++) {
			if(names[i].startsWith(dataEntry)){
				nameslist.add(names[i].substring(dataEntry.length()+1));
			}
		}
		if (nameslist.isEmpty()) throw new Exception("Reading Exception: " +dataEntry+ " entry does not exist in the file " + filePath);

		return nameslist;
	}

	@Override
	public void close() {
		try {
			if (file != null) {
				file.close();
			}
		} catch (Exception e) {
			logger.debug("Cannot close " + filePath, e);
		}
	}

	public IHierarchicalDataFile getFile(){
		return file;
	}

	public String getFilePath(){
		return filePath;
	}

	@Override
	public boolean isRegionSupported(ROIBase roi) {
		return ROIBeanConverter.isROISupported(roi);
	}

	@Override
	public void setFunctions(Map<String, AFunction> functions) throws Exception {
		writeH5Functions(functions);
	}

	@Override
	public void addFunction(String name, AFunction function)
			throws Exception {
		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);

		Group parent = createParentEntry(FUNCTION_ENTRY);

		// JSON serialisation
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		writeFunction(file, parent, name, function, gson);

	}

	@Override
	public AFunction getFunction(String functionName) throws Exception {
		AFunction function = null;
		function = readH5Function(functionName);
		return function;
	}

	@Override
	public Map<String, AFunction> getFunctions(IMonitor mon) throws Exception {
		Map<String, AFunction> functions = null;
		functions = readH5Functions(mon);
		return functions;
	}

	@Override
	public List<String> getFunctionNames(IMonitor mon) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Method to write function data to an HDF5 file given a specific path entry to save the data.<br>
	 * The rois are serialised using GSON and are saved as JSON format in the HDF5 file.
	 * 
	 * @param functions
	 * @throws Exception
	 */
	private void writeH5Functions(final Map<String, AFunction> functions) throws Exception {

		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);

		Group parent = createParentEntry(FUNCTION_ENTRY);

		if (functions != null) {
			// JSON serialisation
			GsonBuilder builder = new GsonBuilder();
			Gson gson = builder.create();

			Iterator<String> it = functions.keySet().iterator();
			while(it.hasNext()){
				String name = it.next();
				AFunction roi = functions.get(name);
				writeFunction(file, parent, name, roi, gson);
			}
		}
	}

	private HObject writeFunction(IHierarchicalDataFile file, Group parent,
			String name, AFunction function, Gson gson) throws Exception {

		FunctionBean fBean = FunctionBeanConverter.AFunctionToFunctionBean(name, function);

		long[] dims = {1};

		String json = gson.toJson(fBean);

		// we create the dataset
		Dataset dat = file.replaceDataset(name, new H5Datatype(Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, Datatype.NATIVE), dims, new int[]{0}, parent);
		// we set the JSON attribute
		file.setAttribute(dat, "JSON", json);

		return dat;
	}

	/**
	 * Method to read function data from an HDF5 file
	 * 
	 * @return AFunction
	 * @throws Exception 
	 */
	private AFunction readH5Function(String functionName) throws Exception{

		String json = file.getAttributeValue(FUNCTION_ENTRY+"/"+functionName);
		if(json == null) throw new Exception("Reading Exception: " +FUNCTION_ENTRY+ " entry does not exist in the file " + filePath);
		// JSON deserialization
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		FunctionBean fBean = FunctionBeanConverter.getFunctionBeanfromJSON(gson, json);
		//convert the bean to AFunction
		AFunction function = FunctionBeanConverter.FunctionBeanToAFunction(fBean);

		return function;
	}

	/**
	 * Method to read function data from an HDF5 file
	 * @return Map<String, AFunction>
	 * @throws Exception 
	 */
	private Map<String, AFunction> readH5Functions(IMonitor mon) throws Exception{
		Map<String, AFunction> functions = new HashMap<String, AFunction>();
		if(file == null)
			file = HierarchicalDataFactory.getReader(filePath);
		// JSON deserialization
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		List<String> names = getROINames(mon);

		Iterator<String> it = names.iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			String json = file.getAttributeValue(FUNCTION_ENTRY+"/"+name+"@JSON");
			json = json.substring(1, json.length()-1); // this is needed as somehow, the getAttribute adds [ ] around the json string...
			FunctionBean fBean = FunctionBeanConverter.getFunctionBeanfromJSON(gson, json);

			//convert the bean to AFunction
			AFunction function = FunctionBeanConverter.FunctionBeanToAFunction(fBean);
			functions.put(name, function);
		}

		return functions;
	}

	private void writeH5DiffractionMetadata(IDiffractionMetadata metadata) throws Exception {
		if (file == null) file = HierarchicalDataFactory.getWriter(filePath);

		Group parent = createParentEntry(DIFFRACTIONMETADATA_ENTRY,Nexus.DETECT);

		//TODO do we want to be an NX_detector?
		//TODO should existing diffraction metadata node be deleted?
		DetectorProperties detprop = metadata.getDetector2DProperties();

		H5Datatype intType = new H5Datatype(Datatype.CLASS_INTEGER, 32/8, Datatype.NATIVE, Datatype.NATIVE);
		H5Datatype doubleType = new H5Datatype(Datatype.CLASS_FLOAT, 64/8, Datatype.NATIVE, Datatype.NATIVE);

		final Dataset nXPix = file.replaceDataset("x_pixel_number", intType, new long[] {1}, new int[]{detprop.getPx()}, parent);
		file.setAttribute(nXPix,NexusUtils.UNIT, "pixels");
		final Dataset nYPix = file.replaceDataset("y_pixel_number", intType, new long[] {1}, new int[]{detprop.getPy()}, parent);
		file.setAttribute(nYPix,NexusUtils.UNIT , "pixels");

		final Dataset sXPix = file.replaceDataset("x_pixel_size", doubleType, new long[] {1}, new double[]{detprop.getHPxSize()}, parent);
		file.setAttribute(sXPix, NexusUtils.UNIT, "mm");
		final Dataset sYPix = file.replaceDataset("y_pixel_size", doubleType, new long[] {1}, new double[]{detprop.getVPxSize()}, parent);
		file.setAttribute(sYPix, NexusUtils.UNIT, "mm");

		double[] beamVector = new double[3];
		detprop.getBeamVector().get(beamVector);
		file.replaceDataset("beam_vector", doubleType, new long[] {3}, beamVector, parent);
		
		double[] beamCentre = detprop.getBeamCentreCoords();
		
		double dist = detprop.getDetectorDistance();
		
		final Dataset centre = file.replaceDataset("beam_centre", doubleType, new long[] {2}, beamCentre, parent);
		file.setAttribute(centre,NexusUtils.UNIT, "pixels");
		final Dataset distance = file.replaceDataset("distance", doubleType, new long[] {1}, new double[] {dist}, parent);
		file.setAttribute(distance, NexusUtils.UNIT, "mm");
		
		Matrix3d or = detprop.getOrientation();
		double[] orientation = new double[] {or.m00 ,or.m01, or.m02,
				or.m10, or.m11, or.m12,
				or.m20, or.m21, or.m22};

		file.replaceDataset("detector_orientation", doubleType, new long[] {9}, orientation, parent);

		DiffractionCrystalEnvironment crysenv = metadata.getDiffractionCrystalEnvironment();

		final Dataset energy = file.replaceDataset("energy", doubleType, new long[] {1}, new double[]{crysenv.getWavelength()}, parent);
		file.setAttribute(energy, NexusUtils.UNIT, "Angstrom");

		final Dataset count = file.replaceDataset("count_time", doubleType, new long[] {1}, new double[]{crysenv.getExposureTime()}, parent);
		file.setAttribute(count, NexusUtils.UNIT, "s");

		final Dataset phi_start = file.createDataset("phi_start", doubleType, new long[] {1}, new double[]{crysenv.getPhiStart()}, parent);
		file.setAttribute(phi_start, NexusUtils.UNIT, "degrees");

		final Dataset phi_range = file.replaceDataset("phi_range", doubleType, new long[] {1}, new double[]{crysenv.getPhiRange()}, parent);
		file.setAttribute(phi_range, NexusUtils.UNIT, "degrees");
	}

	private IDiffractionMetadata readH5DiffractionMetadata(IMonitor mon) throws Exception {
		
		NexusDiffractionMetaReader nexusDiffReader = new NexusDiffractionMetaReader(filePath);
		
		return nexusDiffReader.getDiffractionMetadataFromNexus(null);
	}
}
