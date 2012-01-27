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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Datatype;

public class HierarchicalDataUtils {

	
	/**
	 * Overwrites destination_file if it exists, creates new if not.
	 * 
	 * @param source_file
	 * @param destination_file
	 * @throws IOException
	 */
	public final static void copy(final File source_file, final File destination_file) throws IOException {
		HierarchicalDataUtils.copy(source_file, destination_file, new byte[4096]);
	}

	/**
	 * Overwrites destination_file if it exists, creates new if not.
	 * 
	 * @param source_file
	 * @param destination_file
	 * @param buffer
	 * @throws IOException
	 */
	public final static void copy(final File source_file, final File destination_file, final byte[] buffer)
			throws IOException {

		if (!source_file.exists()) {
			return;
		}

		final File parTo = destination_file.getParentFile();
		if (!parTo.exists()) {
			parTo.mkdirs();
		}
		if (!destination_file.exists()) {
			destination_file.createNewFile();
		}

		InputStream source = null;
		OutputStream destination = null;
		try {

			source = new BufferedInputStream(new FileInputStream(source_file));
			destination = new BufferedOutputStream(new FileOutputStream(destination_file));
			int bytes_read;
			while (true) {
				bytes_read = source.read(buffer);
				if (bytes_read == -1) {
					break;
				}
				destination.write(buffer, 0, bytes_read);
			}

		} finally {
			source.close();
			destination.close();
		}
	}

	
	/**
	 * @throws Exception 
	 * @throws OutOfMemoryError 
	 * 
	 */
	public static boolean isDataType(Dataset set, int requiredType) throws OutOfMemoryError, Exception {
		
		if (requiredType<0) return true; // Numbers less than 0 are any dataset 
		
        final int   type  = set.getDatatype().getDatatypeClass();
        if (type==Datatype.CLASS_FLOAT || type==Datatype.CLASS_INTEGER) {
        	if (IHierarchicalDataFile.NUMBER_ARRAY==requiredType) {
        		long[]shape = getDims(set);
                if (shape==null) return true;
        		return shape.length>1 || shape[0]>1;
        	}
        }
        
        if (type==Datatype.CLASS_CHAR || type==Datatype.CLASS_STRING) {
        	return IHierarchicalDataFile.TEXT==requiredType;
        }

        return requiredType==type;
	}

	public static long[] getDims(final Dataset set) throws Exception {
		
        if (set.getDims()==null) {
        	set.getMetadata();
        }
        return set.getDims();
	}

	public static long getSize(final Dataset set) throws Exception {
		
		long[] shape;
		try {
			shape = HierarchicalDataUtils.getDims((Dataset)set);
		} catch (Exception e) {
			return -1;
		}
		if (shape==null) return -1;
		
		long size = shape[0];
		for (int i = 1; i < shape.length; i++) size*=shape[i];

		final int bpi  = set.getDatatype().getDatatypeSize();
        return  bpi*size;
         
         
	}

}
