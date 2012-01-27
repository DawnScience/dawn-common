/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.plot;

public enum PlotType {

    IMAGE(false), 
    
    SURFACE(false), 
    
    PT1D(true), 
    
    PT1D_MULTI(true), 
    
    PT1D_STACKED(true),

    PT1D_3D(true);
    
	private final boolean is1D;

	private PlotType(boolean is1D) {
    	this.is1D = is1D;
    }
	
	public static PlotType forSliceIndex(int type) {
		switch(type) {
		case 0:
			return IMAGE;
		case 1:
			return SURFACE;
		}
		return null;
	}

	public boolean is1D() {
		return is1D;
	}
}
