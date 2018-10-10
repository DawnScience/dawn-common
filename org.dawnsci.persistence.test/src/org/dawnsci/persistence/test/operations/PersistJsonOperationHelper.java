/*
 * Copyright (c) 2012, 2017 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.persistence.test.operations;

import org.dawnsci.persistence.internal.PersistJsonOperationsNode;
import org.eclipse.dawnsci.analysis.api.processing.IOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.model.IOperationModel;
import org.eclipse.dawnsci.analysis.api.tree.GroupNode;
import org.eclipse.dawnsci.analysis.api.tree.Tree;
import org.eclipse.dawnsci.nexus.NexusConstants;
import org.eclipse.dawnsci.nexus.NexusFile;
import org.eclipse.january.metadata.OriginMetadata;

/**
 * 
 * @author Matthew Gerring
 *
 */
public class PersistJsonOperationHelper {
	// Static stuff
	private static final String PROCESSED_ENTRY = Tree.ROOT + "processed" + NexusFile.NXCLASS_SEPARATOR + NexusConstants.ENTRY;
	private final static String ORIGIN = "origin";
	
	public void writeOperations(NexusFile file, IOperation<? extends IOperationModel, ? extends OperationData>... operations) throws Exception {
		GroupNode entryGroup = file.getGroup(PROCESSED_ENTRY, true);
		GroupNode processGroup = PersistJsonOperationsNode.writeOperationsToNode(operations);
		file.addNode(entryGroup, "process", processGroup);
	}

	public void writeOriginalDataInformation(NexusFile file, OriginMetadata origin) throws Exception {
		GroupNode groupEntry = file.getGroup(PROCESSED_ENTRY, true);
		
		GroupNode processNode = file.getGroup(groupEntry, "process", NexusConstants.PROCESS, true);
		GroupNode originNode = PersistJsonOperationsNode.writeOriginalDataInformation(origin);
		file.addNode(processNode, ORIGIN, originNode);
	}
}
