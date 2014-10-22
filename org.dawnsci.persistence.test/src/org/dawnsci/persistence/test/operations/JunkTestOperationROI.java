package org.dawnsci.persistence.test.operations;

import org.eclipse.dawnsci.analysis.api.processing.AbstractOperation;
import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;

public class JunkTestOperationROI extends AbstractOperation<JunkTestModelROI, OperationData> {

	@Override
	public String getId() {
		return "org.dawnsci.persistence.test.operations.JunkTestOperationROI";
	}
	
	@Override
	public String getName(){
		return "JunkTestOperationROI";
	}

	@Override
	public OperationRank getInputRank() {
		return OperationRank.TWO;
	}

	@Override
	public OperationRank getOutputRank() {
		return OperationRank.TWO;
	}

}
