/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.util.object;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

public class ObjectUtils {

	/**
	 * Tries to get the object as int.
	 * @param object
	 * @throws NullPointerException, NumberFormatException
	 * @return
	 */
	public static int getInteger(final Object object) {
		if (object instanceof Integer) {
			return ((Integer)object).intValue();
		}
		
		return Integer.parseInt(object.toString());
	}
	
	/**
	 * Uses serialization to complete a deep copy of an object.
	 * 
	 * Particularly usefuly for arrays of serializable objects.
	 * 
	 * @param oldObj
	 * @return
	 * @throws Exception
	 */
	public static final Object deepCopy(Object oldObj, ClassLoader loader) throws Exception {
		
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			final ByteArrayOutputStream bos =  new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			// serialize and pass the object
			oos.writeObject(oldObj);
			oos.flush();     
			
			ByteArrayInputStream bin =  new ByteArrayInputStream(bos.toByteArray());
			ois = new ObjectInputStreamWithLoader(bin, loader); 
			
			// return the new object
			return ois.readObject();

		} catch(Exception e) {
			throw(e);
			
		} finally {
			oos.close();
			ois.close();
		}
	}

	private static final class ObjectInputStreamWithLoader extends ObjectInputStream {
		private ClassLoader loader;

		ObjectInputStreamWithLoader(InputStream in, ClassLoader cl) throws IOException {
			super(in);
			this.loader = cl;
			if (loader==null) loader = getClass().getClassLoader();
		}

		protected Class<?> resolveClass(ObjectStreamClass classDesc)
				throws IOException, ClassNotFoundException {
			return Class.forName(classDesc.getName(), false, loader);
		}
	}
}
