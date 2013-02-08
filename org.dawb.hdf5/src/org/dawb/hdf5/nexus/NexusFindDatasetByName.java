package org.dawb.hdf5.nexus;


import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5ScalarDS;

public class NexusFindDatasetByName implements IFindInNexus {

	public String text;
	
	public NexusFindDatasetByName(String attributeText) {
		text = attributeText;
	}
	
	@Override
	public boolean inNexus(HObject nexusObject) {
		if(nexusObject instanceof H5ScalarDS) {
			if (((H5ScalarDS)nexusObject).getName().toLowerCase().equals(text.toLowerCase())) {
				return true;
			}
		}
		
		return false;
	}
}
