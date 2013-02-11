package org.dawnsci.persistence;

import org.dawb.common.services.IPersistenceService;
import org.dawnsci.persistence.internal.PersistenceServiceImpl;

/**
 * Class used to test the PersistenceServiceImpl
 * @author wqk87977
 *
 */
public class PersistenceServiceCreator {

	public PersistenceServiceCreator(){
		
	}

	public static IPersistenceService createPersistenceService(){
		return new PersistenceServiceImpl();
	}
}
