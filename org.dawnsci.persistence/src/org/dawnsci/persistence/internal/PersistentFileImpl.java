package org.dawnsci.persistence.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.h5.H5Datatype;

import org.dawb.common.services.IPersistentFile;
import org.dawb.gda.extensions.loaders.H5Utils;
import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.dawb.hdf5.Nexus;
import org.dawnsci.persistence.roi.ROIBean;
import org.dawnsci.persistence.roi.ROIBeanConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
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
	private final String DATA_ENTRY = "/entry/data";
	private final String MASK_ENTRY = "/entry/mask";
	private final String ROI_ENTRY = "/entry/region";

	/**
	 * For save
	 * @param file
	 */
	PersistentFileImpl(IHierarchicalDataFile file) {
		this.file = file;
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
	public void addMask(String name, BooleanDataset mask) {
		// TODO Auto-generated method stub
		
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
	public void addROI(String name, ROIBase roi) {
		// TODO Auto-generated method stub
		
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
		DataHolder dh = LoaderFactory.getData(filePath, true, mon);
		names = getNames(dh, ROI_ENTRY);
		
		return names;
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
		String fullEntry = DATA_ENTRY;
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
				file.setNexusAttribute(entry, Nexus.DATA);
				parent = entry;
			} else {
				entry = file.group(entries[i], parent);
				file.setNexusAttribute(entry, Nexus.ENTRY);
				parent = entry;
			}
		}
		if(data != null){
			String dataName = !data.getName().equals("") ? data.getName() : "data"; //max = (a > b) ? a : b;
			final Datatype      datatype = H5Utils.getDatatype(data);
			final long[]         shape = new long[data.getShape().length];
			for (int i = 0; i < shape.length; i++) shape[i] = data.getShape()[i];
			final Dataset dataset = file.createDataset(dataName,  datatype, shape, data.getBuffer(), parent);
			file.setNexusAttribute(dataset, Nexus.SDS);
		}
		if(xAxisData != null){
			String xAxisName = !xAxisData.getName().equals("") ? xAxisData.getName() : "X Axis";
			final Datatype      xDatatype = H5Utils.getDatatype(xAxisData);
			final long[]         xShape = new long[xAxisData.getShape().length];
			for (int i = 0; i < xShape.length; i++) xShape[i] = xAxisData.getShape()[i];
			final Dataset xDataset = file.createDataset(xAxisName,  xDatatype, xShape, xAxisData.getBuffer(), parent);
			file.setNexusAttribute(xDataset, Nexus.SDS);
		}

		if(yAxisData != null){
			String yAxisName = !yAxisData.getName().equals("") ? yAxisData.getName() : "Y Axis";
			final Datatype      yDatatype = H5Utils.getDatatype(yAxisData);
			final long[]         yShape = new long[yAxisData.getShape().length];
			for (int i = 0; i < yShape.length; i++) yShape[i] = yAxisData.getShape()[i];
			final Dataset yDataset = file.createDataset(yAxisName,  yDatatype, yShape, yAxisData.getBuffer(), parent);
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
		String fullEntry = MASK_ENTRY;
		String[] entries = fullEntry.split("/");
		entries = cleanArray(entries);
		Group parent = null;
		for (int i = 0; i < entries.length; i++) {
			Group entry = null;
			if (i == 0) {
				entry = file.group(entries[i]);
				file.setNexusAttribute(entry, Nexus.ENTRY);
				parent = entry;
			} else if (i == entries.length - 1) {
				entry = file.group(entries[i], parent);
				file.setNexusAttribute(entry, Nexus.DATA);
				parent = entry;
			} else {
				entry = file.group(entries[i], parent);
				file.setNexusAttribute(entry, Nexus.ENTRY);
				parent = entry;
			}
		}
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
				final Dataset dataset = file.createDataset(name, datatype, shape, id.getBuffer(), parent);
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

		if(file == null)
			file = HierarchicalDataFactory.getWriter(filePath);
		String fullEntry = ROI_ENTRY;
		String[] entries = fullEntry.split("/");
		entries = cleanArray(entries);
		Group parent = null;
		for (int i = 0; i < entries.length; i++) {
			Group entry = null;
			if (i == 0) {
				entry = file.group(entries[i]);
				file.setNexusAttribute(entry, Nexus.ENTRY);
				parent = entry;
			} else if (i == entries.length - 1) {
				entry = file.group(entries[i], parent);
				file.setNexusAttribute(entry, Nexus.DATA);
				parent = entry;
			} else {
				entry = file.group(entries[i], parent);
				file.setNexusAttribute(entry, Nexus.ENTRY);
				parent = entry;
			}
		}
		if (rois != null) {
			Set<String> names = rois.keySet();
			// JSON serialisation
			GsonBuilder builder = new GsonBuilder();
			//TODO: serialiser to be worked on...
			//builder.registerTypeAdapter(ROIBean.class, new ROISerializer());
			Gson gson = builder.create();

			Iterator<String> it = names.iterator();
			while(it.hasNext()){
				String name = it.next();
				ROIBase roi = rois.get(name);
				ROIBean roibean = ROIBeanConverter.ROIBaseToROIBean(name, roi);
				
				long[] dims = {1};
				
				String json = gson.toJson(roibean);
				// we create the dataset
				Dataset dat = file.createDataset(name, new H5Datatype(Datatype.CLASS_INTEGER, 4, Datatype.NATIVE, Datatype.NATIVE), dims, new int[]{0}, parent);
				// we set the JSON attribute
				file.setAttribute(dat, "JSON", json);
			}

		}
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
		BooleanDataset bd = (BooleanDataset)dh.getDataset(MASK_ENTRY+maskName);
		if(bd == null) throw new Exception("Reading Exception: " +MASK_ENTRY+ " entry does not exist in the file " + filePath);
		return bd;
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
			
			ShortDataset sdata = (ShortDataset)LoaderFactory.getDataSet(filePath, MASK_ENTRY+name, mon);
			BooleanDataset bd = (BooleanDataset) DatasetUtils.cast(sdata, AbstractDataset.BOOL);
			name = cleanArray(name.split("/"))[0]; //take the / out of the name
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
	
		String json = file.getAttributeValue(ROI_ENTRY+roiName);
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

		Iterator<String> it = names.iterator();
		while (it.hasNext()) {
			String name = (String) it.next();
			String json = file.getAttributeValue(ROI_ENTRY+name+"@JSON");
			json = json.substring(1, json.length()-1); // this is needed as somehow, the getAttribute adds [ ] around the json string...
			ROIBean roibean = ROIBeanConverter.getROIBeanfromJSON(gson, json);
			
			//convert the bean to roibase
			ROIBase roi = ROIBeanConverter.ROIBeanToROIBase(roibean);
			String[] str = cleanArray(name.split("/")); //take the / out of the name
			rois.put(str[0], roi);
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
				nameslist.add(names[i].split(dataEntry)[1]);
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
		// TODO FIXME Baha please could we have a method to determine if a give ROIBase is supported?
		return false;
	}
}
