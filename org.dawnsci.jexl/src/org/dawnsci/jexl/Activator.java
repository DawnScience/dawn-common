package org.dawnsci.jexl;

import java.util.Hashtable;

import org.dawb.common.services.expressions.IExpressionService;
import org.dawnsci.jexl.internal.ExpressionServiceImpl;
import org.dawnsci.jexl.Activator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

	private static BundleContext context;
	Logger logger = LoggerFactory.getLogger(Activator.class);

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		logger.info("Starting org.dawnsci.jexl");
		Hashtable<String, String> props = new Hashtable<String, String>(1);
		props.put("description", "A service used to create an expression engine");
		context.registerService(IExpressionService.class, new ExpressionServiceImpl(), props);
		Activator.context = context;
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		logger.info("Stopping org.dawnsci.jexl");
		Activator.context = null;
	}
}