package org.dawb.hdf5.model.internal;

import org.dawb.hdf5.model.IHierarchicalDataFileModel;


public interface IHierarchicalDataModelGetFileModel {
	IHierarchicalDataFileModel createFileModel(String fullPath);
}
