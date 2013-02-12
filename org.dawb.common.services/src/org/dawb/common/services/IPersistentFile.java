package org.dawb.common.services;

import java.util.List;
import java.util.Map;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
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
	 * Method to set a map of masks
	 * @param Map
	 */
	public void setMasks(Map<String, BooleanDataset> masks);

	/**
	 * Method to add a mask to the current map of masks<br>
	 * Not implemented yet.
	 * @param String
	 * @param BooleanDataset
	 */
	public void addMask(String name, BooleanDataset mask);

	/**
	 * Method to set a dataset: can be an image or a stack of images<br>
	 * 
	 * @param data
	 */
	public void setData(AbstractDataset data);

	/**
	 * Method to set the axes
	 * @param axes
	 */
	public void setAxes(List<AbstractDataset> axes);

	/**
	 * Method to set a map of ROIs
	 * @param Map
	 */
	public void setROIs(Map<String, ROIBase> rois);

	/**
	 * Method to add a ROI to the current map of ROIs<br>
	 * Not implemented yet.
	 * @param String
	 * @param ROIBase
	 */
	public void addROI(String name, ROIBase roi);

	/**
	 * Method that returns an ROI given its name
	 * @param roiName
	 * @return ROIBase
	 */
	public ROIBase getROI(String roiName);

	/**
	 * Method that returns a map of ROIs
	 * @param mon
	 * @return Map
	 */
	public Map<String, ROIBase> getROIs(IMonitor mon);

	/**
	 * Method that returns an ILazyDataset. Could be an image or a stack of images.
	 * @param dataName
	 * @param mon
	 * @return ILazyDataset
	 */
	public ILazyDataset getData(String dataName, IMonitor mon);

	/**
	 * Method that returns a List of axes.
	 * @param xAxisName
	 * @param yAxisName
	 * @param mon
	 * @return List<ILazyDataset>
	 */
	public List<ILazyDataset> getAxes(String xAxisName, String yAxisName, IMonitor mon);

	/**
	 * Method that returns a map of all available masks in the file.
	 * @param mon
	 * @return Map
	 */
	public Map<String, BooleanDataset> getMasks(IMonitor mon);

	/**
	 * Method that returns a mask with a given name
	 * @param maskName
	 * @param mon
	 * @return BooleanDataset
	 */
	public BooleanDataset getMask(String maskName, IMonitor mon);

	/**
	 * Close the Hierarchical file<br>
	 * This method needs to be called after the saving / writing of the file is done.
	 */
	public void close();

	/**
	 * Method that returns the list of data names saved in the file.
	 * @param mon
	 * @return List<String>
	 */
	public List<String> getDataNames(IMonitor mon);

	/**
	 * Method that returns the list of mask names saved in the file.
	 * @param mon
	 * @return List<String>
	 */
	public List<String> getMaskNames(IMonitor mon);

	/**
	 * Method that returns the list of roi names saved in the file.
	 * @param mon
	 * @return List<String>
	 */
	public List<String> getROINames(IMonitor mon);
}
