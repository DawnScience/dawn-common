package org.dawnsci.conversion.converters;

import org.dawb.common.services.conversion.IConversionContext;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class VisitorConversion extends AbstractConversion {

	public VisitorConversion(IConversionContext context) {
		super(context);
		if (context.getConversionVisitor()==null) throw new RuntimeException("You must set a conversion visitor to use "+getClass().getName());
	}

	@Override
	protected void convert(AbstractDataset slice) {
		context.getConversionVisitor().visit(context, slice);
	}

}
