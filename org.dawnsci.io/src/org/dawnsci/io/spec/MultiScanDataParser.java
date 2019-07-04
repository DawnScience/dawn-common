/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.io.spec;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;

/**
 * Class deals with multi-scan ASCII SPEC file data.
 * 
 * This file acts like a data structure returned by LoaderFactory but
 * has more levels and deals normally with small data.
 * 
 * Unlike hdf5 everything is read into memory at the start.
 * 
 * @author gerring
 *
 */
public class MultiScanDataParser {

	private int scanNumber = 0;
	private Map<String,Collection<Dataset>> data;

	/**
	 * Parses everything into memory, blocks until done.
	 * @param input
	 * @throws Exception
	 */
	public MultiScanDataParser(final InputStream input) throws Exception {
		
		data = new LinkedHashMap<String, Collection<Dataset>>(27);
		createData(input);
		finishScan(getScanName());
	}

	public Collection<Dataset> getSets(final String scanName) {
		return data.get(scanName);
	}

	public Collection<Dataset> removeScan(final String scanName) {
		return data.remove(scanName);
	}

	public Collection<String> getScanNames() {
		return data.keySet();
	}

	public void clear() {
		data.clear();
	}

	private void createData(final InputStream in) throws Exception {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
		try {
			String line = null;
			boolean firstLine = true;
			while ((line = reader.readLine()) != null) {
				if (firstLine) {
					firstLine = false;
					if (line.trim().startsWith("&")) {
						throw new Exception("Cannot load SRS files with SpecLoader!");
					}
				}
				processLine(line);
			}
		} finally {
			reader.close();
		}
	}

	private String             previousLine = null;
	private List<NumberObject> currentScans;
	private List<String>       currentNames;

	/**
	 * Processes a line and adds it to the scan data.
	 * Returns true if data added requires a replot.
	 * @param line
	 */
	public synchronized void processLine(String line) {
		if (line==null)      return;
		line = line.trim();
		if (line.isEmpty()) return;

		final Matcher com = SpecSyntax.COMMENT_LINE.matcher(line);
		if (com.matches()) {
			previousLine = line;
			return;
		}

		final Matcher scan = SpecSyntax.SCAN_LINE.matcher(line);
		if (!scan.matches()) return;

		boolean newScan = false;
		if (previousLine != null) {
			final Matcher labelLine = SpecSyntax.LABEL_LINE.matcher(previousLine);

			if (labelLine.matches()) {
				newScan = true;
				finishScan(getScanName());
				startScan(SpecSyntax.LABEL_VALUE.matcher(previousLine.substring(2))); // remove "#L" from label line
			}
		}

		addData(newScan, SpecSyntax.SCAN_VALUE.matcher(line));
		return;
	}

	/**
	 * Converts currentScans to datasets and adds them to data
	 * @return true if listener and it is still interested, 
	 *         true if no listener
	 *         false if listener is finished.
	 */
	private void finishScan(final String scanName) {
		update(true);
	}

	public void update(final boolean endScan) {
		if (currentScans == null) return;

		final Collection<Dataset> sets = data.get(getScanName());

		// Now add new datasets
		if (sets != null) {
			sets.clear();
			for (NumberObject o : currentScans) {
				sets.add(o.toDataset());
			}
		}

		if (endScan) currentScans.clear();
	}

	private void startScan(final Matcher labels) {
		previousLine = null;
		scanNumber++;

		if (currentNames==null) currentNames = new ArrayList<String>(27);
		if (currentScans==null) currentScans = new ArrayList<NumberObject>(27);
		currentNames.clear();
		currentScans.clear();

		int l = 0;
		while (labels.find()) {
			l++;
			String name = createGoodName(currentNames, labels.group(), l);
			currentNames.add(name);
		}

		Collection<Dataset> sets = new ArrayList<>(currentScans.size());
		data.put(getScanName(), sets);
	}

	static String createGoodName(List<String> names, String name, int l) {
		if (name == null) {
			name = "Column " + l;
		} else {
			name = name.trim();
			if (name.isEmpty()) {
				name = "Column " + l;
			}
		}
		if (names.contains(name)) {
			String n = name;
			int i = 2;
			do {
				n = name + i++;
			} while (names.contains(n));
			name = n;
		}

		return name;
	}

	private String getScanName() {
		return "Scan " + scanNumber;
	}

	private void addData(final boolean newScan, final Matcher values) {
		
		int index = -1;
		while (values.find()) {
			index++;
			final String val = values.group();
			if (val==null || val.isEmpty()) continue;
			if (val.toLowerCase().startsWith("e")) continue;

			// Pretty inefficient but there we go.
			if (newScan) {
				final NumberObject ad = new NumberObject();
				String name;
				if (index < currentNames.size()) {
					name = currentNames.get(index);
				} else {
					name = "Column " + index + 1;
				}
				ad.setName(name);
				currentScans.add(ad);
			}

			final NumberObject ad = currentScans.get(index);
			ad.add(Float.parseFloat(val.trim()));
		}
		previousLine = null;
	}

	private static class NumberObject {

		private String name;
		private List<Number> numbers;

		NumberObject() {
			setNumbers(new ArrayList<Number>(31));
		}

		public Dataset toDataset() {
			if (numbers.isEmpty()) {
				return null;
			}

			Dataset ret = DatasetFactory.createFromList(numbers);
			ret.setName(getName());

			return ret;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setNumbers(List<Number> numbers) {
			this.numbers = numbers;
		}

		public void add(Number n) {
			numbers.add(n);
		}
	}
}
