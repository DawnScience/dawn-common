package org.dawb.common.services;

import uk.ac.diamond.scisoft.analysis.persistence.bean.function.FunctionBean;
import uk.ac.diamond.scisoft.analysis.persistence.bean.roi.ROIBean;

/**
 * This service can be called to save and/or load data from an hdf5 file.
 * 
 * @author wqk87977
 *
 */
public interface IPersistenceService {

	/**
	 * Method to read / load an IPersistentFile
	 *  
	 * @param filePath
	 * @return IPersistentFile
	 */
	public IPersistentFile getPersistentFile(String filePath) throws Exception;

	/**
	 * Method to save an IPersistentFile
	 * 
	 * @param filePath
	 * @return IPersistentFile
	 */
	public IPersistentFile createPersistentFile(String filePath) throws Exception;

	/**
	 * Returns a Roi bean given a JSon String
	 * @param json
	 * @return ROIBean
	 */
	public ROIBean unmarshallToROIBean(String json);

	/**
	 * Returns a JSON string given a ROIBean
	 * @param roi
	 * @return string
	 */
	public String marshallFromROIBean(ROIBean roi);

	/**
	 * Returns a FunctionBean given a JSON String
	 * @param json
	 * @return FunctionBean
	 */
	public FunctionBean unmarshallToFunctionBean(String json);

	/**
	 * Returns a JSON String given a Function bean
	 * @param function
	 * @return
	 */
	public String marshallFromFunctionBean(FunctionBean funtion);
}
