package org.dawnsci.macro.generator;

import org.dawnsci.macro.IMacroGenerator;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
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
class RegionGenerator implements IMacroGenerator {

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
		
		IRootFlattener service = FlatteningService.getFlattener();
		Object flat = service.flatten(roi);
		
        // Ignore __type__, name
		StringBuilder cmd = new StringBuilder(evt.getLegalName(roi.getName()));
		cmd.append(" = ");
		//cmd.append();
		
		return evt;
	}

}
