/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.dawnsci.analysis.api.image.IImageFilterService;
import org.eclipse.dawnsci.analysis.api.image.IImageStitchingProcess;
import org.eclipse.dawnsci.analysis.api.image.IImageTransform;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistenceService;
import org.eclipse.dawnsci.analysis.api.processing.IOperationService;
import org.eclipse.ui.PlatformUI;

import uk.ac.diamond.daq.util.logging.deprecation.DeprecationLogger;

/**
 * This class gets a service even if we are running in headless mode.
 * 
 * This is useful for unit tests and workflows to get services which would otherwise
 * only be available when a workbench is running
 * 
 * @author Matthew Gerring
 * 
 * @deprecated Use OSGI Services instead. Get in touch if you need help with this...
 */
@Deprecated(since="Dawn 1.8")
public class ServiceManager {
	
	private static final String REPLACEMENT_TYPE = "OSGI Services";
	private static final DeprecationLogger logger = DeprecationLogger.getLogger(ServiceManager.class);
	private static final Collection<Class<? extends Object>> OFFLINE_SERVICES; 
	static {
		OFFLINE_SERVICES = new HashSet<Class<? extends Object>>(12);
		OFFLINE_SERVICES.add(IHardwareService.class);
		OFFLINE_SERVICES.add(ILoaderService.class);
		OFFLINE_SERVICES.add(IUserInputService.class);
		OFFLINE_SERVICES.add(ISystemService.class);
		OFFLINE_SERVICES.add(ITransferService.class);
		OFFLINE_SERVICES.add(IPersistenceService.class);
		OFFLINE_SERVICES.add(IImageFilterService.class);
		OFFLINE_SERVICES.add(IImageTransform.class);
		OFFLINE_SERVICES.add(IImageStitchingProcess.class);
		OFFLINE_SERVICES.add(IOperationService.class);
	}

	/**
	 * Tries eclipse service, then osgi service, then reading eclipse extension points.
	 * 
	 * @param serviceClass
	 * @return
	 * @throws Exception
	 */
	public static Object getService(final Class<?> serviceClass) throws Exception {
		logger.deprecatedMethod("getService(Class<?>)", null, REPLACEMENT_TYPE);
		return getService(serviceClass, true);
	}

	private static Map<Class<?>, Object> overrides;
	/**
	 * Tries eclipse service, then osgi service, then reading eclipse extension points.
     *
	 * Gets a service given a class, using the workbench if we are not
	 * in headless mode, otherwise returns our services providing the
	 * class declared for the extension point, also implements the service.
	 * 
	 * @param serviceClass
	 * @return
	 */
	public static Object getService(final Class<?> serviceClass, boolean exceptionOnError) throws Exception {
		
		logger.deprecatedMethod("getService(Class<?>, boolean)", null, REPLACEMENT_TYPE);
		
		if (overrides!=null && overrides.containsKey(serviceClass)) {
			return overrides.get(serviceClass);
		}
		
		if (PlatformUI.isWorkbenchRunning()) {
			Object instance = PlatformUI.getWorkbench().getService(serviceClass);
			if (instance!=null) return instance;
		}
		
		try {
			// Try to get it from OSGI if we can.
			try {
				Object instance = Activator.getService(serviceClass);
				if (instance!=null) return instance;
			} catch (Throwable ignored) {
				// We check the OFFLINE_SERVICES now...
			}
			
			// Designed to get dawb factories which implement the serviceClass, might not
			// get other services properly. To help this we throw an exception if it is
			// one we don't know about.
			if (!OFFLINE_SERVICES.contains(serviceClass)) throw new Exception("Cannot get an implementor for "+serviceClass+" in headless mode!");
			final IConfigurationElement[] ele = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.ui.services");
			for (IConfigurationElement i : ele) {
				final Object factory = i.createExecutableExtension("factoryClass");
				if (factory!=null && factory.getClass()==serviceClass) return factory;
			}
		} catch (Exception ne) {
			if (exceptionOnError) throw ne;
			ne.printStackTrace();// Only in test decks does this happen
			return null;
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param serviceClass
	 * @param service
	 */
	public static void setService(final Class<?> serviceClass, Object service) {
		logger.deprecatedMethod("setService(Class<?>, Object)", null, REPLACEMENT_TYPE);
		if (overrides==null) overrides = new HashMap<Class<?>, Object>(7);
		overrides.put(serviceClass, service);
	}
	
	public static void addOffline(final Class<?> service) {
		logger.deprecatedMethod("addOffline(Class<?>)", null, REPLACEMENT_TYPE);
		OFFLINE_SERVICES.add(service);
	}
}
