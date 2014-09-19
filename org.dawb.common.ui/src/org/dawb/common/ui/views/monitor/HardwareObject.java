/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.ui.views.monitor;

import java.util.HashSet;
import java.util.Set;

/**
 * A class to hold information about the object viewed in the dashboard.
 * 
 * Will be serialized to XML.
 */
public abstract class HardwareObject {
	
	/**
	 * Code for a finished event, i.e. stop minitoring
	 */
	public static final HardwareObject NULL = new HardwareObject() {
		@Override
		protected void connect() throws Exception { }
		@Override
		protected void disconnect() { }
	};

	
	// Not XML serializable.
	protected transient Set<HardwareObjectListener> listeners;
	protected transient volatile Object value;
	protected transient Object maximum;
	protected transient Object minimum;

	// Serialized
	protected String hardwareName;
	protected String tooltip;
	protected String  label,unit;
	protected boolean error;
	protected String  className;
	protected String  description;
	
	// Abstract
    protected abstract void connect() throws Exception;
    protected abstract void disconnect();

   
	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @return Returns the value.
	 */
	public Object getValue() {
		return value;
	}
	/**
	 * @return Returns the unit.
	 */
	public String getUnit() {
		return unit;
	}
	/**
	 * @param unit The unit to set.
	 */
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	/**
	 * @param l
	 */
	public void addServerObjectListener(HardwareObjectListener l) {
		if (listeners==null) listeners = new HashSet<HardwareObjectListener>(7);
		listeners.add(l);
	}
	
	/**
	 * @param l
	 */
	public void removeServerObjectListener(HardwareObjectListener l) {
		if (listeners==null) return;
		listeners.remove(l);
	}
	
	/**
	 * May be called by any thread!!
	 * @param evt
	 */
	protected void notifyServerObjectListeners(final HardwareObjectEvent evt) {
		for (HardwareObjectListener l : listeners) l.hardwareObjectChangePerformed(evt);
	}
	/**
	 * @return Returns the tooltip.
	 */
	public String getTooltip() {
		return tooltip;
	}
	/**
	 * @param tooltip The tooltip to set.
	 */
	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}
	/**
	 * @return Returns the error.
	 */
	public boolean isError() {
		return error;
	}
	/**
	 * @param error The error to set.
	 */
	public void setError(boolean error) {
		this.error = error;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	/**
	 * @return Returns the maximum.
	 */
	public Object getMaximum() {
		return maximum;
	}
	/**
	 * @param maximum The maximum to set.
	 */
	public void setMaximum(Object maximum) {
		this.maximum = maximum;
	}
	/**
	 * @return Returns the minimum.
	 */
	public Object getMinimum() {
		return minimum;
	}
	/**
	 * @param minimum The minimum to set.
	 */
	public void setMinimum(Object minimum) {
		this.minimum = minimum;
	}
	/**
	 * @return Returns the className.
	 */
	protected String getClassName() {
		return className;
	}
	/**
	 * @param className The className to set.
	 */
	protected void setClassName(String className) {
		this.className = className;
	}
	/**
	 * @return Returns the description.
	 */
	protected String getDescription() {
		return description;
	}
	/**
	 * @param description The description to set.
	 */
	protected void setDescription(String description) {
		this.description = description;
	}
	public String getHardwareName() {
		return hardwareName;
	}
	public void setHardwareName(String hardwareName) {
		this.hardwareName = hardwareName;
	}

}
