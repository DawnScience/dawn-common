package org.dawnsci.conversion.converters;

import org.dawb.common.services.conversion.IConversionContext;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

public class VisitorConversion extends AbstractConversion {

	public VisitorConversion(IConversionContext context) throws Exception {
		super(context);
		if (context.getConversionVisitor()==null) throw new RuntimeException("You must set a conversion visitor to use "+getClass().getName());
	    context.getConversionVisitor().init(context);
	}

	@Override
	protected void convert(AbstractDataset slice) throws Exception{
		context.getConversionVisitor().visit(context, slice);
	}

	public void close(IConversionContext context) throws Exception{
		context.getConversionVisitor().close(context);
	}

}
