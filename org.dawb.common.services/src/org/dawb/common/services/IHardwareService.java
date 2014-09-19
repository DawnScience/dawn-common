/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.services;

/**
 * This public interface is designed for a given plugin to contribute a 
 * service to the workbench which contributes to hardware values.
 * 
 * NOTE this interface intentionally exposes mock value so that the user of the
 * service can decide about using mock values or not.
 *  
 * The implementor or this service contributes using an eclipse extension
 * point and then later any plugin may ask for an implementation of the service.
 *
 * @author fcp94556
 *
 */
public interface IHardwareService {
	
	/**
	 * Get a value.
	 * @param motorName
	 * @return
	 */
	public Object getValue(String motorName)  throws Exception ;
	
	/**
	 * Set a value in the service
	 * @param motorName
	 * @param value
	 */
	public void setValue(String motorName, Object value)  throws Exception ;
	
	/**
	 * Run a command
	 * @param motorName
	 * @param message
	 * @param value
	 */
	// TODO public void runCommand(String hardwareUri, String message, String command)  throws Exception ;


	/**
	 * Get a mock value in the service which does not connect to real hardware.
	 * @param motorName
	 * @return
	 */
	public Object getMockValue(String motorName);
	
	/**
	 * Set a mock value in the service without really setting the hardware value.
	 * @param motorName
	 * @param value
	 */
	public void setMockValue(String motorName, Object value);
	
	/**
	 * Create and notify listeners of a mock value change.
	 * @param motorName
	 * @param message
	 * @param value
	 */
	public void notifyMockCommand(String motorName, String message, String value);
}
