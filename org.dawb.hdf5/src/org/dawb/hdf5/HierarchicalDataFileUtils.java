package org.dawb.hdf5;

import java.util.ArrayList;
import java.util.List;

import ncsa.hdf.object.Group;

public class HierarchicalDataFileUtils {
	// delete empty strings
	private static String[] cleanArray(String[] array) {

		List<String> list = new ArrayList<String>();
		for (int i = 0; i < array.length; i++) {
			if (!array[i].isEmpty()) {
				list.add(array[i]);
			}
		}
		String[] result = new String[list.size()];
		return list.toArray(result);
	}

	/**
	 * Creates the group identified by fullEntry, marking all nodes as Nexus, and the last as the given nexusEntry
	 * 
	 * @param file
	 * @param fullEntry - path to be created
	 * @param nexusEntry - attribute value for final node
	 * @return
	 * @throws Exception
	 */
	public static Group createParentEntry(IHierarchicalDataFile file,
			String fullEntry, String nexusEntry) throws Exception {
		String[] entries = fullEntry.split("/");
		entries = cleanArray(entries);
		Group parent = null;
		for (int i = 0; i < entries.length; i++) {
			ncsa.hdf.object.Group entry = null;
			if (i == 0) {
				entry = file.group(entries[i]);
				file.setNexusAttribute(entry, Nexus.ENTRY);
				parent = entry;
			} else if (i == entries.length - 1) {
				entry = file.group(entries[i], parent);
				file.setNexusAttribute(entry, nexusEntry);
				parent = entry;
			} else {
				entry = file.group(entries[i], parent);
				file.setNexusAttribute(entry, Nexus.ENTRY);
				parent = entry;
			}
		}
		return parent;
	}

}
