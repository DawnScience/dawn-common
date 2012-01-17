/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.util.io;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Collection;

public class IOCollectionUtils {

	public static void saveCollection(final String path,final Collection<?> collection) throws Exception {
		IOCollectionUtils.saveCollection(new File(path), collection);
	}

	/**
	 * Save collection to file
	 * @param file
	 * @param collection
	 * @throws Exception
	 */
	public static void saveCollection(final File file,
			                          final Collection<?> collection) throws Exception {
		
		if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
		if (!file.exists()) file.createNewFile();
		
		final XMLEncoder encoder = new XMLEncoder(new FileOutputStream(file));
		try {
			encoder.writeObject(collection);
		} finally {
			encoder.close();
		}
	}
	
	public static Collection<?> readCollection(final String path) throws Exception {
		return IOCollectionUtils.readCollection(new File(path));
	}

	public static Collection<?> readCollection(final File file) throws Exception {

		final XMLDecoder decoder = new XMLDecoder(new FileInputStream(file));
		try {
			return (Collection<?>)decoder.readObject();
		} finally {
			decoder.close();
		}
	}

}
