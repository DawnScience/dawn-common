/*
 * Copyright (c) 2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.util.object;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.ide.ResourceUtil;

public class AdapterUtils {
	// Source code and JavaDoc adapted from org.eclipse.ui.internal.util.Util.getAdapter
	/**
	 * If it is possible to adapt the given object to the given type, this
	 * returns the adapter. Performs the following checks:
	 * 
	 * <ol>
	 * <li>Returns <code>sourceObject</code> if it is an instance of the
	 * adapter type.</li>
	 * <li>If sourceObject implements IAdaptable, it is queried for adapters.</li>
	 * <li>If sourceObject is not an instance of PlatformObject (which would have
	 * already done so), the adapter manager is queried for adapters</li>
	 * </ol>
	 * 
	 * Otherwise returns null.
	 * 
	 * @param sourceObject
	 *            object to adapt, or null
	 * @param adapterType
	 *            type to adapt to
	 * @return a representation of sourceObject that is assignable to the
	 *         adapter type, or null if no such representation exists
	 */
	public static <T> T getAdapter(Object sourceObject, Class<T> adapterType) {
		Assert.isNotNull(adapterType);
	    if (sourceObject == null) {
	        return null;
	    }
	    if (adapterType.isInstance(sourceObject)) {
			return adapterType.cast(sourceObject);
	    }
	
	    return ResourceUtil.getAdapter(sourceObject, adapterType, true);
	}

	// JavaDoc adapted from org.dawb.common.ui.EclipseUtils.getAdapter
	/**
	 * If it is possible to adapt the given object to one of the given types, this
	 * returns the adapter of first adaptable type.
	 * 
	 * <ol>
	 * <li>Returns <code>sourceObject</code> if it is an instance of one of the
	 * adapter types.</li>
	 * <li>If sourceObject implements IAdaptable, it is queried for adapters.</li>
	 * <li>If sourceObject is not an instance of PlatformObject (which would have
	 * already done so), the adapter manager is queried for adapters</li>
	 * </ol>
	 * 
	 * Otherwise returns null.
	 * 
	 * @param sourceObject
	 *            object to adapt, or null
	 * @param adapterTypes
	 *            array of types to adapt to
	 * @return a representation of sourceObject that is assignable to the
	 *         first adaptable adapter type, or null if no such representation exists
	 */
	public static Object getFirstAdapter(Object sourceObject, Class<?>[] adapterTypes) {
		Assert.isNotNull(adapterTypes);
	    if (sourceObject == null) {
	        return null;
	    }
	    for(final Class<?> adapterType : adapterTypes) {
	    	final Object adapter = getAdapter(sourceObject, adapterType);
    	    if (adapter != null)
    	    	return adapter;
	    }
	    return null;
	}

}
