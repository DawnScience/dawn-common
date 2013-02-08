package org.dawb.hdf5.nexus;

import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;

public class NexusFindGroupByAttributeText implements IFindInNexus{
	
	public String text;
	
	public NexusFindGroupByAttributeText(String attributeText) {
		text = attributeText;
	}
	
	@Override
	public boolean inNexus(HObject nexusObject) {
		if (nexusObject instanceof Group) {
			if (NexusUtils.getNexusGroupAttribute((Group)nexusObject).toLowerCase().equals(text.toLowerCase())) {
				return true;
			}
		}
		return false;
	}
}
