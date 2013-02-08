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
			if (NexusUtils.getNexusGroupAttributeValue((Group)nexusObject, attributeName).toLowerCase().equals(attributeValue.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
