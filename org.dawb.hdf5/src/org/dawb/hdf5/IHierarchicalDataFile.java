/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.hdf5;

import java.util.List;
import java.util.Map;

import javax.swing.tree.TreeNode;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;

/**
 * Having this big interface here is not ideal but
 * is designed to avoid temptation of talking to objects
 * directly in the {@link HierarchicalDataFile} which 
 * are not thread safe.
 * 
 * @author gerring
 *
 */
public interface IHierarchicalDataFile {

	public static final int NUMBER_ARRAY = 17061;
	public static final int TEXT         = 17062;
	/**
	 * Must be called to close the file.
	 * @throws Exception
	 */
	public void close() throws Exception;

	/**
	 * Get the root TreeNode
	 * @return
	 */
	public TreeNode getNode();
	
	/**
	 * Return the file path
	 * @return
	 */
	public String getPath();

	/**
	 * Return the root Group
	 * @return
	 */
	public Group getRoot();

	/**
	 * Print the full file tree
	 * @throws Exception
	 */
	public void print() throws Exception;

	/**
	 * 
	 * @param fullPath
	 * @return
	 * @throws Exception
	 */
	public HObject getData(String fullPath) throws Exception;
	
	/**
	 * Extracts names, sizes and shapes in one pass.
	 * @param dataType
	 * @return
	 * @throws Exception
	 */
	public HierarchicalInfo getDatasetInformation(int dataType) throws Exception;


	/**
	 * dataType one of NUMBER_ARRAY or TEXT or one of the Datatype.CLASS_* variables.
	 * @return
	 */
	public List<String> getDatasetNames(int dataType)  throws Exception;

	/**
	 * 
	 * @return
	 */
	public Map<String, Integer> getDatasetSizes(int dataType)  throws Exception;

	/**
	 * 
	 * @return
	 */
	public Map<String, int[]> getDatasetShapes(int dataType)  throws Exception;

	/**
	 * A group at the top level, creating one if it does not exist.
	 * @param string
	 * @return
	 */
	public Group group(String name) throws Exception;

	/**
	 * A group in this parent, creating one if it does not exist.
	 * @param string
	 * @return
	 */
	public Group group(String name, final Group parent) throws Exception;
	
	/**
	 * Does not set the attribute again if it is already set.
	 * @param object
	 * @param attribute one of the values defined in {@link Nexus}
	 * @throws Exception
	 */
	public void setNexusAttribute(final HObject object, final String attribute) throws Exception;

	/**
	 * Creates and returns a new dataset with the given name and parent
	 * If it already exists then an integer will be appended to the name and it will still be written.
	 * 
	 * @param name
	 * @param value
	 */
	public Dataset createDataset(final String name, final String value, final Group parent) throws Exception;

	/**
	 * Creates and returns a new dataset with the given name and parent
	 * If it already exists then an integer will be appended to the name and it will still be written.
     *
	 * @param name
	 * @param shape
	 * @param buffer
	 * @param data
	 */
	public Dataset createDataset(final String name, final Datatype dtype, final long[] shape, final Object buffer, final Group data) throws Exception;

	/**
	 * Method finds the given data set in the group and adds buffer to the end of the stack.
	 * 
	 * If the data set does not exist it is created with dimensions [bufferShape]
	 * 
	 * If the data set exists the first dimension is created and increased by one to accomodate it, for instance
	 * the second image in the stack would resize the data shape to [2, bufferShape...] and
	 * so forth.
	 * 
	 * A more efficient algorithm could be used than increasing by 1 if this proves slow.
	 * 
	 * @param datasetName
	 * @param d
	 * @param shape
	 * @param buffer
	 * @param group
	 * @return
	 */
	public Dataset appendDataset(String datasetName, Datatype d, long[] bufferShape, Object buffer, Group group)  throws Exception;

}
