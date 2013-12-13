/*
 * Copyright (c) 2013 European Molecular Biology Laboratory
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.apache.commons.collections4.set;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.apache.commons.collections4.map.ListHashedMap;

/**
 * This class implements the {@code Set} interface, backed by a hash table
 * (actually a {@code HashMap} instance). It makes no guarantees as to the
 * iteration order of the set; in particular, it does not guarantee that the
 * order will remain constant over time. This class permits the {@code null}
 * element.
 * 
 * <p>
 * This class offers constant time performance for the basic operations (
 * {@code add}, {@code remove}, {@code contains} and {@code size}), assuming
 * the hash function disperses the elements properly among the buckets.
 * Iterating over this set requires time proportional to the sum of the
 * {@code HashSet} instance's size (the number of elements) plus the "capacity"
 * of the backing {@code HashMap} instance (the number of buckets). Thus, it's
 * very important not to set the initial capacity too high (or the load factor
 * too low) if iteration performance is important.
 * 
 * <p>
 * <strong>Note that this implementation is not synchronized.</strong> If
 * multiple threads access a hash set concurrently, and at least one of the
 * threads modifies the set, it <i>must</i> be synchronized externally. This is
 * typically accomplished by synchronizing on some object that naturally
 * encapsulates the set.
 * 
 * If no such object exists, the set should be "wrapped" using the
 * {@link SynchronizedSet#synchronizedSet SynchronizedSet.synchronizedSet} method. This
 * is best done at creation time, to prevent accidental unsynchronized access to
 * the set:
 * 
 * <pre>
 *   Set s = SynchronizedSet.synchronizedSet(new ListHashedSet(...));
 * </pre>
 * 
 * <p>
 * The iterators returned by this class's {@code iterator} method are
 * <i>fail-fast</i>: if the set is modified at any time after the iterator is
 * created, in any way except through the iterator's own {@code remove} method,
 * the Iterator throws a {@link ConcurrentModificationException}. Thus, in the
 * face of concurrent modification, the iterator fails quickly and cleanly,
 * rather than risking arbitrary, non-deterministic behavior at an undetermined
 * time in the future.
 * 
 * <p>
 * Note that the fail-fast behavior of an iterator cannot be guaranteed as it
 * is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification. Fail-fast iterators throw
 * {@code ConcurrentModificationException} on a best-effort basis. Therefore,
 * it would be wrong to write a program that depended on this exception for its
 * correctness: <i>the fail-fast behavior of iterators should be used only to
 * detect bugs.</i>
 * 
 * <p>
 * This class is a member of the <a href="{@docRoot}
 * /../technotes/guides/collections/index.html"> Java Collections Framework</a>.
 * 
 * @param <E>
 *            the type of elements maintained by this set
 * 
 * @see Set
 * @see List
 * @since ?
 * 
 * @author Gábor Náray
 * 
 */

