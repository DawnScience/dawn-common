/*-
 * Copyright 2014 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dawnsci.persistence.internal;

public class PersistenceConstants {

	// NOTE You cannot have a version above .9 because double are used.
	public static final String CURRENT_VERSION = "1.3";
	public static final String ENTRY          = "/entry";
	public static final String DATA_ENTRY     = "/entry/data";
	public static final String HISTORY_ENTRY  = "/entry/history";
	public static final String MASK_ENTRY     = "/entry/mask";
	public static final String ROI_ENTRY      = "/entry/region";
	public static final String FUNCTION_ENTRY = "/entry/function";
	public static final String PROCESS_ENTRY = "/entry/process";
	public static final String DIFFRACTIONMETADATA_ENTRY = "/entry/instrument";
}
