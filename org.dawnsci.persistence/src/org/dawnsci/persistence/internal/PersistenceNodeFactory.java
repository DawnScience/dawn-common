package org.dawnsci.persistence.internal;

import java.io.Serializable;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.diffraction.IPowderCalibrationInfo;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentNodeFactory;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.january.metadata.OriginMetadata;

public class PersistenceNodeFactory implements IPersistentNodeFactory {

	@Override
	public IOperation[] readOperationsFromTree(Tree tree) throws Exception {
		return PersistJsonOperationsNode.readOperations(tree);
	}

	@Override
	public GroupNode writeOperationsToGroup(IOperation[] operations) {
		return PersistJsonOperationsNode.writeOperationsToNode(operations);
	}

	@Override
	public GroupNode writeOriginalDataInformation(OriginMetadata origin) {
		return PersistJsonOperationsNode.writeOriginalDataInformation(origin);
	}
	
	@Override
	public GroupNode writePowderCalibrationToFile(IDiffractionMetadata metadata,IDataset calibrationImage, IPowderCalibrationInfo info) {
		return PersistSinglePowderCalibrationNode.persistSingleCalibration(calibrationImage, metadata, info);
	}

	@Override
	public GroupNode writeOperationFieldsToGroup(Map<String, Serializable> fields) {
		return PersistJsonOperationsNode.writeAutoConfiguredFieldsToNode(fields);
	}

	@Override
	public Map<String, Serializable> readOperationFields(GroupNode note) {
		return PersistJsonOperationsNode.readAutoConfiguredFields(note);
	}
}
