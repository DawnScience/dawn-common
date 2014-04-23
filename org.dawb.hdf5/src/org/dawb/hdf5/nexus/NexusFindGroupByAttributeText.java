/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.hdf5.nexus;

import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;

public class NexusFindGroupByAttributeText implements IFindInNexus{
	
	public String attributeValue;
	public String attributeName;
	
	public NexusFindGroupByAttributeText(String attributeValue, String attributeName) {
		this.attributeValue = attributeValue;
		this.attributeName = attributeName;
	}
	
	@Override
	public boolean inNexus(HObject nexusObject) {
		if (nexusObject instanceof Group) {
			if (attributeName != null) {
				String attrNexusObject = NexusUtils.getNexusGroupAttributeValue((Group) nexusObject, attributeName);
				if (attrNexusObject != null && attributeValue != null
						&& attrNexusObject.toLowerCase().equals(attributeValue.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}
}
