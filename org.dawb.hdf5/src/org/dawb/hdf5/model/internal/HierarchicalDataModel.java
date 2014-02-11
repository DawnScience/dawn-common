package org.dawb.hdf5.model.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dawb.hdf5.model.IHierarchicalDataFileModel;
import org.dawb.hdf5.model.IHierarchicalDataModel;
import org.eclipse.core.resources.IFile;

public class HierarchicalDataModel implements IHierarchicalDataModel {
	private Map<String, IHierarchicalDataFileModel> cache = Collections
			.synchronizedMap(new HashMap<String, IHierarchicalDataFileModel>());
	private IHierarchicalDataModelGetFileModel getModel;

	public HierarchicalDataModel(IHierarchicalDataModelGetFileModel getModel) {
		this.getModel = getModel;
	}

	@Override
	public IHierarchicalDataFileModel getFileModel(IFile file) {
		synchronized (cache) {
			final String fullPath = file.getRawLocation().toOSString();
			if (cache.containsKey(fullPath)) {
				return cache.get(fullPath);
			}

			IHierarchicalDataFileModel model = getModel.createFileModel(fullPath);
			cache.put(fullPath, model);
			return model;
		}
	}

	/**
	 * Remove any cached information about a given file.
	 * <p>
	 * It is expected that this is called on Eclipse resource deltas to remove
	 * files that have been modified.
	 *
	 * @param file
	 *            to expunge cache for
	 */
	public void clearFileCache(IFile file) {
		if (file==null) return;
		synchronized (cache) {
			cache.remove(file.getRawLocation().toOSString());
		}
	}
}
