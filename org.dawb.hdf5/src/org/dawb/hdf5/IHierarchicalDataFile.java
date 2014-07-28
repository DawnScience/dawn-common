/*-
 * Copyright (c) 2013 Diamond Light Source Ltd.
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
public interface IHierarchicalDataFile extends AutoCloseable {

	public static final int NUMBER_ARRAY              = 17061;
	public static final int SCALAR                    = 17063;
	public static final int TEXT                      = 17062;
	/**
	 * Must be called to close the file.
	 * 
     * For historical reasons (close was written before AutoCloseable was
     * added) this method does not follow strongly encouragement not to throw
     * Exception. 
	 */
	public void close() throws Exception;
	
	/**
	 * @return true if the file has been closed in an unfriendly closeAll(...) call.
	 */
	public boolean isClosed();

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
	 * This call can also be used to get links out of the data by the path in the link file.
	 * @param fullPath
	 * @return
	 * @throws Exception
	 */
	public HObject getData(String fullPath) throws Exception;
	
	/**
	 * Attempt to delete the HObject at this path and unlink any children.
	 * 
	 * @param fullPath
	 */
	//public void delete(String fullPath) throws Exception;
	
	/**
	 * 
	 * @param path
	 * @return the parent of the node at this path
	 * @throws Exception
	 */
	public Group getParent(final String path) throws Exception;
	
	/**
	 * The full attribute key is: <node full path>@<attribute name>
	 * e.g. /entry1/data@napimount
	 * @param fullAttributeKey
	 * @return
	 * @throws NullPointerException if path (part before @) does not exist 
	 * in file
	 * @throws ArrayIndexOutOfBoundsException if there is no @ in fullAttributeKey
	 */
	public String getAttributeValue(String fullAttributeKey) throws Exception;

	/**
	 * Reads all the attributes for the given object and returns a Map of attribute
	 * name to attribute values. NOTE unlike {@link #getAttributeValues()} which is
	 * recursive, this method only gets the attribute for the given path.
	 * 
	 * @return map of attribute names (part after the @) to attribute values or 
	 * <code>null</code> if path does not exist.
	 */
	Map<String, Object> getAttributeValues(String fullPath);
	
	/**
	 * Reads all the attribute Lists from the object and puts it in a map of the full paths
	 * to the attributes using paths of the form: <node full path>@<attribute name>
	 * @return Map<String, Object>
	 */
	public Map<String, Object> getAttributeValues();

	
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
	 * 
	 * @param object
	 * @param name
	 * @param value
	 * @throws Exception
	 */
	public void setAttribute(final HObject object, final String name, final String value) throws Exception;
	
	/**
	 * 
	 * @param object
	 * @param name
	 * @param value
	 * @throws Exception
	 */
	public void setAttribute(final HObject object, final String name, final String value, boolean overwrite) throws Exception;

	/**
	 * Set an integer attribute on an HObject, useful for nexus signal and axis calls.
	 * Does not overwrite the value if it is already set.
	 * 
	 * @param entry
	 * @param name
	 * @param value
	 * @throws Exception
	 */
	public void setIntAttribute(final HObject   entry, final String    name, final int       value) throws Exception;
	
	/**
	 * This method returns the dataset axes for a given signal node. The nexus path must be the path
	 * to the signal
	 * 
	 * @param signalPath
	 * @param dimension
	 * @return list of strings which represent the path to the dataset
	 */
	public List<String> getNexusAxesNames(String signalPath, int dimension) throws Exception;
	
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
	 * Creates and returns a new dataset with the given name and parent
	 * If it already exists then an integer will be appended to the name and it will still be written.
     *
	 * @param name
	 * @param shape
	 * @param buffer
	 * @param data
	 * @param overwrite
	 */
	public Dataset createDataset(final String name, final Datatype dtype, final long[] shape, final Object buffer, final Group data, final boolean overwrite) throws Exception;

	/**
	 * Creates and returns a new dataset with the given name and parent
	 * If it already exists then the dataset is overwritten
	 * 
	 * @param name
	 * @param value
	 */
	public Dataset replaceDataset(final String name, final String value, final Group parent) throws Exception;

	/**
	 * Creates and returns a new dataset with the given name and parent
	 * If it already exists then the dataset is overwritten
     *
	 * @param name
	 * @param shape
	 * @param buffer
	 * @param data
	 */
	public Dataset replaceDataset(final String name, final Datatype dtype, final long[] shape, final Object buffer, final Group data) throws Exception;

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

	/**
	 * Creates a link to an existing object
	 * 
	 * @param targetGroup
	 * @param linkName
	 * @param sourceFullPath
	 * @return The link object, or null if sourcePath doesn't exist
	 */
	public HObject createLink(Group targetGroup, String linkName, String sourceFullPath) throws Exception;

	/**
	 * Gets the size of a dimension of the dataset.
	 * @param datasetName
	 * @param dimension
	 * @return the size of a dimension of the dataset or -1 if a problem occurred
	 * @throws Exception
	 */
	public long getDimensionSize(String datasetName, int dimension) throws Exception;
	
	/**
	 * Method finds the given data set in the group and set a slice of data into it.
	 * 
	 * If the data set does not exist, it is created with dimensions [totalShape], and
	 * the data [buffer] is inserted in the positions designated in startStopStep
	 * 
	 * 
	 * @param name
	 * @param dtype
	 * @param buffer
	 * @param parent
	 * @param startStopStep
	 * @param totalShape
	 * @return
	 */
	public Dataset insertSlice(String name,  
            final Datatype dtype,
            final Object   buffer,
            final Group    parent,
            final long[][] startStopStep,
            final long[] totalShape) throws Exception;

}
