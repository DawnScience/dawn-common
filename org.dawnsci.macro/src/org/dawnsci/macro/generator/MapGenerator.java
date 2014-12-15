package org.dawnsci.macro.generator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.macro.api.AbstractMacroGenerator;

public class MapGenerator extends AbstractMacroGenerator {

	@Override
	public String getPythonCommand(Object source) {
		return createFlattenCommands((Map<String,? extends Object>)source, 0);
	}

	@Override
	public String getJythonCommand(Object source) {
		return createFlattenCommands((Map<String,? extends Object>)source, 1);
	}

	
	private String createFlattenCommands(Map<String, ? extends Object> data, int type) {
		
		if (data==null || data.isEmpty()) return null;
		
		final StringBuilder     buf = new StringBuilder();
		final List<String> sentData = new ArrayList<String>(3);
		for (String varName : data.keySet()) {
			
			final Object object = data.get(varName);		
			
			AbstractMacroGenerator gen = MacroFactory.getGenerator(object.getClass());
 
			if (gen!=null) {
			
				if (object instanceof IDataset) {
					IDataset dset = (IDataset)object;
					String origName = dset.getName();
					dset.setName(varName);
					buf.append(type==0 ? gen.getPythonCommand(dset) : gen.getJythonCommand(dset));
					dset.setName(origName);
					
				} else {
					buf.append(type==0 ? gen.getPythonCommand(object) : gen.getJythonCommand(object));

				}
				sentData.add(varName);
			}
		}
		
		// We make a dictionary for them.
		if (sentData.size()>1) {
			buf.append("data = "+createDictionaryText(sentData)+"\n");
		}
		if (buf.length()>0) return buf.toString();
		
		return null;

	}


	private String createDictionaryText(List<String> sentData) {
		
		final StringBuilder buf = new StringBuilder("{");
		for (Iterator<String> iterator = sentData.iterator(); iterator.hasNext();) {
			String varName = iterator.next();
			buf.append("'");
			buf.append(varName);
			buf.append("' : ");
			buf.append(varName);
			if (iterator.hasNext()) buf.append(", ");
		}
		buf.append("}");
		return buf.toString();
	}

}
