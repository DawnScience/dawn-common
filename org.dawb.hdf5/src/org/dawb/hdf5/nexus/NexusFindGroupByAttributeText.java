/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.hdf5.nexus;

import org.dawb.hdf5.IHierarchicalDataFile;

public class NexusFindGroupByAttributeText implements IFindInNexus{
	
	public String attributeValue;
	public String attributeName;
	private IHierarchicalDataFile file;
	
	public NexusFindGroupByAttributeText(IHierarchicalDataFile file, String attributeValue, String attributeName) {
		this.file           = file;
		this.attributeValue = attributeValue;
		this.attributeName  = attributeName;
	}
	
	@Override
	public boolean inNexus(String nexusObject) {
		try {
			if (file.isGroup(nexusObject)) {
				if (attributeName != null) {
					String attrNexusObject = NexusUtils.getNexusGroupAttributeValue(file, nexusObject, attributeName);
					if (attrNexusObject != null && attributeValue != null
							&& attrNexusObject.toLowerCase().equals(attributeValue.toLowerCase())) {
						return true;
					}
				}
			}
		} catch (Exception ne) {
			return false;
		}
		return false;
	}
}
