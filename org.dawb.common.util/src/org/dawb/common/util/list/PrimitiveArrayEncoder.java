/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.common.util.list;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;


/**
 * Simple class for ensuring that number arrays can be encoded to 
 * XML without separate tags which bloat the XML.
 */
public class PrimitiveArrayEncoder {

	/**
	 * String->int[]
	 * @param dataString
	 * @return i
	 */
	public static int[] getIntArray(String dataString) {
		if (dataString == null) return null;
		final String tmp = dataString.substring(1,dataString.length()-1);
		
		return getIntArrayNoBrackets(tmp);
	}
	

	public static int[] getIntArrayNoBrackets(String commaSeped) {
		final String[] d = commaSeped.split(", ");
		final int[] ret  = new int[d.length];
		for (int i = 0; i < d.length; i++) {
			ret[i] = Integer.parseInt(d[i]);
		}
	    return ret;	
	}


	/**
	 * int[] -> String
	 * Simple comma separated encoding.
	 * @param data
	 * @return s
	 */
	public static String getString(int[] data) {
		if (data==null) return null;
		final List<Integer> list = new ArrayList<Integer>(data.length);
		for (int i = 0; i < data.length; i++) list.add(data[i]);
		return list.toString();
	}

	
	/**
	 * String->double[]
	 * @param dataString
	 * @return i
	 */
	public static double[] getDoubleArray(String dataString) {
		if (dataString == null) return null;
		if ("[]".equals(dataString)) return null;
		final String tmp = dataString.substring(1,dataString.length()-1);
		final String[] d = tmp.split(", ");
		final double[] ret  = new double[d.length];
		for (int i = 0; i < d.length; i++) {
			ret[i] = Double.parseDouble(d[i]);
		}
		return ret;
	}

	/**
	 * double[] -> String
	 * Simple comma separated encoding.
	 * @param data
	 * @return s
	 */
	public static String getString(double[] data) {
		if (data==null) return null;
		final List<Double> list = new ArrayList<Double>(data.length);
		for (int i = 0; i < data.length; i++) list.add(data[i]);
		return list.toString();
	}

	/**
	 * Compress a string
	 * @param expanded
	 * @return compressed string.
	 */
	public static String compress(final String expanded) {

		try {
			byte[] input = expanded.getBytes("US-ASCII");

			// Compress the bytes
			byte[] output = new byte[input.length];
			Deflater compresser = new Deflater();
			compresser.setInput(input);
			compresser.finish();
			final int size  = compresser.deflate(output);

			return new String(output, 0, size, "US-ASCII");
		} catch (Exception ne) {
			ne.printStackTrace();
			return null;
		}
	}

	/**
	 * Inflate a string
	 * @param compressed
	 * @return Inflated string.
	 */
	public static String expand(final String compressed) {

		try {
			final byte[] output = compressed.getBytes("US-ASCII");

			// Decompress the bytes
			Inflater decompresser = new Inflater();
			decompresser.setInput(output, 0, output.length);

			byte[] result = new byte[output.length*2];
			int resultLength = decompresser.inflate(result);
			decompresser.end();

			// Decode the bytes into a String
			return new String(result, 0, resultLength, "US-ASCII");

		} catch (Exception ne) {
			ne.printStackTrace();
			return null;
		}

	}

}