public class ListHashedSet<E> extends AbstractSet<E> implements Set<E>, List<E>,
		Cloneable, java.io.Serializable {

	private static final long serialVersionUID = 3149055294889160912L;

	private transient ListHashedMap<E, Object> map;

	// Dummy value to associate with an Object in the backing Map
	private static final Object PRESENT = new Object();

	/**
	 * Constructs a new, empty set; the backing {@code HashMap} instance has
	 * default initial capacity (16) and load factor (0.75).
	 */
	public ListHashedSet() {
		map = new ListHashedMap<E, Object>();
	}

	/**
	 * Constructs a new set containing the elements in the specified collection.
	 * The {@code HashMap} is created with default load factor (0.75) and an
	 * initial capacity sufficient to contain the elements in the specified
	 * collection.
	 * 
	 * @param c
	 *            the collection whose elements are to be placed into this set
	 * @throws NullPointerException
	 *             if the specified collection is null
	 */
	public ListHashedSet(Collection<? extends E> c) {
		map = new ListHashedMap<E, Object>(Math.max((int) (c.size() / .75f) + 1, 16));
		addAll(c);
	}

	/**
	 * Constructs a new, empty set; the backing {@code HashMap} instance has
	 * the specified initial capacity and the specified load factor.
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the hash map
	 * @param loadFactor
	 *            the load factor of the hash map
	 * @throws IllegalArgumentException
	 *             if the initial capacity is less than zero, or if the load
	 *             factor is nonpositive
	 */
	public ListHashedSet(int initialCapacity, float loadFactor) {
		map = new ListHashedMap<E, Object>(initialCapacity, loadFactor);
	}

	/**
	 * Constructs a new, empty set; the backing {@code HashMap} instance has
	 * the specified initial capacity and default load factor (0.75).
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the hash table
	 * @throws IllegalArgumentException
	 *             if the initial capacity is less than zero
	 */
	public ListHashedSet(int initialCapacity) {
		map = new ListHashedMap<E, Object>(initialCapacity);
	}

	/**
	 * Constructs a new, empty linked hash set. (This package private
	 * constructor is only used by LinkedHashSet.) The backing HashMap instance
	 * is a LinkedHashMap with the specified initial capacity and the specified
	 * load factor.
	 * 
	 * @param initialCapacity
	 *            the initial capacity of the hash map
	 * @param loadFactor
	 *            the load factor of the hash map
	 * @param dummy
	 *            ignored (distinguishes this constructor from other int, float
	 *            constructor.)
	 * @throws IllegalArgumentException
	 *             if the initial capacity is less than zero, or if the load
	 *             factor is nonpositive
	 */
	ListHashedSet(int initialCapacity, float loadFactor, boolean dummy) {
		map = new ListHashedMap<E, Object>(initialCapacity, loadFactor);
	}

	/**
	 * Returns an iterator over the elements in this set. The elements are
	 * returned in no particular order.
	 * 
	 * @return an Iterator over the elements in this set
	 * @see ConcurrentModificationException
	 */
	@Override
	public Iterator<E> iterator() {
		return map.keySet().iterator();
	}

	/**
	 * Returns the number of elements in this set (its cardinality).
	 * 
	 * @return the number of elements in this set (its cardinality)
	 */
	@Override
	public int size() {
		return map.size();
	}

	/**
	 * Returns {@code true} if this set contains no elements.
	 * 
	 * @return {@code true} if this set contains no elements
	 */
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}

	/**
	 * Returns {@code true} if this set contains the specified element. More
	 * formally, returns {@code true} if and only if this set contains an
	 * element {@code e} such that
	 * {@code (o==null ? e==null : o.equals(e))}.
	 * 
	 * @param o
	 *            element whose presence in this set is to be tested
	 * @return {@code true} if this set contains the specified element
	 */
	@Override
	public boolean contains(final Object o) {
		return map.containsKey(o);
	}

	/**
	 * Returns an array containing all of the elements in this collection.
	 * <p>
	 * This implementation returns an array containing all the elements returned
	 * by the backing map in the same order, stored in consecutive elements of
	 * the array, starting with index {@code 0}. The length of the returned
	 * array is equal to the number of elements of the backing map. This method
	 * does not permit concurrent modification, use synchronization if that is
	 * required.
	 * 
	 * <p>
	 * The returned array will be "safe" in that no references to it are
	 * maintained by this collection. (In other words, this method must allocate
	 * a new array even if this collection is backed by an array). The caller is
	 * thus free to modify the returned array.
	 * 
	 * <p>
	 * This method acts as bridge between array-based and collection-based APIs.
	 * 
	 * @return an array containing all of the elements in this collection
	 */
	public Object[] toArray() {
		return map.toArray();
	}

	/**
	 * Returns an array containing all of the elements in this collection; the
	 * runtime type of the returned array is that of the specified array. If the
	 * collection fits in the specified array, it is returned therein.
	 * Otherwise, a new array is allocated with the runtime type of the
	 * specified array and the size of this collection.
	 * 
	 * <p>
	 * If this collection fits in the specified array with room to spare (i.e.,
	 * the array has more elements than this collection), the element in the
	 * array immediately following the end of the collection is set to
	 * <tt>null</tt>. (This is useful in determining the length of this
	 * collection <i>only</i> if the caller knows that this collection does not
	 * contain any <tt>null</tt> elements.)
	 * 
	 * <p>
	 * This implementation returns an array containing all the elements returned
	 * by the backing map in the same order, stored in consecutive elements of
	 * the array, starting with index {@code 0}. If the number of elements
	 * returned by the iterator is too large to fit into the specified array,
	 * then the elements are returned in a newly allocated array with length
	 * equal to the number of elements returned by the backing map. This method
	 * does not permit concurrent modification, use synchronization if that is
	 * required.
	 * 
	 * <p>
	 * Like the {@link #toArray()} method, this method acts as bridge between
	 * array-based and collection-based APIs. Further, this method allows
	 * precise control over the runtime type of the output array, and may, under
	 * certain circumstances, be used to save allocation costs.
	 * 
	 * <p>
	 * Suppose <tt>x</tt> is a collection known to contain only strings. The
	 * following code can be used to dump the collection into a newly allocated
	 * array of <tt>String</tt>:
	 * 
	 * <pre>
	 * String[] y = x.toArray(new String[0]);
	 * </pre>
	 * 
	 * Note that <tt>toArray(new Object[0])</tt> is identical in function to
	 * <tt>toArray()</tt>.
	 * 
	 * @param a
	 *            the array into which the elements of this collection are to be
	 *            stored, if it is big enough; otherwise, a new array of the
	 *            same runtime type is allocated for this purpose.
	 * @return an array containing all of the elements in this collection
	 * @throws ArrayStoreException
	 *             if the runtime type of the specified array is not a supertype
	 *             of the runtime type of every element in this collection
	 * @throws NullPointerException
	 *             if the specified array is null
	 */
	public <T> T[] toArray(T[] a) {
		return map.toArray(a);
	}

	/**
	 * Adds the specified element to this set if it is not already present. More
	 * formally, adds the specified element {@code e} to this set if this set
	 * contains no element {@code e2} such that
	 * {@code (e==null ? e2==null& : e.equals(e2))}. If this
	 * set already contains the element, the call leaves the set unchanged and
	 * returns {@code false}.
	 * 
	 * @param e
	 *            element to be added to this set
	 * @return {@code true} if this set did not already contain the specified
	 *         element
	 */
	@Override
	public boolean add(final E e) {
		return map.put(e, PRESENT) == null;
	}

	/**
	 * Removes the specified element from this set if it is present. More
	 * formally, removes an element {@code e} such that
	 * {@code (o==null ? e==null : o.equals(e))}, if this
	 * set contains such an element. Returns {@code true} if this set contained
	 * the element (or equivalently, if this set changed as a result of the
	 * call). (This set will not contain the element once the call returns.)
	 * 
	 * @param o
	 *            object to be removed from this set, if present
	 * @return {@code true} if the set contained the specified element
	 */
	@Override
	public boolean remove(final Object o) {
		return map.remove(o) == PRESENT;
	}

	/**
	 * Removes all of the elements from this set. The set will be empty after
	 * this call returns.
	 */
	@Override
	public void clear() {
		map.clear();
	}

	/**
	 * Returns a shallow copy of this {@code HashSet} instance: the elements
	 * themselves are not cloned.
	 * 
	 * @return a shallow copy of this set
	 */
	@Override
	public Object clone() {
		try {
			@SuppressWarnings("unchecked")
			ListHashedSet<E> newSet = (ListHashedSet<E>) super.clone();
			newSet.map = (ListHashedMap<E, Object>) map.clone();
			return newSet;
		} catch (CloneNotSupportedException e) {
			throw new InternalError();
		}
	}

	/**
	 * Save the state of this {@code HashSet} instance to a stream (that is,
	 * serialize it).
	 * 
	 * @serialData The capacity of the backing {@code HashMap} instance (int),
	 *             and its load factor (float) are emitted, followed by the size
	 *             of the set (the number of elements it contains) (int),
	 *             followed by all of its elements (each an Object) in no
	 *             particular order.
	 */
	private void writeObject(java.io.ObjectOutputStream s)
			throws java.io.IOException {
		// Write out any hidden serialization magic
		s.defaultWriteObject();

		// Write out HashMap capacity and load factor
		s.writeInt(map.getCapacity());
		s.writeFloat(map.getLoadFactor());

		// Write out size
		s.writeInt(map.size());

		// Write out all elements in the proper order.
		for (E e : map.keySet())
			s.writeObject(e);
	}

	/**
	 * Reconstitute the {@code HashSet} instance from a stream (that is,
	 * deserialize it).
	 */
	private void readObject(java.io.ObjectInputStream s)
			throws java.io.IOException, ClassNotFoundException {
		// Read in any hidden serialization magic
		s.defaultReadObject();

		// Read in HashMap capacity and load factor and create backing HashMap
		int capacity = s.readInt();
		float loadFactor = s.readFloat();
		map = new ListHashedMap<E, Object>(capacity, loadFactor);

		// Read in size
		int size = s.readInt();

		// Read in all elements in the proper order.
		for (int i = 0; i < size; i++) {
			@SuppressWarnings("unchecked")
			E e = (E) s.readObject();
			map.put(e, PRESENT);
		}
	}

	/**
	 * Adds all of the elements in the specified collection to this set if
	 * they're not already present (optional operation). If the specified
	 * collection is also a set, the <tt>addAll</tt> operation effectively
	 * modifies this set so that its value is the <i>union</i> of the two sets.
	 * The behavior of this operation is undefined if the specified collection
	 * is modified while the operation is in progress.
	 * <p>
	 * This implementation iterates over the specified collection, and adds each
	 * object returned by the iterator to this collection, in turn.
	 * 
	 * @param c
	 *            collection containing elements to be added to this set
	 * @return <tt>true</tt> if this set changed as a result of the call
	 * 
	 * @throws NullPointerException
	 *             if the specified collection contains one or more null
	 *             elements and this set does not permit null elements, or if
	 *             the specified collection is null
	 * @see #add(Object)
	 */
	public boolean addAll(Collection<? extends E> c) {
		boolean modified = false;
		for (E e : c)
			if( !map.containsKey(e) ) {
				add(e);
				modified = true;
			}
		return modified;
	}

	/**
	 * Adds the elements of specified collection to this set at index position
	 * if the elements are not already present. More formally, adds each element
	 * {@code e} of specified collection to this set if this set contains no
	 * element {@code e2} such that {@code (e==null ? e2==null : e.equals(e2))}
	 * . If this set already contains any element, those elements are ignored.
	 * 
	 * @param index
	 *            the list index where to add
	 * @param c
	 *            collection to be added to this set
     * @return <tt>true</tt> if this set changed as a result of the call
	 */
	@Override
	public boolean addAll(final int index, final Collection<? extends E> c) {
//		return map.addAll(index, c);
		boolean modified = false;
		int i = index;
		for (E e : c)
			if( !map.containsKey(e) ) {
				map.put(i, e, PRESENT);
				i++;
				modified = true;
			}
		return modified;
	}

	/**
	 * Removes from this set all of its elements that are contained in the
	 * specified collection (optional operation). If the specified collection is
	 * also a set, this operation effectively modifies this set so that its
	 * value is the <i>asymmetric set difference</i> of the two sets.
	 * <p>
	 * This implementation loops over this collection backwards, checking each
	 * element at current index in turn to see if it's contained in the
	 * specified collection. If it's so contained, it's removed from this
	 * collection with the <tt>remove</tt> method.
	 * 
	 * @param c
	 *            collection containing elements to be removed from this set
	 * @return <tt>true</tt> if this set changed as a result of the call
	 * @throws NullPointerException
	 *             if this set contains a null element and the specified
	 *             collection does not permit null elements (<a
	 *             href="Collection.html#optional-restrictions">optional</a>),
	 *             or if the specified collection is null
	 * @see #remove(Object)
	 * @see #contains(Object)
	 */
	public boolean removeAll(Collection<?> c) {
		return map.removeAll(c);
	}

	/**
	 * Retains only the elements in this set that are contained in the specified
	 * collection (optional operation). In other words, removes from this set
	 * all of its elements that are not contained in the specified collection.
	 * If the specified collection is also a set, this operation effectively
	 * modifies this set so that its value is the <i>intersection</i> of the two
	 * sets.
	 * <p>
	 * This implementation loops over this collection backwards, checking each
	 * element at current index in turn to see if it's contained in the
	 * specified collection. If it's not so contained, it's removed from this
	 * collection with the <tt>remove</tt> method.
	 * 
	 * @param c
	 *            collection containing elements to be retained in this set
	 * @return <tt>true</tt> if this set changed as a result of the call
	 * @throws NullPointerException
	 *             if this set contains a null element and the specified
	 *             collection does not permit null elements (<a
	 *             href="Collection.html#optional-restrictions">optional</a>),
	 *             or if the specified collection is null
	 * @see #remove(Object)
	 * @see #contains(Object)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		return map.retainAll(c);
	}

	@Override
	public E get(final int index) {
		return map.get(index);
	}

	@Override
	public E set(final int index, final E element) {
		E result = map.get(index);
		map.set(index, element, PRESENT);
		return result;
	}

	/**
	 * Adds the specified element to this set at index position if it is not
	 * already present. More formally, adds the specified element {@code e} to
	 * this set if this set contains no element {@code e2} such that
	 * {@code (e==null ? e2==null : e.equals(e2))}. If this set already
	 * contains the element, the call leaves the set unchanged.
	 * 
	 * @param index
	 *            the list index where to add
	 * @param e
	 *            element to be added to this set
	 */
	@Override
	public void add(final int index, final E element) {
		if( map.containsKey(element) )
			return;
		map.put(index, element, PRESENT);
	}

	@Override
	public E remove(final int index) {
		E result = map.get(index);
		map.remove(index);
		return result;
	}

	@Override
	public int indexOf(final Object o) {
		return map.indexOf(o);
	}

	@Override
	public int lastIndexOf(final Object o) {
		return map.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return map.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(final int index) {
		return map.listIterator(0);
	}

	/**
	 * <p>Marked as deprecated, meaning it is not supported yet, but might be in the future.
	 * 
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	@Override
	@Deprecated
	public List<E> subList(int fromIndex, final int toIndex) {
		return map.subList(fromIndex, toIndex);
	}
}
