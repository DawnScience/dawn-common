/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.util.list;

import java.util.ArrayList;

/**
 * Not an efficient implementation but works with ==
 * @author gerring
 *
 * @param <E>
 */
public class IdentityList<E> extends ArrayList<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean contains(Object o) {
		for (int i = 0; i < size(); i++) {
			if (get(i)==o) return true;
		}
		return false;
	}
	public boolean remove(Object o) {
		for (int i = 0; i < size(); i++) {
			if (get(i)==o) return super.remove(i)!=null;
		}
		return false;
	}

	public int indexOf(final Object o) {
		for (int i = 0; i < size(); i++) {
			if (get(i)==o) return i;
		}
		return -1;
	}
}
