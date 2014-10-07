/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;

public class Grep {

    // Pattern used to parse lines
    private static Pattern LINE_PATTERN = Pattern.compile(".*\r?\n?");


    // Use the linePattern to break the given CharBuffer into lines, applying
    // the input pattern to each line to see if we have a match
    private static void grep(final CharBuffer         cb,
					    	 final Pattern            pattern,
					    	 final List<CharSequence> ret) {
    	
        Matcher lm = LINE_PATTERN.matcher(cb);  // Line matcher
        Matcher pm = null;                     // Pattern matcher
        //int lines = 0;
        while (lm.find()) {
            //lines++;
            CharSequence cs = lm.group();      // The current line
            if (pm == null)
                pm = pattern.matcher(cs);
            else
                pm.reset(cs);
            while (pm.find())
            	ret.add(cs);
            if (lm.end() == cb.limit())
                break;
        }
    }

    /**
     * 
     * @param toSearch
     * @param regExp
     * @param charsetText
     * @return
     * @throws Exception
     */
    public static List<CharSequence> grep(final File toSearch, 
								          final String regExp) throws Exception {
    	
        return grep(new FileInputStream(toSearch), regExp, "UTF-8");
    }

    /**
     * 
     * @param toSearch
     * @param regExp
     * @param charsetText
     * @return
     * @throws Exception
     */
    public static List<CharSequence> grep(final File toSearch, 
								          final String regExp,
								          final String charsetText) throws Exception {
    	
        return grep(new FileInputStream(toSearch), regExp, charsetText);
    }
    
    /**
     * 
     * @param toSearch
     * @param regExp
     * @return
     * @throws Exception
     */
    public static List<CharSequence> grep(final IFile toSearch, 
								          final String regExp) throws Exception {

    	FileInputStream stream = null;
    	final InputStream in = toSearch.getContents();
    	if (in instanceof FileInputStream) {
    		stream = (FileInputStream)in;
    	} else {
    		stream = new FileInputStream(toSearch.getLocation().toFile());
    	}
        return grep(stream, regExp, toSearch.getCharset());
   }
    /**
     * Search for occurrences of the input pattern in the given file
     * @param toSearch
     * @param regExp
     * @param charsetText
     * @return
     * @throws IOException
     */
    public static List<CharSequence> grep(final FileInputStream fis, 
		    		                      final String regExp,
		                                  final String charsetText) throws IOException {

        final Charset        charset = Charset.forName(charsetText); // "ISO-8859-15"
        final CharsetDecoder decoder = charset.newDecoder();

        final Pattern pattern = Pattern.compile(regExp);

        // Open the file and then get a channel from the stream
        final FileChannel     fc  = fis.getChannel();

        final List<CharSequence> ret = new ArrayList<CharSequence>(7);
        try {
	        // Get the file's size and then map it into memory
	        int sz = (int)fc.size();
	        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
	
	        // Decode the file into a char buffer
	        CharBuffer cb = decoder.decode(bb);
	
	        // Perform the search
	        grep(cb, pattern, ret);
	
	        // Close the channel and the stream
        } finally {
	        fc.close();
        }
        
        return ret;
    }
    

    public static Matcher matcher(final File toSearch, 
					    		final String regExp,
					    		final String charsetText) throws Exception {

    	return matcher(new FileInputStream(toSearch), regExp, charsetText);
    }

    public static Matcher matcher(final IFile toSearch, 
    							  final String regExp) throws Exception {

    	FileInputStream stream = null;
    	final InputStream in = toSearch.getContents();
    	if (in instanceof FileInputStream) {
    		stream = (FileInputStream)in;
    	} else {
    		stream = new FileInputStream(toSearch.getLocation().toFile());
    	}
    	return matcher(stream, regExp, toSearch.getCharset());
    }
    
