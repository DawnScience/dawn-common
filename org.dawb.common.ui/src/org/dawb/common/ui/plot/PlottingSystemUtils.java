package org.dawb.common.ui.plot;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;

public class PlottingSystemUtils {

	/**
	 * @return a plotting system if one is active or else null
	 */
	public static IPlottingSystem<?> getPlottingSystem() {

		// Perhaps the plotting system is on a dialog
		final Shell[] shells = Display.getDefault().getShells();
		if (shells != null) {
			for (Shell shell : shells) {
				final Object o = shell.getData();
				if (o != null && o instanceof IAdaptable) {
					IPlottingSystem<?> a = ((IAdaptable) o).getAdapter(IPlottingSystem.class);
					if (a != null) {
						return a;
					}
				}
			}
		}

		final IWorkbenchPart part = EclipseUtils.getPage().getActivePart();
		if (part != null) {
			// First test if part is a tool page which might have its own plotting system
			IToolPageSystem a = part.getAdapter(IToolPageSystem.class);

			if (a != null && a instanceof IPlottingSystem) {
				return (IPlottingSystem<?>) a;
			}

			return part.getAdapter(IPlottingSystem.class);
		}

		return null;
	}

}