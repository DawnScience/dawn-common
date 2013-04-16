/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.dawb.common.util.list;

/**
 *
 */
public class IntersectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String firstName, secondName;
	/**
	 * 
	 */
	public IntersectionException() {
	}

	/**
	 * @param message
	 */
	public IntersectionException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public IntersectionException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public IntersectionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @return Returns the firstName.
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * @param firstName The firstName to set.
	 */
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	/**
	 * @return Returns the secondName.
	 */
	public String getSecondName() {
		return secondName;
	}

	/**
	 * @param secondName The secondName to set.
	 */
	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}

}
