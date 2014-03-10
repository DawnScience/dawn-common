/*-
 * Copyright 2013 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dawnsci.persistence.internal;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.dawnsci.persistence.json.JacksonMarshaller;
import org.dawnsci.persistence.workflow.xml.MomlUpdater;

/**
 * Implementation of IPersistenceService<br>
 * 
 * This class is internal and not supposed to be used out of this bundle.
 * 
 * @author wqk87977
 *
 */
public class PersistenceServiceImpl implements IPersistenceService{

	/**
	 * Default Constructor
	 */
	public PersistenceServiceImpl(){
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
	public String marshal(Object obj) {
		return new JacksonMarshaller().marshal(obj);
	}

	@Override
	public String deleteExpressionModeFromMoml(String filePath) {
		return MomlUpdater.updateMoml(filePath);
	}
}