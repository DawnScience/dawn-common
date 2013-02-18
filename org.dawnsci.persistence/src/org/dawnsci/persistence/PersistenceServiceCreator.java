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

	/**
	 * Used only for testing - DO NOT USE Externally. Instead get the Service by class.
	 * @return
	 */
	public static IPersistenceService createPersistenceService(){
		return new PersistenceServiceImpl();
	}
}
