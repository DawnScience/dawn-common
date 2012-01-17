/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.file;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class ExtensionFileFilter  extends FileFilter {

	private String extension;

	public ExtensionFileFilter(final String extension) {
		this.extension = extension;
	}
	
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean accept(final File f) {
        if (f != null) {
            if (f.isDirectory()) {
                return true;
            }
            String name = f.getName();
            return name.toLowerCase().endsWith("."+extension);
        }
        return true;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "Data files (*."+extension+")";
    }
}
