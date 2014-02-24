package org.dawb.common.util.io;


public class DLSPath {


	public static String getCorrectedPath(String path) {
		if (path ==null) return null;
		if (isWindowsOS() && path.startsWith("/dls/")) {
			path = "\\\\Data.diamond.ac.uk\\"+path.substring(5);
		}
		return path;
	}

	/**
	 * @return true if windows
	 */
	static public boolean isWindowsOS() {
		return (System.getProperty("os.name").indexOf("Windows") == 0);
	}

}
