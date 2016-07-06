/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.python.rpc.commandline;

/**
 * @author wim
 */
public class CommandLineException extends Exception {

  private static final long serialVersionUID = 9175542101325245662L;

  public CommandLineException(String message) {
    super(message);
  }
}
