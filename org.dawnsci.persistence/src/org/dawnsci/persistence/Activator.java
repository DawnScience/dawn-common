package org.dawnsci.persistence;

import java.util.Hashtable;

import org.dawb.common.services.IPersistenceService;
import org.dawnsci.persistence.internal.PersistenceServiceImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	Logger logger = LoggerFactory.getLogger(Activator.class);

	public void start(BundleContext context) throws Exception {
		logger.info("Starting org.dawnsci.persistence");
		Hashtable<String, String> props = new Hashtable<String, String>(1);
		props.put("description", "A service used to save and/or load data to hdf5 files");
		context.registerService(IPersistenceService.class, new PersistenceServiceImpl(), props);
		
	}

	public void stop(BundleContext context) throws Exception {
		logger.info("Stopping org.dawnsci.persistence");
	}

}
