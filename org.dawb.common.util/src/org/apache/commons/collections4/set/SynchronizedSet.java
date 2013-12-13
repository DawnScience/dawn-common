/*
 * Copyright (c) 2013 European Molecular Biology Laboratory
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.apache.commons.collections4.set;

import java.util.Set;

import org.apache.commons.collections4.collection.SynchronizedCollection;

/**
 * Decorates another {@link Set} to synchronize its behaviour for a
 * multi-threaded environment.
 * <p>
 * Iterators must be manually synchronized:
 * 
 * <pre>
 * synchronized (set) {
 * 	Iterator it = set.iterator();
 * 	// do stuff with iterator
 * }
 * </pre>
 * <p>
 * This class is Serializable from Commons Collections ?.
 * 
 * @param <E>
 *            the type of the elements in the set
 * 
 * @since ?
 * 
 * @author Gábor Náray
 * 
 */
public class SynchronizedSet<E> extends SynchronizedCollection<E> implements
		Set<E> {

	/** Serialization version */
	private static final long serialVersionUID = -4250031980017252271L;

	/**
	 * Factory method to create a synchronized set.
	 * 
	 * @param <T>
	 *            the type of the elements in the set
	 * @param set
	 *            the set to decorate, must not be null
	 * @return a new synchronized set
	 * @throws IllegalArgumentException
	 *             if set is null
	 * @since ?
	 */
	public static <T> SynchronizedSet<T> synchronizedSet(final Set<T> set) {
		return new SynchronizedSet<T>(set);
	}

	// -----------------------------------------------------------------------
	/**
	 * Constructor that wraps (not copies).
	 * 
	 * @param set
	 *            the set to decorate, must not be null
	 * @throws IllegalArgumentException
	 *             if the set is null
	 */
	protected SynchronizedSet(final Set<E> set) {
		super(set);
	}

	/**
	 * Constructor that wraps (not copies).
	 * 
	 * @param set
	 *            the set to decorate, must not be null
	 * @param lock
	 *            the lock object to use, must not be null
	 * @throws IllegalArgumentException
	 *             if the set is null
	 */
	protected SynchronizedSet(final Set<E> set, final Object lock) {
		super(set, lock);
	}

	/**
	 * Gets the set being decorated.
	 * 
	 * @return the decorated set
	 */
	protected Set<E> decorated() {
		return (Set<E>) super.decorated();
	}

	// -----------------------------------------------------------------------
}
