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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.FileChannel;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IOUtils {


	private final static Logger logger = LoggerFactory.getLogger(IOUtils.class);
	/**
	 * Unconditionally close a <code>ZipFile</code>.
	 * and optinally log errors 
	 */
	public static void close( FileChannel channel, String msg)
	{
		if( channel == null )  {
			logger.error("FileChannel is null", msg);
			return;
		}

		try
		{
			channel.close();
		}
		catch( IOException ioe )
		{
			logger.error("Cannot close stream", ioe);    
		}
	}

	/**
	 * Unconditionally close a <code>ZipFile</code>.
	 * and optinally log errors 
	 *
	 * @param input A (possibly null) Reader
	 */
	public static void close( ZipFile input, String msg)
	{
		if( input == null )
		{
			logger.error("ZipFile is null",msg,1);
			return;
		}

		try
		{
			input.close();
		}
		catch( IOException ioe )
		{
			logger.error("Cannot close stream", ioe);    
		}
	}

	/**
	 * Unconditionally close a <code>Reader</code>.
	 * and optinally log errors 
	 *
	 * @param input A (possibly null) Reader
	 */
	public static void close( Reader input, String msg)
	{
		if( input == null )
		{
			logger.error("Reader is null",msg,1);
			return;
		}

		try
		{
			input.close();
		}
		catch( IOException ioe )
		{
			logger.error("Cannot close stream", ioe);   
		}
	}

	/**
	 * Unconditionally close an <code>Writer</code>.
	 * and optinally log errors
	 *
	 * @param output A (possibly null) Writer
	 */
	public static void close( Writer output, String msg)
	{
		if( output == null )
		{
			logger.error("Writer is null",msg,1);
			return;
		}

		try
		{
			output.close();
		}
		catch( IOException ioe )
		{
			logger.error("Cannot close stream", ioe);   
		}
	}

	/**
	 * Unconditionally close an <code>OutputStream</code>.
	 * and optinally log errors
	 * @param output A (possibly null) OutputStream
	 */
	public static void close( OutputStream output, String msg)
	{
		if( output == null )
		{
			logger.error("OutputStream is null",msg,1);
			return;
		}

		try
		{
			output.close();
		}
		catch( IOException ioe )
		{
			logger.error("Cannot close stream", ioe);   
		}
	}

	/**
	 * Unconditionally close an <code>InputStream</code>.
	 * and optinally log errors
	 * @param input A (possibly null) InputStream
	 */
	public static void close( InputStream input, String msg)
	{
		if( input == null )
		{
			logger.error("InputStream is null",msg,1);
			return;
		}

		try
		{
			input.close();
		}
		catch( IOException ioe )
		{
			logger.error("Cannot close stream", ioe);   
		}
	}

	/** return information about possible null file for use in diagnostic message */
	public static String fileInfo(File f) {
		if (f==null) {
			return "File is null";
		}
		else {
			return f.getPath();
		}
	}

	/** return information about possible null file for use in diagnostic message */
	public static String fileInfo(ZipFile f) {
		if (f==null) {
			return "File is null";
		}
		else {
			return f.getName();
		}
	}

	/**
	 * Checks if the path given is a directory (readable or writable)
	 * @param path
	 * @param forRead
	 * @return boolean
	 */
	public static boolean checkDirectory(final String path, boolean forRead) {
		if (path == null || path.length() == 0) {
			logger .warn("No path given");
			return false;
		}
		File f = new File(path);
		if (!f.exists() || f.isFile()) {
			logger.warn("Path does not exist or is not a directory");
			return false;
		}
		return forRead ? f.canRead() : f.canWrite();
	}

	/**
	 * Checks if the path given links to a file (readable or writable)
	 * @param path
	 * @param forWrite
	 * @return boolean
	 */
	public static boolean checkFile(final String path, boolean forWrite) {
		if (path == null || path.length() == 0) {
			logger.warn("No path given");
			return false;
		}
		File f = new File(path);
		if (!f.exists() || !f.isFile()) {
			logger.warn("Path does not exist or is not a file");
			return false;
		}
		return forWrite ? f.canWrite() : f.canRead();
	}
}
