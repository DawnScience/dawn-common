/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.conversion.ui.pages;

import org.dawnsci.conversion.ui.IConversionWizardPage;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * AbstractSliceConversionPage which declares as IConversionWizardPage specifically for use
 * with the conversion wizard.
 * @author Matthew Gerring
 *
 */
public abstract class AbstractSliceConversionPage extends org.dawb.common.ui.wizard.AbstractSliceConversionPage implements IConversionWizardPage {

	public AbstractSliceConversionPage(String pageName, String description, ImageDescriptor icon) {
		super(pageName, description, icon);
	}

}
