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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5Datatype;

import org.dawb.hdf5.HierarchicalDataFactory;
import org.dawb.hdf5.IHierarchicalDataFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to mark groups in the hdf5 tree with nexus attributes.
 * 
 * This is a way not to use the nexus API.
 * 
 * @author gerring
 *
 */
public class NexusUtils {

	public static final String NXCLASS = "NX_class";	
	public static final String AXIS    = "axis";	
	public static final String LABEL   = "label";	
	public static final String PRIM    = "primary";	
	public static final String SIGNAL  = "signal";	
	public static final String UNIT    = "unit";	
	
	private final static Logger logger = LoggerFactory.getLogger(NexusUtils.class);

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
		
		setAttribute(file, entry, NXCLASS, entryKey);
	}
	
	/**
	 * Set any attribute on a HObject.
	 * 
	 * @param file
	 * @param entry
	 * @param name
	 * @param entryKey
	 * @throws Exception
	 */
	public static void setAttribute(final FileFormat file, 
									final HObject    entry,
									final String     name,
									final String     entryKey) throws Exception {
	
		// Check if attribute is already there
		final List attrList = entry.getMetadata();
		if (attrList!=null) for (Object object : attrList) {
			if (object instanceof Attribute) {
				final Attribute a      = (Attribute)object;
				final String[]  aValue = (String[])a.getValue();
				if (name.equals(a.getName()) && entryKey.equals(aValue[0])) return;
			}
		}
		
		final int id = entry.open();
		try {
	        String[] classValue = {entryKey};
	        Datatype attrType = new H5Datatype(Datatype.CLASS_STRING, classValue[0].length()+1, -1, -1);
	        Attribute attr = new Attribute(name, attrType, new long[]{1});
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

	/**
	 * Does not replace the attribute if it exists
	 * @param file
	 * @param entry
	 * @param name
	 * @param value
	 * @throws Exception
	 */
	public static void setIntAttribute(final FileFormat file, 
							           final HObject   entry,
							           final String    name,
							           final int       value) throws Exception {

		
		final List attrList = entry.getMetadata();
		if (attrList!=null) for (Object object : attrList) {
			if (object instanceof Attribute) {
				final Attribute a      = (Attribute)object;
				if (name.equals(a.getName())) return;
			}
		}
		
		final int id = entry.open();
		try {
	        Datatype attrType = new H5Datatype(Datatype.CLASS_INTEGER, 1, -1, -1);
	        Attribute attr = new Attribute(name, attrType, new long[]{1});
	        attr.setValue(new int[]{value});
			
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
	
	/**
	 * Gets the nexus axes from the data node, if there are any there
	 * 
	 * TODO Deal with label attribute?
	 * 
	 * @param FileFormat - the file
	 * @param dataNode - the node with the signal
	 * @param dimension, we want the axis for 1, 2, 3 etc.
	 * @return
	 * @throws Exception especially if dims are ask for which the signal does not have.
	 */
	public static List<Dataset> getAxes(final FileFormat file, final Dataset signal, int dimension) throws Exception {
		
		final List<Dataset>         axesTmp = new ArrayList<Dataset>(3);
        final Map<Integer, Dataset> axesMap = new TreeMap<Integer, Dataset>();
		
        signal.getMetadata();
        final long size = signal.getDims()[dimension-1];

        final String parentPath = signal.getFullName().substring(0, signal.getFullName().lastIndexOf("/"));
        
        final Group parent = (Group)file.get(parentPath);
        
        final List<HObject> children = parent.getMemberList();
		for (HObject hObject : children) {
			final List<?> att = hObject.getMetadata();
			if (!(hObject instanceof Dataset)) continue;
			if (hObject.getFullName().equals(signal.getFullName())) continue;
			
			Dataset axis = null;
			int     pos  = -1;
			boolean isSignal = false;
			for (Object object : att) {
				if (object instanceof Attribute) {
					Attribute attribute = (Attribute)object;
					if (AXIS.equals(attribute.getName())) {
						int iaxis = getAttributeIntValue(attribute);
						if (iaxis == dimension) axis = (Dataset)hObject;
						
					} else if (PRIM.equals(attribute.getName())) {
						pos = getAttributeIntValue(attribute);
					} else if (SIGNAL.equals(attribute.getName())) {
						isSignal = true;
						axis     = null;
						pos      = -1;
						break;
					}
				}
			}
			
			// Add any the same shape as this dimension
			// providing that they are not signals
			// Some nexus files set axis wrong
			if (axis==null && !isSignal) {
				final long[] dims = ((Dataset)hObject).getDims();
				if (dims[0]==size && dims.length==1) {
					axis = (Dataset)hObject;
				}
			}
			
			if (axis!=null) {
				if (pos<0) {
					axesTmp.add(axis);
				} else {
					axesMap.put(pos, axis);
				}
			}
		}
		
		final List<Dataset>         axes = new ArrayList<Dataset>(3);
		if (!axesMap.isEmpty()) {
			for (Integer pos : axesMap.keySet()) {
				axes.add(axesMap.get(pos));
			}
		}
		axes.addAll(axesTmp);
		
		if (axes.isEmpty()) return null;
		
		return axes;
	}

	/**
	 * Gets the int value or returns -1 (Can only be used for values which are not allowed to be -1!)
	 * @param attribute
	 * @return
	 */
	private static int getAttributeIntValue(Attribute attribute) {
		final Object ob = attribute.getValue();
		if (ob instanceof int[]) {
			int[] ia = (int[])ob;
			return ia[0];
		} else if (ob instanceof String[]) {
			String[] sa = (String[])ob;
			try {
				return Integer.parseInt(sa[0]);
			} catch (Throwable ne) {
				return -1;
			}
		}

		return -1;
	}

	/**
	 * Returns names of axes in group at same level as name passed in.
	 * 
	 * This opens and safely closes a nexus file if one is not already open for
	 * this location.
	 * 
	 * @param filePath
	 * @param nexusPath - path to signal dataset
	 * @param dimension, the dimension we want the axis for starting with 1
	 * @return
	 * @throws Exception
	 */
	public static List<String> getAxisNames(String filePath, String nexusPath, int dimension) throws Exception {

		if (filePath==null || nexusPath==null) return null;
		if (dimension<1) return  null;
       	IHierarchicalDataFile file = null;
        try {
        	file = HierarchicalDataFactory.getReader(filePath);
        	final List<Dataset> axes = file.getNexusAxes(nexusPath, dimension);
        	if (axes==null) return null;
       
        	final List<String> names = new ArrayList<String>(axes.size());
        	for (Dataset ds : axes) names.add(ds.getName());
        	
        	return names;
        } finally {
        	if (file!=null) file.close();
        }
	}
	
	/**
	 * Returns the attribute name of a nexus group.
	 * 
	 * If the group has more than one attribute only the first is returned
	 * 
	 * @param group
	 */
	public static String getNexusGroupAttribute(Group group) {
		try {
			for (Object ob: group.getMetadata()) {
				if (ob instanceof Attribute) {
					Object test = ((Attribute)ob).getValue();
					if (test instanceof String[])
						return ((String[])test)[0];
				}
			}
		} catch (Exception e) {
		}
		return null;
	}
	
	/**
	 * Breath first search of a hierarchical data file group.
	 * 
	 * @param finder - IFindInNexus object, used to test a group
	 * @param rootGroup - the group to be searched
	 * @param findFirst - whether the search returns when the first object is found (quicker for single objects)
	 */
	public static List<HObject> nexusBreadthFirstSearch(IFindInNexus finder, Group rootGroup, boolean findFirst) {
		
		List<HObject> out = new ArrayList<HObject>();
		
		Queue<Group> queue = new LinkedList<Group>();
		for (HObject nxObject: rootGroup.getMemberList()) {
			if (finder.inNexus(nxObject)) {
				
				if (findFirst) return Arrays.asList(nxObject);
				else out.add(nxObject);
			}
			
			if(nxObject instanceof Group) {
				queue.add((Group)nxObject);
			}
		}
		
		Integer i = 0;
		
		while (queue.size() != 0) {
			Group group = queue.poll();
			for (HObject nxObject: group.getMemberList()) {
				
				if (finder.inNexus(nxObject)) {
					if (findFirst) return Arrays.asList(nxObject);
					else out.add(nxObject);
				}
				
				if (nxObject instanceof Group) {
					queue.add((Group)nxObject);
				}
				
				i++;
			}
		}
		logger.debug("This many times through loop: " + i.toString());
		return out;
	}
}
