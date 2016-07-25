/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.util.image;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.regex.Pattern;

public class ImageFileUtils {
	
	final static String EDFFORMAT = "edf";
	final static String EDFFORMATGZip = "edf.gz";
	final static String EDFFORMATBz2 = "edf.bz2";
	
	final static String CORFORMAT = "cor";
	final static String CORFORMATGZip = "cor.gz";
	final static String CORFORMATBz2 = "cor.bz2";
	
	final static String TIFFORMAT = "tif";
	final static String TIFFORMATGZip = "tif.gz";
	final static String TIFFORMATBz2 = "tif.bz2";
	final static String TIFF_FORMAT = "tiff", TIFF_FORMATGZip = "tiff.gz",
			TIFF_FORMATBz2 = "tiff.bz2";
	
	final static String ADSCFORMAT = "img", ADSCFORMATGZip = "img.gz",
			ADSCFORMATBz2 = "img.bz2";
	
	final static String MCCDFORMAT = "mccd", MCCDFORMATGZip = "mccd.gz",
			MCCDFORMATBz2 = "mccd.bz2";
	
	final static String MAR2300FORMAT = "mccd", mar2300FormatGZip = "mccd.gz",
			MAR2300FORMATBz2 = "mccd.bz2";
	final static String PNMFORMAT = "pnm", PGMFORMAT = "pgm", PBMFORMAT = "pbm";
	
	final static String BRUKERFORMAT = "\\.\\d{4}$",
			BRUKERFORMATBz2 = "\\.\\d{4}\\.bz2$";
	final static String CCDFORMAT = "ccd";
	final static String UPDATEFILES_EVENT = "updatefiles";

	/**
	 * This is a regular expression for all fabio type.
	 */
	static String REGEX_FABIO_TYPES = EDFFORMAT + "|" + EDFFORMATGZip + "|"
			+ EDFFORMATBz2 + "|" + CORFORMAT + "|" + CORFORMATGZip + "|"
			+ CORFORMATBz2 + "|" + CCDFORMAT + "|" + TIFFORMAT + "|"
			+ TIFFORMATGZip + "|" + TIFFORMATBz2 + "|" + TIFF_FORMAT + "|"
			+ TIFF_FORMATGZip + "|" + TIFF_FORMATBz2 + "|" + ADSCFORMAT + "|"
			+ ADSCFORMATGZip + "|" + ADSCFORMATBz2 + "|" + MCCDFORMAT + "|"
			+ MCCDFORMATGZip + "|" + MCCDFORMATBz2 + "|" + MAR2300FORMAT + "|"
			+ mar2300FormatGZip + "|" + MAR2300FORMATBz2 + "|" + PNMFORMAT
			+ "|" + PGMFORMAT + "|" + PBMFORMAT + "|" + BRUKERFORMAT + "|"
			+ BRUKERFORMATBz2;

	/**
	 * this string represents a list of file type name managed by fabio.
	 * <p>
	 * This list set the name of "bruker" in the list of type files (for example
	 * in <code>SampleNavigatorPreferences</code>) instead of a regular
	 * expression.
	 */
	public static String FABIO_TYPES = EDFFORMAT + "|" + EDFFORMATGZip + "|"
			+ EDFFORMATBz2 + "|" + CORFORMAT + "|" + CORFORMATGZip + "|"
			+ CORFORMATBz2 + "|"  + "bruker" + "|"
			+ "bruker.bz2";