    /**
     * Search for occurrences of the input pattern in the given file
     * @param toSearch
     * @param regExp
     * @param charsetText
     * @return
     * @throws IOException
     */
    public static Matcher matcher(final FileInputStream fis, 
								  final String regExp,
								  final String charsetText) throws IOException {

    	final Charset        charset = Charset.forName(charsetText); // "ISO-8859-15"
    	final CharsetDecoder decoder = charset.newDecoder();

    	final Pattern pattern = Pattern.compile(regExp);

    	// Open the file and then get a channel from the stream
    	final FileChannel     fc  = fis.getChannel();

     	try {
    		// Get the file's size and then map it into memory
    		int sz = (int)fc.size();
    		MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);

    		// Decode the file into a char buffer
    		CharBuffer cb = decoder.decode(bb);

    		// Perform the search
    		return matcher(cb, pattern);

    		// Close the channel and the stream
    	} finally {
    		fc.close();
    	}
    }


    private static Matcher matcher(final CharBuffer         cb,
    		                       final Pattern            pattern) {

    	Matcher lm = LINE_PATTERN.matcher(cb);  // Line matcher
    	Matcher pm = null;                     // Pattern matcher
    	//int lines = 0;
    	while (lm.find()) {
    		//lines++;
    		CharSequence cs = lm.group();      // The current line
    		if (pm == null)
    			pm = pattern.matcher(cs);
    		else
    			pm.reset(cs);
    		if (pm.find()) return pm;
    		if (lm.end() == cb.limit()) break;
    	}
    	
    	return null;
    }

    
    

    // Use the linePattern to break the given CharBuffer into lines, applying
    // the input pattern to each line to see if we have a match
    private static void group(final CharBuffer         cb,
					    	 final Pattern            pattern,
					    	 final List<CharSequence> ret,
					    	 final int                group) {
    	
        Matcher lm = LINE_PATTERN.matcher(cb);  // Line matcher
        Matcher pm = null;                     // Pattern matcher
        //int lines = 0;
        while (lm.find()) {
            //lines++;
            CharSequence cs = lm.group();      // The current line
            if (pm == null)
                pm = pattern.matcher(cs);
            else
                pm.reset(cs);
            while (pm.find())
            	ret.add(pm.group(group));
            if (lm.end() == cb.limit())
                break;
        }
 
    }

    /**
     * 
     * @param toSearch
     * @param regExp
     * @param charsetText
     * @return
     * @throws Exception
     */
    public static List<CharSequence> group(final File toSearch, 
								          final String regExp,
								          final String charsetText,
									      final int    group) throws Exception {
    	
        return group(new FileInputStream(toSearch), regExp, charsetText, group);
    }
    
    /**
     * 
     * @param toSearch
     * @param regExp
     * @return
     * @throws Exception
     */
    public static List<CharSequence> group(final IFile  toSearch, 
								           final String regExp,
									       final int    group) throws Exception {

    	FileInputStream stream = null;
    	final InputStream in = toSearch.getContents();
    	if (in instanceof FileInputStream) {
    		stream = (FileInputStream)in;
    	} else {
    		stream = new FileInputStream(toSearch.getLocation().toFile());
    	}
        return group(stream, regExp, toSearch.getCharset(), group);
   }
    /**
     * Search for occurrences of the input pattern in the given file
     * @param toSearch
     * @param regExp
     * @param charsetText
     * @return
     * @throws IOException
     */
    public static List<CharSequence> group(final FileInputStream fis, 
		    		                       final String regExp,
		                                   final String charsetText,
		     					    	   final int    group) throws IOException {

        final Charset        charset = Charset.forName(charsetText); // "ISO-8859-15"
        final CharsetDecoder decoder = charset.newDecoder();

        final Pattern pattern = Pattern.compile(regExp);

        // Open the file and then get a channel from the stream
        final FileChannel     fc  = fis.getChannel();

        final List<CharSequence> ret = new ArrayList<CharSequence>(7);
        try {
	        // Get the file's size and then map it into memory
	        int sz = (int)fc.size();
	        MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
	
	        // Decode the file into a char buffer
	        CharBuffer cb = decoder.decode(bb);
	
	        // Perform the search
	        group(cb, pattern, ret, group);
	
	        // Close the channel and the stream
        } finally {
	        fc.close();
        }
        
        return ret;
    }

    /**
     * Returns all the groups which match from the input string.
     * @param fileName
     * @param regExp
     * @param group
     * @return
     */
	public static List<String> group(String data, String regExp, int group) {
		
		final List<String> ret = new ArrayList<String>(7);
		final Pattern p = Pattern.compile(regExp);
		final Matcher m = p.matcher(data);
		while (m.find()) {
			ret.add(m.group(group));
		}
		return ret;
	}
    

}
