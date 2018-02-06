package org.dawb.common.ui;

import org.eclipse.dawnsci.analysis.api.EventTracker;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;;

/**
 * This class is used to inject services using OSGI
 * @author wqk87977
 *
 */
public class ServiceLoader {

	private static EventTracker eventTrackerService;
	private static IPersistenceService persistenceService;
	private static ILoaderService loaderService;

	/**
	 * Used for OSGI injection
	 */
	public ServiceLoader() {
		
	}

	/**
	 * Injected by OSGI
	 * @param et
	 */
	public void setEventTrackerService(EventTracker et) {
		eventTrackerService = et;
	}

	public static EventTracker getEventTrackerService() {
		return eventTrackerService;
	}

	/**
	 * Injected by OSGI
	 * @param ps
	 */
	public void setPersistenceService(IPersistenceService ps) {
		persistenceService = ps;
	}

	public static IPersistenceService getPersistenceService() {
		return persistenceService;
	}

	/**
	 * Injected by OSGI
	 * @param ls
	 */
	public void setLoaderService(ILoaderService ls) {
		loaderService = ls;
	}

	public static ILoaderService getLoaderService() {
		return loaderService;
	}
}
