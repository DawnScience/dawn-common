package org.dawb.common.ui.selection;

/**
 * Class to hold file path, node path and selected object
 *
 */
public class SelectedTreeItemInfo {
	String file;
	String node;
	Object item;

	/**
	 * @return full path to file
	 */
	public String getFile() {
		return file;
	}

	/**
	 * @return full path to selected item
	 */
	public String getNode() {
		return node;
	}

	/**
	 * @return selected item
	 */
	public Object getItem() {
		return item;
	}
}
