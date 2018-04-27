/*
 * Copyright (c) 2012-2016 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.internal;

public class PersistenceConstants {

	// NOTE You cannot have a version above .9 because double are used.
	public static final String CURRENT_VERSION = "1.4";
	public static final String ENTRY          = "/entry";
	public static final String DATA_ENTRY     = "/entry/data";
	public static final String HISTORY_ENTRY  = "/entry/history";
	public static final String MASK_ENTRY     = "/entry/mask";
	public static final String ROI_ENTRY      = "/entry/region";
	public static final String FUNCTION_ENTRY = "/entry/function";
	public static final String PROCESS_ENTRY  = "/entry/process";
	public static final String DIFFRACTIONMETADATA_ENTRY = "/entry/instrument";
	public static final String SITE = "Diamond Light Source";
}
