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
    
	IMAGE(false, true, false), 
    
    SURFACE(false, false, true), 
    
    XY(true, false, false), 
        
    XY_STACKED(true, false, false),

    XY_STACKED_3D(false, false, true);
    
	
	
	
	private final boolean is1D, is2D, is3D;

	private PlotType(boolean is1D, boolean is2D, boolean is3D) {
    	this.is1D = is1D;
    	this.is2D = is2D;
    	this.is3D = is3D;
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
	public boolean is2D() {
		return is2D;
	}
	public boolean is3D() {
		return is3D;
	}
}
