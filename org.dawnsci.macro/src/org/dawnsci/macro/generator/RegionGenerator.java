package org.dawnsci.macro.generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dawnsci.macro.AbstractMacroGenerator;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.macro.api.MacroEventObject;
import org.eclipse.dawnsci.plotting.api.region.IRegion;

import uk.ac.diamond.scisoft.analysis.rpc.FlatteningService;
import uk.ac.diamond.scisoft.analysis.rpc.flattening.IRootFlattener;

/**
 * Generates the python to create regions in the scripting layer.
 * Uses the flattening service to create the correct commands.
 * 
 * @author fcp94556
 *
 */
class RegionGenerator extends AbstractMacroGenerator {
	
	static final Map<ClassKey, String> pythonCmdMap;
	static {
		 Map<ClassKey, String>  tmp = new HashMap<ClassKey, String>(7);
		 tmp.put(new ClassKey(LinearROI.class),      "line");
	     tmp.put(new ClassKey(RectangularROI.class), "rectangle");
	     tmp.put(new ClassKey(SectorROI.class),      "sector");
	     tmp.put(new ClassKey(CircularROI.class),    "circle");
	     tmp.put(new ClassKey(EllipticalROI.class),  "ellipse");
		 
		 pythonCmdMap = Collections.unmodifiableMap(tmp);
	}

	@Override
	public MacroEventObject generate(MacroEventObject evt) {
		
        final Object source = evt.getSource();
        IROI roi = null;
        if (source instanceof IRegion) roi = ((IRegion)source).getROI();
        if (source instanceof IROI)    roi = (IROI)source;
        if (roi    == null) return evt;
        
        return  createCommands(roi, evt);
	}

	/**
	 * 
	 * @param roi
	 * @param evt
	 */
	private MacroEventObject createCommands(IROI roi, MacroEventObject evt) {
		
		if (!pythonCmdMap.containsKey(new ClassKey(roi.getClass()))) {
			String cmd = "print '"+roi.getClass().getSimpleName()+" does not have a python command'";
			evt.setPythonCommand(cmd);
			evt.setJythonCommand(cmd);
			return evt;
		}
		IRootFlattener service = FlatteningService.getFlattener();
		Map<String, Object> flat = (Map<String, Object>)service.flatten(roi);
		
 		StringBuilder cmd = new StringBuilder(evt.getLegalName(roi.getName()));
		cmd.append(" = ");
		cmd.append("dnp.roi.");
		cmd.append(pythonCmdMap.get(new ClassKey(roi.getClass())));
		cmd.append("(");
		cmd.append(getArguments(flat));
		cmd.append(")");
		evt.setPythonCommand(cmd.toString());
		evt.setJythonCommand(cmd.toString());
		return evt;
	}

	private String getArguments(Map<String, Object> flat) {
		
	    // Ignore __type__, name
		StringBuilder args = new StringBuilder();
		for (Iterator<String> it = flat.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			if ("__type__".equals(key) || "name".equals(key)) continue;
			
			args.append(key);
			args.append(" = ");
			args.append(toPythonString(flat.get(key)));
			if (it.hasNext()) args.append(", ");
		}
		
		return args.toString();
	}

}
