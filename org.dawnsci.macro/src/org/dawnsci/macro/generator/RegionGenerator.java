package org.dawnsci.macro.generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.CircularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
import org.eclipse.dawnsci.analysis.dataset.roi.SectorROI;
import org.eclipse.dawnsci.macro.api.AbstractMacroGenerator;
import org.eclipse.dawnsci.macro.api.MacroEventObject;
import org.eclipse.dawnsci.macro.api.MacroUtils;
import org.eclipse.dawnsci.plotting.api.region.IRegion;

import uk.ac.diamond.scisoft.analysis.rpc.FlatteningService;
import uk.ac.diamond.scisoft.analysis.rpc.flattening.IRootFlattener;

/**
 * Generates the python to create regions in the scripting layer.
 * Uses the flattening service to create the correct commands.
 * 
 * @author Matthew Gerring
 *
 */
class RegionGenerator extends AbstractMacroGenerator<Object> {
	
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
        
        String cmd = createCommand(roi);
        if (cmd!=null) {
           	evt.setPythonCommand(cmd);
           	evt.setJythonCommand(cmd);
        }
        return evt;
	}

	private static int count;
	/**
	 * 
	 * @param roi
	 * @param evt
	 */
	private String createCommand(IROI roi) {
		
		if (!pythonCmdMap.containsKey(new ClassKey(roi.getClass()))) {
			return "print '"+roi.getClass().getSimpleName()+" does not have a python command'";
		}
		IRootFlattener service = FlatteningService.getFlattener();
		Map<String, Object> flat = (Map<String, Object>)service.flatten(roi);
		
		String varName = roi.getName();
		if (varName==null || "".equals(varName)) varName = "roi"+(++count);
 		StringBuilder cmd = new StringBuilder(MacroUtils.getLegalName(varName));
		cmd.append(" = ");
		cmd.append("dnp.roi.");
		cmd.append(pythonCmdMap.get(new ClassKey(roi.getClass())));
		cmd.append("(");
		cmd.append(getArguments(flat));
		cmd.append(")\n");
		return cmd.toString();
	}

	private String getArguments(Map<String, Object> flat) {
		
	    // Ignore __type__, name
		StringBuilder args = new StringBuilder();
		for (Iterator<String> it = flat.keySet().iterator(); it.hasNext();) {
			String key = it.next();
			if ("__type__".equals(key) || "name".equals(key)) continue;
			
			args.append(key);
			args.append(" = ");
			args.append(MacroUtils.toPythonString(flat.get(key)));
			if (it.hasNext()) args.append(", ");
		}
		
		return args.toString();
	}

	@Override
	public String getPythonCommand(Object source) {
        IROI roi = null;
        if (source instanceof IRegion) roi = ((IRegion)source).getROI();
        if (source instanceof IROI)    roi = (IROI)source;
        if (roi    == null) return null;
        return createCommand(roi);
 	}

	@Override
	public String getJythonCommand(Object source) {
        IROI roi = null;
        if (source instanceof IRegion) roi = ((IRegion)source).getROI();
        if (source instanceof IROI)    roi = (IROI)source;
        if (roi    == null) return null;
        return createCommand(roi);
 	}

}
