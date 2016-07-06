/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.util.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {

	private static final int BUFFER = 4096;
	
	public static void unzip(final File zip, final File dir) throws Exception {
		
		if (!zip.exists()) throw new FileNotFoundException("Cannot find "+zip);
		if (!dir.exists()) throw new FileNotFoundException("Cannot find "+dir);
        if (!zip.isFile())      throw new FileNotFoundException("Zip file must be a file "+zip);
        if (!dir.isDirectory()) throw new FileNotFoundException("Dir argument must be a directory "+dir);

		
        final ZipInputStream  zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)));

		try {
			
			ZipEntry entry;
			
			while((entry = zis.getNextEntry()) != null) {
				
				int count;
				byte data[] = new byte[BUFFER];
				
				final String              path = dir.getAbsolutePath()+"/"+entry.getName();
				final File                out  = new File(path);
				if (entry.isDirectory()) {
					out.mkdirs();
					continue;
				} else {
					out.getParentFile().mkdirs();
					out.createNewFile();
				}
				final FileOutputStream     fos  = new FileOutputStream(out);
				final BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
				try {
					while ((count = zis.read(data, 0, BUFFER))  != -1) {
						dest.write(data, 0, count);
					}
					dest.flush();
				} finally {
					dest.close();
				}
			}
			
		} finally {
			zis.close();
		}

	}
}
