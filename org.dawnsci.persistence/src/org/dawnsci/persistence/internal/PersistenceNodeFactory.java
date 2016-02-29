package org.dawnsci.persistence.internal;

import org.eclipse.dawnsci.analysis.api.metadata.OriginMetadata;
import org.eclipse.dawnsci.analysis.api.persistence.IPersistentNodeFactory;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Tree;

public class PersistenceNodeFactory implements IPersistentNodeFactory {

	@Override
	public IOperation[] readOperationsFromTree(Tree tree) throws Exception {
		PersistJsonOperationsNode pjoh = new PersistJsonOperationsNode();
		return pjoh.readOperations(tree);
	}

	@Override
	public GroupNode writeOperationsToGroup(IOperation[] operations) {
		PersistJsonOperationsNode pjoh = new PersistJsonOperationsNode();
		return pjoh.writeOperationsToNode(operations);
	}

	@Override
	public GroupNode writeOriginalDataInformation(OriginMetadata origin) {
		PersistJsonOperationsNode pjoh = new PersistJsonOperationsNode();
		return pjoh.writeOriginalDataInformation(origin);
	}
	
}
