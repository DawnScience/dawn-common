package org.dawb.common.services;

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

}
