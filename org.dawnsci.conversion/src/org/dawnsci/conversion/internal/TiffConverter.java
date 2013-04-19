package org.dawnsci.conversion.internal;

import org.dawb.common.services.conversion.IConversionContext;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class TiffConverter extends AbstractConversion {

	public TiffConverter(IConversionContext context) {
		super(context);
	}

	@Override
	protected void convert(AbstractDataset slice) {
		// TODO Auto-generated method stub

	}
	
	@Override
	public void close(IConversionContext context) {
        //optionally you may do something on close.
	}
}
