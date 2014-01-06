/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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

	/**
	 * Returns an object bean given a JSon String
	 * @param json
	 * @return obj
	 */
	public Object unmarshal(String json) throws Exception;

	/**
	 * Returns a JSON string given an Object bean
	 * @param obj
	 * @return string
	 */
	public String marshal(Object obj);

	/**
	 * Returns a MOML file (serialised as a string) without the expression mode nodes
	 * @param filePath
	 *           The full file path of the MOML file
	 * @return string
	 *           updated XML tree as a string
	 */
	public String deleteExpressionModeFromMoml(String filePath);
}
