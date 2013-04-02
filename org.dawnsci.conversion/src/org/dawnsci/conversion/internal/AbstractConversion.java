package org.dawnsci.conversion.internal;

import java.util.List;

import org.dawb.common.services.IConversionContext;

public abstract class AbstractConversion {

	public void process(IConversionContext context) throws Exception{
		// Process regular expression
		final List<String> paths = expand(context);
		for (String path : paths) {
			processFile(path, context);
		}
	}
	
	protected abstract void processFile(String filePathNoRegex, IConversionContext context);

	/**
	 * TODO expand the regex according to the javadoc.
	 * @param context
	 * @return
	 */
	private List<String> expand(IConversionContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
