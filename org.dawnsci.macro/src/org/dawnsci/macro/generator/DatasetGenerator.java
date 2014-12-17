package org.dawnsci.macro.generator;

import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.macro.api.AbstractMacroGenerator;
import org.eclipse.dawnsci.macro.api.MacroUtils;

import uk.ac.diamond.scisoft.analysis.rpc.FlatteningService;

public class DatasetGenerator extends AbstractMacroGenerator<IDataset> {

	@Override
	public String getPythonCommand(IDataset source) {
	   	return getCommand(source, 0);
	}

	@Override
	public String getJythonCommand(IDataset source) {
    	return getCommand(source, 1);
	}

	private static int count = 0;
	
    private String getCommand(IDataset set, int type) {
    	
    	final StringBuilder buf = new StringBuilder();
		Object object = FlatteningService.getFlattener().flatten(set);
		if (object instanceof Map) {

			final Map<String, Object> map = (Map<String, Object>)object;

			String flattenedPath = (String)map.get("filename");

			if (flattenedPath!=null) {
				String varName = set.getName();
				if (varName==null || "".equals(varName)) varName = "dataset"+(++count);
				buf.append(MacroUtils.getLegalName(varName));
				buf.append(" = ");
				if (type == 0) { // Python
					buf.append( "numpy.load(r'"+flattenedPath+"')\n");
				} else {
					buf.append( "dnp.io.load(r'"+flattenedPath+"')\n");
				}
			}
		}

		return buf.toString();
    }
}
