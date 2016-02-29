/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.internal;

import org.dawnsci.persistence.json.JacksonMarshaller;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentFile;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentNodeFactory;
import org.eclipse.dawnsci.hdf5.HierarchicalDataFactory;
import org.eclipse.dawnsci.hdf5.IHierarchicalDataFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Implementation of IPersistenceService<br>
 * 
 * This class is internal and not supposed to be used out of this bundle.
 * 
 * @author wqk87977
 *
 */
public class PersistenceServiceImpl implements IPersistenceService{

	private final Logger logger = LoggerFactory.getLogger(PersistenceServiceImpl.class);

	static {
		System.out.println("Starting persistence service");
	}
	/**
	 * Default Constructor
	 */
	public PersistenceServiceImpl(){
		// Do nothing
	}

	@Override
	public IPersistentFile getPersistentFile(String filePath) throws Exception{
		return new PersistentFileImpl(filePath);
	}

	@Override
	public IPersistentFile createPersistentFile(String filePath) throws Exception {
		IHierarchicalDataFile file = HierarchicalDataFactory.getWriter(filePath);
		return new PersistentFileImpl(file);
	}
	
	@Override
	public IPersistentFile createPersistentFile(Object file) throws Exception {
		return new PersistentFileImpl((IHierarchicalDataFile)file);
	}


	@Override
	public Object unmarshal(String json) throws Exception {
		return new JacksonMarshaller().unmarshal(json);
	}
	
	@Override
	public <T extends Object> T unmarshal(String json, Class<T> clazz) throws Exception {
		return new ObjectMapper().readValue(json, clazz);
	}

	@Override
	public String marshal(Object obj) throws Exception{
		try {
			return new JacksonMarshaller().marshal(obj);
		} catch (Exception e) {
			logger.error("Error while marshalling object " + obj + " : " + e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public IPersistentNodeFactory getPersistentNodeFactory() {
		return new PersistenceNodeFactory();
	}
}
