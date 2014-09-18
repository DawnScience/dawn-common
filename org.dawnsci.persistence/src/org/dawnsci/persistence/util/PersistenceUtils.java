package org.dawnsci.persistence.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.dawnsci.analysis.api.fitting.functions.IFunction;

public class PersistenceUtils {

	/**
	 * Method that reads a file and returns the resulting String
	 * @param filePath
	 * @return String
	 *       the content of the file read
	 */
	public static String readFile(String filePath){
		StringBuilder contents = new StringBuilder();
		
		try {
			// use buffering, reading one line at a time
			// FileReader always assumes default encoding is OK!
			BufferedReader input = new BufferedReader(new FileReader(filePath));
			try {
				String line = null;int i = 0;
				while ((line = input.readLine()) != null) {
					if(i>0)
						contents.append(System.getProperty("line.separator"));
					contents.append(line);
					i++;
				}

			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return contents.toString();
	}

	/**
	 * Returns a class instance
	 * @param sClazz
	 * @return
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws InvocationTargetException
	 */
	public static Object getInstance(String sClazz)
			throws ClassNotFoundException, NoSuchMethodException,
			SecurityException, InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		Class<?> clazz = Class.forName(sClazz);
		Constructor<?> constructor = clazz.getConstructor();
		return (IFunction) constructor.newInstance();
	}
}
