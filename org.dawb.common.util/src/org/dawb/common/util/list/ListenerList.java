/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.common.util.list;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * This class is a list that is designed for storing list of listeners.
 *
 * <a name="same">A ListenerList handles the <i>same</i> listener being added
 * multiple times, and tolerates removal of listeners that are the same as other
 * listeners in the list.  For this purpose, listeners can be compared with each other
 * using either equality or identity, as specified in the list constructor.
 *
 * <p><b>Note that this implementation is not synchronized.</b>
 *
 * <p>Use the following pattern when notifying listeners. The recommended
 * code sequence for notifying all registered listeners of say,
 * <code>FooListener.eventHappened</code>, is:
 *
 *<pre>
 * //Vector<FooListener> listeners = myListenerList;
 * int iSup = listeners.size();
 * for (int i = 0; i < iSup; ++i) {
 *     listeners.get(i).eventHappened(event);
 * }
 * </pre>
 * or a slower solution:
 * <pre>
 * for (FooListener listener : listeners) {
 *     listener.eventHappened(event);
 * }
 * </pre>
 *
 * @author Gábor Náray
 */
public class ListenerList<E> extends Vector<E> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2210796681565314379L;

	/**
	 * Mode constant (value 0) indicating that listeners should be considered
	 * the <a href="ListenerList.html#same">same</a> if they are equal.
	 */
	public static final int EQUALITY = 0;

	/**
	 * Mode constant (value 1) indicating that listeners should be considered
	 * the <a href="ListenerList.html#same">same</a> if they are identical.
	 */
	public static final int IDENTITY = 1;

	/**
	 * Indicates the comparison mode used to determine if two
	 * listeners are equivalent
	 */
	protected final boolean identity;

	/**
	 * Creates a ListenerList in which listeners are compared using equality.
	 */
	public ListenerList() {
		this(EQUALITY);
	}

	/**
	 * Creates a ListenerList using the provided comparison mode.
	 * 
	 * @param mode The mode used to determine if listeners are the <a href="ListenerList.html#same">same</a>
	 * @throws IllegalArgumentException if listener not found
	 */
	public ListenerList(int mode) {
		super();
		if (mode != EQUALITY && mode != IDENTITY)
			throw new IllegalArgumentException();
		this.identity = mode == IDENTITY;
	}

	/**
	 * Creates a ListenerList from another ListenerList.
	 * 
	 * @param list The another ListenerList
	 */
	public ListenerList(ListenerList<? extends E> list) {
		super(list);
		identity = list.identity;
	}

	/**
	 * Creates a ListenerList from this ListenerList.
	 */
	@Override
	public ListenerList<E> clone() {
		return new ListenerList<E>(this);
	}

	/**
	 * Finds the <a href="ListenerList.html#same">same</a> listener in this list.
	 *
	 * @param listener the non-<code>null</code> listener to remove
	 * @return the index of found listener
	 * @throws IllegalArgumentException if listener not found
	 */
	@Override
	public int indexOf(Object listener) {
		if (listener == null)
			throw new IllegalArgumentException("Nulls are not allowed");
		int i = -1;
		if( identity ) {
			int iSup = size();
			for( i = 0; i < iSup; i++ )
				if( get(i) == listener )
					break;
			if( i == iSup )
				i = -1;
		} else {
			i = super.indexOf(listener);
		}
		return i;
	}

	/**
	 * Finds the <a href="ListenerList.html#same">same</a> listener in this list.
	 *
	 * @param listener the non-<code>null</code> listener to remove
	 * @return {@code true} if listener is found, else {@code false}
	 * @throws IllegalArgumentException if listener not found
	 */
	@Override
	public boolean contains(Object listener) {
		if( listener == null )
			throw new NullPointerException("Nulls are not allowed"); //Contract of Collection. IMHO, returning false would be good enough
		@SuppressWarnings("unchecked")
		E listenerThis = (E)listener;
		return indexOf(listenerThis) >= 0;
	}

	/**
	 * Adds a listener to this list. This method has no effect if the <a href="ListenerList.html#same">same</a>
	 * listener is already registered.
	 * 
	 * @param listener the non-<code>null</code> listener to add
	 * @return {@code true} if the listener was added
	 * @throws IllegalArgumentException if listener not found
	 */
	public boolean add(E listener) {
		if (listener == null)
			throw new NullPointerException("Nulls are not allowed");
		if( identity ) {
			int iSup = size();
			for( int i = 0; i < iSup; i++ )
				if( get(i) == listener )
					return false;
		} else {
			if( contains(listener) )
				return false;
		}
		super.add(listener);
		return true;
	}

	/**
	 * Removes a listener from this list. Has no effect if the <a href="ListenerList.html#same">same</a>
	 * listener was not already registered.
	 *
	 * @param listener the non-<code>null</code> listener to remove
	 * @return {@code true} if the listener was removed
	 * @throws IllegalArgumentException if listener not found
	 */
	@Override
	public boolean remove(Object listener) {
		if (listener == null)
			throw new IllegalArgumentException("Nulls are not allowed");
		int i = -1;
		if( identity ) {
			int iSup = size();
			for( i = 0; i < iSup; i++ )
				if( get(i) == listener )
					break;
			if( i == iSup )
				i = -1;
		} else {
			i = indexOf(listener);
		}
		if( i != -1 )
			remove(i);
		return i != -1;
	}

	/**
	 * Returns {@code true} if this ListenerList contains all of the elements in the
	 * specified Collection.
	 *
	 * @param   c a collection whose elements will be tested for containment
	 *          in this ListenerList
	 * @return {@code true} if this ListenerList contains all of the elements in the
	 *         specified collection
	 * @throws NullPointerException if the specified collection is null
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		boolean result = true;
		for( final Object e : c ) {
			if( !contains(e) ) {
				result = false;
				break;
			}
		}
		return result;
	}

	/**
	 * Appends all of the elements in the specified Collection to the end of
	 * this ListenerList, in the order that they are returned by the specified
	 * Collection's Iterator.  The behavior of this operation is undefined if
	 * the specified Collection is modified while the operation is in progress.
	 * (This implies that the behavior of this call is undefined if the
	 * specified Collection is this ListenerList, and this ListenerList is nonempty.)
	 *
	 * @param c elements to be inserted into this ListenerList
	 * @return {@code true} if this ListenerList changed as a result of the call
	 * @throws NullPointerException if the specified collection is null
	 */
	@Override
	public boolean addAll(Collection<? extends E> c) {
		boolean result = false;
		for( final E e : c ) {
			if( add(e) ) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Removes from this ListenerList all of its elements that are contained in the
	 * specified Collection.
	 *
	 * @param c a collection of elements to be removed from the ListenerList
	 * @return {@code true} if this ListenerList changed as a result of the call
	 * @throws ClassCastException if the types of one or more elements
	 *         in this ListenerList are incompatible with the specified
	 *         collection
	 * (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if this ListenerList contains one or more null
	 *         elements and the specified collection does not support null
	 *         elements
	 * (<a href="Collection.html#optional-restrictions">optional</a>),
	 *         or if the specified collection is null
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		boolean result = false;
		for( final Object o : c ) {
			if( remove(o) ) {
				result = true;
			}
		}
		return result;
	}

	/**
	 * Retains only the elements in this ListenerList that are contained in the
	 * specified Collection.  In other words, removes from this ListenerList all
	 * of its elements that are not contained in the specified Collection.
	 *
	 * @param c a collection of elements to be retained in this ListenerList
	 *          (all other elements are removed)
	 * @return {@code true} if this ListenerList changed as a result of the call
	 * @throws ClassCastException if the types of one or more elements
	 *         in this ListenerList are incompatible with the specified
	 *         collection
	 * (<a href="Collection.html#optional-restrictions">optional</a>)
	 * @throws NullPointerException if this ListenerList contains one or more null
	 *         elements and the specified collection does not support null
	 *         elements
	 *         (<a href="Collection.html#optional-restrictions">optional</a>),
	 *         or if the specified collection is null
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		//Currently same implementation as of Vector, but might improve it later
		boolean result = false;
		final Iterator<E> it = iterator();
		while (it.hasNext()) {
			final E e = it.next();
			if (!c.contains(e)) {
				it.remove();
				result = true;
			}
		}
		return result;
	}

	/**
	 * Returns an iterator over those elements in this list in proper sequence
	 * which result in true by the predicator.
	 * 
	 * <p>
	 * The returned iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
	 * 
	 * @return an iterator over the elements in this list in proper sequence
	 */
	public synchronized Iterator<E> iteratorOf(IListenerListIteratorPredicate<E> predicator) {
		return new ItrOfClass(predicator);
	}

	/**
	 * An optimized version of AbstractList.Itr
	 */
	private class ItrOfClass implements Iterator<E> {
		int cursor = 0; // index of next element to return
		int lastRet = -1; // index of last element returned; -1 if no such
		int expectedModCount = modCount;
		IListenerListIteratorPredicate<E> predicator;

//		public ItrOfClass() {
//			this(null);
//		}

		public ItrOfClass(IListenerListIteratorPredicate<E> predicator) {
			this.predicator = predicator;
		}

		@SuppressWarnings("unchecked")
		protected void findNext() {
			while( cursor < elementCount && !predicator.evaluate((E)elementData[cursor]) )
				cursor++;
		}

		protected void findNext(int i) {
			cursor = i;
			findNext();
		}

		public boolean hasNext() {
			synchronized (ListenerList.this) {
				checkForComodification();
				findNext();
				return cursor != elementCount;
			}
		}

		public E next() {
			synchronized (ListenerList.this) {
				checkForComodification();
				int i = cursor;
				if (i >= elementCount)
					throw new NoSuchElementException();
				findNext(i + 1);
				@SuppressWarnings("unchecked")
				E element = (E)elementData[lastRet = i];
				return element;
			}
		}

		public void remove() {
			if (lastRet == -1)
				throw new IllegalStateException();
			synchronized (ListenerList.this) {
				checkForComodification();
				ListenerList.this.remove(lastRet);
				expectedModCount = modCount;
				findNext(lastRet);
				lastRet = -1;
			}
		}

		final void checkForComodification() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
		}
	}
}
