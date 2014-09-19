/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.util.list;

/**
 * Defines a functor interface implemented by classes that perform a predicate
 * test on an object.
 * A {@code Predicate} is the object equivalent of an {@code if} statement.
 * It uses the input object to return a true or false value, and is often used
 * in validation or filtering.
 *
 * @param <T>
 *
 * @author Gábor Náray
 */
public interface IListenerListIteratorPredicate<T> {
	/**
	 * Use the specified parameter to perform a test that returns true or false.
	 * @param {@code object} - the object to evaluate, should not be changed
	 * @return true or false
	 * @throws
	 * ClassCastException - (runtime) if the input is the wrong class
	 * IllegalArgumentException - (runtime) if the input is invalid
	 * FunctorException - (runtime) if the predicate encounters a problem
	 */
	boolean evaluate(final T object);
}
