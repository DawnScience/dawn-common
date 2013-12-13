/*
 * Copyright (c) 2013 European Molecular Biology Laboratory
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.apache.commons.collections4.list;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

import org.apache.commons.collections4.collection.SynchronizedCollection;

/**
 * Decorates another {@link List} to synchronize its behaviour for a
 * multi-threaded environment.
 * <p>
 * Iterators must be manually synchronized:
 * 
 * <pre>
 * synchronized (list) {
 * 	Iterator it = list.iterator();
 * 	// do stuff with iterator
 * }
 * </pre>
 * The list must be manually synchronized when iterating over any of its
 * collection views:
 * 
 * <pre>
 * List l = list.subList();
 * synchronized (list) {
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
public class SynchronizedList<E> extends SynchronizedCollection<E> implements
		List<E> {
	/** Serialization version */
	private static final long serialVersionUID = -7960158206161791602L;

	/** The list to decorate */
	private final List<E> list;

	/**
	 * Factory method to create a synchronized list.
	 * 
	 * @param <T>
	 *            the type of the elements in the list
	 * @param list
	 *            the list to decorate, must not be null
	 * @return a new synchronized list
	 * @throws IllegalArgumentException
	 *             if list is null
	 * @since ?
	 */
	public static <T> SynchronizedList<T> synchronizedList(final List<T> list) {
		return new SynchronizedList<T>(list);
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
	protected SynchronizedList(List<E> list) {
		super(list);
		this.list = list;
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
	protected SynchronizedList(final List<E> list, final Object mutex) {
		super(list, mutex);
		this.list = list;
	}

	/**
	 * Gets the list being decorated.
	 * 
	 * @return the decorated list
	 */
	protected List<E> decorated() {
		return (List<E>) super.decorated();
	}

	// -----------------------------------------------------------------------

	public boolean equals(final Object o) {
		if (this == o)
			return true;
		synchronized (lock) {
			return list.equals(o);
		}
	}

	public int hashCode() {
		synchronized (lock) {
			return list.hashCode();
		}
	}

	public E get(final int index) {
		synchronized (lock) {
			return list.get(index);
		}
	}

	public E set(final int index, final E element) {
		synchronized (lock) {
			return list.set(index, element);
		}
	}

	public void add(final int index, final E element) {
		synchronized (lock) {
			list.add(index, element);
		}
	}

	public E remove(final int index) {
		synchronized (lock) {
			return list.remove(index);
		}
	}

	public int indexOf(final Object o) {
		synchronized (lock) {
			return list.indexOf(o);
		}
	}

	public int lastIndexOf(final Object o) {
		synchronized (lock) {
			return list.lastIndexOf(o);
		}
	}

	public boolean addAll(final int index, final Collection<? extends E> c) {
		synchronized (lock) {
			return list.addAll(index, c);
		}
	}

	/**
	 * Iterators must be manually synchronized.
	 * 
	 * <pre>
	 * synchronized (list) {
	 * 	Iterator it = list.listIterator();
	 * 	// do stuff with iterator
	 * }
	 * </pre>
	 * 
	 * @return an iterator that must be manually synchronized on the list
	 */
	public ListIterator<E> listIterator() {
		return list.listIterator();
	}

	/**
	 * Iterators must be manually synchronized.
	 * 
	 * <pre>
	 * synchronized (list) {
	 * 	Iterator it = list.listIterator(index);
	 * 	// do stuff with iterator
	 * }
	 * </pre>
	 * 
	 * @return an iterator that must be manually synchronized on the list
	 */
	public ListIterator<E> listIterator(final int index) {
		return list.listIterator(index);
	}

	/**
	 * The list must be manually synchronized when iterating over any of its
	 * collection views:
	 * 
	 * <pre>
	 * List l = list.subList();
	 * synchronized (list) {
	 * 	Iterator it = l.iterator();
	 * 	// do stuff with iterator
	 * }
	 * </pre>
	 * 
	 * @return a list, which when iterated over, the user must manually synchronize on the list
	 */
	public List<E> subList(final int fromIndex, final int toIndex) {
		synchronized (lock) {
			return new SynchronizedList<E>(list.subList(fromIndex, toIndex),
					lock);
		}
	}

	/**
	 * SynchronizedRandomAccessList instances are serialized as SynchronizedList
	 * instances to allow them to be deserialized in pre-1.4 JREs (which do not
	 * have SynchronizedRandomAccessList). This method inverts the
	 * transformation. As a beneficial side-effect, it also grafts the
	 * RandomAccess marker onto SynchronizedList instances that were serialized
	 * in pre-1.4 JREs.
	 * 
	 * Note: Unfortunately, SynchronizedRandomAccessList instances serialized in
	 * 1.4.1 and deserialized in 1.4 will become SynchronizedList instances, as
	 * this method was missing in 1.4.
	 */
	private Object readResolve() {
		return (list instanceof RandomAccess ? new SynchronizedRandomAccessList<E>(
				list) : this);
	}

}
