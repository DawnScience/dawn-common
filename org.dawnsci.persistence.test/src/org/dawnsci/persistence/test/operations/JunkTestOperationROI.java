package org.dawnsci.persistence.test.operations;

import org.eclipse.dawnsci.analysis.api.processing.OperationData;
import org.eclipse.dawnsci.analysis.api.processing.OperationException;
import org.eclipse.dawnsci.analysis.api.processing.OperationRank;
import org.eclipse.dawnsci.analysis.dataset.operations.AbstractOperation;
import org.eclipse.january.IMonitor;
import org.eclipse.january.dataset.IDataset;

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

	@Override
	protected OperationData process(IDataset input, IMonitor monitor)
			throws OperationException {
		// TODO Auto-generated method stub
		return null;
	}

}
