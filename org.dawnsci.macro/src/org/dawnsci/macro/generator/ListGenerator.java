package org.dawnsci.macro.generator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.macro.api.AbstractMacroGenerator;

/**
 * Creates a python dictionary from whatever 
 * @author fcp94556
 *
 */
public class ListGenerator extends AbstractMacroGenerator {

	@Override
	public String getPythonCommand(Object source) {
		return createFlattenCommands((List<? extends Object>)source, 0);
	}

	@Override
	public String getJythonCommand(Object source) {
		return createFlattenCommands((List<? extends Object>)source, 1);
	}

	
	private String createFlattenCommands(List<? extends Object> data, int type) {
		
		if (data==null || data.isEmpty()) return null;
		
		final StringBuilder     buf = new StringBuilder();
		final List<String> sentData = new ArrayList<String>(3);
		for (Object var : data) {		
			
			AbstractMacroGenerator gen = MacroFactory.getGenerator(var.getClass());
			if (gen!=null) {
				String cmd = type==0 ? gen.getPythonCommand(var) : gen.getJythonCommand(var);
				buf.append(cmd);
				
				// The name may have been changed when the command was generated.
				// Therefore we parse it back out.
				if (cmd!=null && cmd.contains("=")) {
					String[] sa = cmd.split("=");
				    if (sa!=null && sa.length>0) sentData.add(sa[0].trim());
				}
			}
		}
		
		// We make a list for them, if there is more than 1.
		if (sentData.size()>1) {
			buf.append("data = "+createListText(sentData)+"\n");
		}
		if (buf.length()>0) return buf.toString();
		
		return null;

	}


	/**
	 * TODO need to test if this works...
	 * @param sentData
	 * @return
	 */
	private String createListText(List<String> sentData) {
		
		final StringBuilder buf = new StringBuilder("[");
		for (Iterator<String> iterator = sentData.iterator(); iterator.hasNext();) {
			String varName = iterator.next();
			buf.append(varName);
			if (iterator.hasNext()) buf.append(", ");
		}
		buf.append("]");
		return buf.toString();
	}

}
