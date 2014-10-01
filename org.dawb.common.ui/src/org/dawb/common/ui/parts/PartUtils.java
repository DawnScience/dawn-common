/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawb.common.ui.parts;

import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.MultiPageEditorPart;

public class PartUtils {

	/**
	 * Attempt to get a plotting system from a part
	 * @param part
	 * @return plotting system (may return null)
	 */
	public static IPlottingSystem getPlottingSystem(IWorkbenchPart part) {
		
		if (part instanceof MultiPageEditorPart) {
			MultiPageEditorPart mpp = (MultiPageEditorPart) part;
			Object page = mpp.getSelectedPage();
			
			if (page instanceof IEditorPart) {
				part = (IWorkbenchPart) page;
			} else {
				return null;
			}
		}

		return (IPlottingSystem)part.getAdapter(IPlottingSystem.class);
	}
}
