package org.dawnsci.macro.generator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.INameable;
import org.eclipse.dawnsci.macro.api.AbstractMacroGenerator;

/**
 * Creates a python dictionary from whatever 
 * @author fcp94556
 *
 */
public class MapGenerator extends AbstractMacroGenerator<Map> {

	@Override
	public String getPythonCommand(Map source) {
		return createFlattenCommands((Map<String,? extends Object>)source, 0);
	}

	@Override
	public String getJythonCommand(Map source) {
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
			
				String cmd;
				
				// If it is nameable, we override its name temporarily with the mapped name
				if (object instanceof INameable) {
					INameable nameable = (INameable)object;
					String origName = nameable.getName();
					try {
						nameable.setName(varName);
						cmd = type==0 ? gen.getPythonCommand(nameable) : gen.getJythonCommand(nameable);
						buf.append(cmd);
					} finally {
					    nameable.setName(origName);
					}

				} else {
					cmd = type==0 ? gen.getPythonCommand(object) : gen.getJythonCommand(object);
					buf.append(cmd);
				}

				// The name may still have been changed when the command was generated
				// Therefore we parse it back out.
				if (cmd!=null && cmd.contains("=")) {
					String[] sa = cmd.split("=");
				    if (sa!=null && sa.length>0) sentData.add(sa[0].trim());
				}

			}
		}
		
		// We make a dictionary for them, if there is more than 1.
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
