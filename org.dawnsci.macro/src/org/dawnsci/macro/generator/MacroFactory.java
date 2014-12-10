package org.dawnsci.macro.generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dawnsci.macro.IMacroGenerator;
import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.macro.api.MacroEventObject;
import org.eclipse.dawnsci.plotting.api.region.IRegion;


/**
 * Class to provide automatic generation of macro content.
 * 
 * @author fcp94556
 *
 */
public class MacroFactory {

	static final Map<ClassKey, IMacroGenerator> translators;
	static {
		Map<ClassKey, IMacroGenerator> tmp = new HashMap<ClassKey, IMacroGenerator>(7);
		tmp.put(new ClassKey(IRegion.class), new RegionGenerator());
		tmp.put(new ClassKey(IROI.class),    new RegionGenerator());
	    translators = Collections.unmodifiableMap(tmp);
	}
	
	/**
	 * 
	 * If there is no generator for MacroEventObject, the original 
	 * event is returned intact.
	 * 
	 * @param evt
	 * @return
	 */
	public static MacroEventObject generate(MacroEventObject evt) {
		
		ClassKey key = new ClassKey(evt.getSource().getClass());
		if (translators.containsKey(key)) {
			return translators.get(key).generate(evt);
		}
		return evt;
	}
	
	private static class ClassKey {
		private Class<? extends Object> cls;

		public ClassKey(Class<? extends Object> cls) {
			this.cls=cls;
		}

		@Override
		public int hashCode() {
			// All in same bucket on purpose.
			return 1;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ClassKey other = (ClassKey) obj;
			if (cls == null) {
				if (other.cls != null)
					return false;
			} else {
				if (cls.isAssignableFrom(other.cls) || 
						other.cls.isAssignableFrom(cls)) {
					return true;
				}
			}
			
			return false;
		}

	}
}
