package org.dawb.common.ui.selection;

import org.eclipse.core.resources.IFile;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;

public class SelectionUtils {

	/**
	 * Obtain file path, node path and last object from a tree selection
	 * @param selection
	 * @return array of file path, node path and last object (any can be null)
	 */
	public static SelectedTreeItemInfo[] parseAsTreeSelection(ITreeSelection selection) {
		TreePath[] paths = selection.getPaths();
		int np = paths.length;
		SelectedTreeItemInfo[] results = new SelectedTreeItemInfo[np];
		for (int p = 0; p < np; p++) {
			TreePath path = paths[p];
			int n = path.getSegmentCount();
			StringBuilder fullPath = new StringBuilder();
			SelectedTreeItemInfo info = new SelectedTreeItemInfo();
			results[p] = info;
			Object obj = null;
			for (int i = 0; i < n; i++) {
				obj = path.getSegment(i);
				if (obj instanceof IFile) {
					info.file = ((IFile) obj).getLocation().toOSString();
				} else if (obj instanceof NodeLink) {
					fullPath.append(Node.SEPARATOR);
					fullPath.append(((NodeLink) obj).getName());
				} else if (obj instanceof Attribute) {
					fullPath.append(Node.ATTRIBUTE);
					fullPath.append(((Attribute) obj).getName());
				}
			}
			if (fullPath.length() > 0) {
				info.node = fullPath.toString();
			}
			info.item = obj;
		}
		return results;
	}
}
