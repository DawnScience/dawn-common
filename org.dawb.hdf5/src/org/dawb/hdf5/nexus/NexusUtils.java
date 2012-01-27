/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.hdf5.nexus;

import java.util.List;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5Datatype;
import ncsa.hdf.object.h5.H5File;

/**
 * Class used to mark groups in the hdf5 tree with nexus attributes.
 * 
 * This is a way not to use the nexus API.
 * 
 * @author gerring
 *
 */
public class NexusUtils {

	private static String NXCLASS = "NX_class";	

	/**
	 * Sets the nexus attribute so that if something is looking for them,
	 * then they are there.
	 * 
	 * @param file
	 * @param entry
	 * @param entryKey
	 * @throws Exception
	 */
	public static void setNexusAttribute(final FileFormat file, 
			                             final HObject    entry,
			                             final String     entryKey) throws Exception {
		
		// Check if attribute is already there
		final List attrList = entry.getMetadata();
		if (attrList!=null) for (Object object : attrList) {
			if (object instanceof Attribute) {
				final Attribute a      = (Attribute)object;
				final String[]  aValue = (String[])a.getValue();
				if (NXCLASS.equals(a.getName()) && entryKey.equals(aValue[0])) return;
			}
		}
		
		final int id = entry.open();
		try {
	        String[] classValue = {entryKey};
	        Datatype attrType = new H5Datatype(Datatype.CLASS_STRING, classValue[0].length()+1, -1, -1);
	        Attribute attr = new Attribute(NXCLASS, attrType, new long[]{1});
	        attr.setValue(classValue);
			
	        file.writeAttribute(entry, attr, false);

	        if (entry instanceof Group) {
	        	attrList.add(attr);
				((Group)entry).writeMetadata(attrList);
	        } else if (entry instanceof Dataset) {
	        	attrList.add(attr);
				((Dataset)entry).writeMetadata(attrList);
	        }
		        
		    
		} finally {
			entry.close(id);
		}
	}

}
