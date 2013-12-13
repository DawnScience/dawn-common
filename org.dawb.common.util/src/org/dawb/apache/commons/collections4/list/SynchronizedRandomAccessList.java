/*
 * Copyright (c) 2013 European Molecular Biology Laboratory
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.apache.commons.collections4.list;

import java.util.List;
import java.util.RandomAccess;

/**
 * Decorates another {@link List} to synchronize its behaviour for a
 * multi-threaded environment.
 * <p>
 * The list must be manually synchronized when iterating over any of its
 * collection views:
 * 
 * <pre>
 * List l = randomAccessList.subList();
 * synchronized (randomAccessList) {
 * 	Iterator it = l.iterator();
 * 	// do stuff with iterator
 * }
 * </pre>
 * <p>
 * This class is Serializable from Commons Collections ?.
 * 
 * @param <E>
 *            the type of the elements in the list
 * @since ?
 * 
 * @author Gábor Náray
 * 
 */
public class SynchronizedRandomAccessList<E> extends SynchronizedList<E>
		implements RandomAccess {

	/** Serialization version */
	private static final long serialVersionUID = 2398472993322822487L;

	/**
	 * Factory method to create a synchronized random access list.
	 * 
	 * @param <T>
	 *            the type of the elements in the list
	 * @param list
	 *            the list to decorate, must not be null
	 * @return a new synchronized random access list
	 * @throws IllegalArgumentException
	 *             if list is null
	 * @since ?
	 */
	public static <T> SynchronizedRandomAccessList<T> synchronizedRandomAccessList(final List<T> list) {
		return new SynchronizedRandomAccessList<T>(list);
	}

	// -----------------------------------------------------------------------
	/**
	 * Constructor that wraps (not copies).
	 * 
	 * @param list
	 *            the list to decorate, must not be null
	 * @throws IllegalArgumentException
	 *             if the list is null
	 */
	protected SynchronizedRandomAccessList(final List<E> list) {
		super(list);
	}

	/**
	 * Constructor that wraps (not copies).
	 * 
	 * @param list
	 *            the list to decorate, must not be null
	 * @param lock
	 *            the lock object to use, must not be null
	 * @throws IllegalArgumentException
	 *             if the list is null
	 */
	protected SynchronizedRandomAccessList(final List<E> list,
			final Object mutex) {
		super(list, mutex);
	}

	// -----------------------------------------------------------------------

	/**
	 * The list must be manually synchronized when iterating over any of its
	 * collection views:
	 * 
	 * <pre>
	 * List l = randomAccessList.subList();
	 * synchronized (randomAccessList) {
	 * 	Iterator it = l.iterator();
	 * 	// do stuff with iterator
	 * }
	 * </pre>
	 * 
	 * @return a list, which when iterated over, the user must manually synchronize on the random access list
	 */
	public List<E> subList(final int fromIndex, final int toIndex) {
		synchronized (lock) {
			return new SynchronizedRandomAccessList<E>(decorated().subList(
					fromIndex, toIndex), lock);
		}
	}

	/**
	 * Allows instances to be deserialized in pre-1.4 JREs (which do not have
	 * SynchronizedRandomAccessList). SynchronizedList has a readResolve method
	 * that inverts this transformation upon deserialization.
	 */
	private Object writeReplace() {
		return new SynchronizedList<E>(decorated());
	}

}
