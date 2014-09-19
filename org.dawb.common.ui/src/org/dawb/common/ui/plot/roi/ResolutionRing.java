/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.dawb.common.ui.plot.roi;

import java.io.Serializable;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Class for Resolution rings in the diffraction viewer.
 */
public class ResolutionRing implements Serializable {
	private static final long serialVersionUID = 1496209098011098370L;
	private double resolution;
	private boolean visible;
	private Color colour;
	private boolean ice;
	private boolean evenSpaced;
	private boolean standard;

	/**
	 * @param resolution in Angstroms
	 */
	public ResolutionRing(double resolution) {
		this(resolution, true, ColorConstants.orange, false, false, false);
	}

	/**
	 * @param resolution in Angstroms
	 * @param visible
	 * @param colour
	 * @param ice if true, then ring is an ice ring
	 * @param evenSpacedRings
	 */
	public ResolutionRing(double resolution, boolean visible, Color colour, boolean ice, boolean evenSpacedRings) {
		this(resolution, visible, colour, ice, evenSpacedRings, false);
	}

	/**
	 * @param resolution in Angstroms
	 * @param visible
	 * @param colour
	 * @param ice if true, then ring is an ice ring
	 * @param evenSpacedRings
	 * @param standard
	 */
	public ResolutionRing(double resolution, boolean visible, Color colour, boolean ice, boolean evenSpacedRings,
			boolean standard) {
		this.resolution = resolution;
		this.visible = visible;
		this.colour = colour;
		this.setIce(ice);
		this.setEvenSpaced(evenSpacedRings);
		this.setStandard(standard);
	}

	public void setVisible(boolean visable) {
		this.visible = visable;
	}

	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param resolution
	 *            The resolution in Angstroms to set.
	 */
	public void setResolution(double resolution) {
		this.resolution = resolution;
	}

	/**
	 * @return Returns the resolution in Angstroms
	 */
	public double getResolution() {
		return resolution;
	}

	public Color getColour() {
		return colour;
	}

	public java.awt.Color getAWTColour() {
		return new java.awt.Color(colour.getRed(), colour.getGreen(), colour.getBlue());
	}

	public void setIce(boolean ice) {
		this.ice = ice;
	}

	public boolean isIce() {
		return ice;
	}

	public void setEvenSpaced(boolean evenSpaced) {
		this.evenSpaced = evenSpaced;
	}

	public boolean isEvenSpaced() {
		return evenSpaced;
	}

	/**
	 * @param standard The standard to set.
	 */
	public void setStandard(boolean standard) {
		this.standard = standard;
	}
	/**
	 * @return Returns the standard.
	 */
	public boolean isStandard() {
		return standard;
	}
}
