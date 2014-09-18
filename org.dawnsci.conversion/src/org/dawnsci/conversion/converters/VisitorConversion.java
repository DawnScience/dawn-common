package org.dawnsci.conversion.converters;

import org.dawb.common.services.conversion.IConversionContext;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;

public class VisitorConversion extends AbstractConversion {

	public VisitorConversion(IConversionContext context) throws Exception {
		super(context);
		if (context.getConversionVisitor()==null) throw new RuntimeException("You must set a conversion visitor to use "+getClass().getName());
	    context.getConversionVisitor().init(context);
	}

	@Override
	protected void convert(IDataset slice) throws Exception{
		context.getConversionVisitor().setExpandedDatasets(getExpandedDatasets());
		context.getConversionVisitor().visit(context, slice);
	}

	public void close(IConversionContext context) throws Exception{
		context.getConversionVisitor().close(context);
	}

}
