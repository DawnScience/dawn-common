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
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

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
	
	    return getAdapter(sourceObject, adapterType, true);
	}

	/**
	 * Returns the specified adapter for the given element, or <code>null</code>
	 * if no such adapter was found.<br>
	 * This method was copied from org.eclipse.ui.ide.ResourceUtil so we don't need to pull the IDE
	 *
	 * @param element
	 *            the model element
	 * @param adapterType
	 *            the type of adapter to look up
	 * @param forceLoad
	 *            <code>true</code> to force loading of the plug-in providing
	 *            the adapter, <code>false</code> otherwise
	 * @return the adapter
	 * @since 3.2
	 */
	public static <T> T getAdapter(Object element, Class<T> adapterType, boolean forceLoad) {
		if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			T o = adaptable.getAdapter(adapterType);
			if (o != null) {
				return o;
			}
		}
		if (forceLoad) {
			return adapterType.cast(Platform.getAdapterManager().loadAdapter(element, adapterType.getName()));
		}
		return Platform.getAdapterManager().getAdapter(element, adapterType);
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
