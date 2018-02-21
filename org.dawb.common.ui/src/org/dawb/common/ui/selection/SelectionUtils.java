package org.dawb.common.ui.selection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.analysis.api.io.IDataHolder;
import org.eclipse.dawnsci.analysis.api.tree.Attribute;
import org.eclipse.dawnsci.analysis.api.tree.Node;
import org.eclipse.dawnsci.analysis.api.tree.NodeLink;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetUtils;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.january.dataset.ILazyDataset;
import org.eclipse.january.metadata.IMetadata;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

public class SelectionUtils {

	private static final Logger logger = LoggerFactory.getLogger(SelectionUtils.class);

	/**
	 * Method that loads data given an IStructuredSelection
	 * @param selection
	 * @return IDataset
	 */
	public static IDataset loadData(IStructuredSelection selection){
		Object item = selection.getFirstElement();
		if (item instanceof IFile) {
			String filename = ((IFile) item).getRawLocation().toOSString();
			return loadData(filename,
						"/entry1/instrument/analyser/data");
		}
		// if the selection is an hdf5 tree item
		else if (selection instanceof ITreeSelection) {
			SelectedTreeItemInfo[] results = SelectionUtils.parseAsTreeSelection((ITreeSelection) selection);
			if (results.length > 0 && results[0].getFile() != null) {
				return loadData(results[0].getFile(), results[0].getNode());
			}
		}
		return null;
	}

	/**
	 * Method that loads data given a filename and a data path
	 * @param fileName
	 *             the name of the data
	 * @param dataPath
	 *             if a NXS file, the data path, otherwise can be null
	 * @return the data loaded as an Dataset, null if none or not found
	 */
	public static Dataset loadData(final String fileName, final String dataPath){
		Dataset dataset = null;
		try {
			IDataHolder data = LoaderFactory.getData(fileName, null);
			IMetadata md = data.getMetadata();
			Map<String, ILazyDataset> map = data.toLazyMap();
			ILazyDataset tmpvalue = map.get(dataPath);
			if(tmpvalue == null) tmpvalue = map.get(data.getName(0));

			ILazyDataset value = tmpvalue.squeezeEnds();
			if(value.getShape().length == 2) {
				dataset = DatasetUtils.sliceAndConvertLazyDataset(value.getSliceView());
				dataset.setMetadata(md);
				return dataset;
			}
			logger.warn("Dataset not the right shape for showing in the preview");
			return null;
		} catch (Exception e) {
			logger.error("Error loading data", e);
			return null;
		}
	}

	/**
	 * Method that gives the file name of the IStructuredSelection
	 * @param selection
	 * @return String
	 */
	public static String getFileName(IStructuredSelection selection){
		Object item = selection.getFirstElement();
		if (item instanceof IFile) {
			return ((IFile) item).getName();
		}
		// if the selection is an hdf5 tree item
		else if (selection instanceof ITreeSelection) {
			SelectedTreeItemInfo[] results = SelectionUtils.parseAsTreeSelection((ITreeSelection) selection);
			if (results.length > 0 && results[0].getFile() != null) {
				File f = new File(results[0].getFile());
				return f.getName();
			}
		}
		return null;
	}

	/**
	 * Method that gives the full file path of the IStructuredSelection
	 * @param selection
	 * @return String
	 */
	public static String getFullFilePath(IStructuredSelection selection){
		Object item = selection.getFirstElement();
		if (item instanceof IFile) {
			return ((IFile) item).getRawLocation().toOSString();
		}
		// if the selection is an hdf5 tree item
		else if (selection instanceof ITreeSelection) {
			SelectedTreeItemInfo[] results = SelectionUtils.parseAsTreeSelection((ITreeSelection) selection);
			if (results.length > 0 && results[0].getFile() != null) {
				return results[0].getFile();
			}
		}
		return null;
	}

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

	/**
	 * Parse tree selection as file paths
	 * @param selection
	 * @return list of file paths
	 */
	public static List<String> parseTreeSelectionAsFilePaths(ITreeSelection selection) {
		List<String> paths = new ArrayList<>();
		for (Object o : selection.toArray()) {
			if (!(o instanceof IFile) && o instanceof IAdaptable) {
				o = ((IAdaptable) o).getAdapter(IFile.class);
			}
			if (o instanceof IFile) {
				IFile file = (IFile) o;
				paths.add(file.getLocation().toOSString());
			}
		}
		return paths;
	}
}
