/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.hdf5;

import org.dawb.hdf5.model.IHierarchicalDataModel;
import org.dawb.hdf5.model.internal.HierarchicalDataWorkspaceModelFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;
	private static Activator plugin;
	private IHierarchicalDataModel model;

	public Activator() {
		plugin = this;
	}

	static BundleContext getContext() {
		return context;
	}

	public static Activator getPlugin() {
		return plugin;
	}


	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		plugin = null;
	}


	/**
	 * Returns the IHierarchicalDataModel. If one does not exists, this method
	 * will create one and register a workspace listener.
	 *
	 * @return
	 */
	public synchronized IHierarchicalDataModel getHierarchicalDataModel() {
		if (model == null) {
			model = HierarchicalDataWorkspaceModelFactory.getHierarchicalDataModel();
		}
		return model;
	}
}
