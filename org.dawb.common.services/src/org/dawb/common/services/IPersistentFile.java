package org.dawb.common.services;

import java.util.List;
import java.util.Map;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;

/**
 * An IPersistent File is defined by what can be saved in an HDF5 file.<br>
 * In this case ROIs, masks, and images.<br>
 * <br>
 * The resulting tree structure of the HDF5 file saved will be the following:<br>
 * <pre>
 *     Entry name        |   Class        |    Description          
 * -----------------------------------------------------------------
 * entry                 |   Group        |                         
 * entry/data            |   Group        |                         
 * entry/data/image      |   Dataset      |  64-bit floating-point  
 * entry/data/xaxis      |   Dataset      |  64-bit floating-point  
 * entry/data/yaxis      |   Dataset      |  64-bit floating-point  
 * entry/mask            |   Group        |                         
 * entry/mask/mask0      |   Dataset      |  8-bit integer          
 * entry/mask/mask0/name |   Attribute    |  String                 
 * entry/mask/mask1      |   Dataset      |  8-bit integer          
 * entry/mask/mask1/name |   Attribute    |  String                 
 * entry/mask/...        |   ...          |  ...                    
 * entry/region          |   Dataset      |  8-bit integer          
 * entry/region/JSON     |   Attribute    |  String                 
 * entry/function        |   Dataset      |  64-bit floating-point  
 * entry/function/JSON   |   Attribute    |  String                 
 * </pre>
 * The image can contain more than one Dataset with its corresponding axes.<br>
 * The region is an JSON serialized dataset. It can therefore contain more than one region.<br>
 * <br>
 * After using an IPersistentFile, the method {@link close()} needs to be called.
 * <br>
 * TODO although defined here as placeholders the functions are not yet implemented.
 * 
 * @author wqk87977
 *
 */
public interface IPersistentFile {

	/**
	 * Method to set a map of masks<br>
	 * This will write the data to entry/mask<br>
	 * If the masks already exist, they will be overwritten.<br>
	 * 
	 * @param Map
	 * @throws Exception 
	 */
	public void setMasks(Map<String, BooleanDataset> masks) throws Exception;

	/**
	 * Method to add a mask to the current map of masks<br>
	 * If the mask already exist, it will be overwritten.<br>
	 * 
	 * @param name
	 *           the name of the mask (must be unique)
	 * @param mask
	 *           the mask value as a BooleanDataset
	 * @param mon
	 * @throws Exception
	 */
	public void addMask(String name, BooleanDataset mask, IMonitor mon) throws Exception;

	/**
	 * Method to set a dataset: can be an image or a stack of images<br>
	 * This will write the data to entry/data<br>
	 * If the data already exist it will be overwritten.<br>
	 * 
	 * @param data
	 * @throws Exception 
	 */
	public void setData(AbstractDataset data) throws Exception;

	/**
	 * Method to set the axes<br>
	 * This will write the data to entry/data<br>
	 * If the axes already exist, they will be overwritten.<br>
	 * 
	 * @param axes
	 * @throws Exception 
	 */
	public void setAxes(List<AbstractDataset> axes) throws Exception;

	/**
	 * Method to set a map of ROIs<br>
	 * This will write the data to entry/region<br>
	 * If the ROIs already exist, they will be overwritten.<br>
	 * 
	 * @param Map
	 * @throws Exception 
	 */
	public void setROIs(Map<String, ROIBase> rois) throws Exception;

	/**
	 * Method to add a ROI to the current map of ROIs<br>
	 * Not implemented yet.
	 * @param String
	 * @param ROIBase
	 */
	public void addROI(String name, ROIBase roi, String roiType) throws Exception ;

	/**
	 * Method that reads a ROI from entry/region<br>
	 * 
	 * @param roiName
	 * @return ROIBase
	 * @throws Exception
	 *              is thrown if no correct entry is found in the file
	 */
	public ROIBase getROI(String roiName) throws Exception;

	/**
	 * Method that reads a map of ROIs from entry/region<br>
	 * 
	 * @param mon
	 * @return Map
	 * @throws Exception
	 *              is thrown if no correct entry is found in the file
	 */
	public Map<String, ROIBase> getROIs(IMonitor mon) throws Exception;

	/**
	 * Method that returns an ILazyDataset. Could be an image or a stack of images.<br>
	 * This method reads from entry/data.<br>
	 * 
	 * @param dataName
	 * @param mon
	 * @return ILazyDataset
	 * @throws Exception
	 *              is thrown if no correct entry is found in the file
	 */
	public ILazyDataset getData(String dataName, IMonitor mon) throws Exception;

	/**
	 * Method that reads a List of axes from entry/data.<br>
	 * 
	 * @param xAxisName
	 * @param yAxisName
	 * @param mon
	 * @return List<ILazyDataset>
	 * @throws Exception
	 *              is thrown if no correct entry is found in the file
	 */
	public List<ILazyDataset> getAxes(String xAxisName, String yAxisName, IMonitor mon) throws Exception;

	/**
	 * Method that reads a map of all available masks from entry/mask.<br>
	 * 
	 * @param mon
	 * @return Map
	 * @throws Exception
	 *              is thrown if no correct entry is found in the file
	 */
	public Map<String, BooleanDataset> getMasks(IMonitor mon) throws Exception;

	/**
	 * Method that reads a mask from entry/mask.<br>
	 * 
	 * @param maskName
	 * @param mon
	 * @return BooleanDataset
	 * @throws Exception
	 *              is thrown if no correct entry is found in the file
	 */
	public BooleanDataset getMask(String maskName, IMonitor mon) throws Exception;

	/**
	 * Close the Hierarchical file<br>
	 * This method needs to be called after the saving / writing of the file is done.
	 */
	public void close();

	/**
	 * Method that returns the list of data names saved in the file.<br>
	 * Reads from entry/data<br>
	 * 
	 * @param mon
	 * @return List<String>
	 * @throws Exception
	 *              is thrown if no correct entry is found in the file
	 */
	public List<String> getDataNames(IMonitor mon) throws Exception;

	/**
	 * Method that returns the list of mask names saved in the file.<br>
	 * Reads from entry/mask<br>
	 * 
	 * @param mon, may be null.
	 * @return List<String>
	 * @throws Exception
	 *              is thrown if no correct entry is found in the file
	 */
	public List<String> getMaskNames(IMonitor mon) throws Exception;

	/**
	 * Method that returns the list of roi names saved in the file.<br>
	 * Reads from entry/region.<br>
	 * 
	 * @param mon
	 * @return List<String>
	 * @throws Exception
	 *              is thrown if no correct entry is found in the file
	 */
	public List<String> getROINames(IMonitor mon) throws Exception;

	/**
	 * A method to test if a given ROIBase can be persisted using JSON
	 * @param roi
	 * @return true 
	 *            if this region can be saved in the persistence file.
	 * 
	 */
	public boolean isRegionSupported(ROIBase roi);
	
	/**
	 * Method to set diffraction metadata<br>
	 * This will write the data to entry/metadata<br>
	 * If the metadata already exists, they will be overwritten.<br>
	 * 
	 * @param Map
	 * @throws Exception 
	 */
	public void setDiffractionMetadata(IDiffractionMetadata metadata) throws Exception;
}
