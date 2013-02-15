package org.dawnsci.persistence.internal;

import org.dawb.common.services.IPersistenceService;
import org.dawb.common.services.IPersistentFile;
import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;

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
}