	private static final Collection<String> IMAGES;
	static {
		Collection<String> set = new HashSet<String>(31);
		set.add(TIFFORMAT);
		set.add(TIFFORMATBz2);
		set.add(TIFFORMATGZip);
		set.add(TIFF_FORMAT);
		set.add(TIFF_FORMATBz2);
		set.add(TIFF_FORMATGZip);
		set.add(ADSCFORMAT);
		set.add(ADSCFORMATBz2);
		set.add(ADSCFORMATGZip);
		set.add(EDFFORMAT);
		set.add(EDFFORMATBz2);
		set.add(EDFFORMATGZip);
		set.add(CORFORMAT);
		set.add(CORFORMATBz2);
		set.add(CORFORMATGZip);
		set.add(BRUKERFORMAT);
		set.add(BRUKERFORMATBz2);
		set.add(MCCDFORMAT);
		set.add(MCCDFORMATBz2);
		set.add(MCCDFORMATGZip);
		set.add(MAR2300FORMAT);
		set.add(MAR2300FORMATBz2);
		set.add(mar2300FormatGZip);
		set.add(CCDFORMAT);
		set.add(PNMFORMAT);
		set.add(PGMFORMAT);
		set.add(PBMFORMAT);
		set.add("jpg");
		set.add("jpeg");
		set.add("png");
		set.add("gif");
		set.add("cbf");
		set.add("cbf.bz2");
		
		IMAGES = Collections.unmodifiableCollection(set);
	}
	
	public static final boolean isImage(final String fileName) {
		int posExt = fileName.lastIndexOf(".");
		String ext = posExt < 0 ? fileName : fileName.substring(posExt+1);
        if (IMAGES.contains(ext.toLowerCase())) return true;
        
        try {
	        posExt = fileName.lastIndexOf('.', posExt-1);
	        ext = posExt < 0 ? fileName : fileName.substring(posExt+1);
	        return IMAGES.contains(ext.toLowerCase());
        } catch (Throwable ne) {
        	return false;
        }
	}
	
	/**
	 * Return true if adsc or compressed adsc - img
	 * @param path
	 * @return
	 */
	public static boolean isTiff(final String path) {
		if (path==null) return false;
		if (path.toLowerCase().endsWith(TIFFORMAT))       return true;
		if (path.toLowerCase().endsWith(TIFFORMATGZip))   return true;
		if (path.toLowerCase().endsWith(TIFFORMATBz2))    return true;
		if (path.toLowerCase().endsWith(TIFF_FORMAT))     return true;
		if (path.toLowerCase().endsWith(TIFF_FORMATGZip)) return true;
		if (path.toLowerCase().endsWith(TIFF_FORMATBz2))  return true;
		return false;
	}
	/**
	 * Return true if adsc or compressed adsc - img
	 * @param path
	 * @return
	 */
	public static boolean isImg(final String path) {
		if (path==null) return false;
		if (path.toLowerCase().endsWith(ADSCFORMAT)) return true;
		if (path.toLowerCase().endsWith(ADSCFORMATGZip)) return true;
		if (path.toLowerCase().endsWith(ADSCFORMATBz2)) return true;
		return false;
	}
	/**
	 * Return true if edf or compressed edf
	 * @param path
	 * @return
	 */
	public static boolean isEdf(final String path) {
		if (path==null) return false;
		if (path.toLowerCase().endsWith(EDFFORMAT)) return true;
		if (path.toLowerCase().endsWith(EDFFORMATGZip)) return true;
		if (path.toLowerCase().endsWith(EDFFORMATBz2)) return true;
		return false;
	}
	
	/**
	 * Return true if cor or compressed cor
	 * @param path
	 * @return
	 */
	public static boolean isCor(final String path) {
		if (path==null) return false;
		if (path.toLowerCase().endsWith(CORFORMAT)) return true;
		if (path.toLowerCase().endsWith(CORFORMATGZip)) return true;
		if (path.toLowerCase().endsWith(CORFORMATBz2)) return true;
		return false;
	}
	
	/**
	 * Return true if cor or compressed cor
	 * @param path
	 * @return
	 */
	public static boolean isBruker(final String path) {
		if (path==null) return false;
		if (Pattern.compile(".*"+BRUKERFORMAT).matcher(path).matches())    return true;
		if (Pattern.compile(".*"+BRUKERFORMATBz2).matcher(path).matches()) return true;
		return false;
	}

}
