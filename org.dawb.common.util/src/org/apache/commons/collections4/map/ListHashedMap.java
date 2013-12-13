/*
 * Copyright (c) 2013 European Molecular Biology Laboratory
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.apache.commons.collections4.map;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.RandomAccess;
import java.util.Set;

import org.apache.commons.collections4.IterableMap;
import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.ResettableIterator;
import org.apache.commons.collections4.iterators.EmptyIterator;
import org.apache.commons.collections4.iterators.EmptyListIterator;
import org.apache.commons.collections4.iterators.EmptyMapIterator;
//import org.apache.commons.collections4.map.AbstractHashedMap;
//import org.apache.commons.collections4.map.HashedMap;

/**
 * This class is a combination of HashMap and ArrayList.
 * It is a HashMap, with an additional ArrayList to provide List like interface
 * for both keys and values.
 * This class would implement List<Entry<K, V>>, if the Java developers would
 * not have made the Map and List classes incompatible.
 * 
 * @param <K>
 * @param <V>
 * 
 * @author Gábor Náray
 *
 */
public class ListHashedMap<K, V> extends HashedMap_Dawn<K, V> implements IterableMap<K, V>, Serializable, Cloneable, RandomAccess
//, List<Map.Entry<K, V>>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4125717239920072723L;

	protected static final String DISABLED_OPERATION = "The called method is disabled (and deprecated)";
	protected static final String INVALID_INDEX = "Array index out of range";
	protected static final String INVALID_RANGE = "End index must not be less than start index";
	
	
//	protected final transient ArrayList<Entry<K, V>> list = new ArrayList<Entry<K, V>>();
	public transient ArrayList<Entry<K, V>> list;

	/**
	 * Constructs a new empty {@code ListHashedMap} with default capacity, size
	 * and load factor.
	 */
	public ListHashedMap() {
		super();
	}

	/**
	 * Constructs a new, empty {@code ListHashedMap} with the specified initial
	 * capacity.
	 * 
	 * @param initialCapacity
	 *            the initial capacity
	 * @throws IllegalArgumentException
	 *             if the initial capacity is negative
	 */
	public ListHashedMap(final int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * Constructs a new, empty {@code ListHashedMap} with the specified initial
	 * capacity and load factor.
	 * 
	 * @param initialCapacity
	 *            the initial capacity
	 * @param loadFactor
	 *            the load factor
	 * @throws IllegalArgumentException
	 *             if the initial capacity is negative
	 * @throws IllegalArgumentException
	 *             if the load factor is less than zero
	 */
	public ListHashedMap(final int initialCapacity, final float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/**
	 * Constructor copying mappings from another map, which is not {@code ListHashedMap}.
	 * Thus the order of mappings is specified by the iterator of map.
	 * 
	 * @param map
	 *            the map to copy
	 * @throws NullPointerException
	 *             if the map is null
	 */
	public ListHashedMap(final Map<? extends K, ? extends V> map) {
		super(map);
	}

	/**
	 * Constructor copying mappings from another {@code ListHashedMap}.
	 * 
	 * @param lhm
	 *            the {@code ListHashedMap} to copy
	 * @throws NullPointerException
	 *             if the {@code ListHashedMap} is null
	 */
	public ListHashedMap(final ListHashedMap<? extends K, ? extends V> lhm) {
		super(lhm.data.length, lhm.loadFactor);
		putAll(lhm);
	}

	/**
	 * Initialise subclasses during construction, cloning or deserialization.
	 */
	@Override
	protected void init() {
		list = new ArrayList<Entry<K, V>>();
//		list = new ArrayList<Entry<K, V>>() {
//			private static final long serialVersionUID = 7994300649012272523L;
//			private transient Object[] elementData = new Object[100];
//			private transient HashEntry<K, V> entry = createEntry(null, 123, (K)new String("1"), (V)new Integer(2));//next, hashCode, key, value
//			private String outOfBoundsMsg(int index) {
//				return "Index: "+index+", Size: "+size;
//			}
//			private void rangeCheck(int index) {
//				if (index >= size)
//					throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
//			}
//			Entry<K, V> elementData(int index) {
//				Object o = elementData[index];
//				return entry;
//			}
//			public Entry<K, V> get(int index) {
//				return super.get(index);
////				return entry;
////				rangeCheck(index);
//	//
////				return elementData(index);
//			}
//		};
	}

	// -----------------------------------------------------------------------
	/**
	 * Clones the {@code ListHashedMap} without cloning the keys or values.
	 * 
	 * @return a shallow clone
	 */
	@Override
	public ListHashedMap<K, V> clone() {
		return new ListHashedMap<K, V>(this); //Why would bother with super.clone()?
	}

	// -----------------------------------------------------------------------

	/**
	 * Do not call this method!
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	@Deprecated
	protected void addMapping(final int hashIndex, final int hashCode, final K key, final V value) {
		throw new UnsupportedOperationException(DISABLED_OPERATION);
	}

	/**
	 * Adds a new key-value mapping into this map.
	 * <p>
	 * This implementation calls <code>createEntry()</code>,
	 * <code>addEntry()</code> and <code>checkCapacity()</code>. It also handles
	 * changes to <code>modCount</code> and <code>size</code>. Subclasses could
	 * override to fully control adds to the map.
	 * 
	 * @param hashIndex
	 *            the index into the data array to store at
	 * @param hashCode
	 *            the hash code of the key to add
	 * @param key
	 *            the key to add
	 * @param value
	 *            the value to add
	 * @param index
	 *            the list index where to add
	 * @param add
	 *            true if adding the mapping at index, false if setting it
	 */
	protected void addMappingAtIndex(final int hashIndex, final int hashCode,
			final K key, final V value, final int index, boolean add) {
		modCount++;
		final HashEntry<K, V> entry = createEntry(data[hashIndex], hashCode,
				key, value);
		addEntry(entry, hashIndex);
		size++;
		checkCapacity();
		if( add )
			list.add(index, entry);
		else {
			super.remove(list.get(index).getKey());
			list.set(index, entry);
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * Removes the specified mapping from the map, also removes it from
	 * the internal list by {@code list.remove(index)} if
	 * {@code index>=0}, else by {@code list.remove(mapping)}.
	 * <p>
	 * This method takes O(1) time with optimal hashing or O(n(this)) with worst
	 * hashing, plus O(1) when {@code index>=0} else O(n(this)).
	 * It is expensive when either hashing is worst or {@code index<0}.
.
	 * @param entry
	 *            the entry to remove
	 * @param hashIndex
	 *            the index into the data structure
	 * @param previous
	 *            the previous entry in the chain
	 * @param index
	 *            the list index where to remove (see description)
	 */
	protected void removeMapping(final HashEntry<K, V> entry,
			final int hashIndex, final HashEntry<K, V> previous, int index) {
		//Must remove from list before removeMapping, because latter destroys entry
		if( index >= 0 )
			list.remove(index);
		else
			list.remove(entry);
		removeMapping(entry, hashIndex, previous);
	}

	/**
	 * Clears the map, resetting the size to zero and nullifying references
	 * to avoid garbage collection issues.
	 * <p>See {@link HashedMap_Dawn#clear()}
	 */

	// -----------------------------------------------------------------------
	@Override
	public void clear() {
		super.clear();
		list.clear();
	}

	/**
	 * Moves a mapping from {@code oldIndex} to {@code newIndex} in the internal list.
	 * <p>
	 * Note: If the {@code oldIndex<newIndex} then due to left shifting of mappings through the
	 * {@code newIndex}, the mapping at {@code newIndex} before the movement will be
	 * the mapping at {@code newIndex-1} after the movement. 
	 * @param oldIndex
	 *            the index to move from
	 * @param newIndex
	 *            the index to move to
	 */
	public void move(final int oldIndex, final int newIndex) {
		if( oldIndex == newIndex )
			return;
		list.add(newIndex, list.remove(oldIndex)); //Could be optimized with better list
	}

	// -----------------------------------------------------------------------
	/**
	 * Puts a key-value mapping into this map, and also inserts it at the index
	 * of the internal list. If the key of mapping previously existed, then that
	 * mapping is removed from the internal list after the new mapping is added
	 * to it. It means the mapping is correctly added as it is expected from a
	 * list, but if the previous mapping had less index, then the index of new
	 * mapping is decreased by one caused by the removal.
	 * 
	 * @param index
	 *            the list index where to add
	 * @param key
	 *            the key to add
	 * @param value
	 *            the value to add
	 * @return the value previously mapped to this key, null if none
	 */
	public V put(final int index, final K key, final V value) {
		int oldIndex = -1;
		V oldValue = null;
		final Object convertedKey = convertKey(key);
		final int hashCode = hash(convertedKey);
		final int hIndex = hashIndex(hashCode, data.length);
		HashEntry<K, V> entry = data[hIndex];
		HashEntry<K, V> previous = null;
		while (entry != null) {
			if (entry.hashCode == hashCode
					&& isEqualKey(convertedKey, entry.key)) {
				oldValue = entry.getValue();
				if( index < size && isEqualKey(convertedKey, ((HashEntry<K, V>)list.get(index)).key) ) {
					updateEntry(entry, value);
					return oldValue;
				}
				oldIndex = indexOf(entry.getKey());
				removeMapping(entry, hIndex, previous);
				break;
			}
			entry = entry.next;
			previous = entry;
		}

		addMappingAtIndex(hIndex, hashCode, key, value, index, true);
		if( entry != null ) {
			if( index < oldIndex ) //In case the new mapping was added below oldIndex, then oldIndex must be corrected
				oldIndex++;
			list.remove(oldIndex);
		}
		return oldValue;
	}

	/**
	 * Puts a key-value mapping into this map, and also adds it to the end of
	 * the internal list. If the key of mapping previously existed, then that
	 * mapping is removed from the internal list after the new mapping is added
	 * to it. It means the mapping is correctly added as it is expected from a
	 * list, but if the previous mapping had less index, then the index of new
	 * mapping is decreased by one caused by the removal. *
	 * 
	 * @param key
	 *            the key to add
	 * @param value
	 *            the value to add
	 * @return the value previously mapped to this key, null if none
	 *         <p>
	 *         See {@link HashedMap_Dawn#put(Object, Object)}
	 */
	@Override
	public V put(final K k, final V v) {
		return put(list.size(), k, v);
	}

	/**
	 * Puts all the key-value mappings from the specified map into this map, and
	 * also inserts those one after the other at the index of the internal list.
	 * If the key of a mapping previously existed, then that mapping is removed
	 * from the internal list after the new mapping is added to it. It means the
	 * mapping is correctly added as it is expected from a list, but if the
	 * previous mapping had less index, then the index of new mapping is
	 * decreased by one caused by the removal. This can cause decreasing the
	 * index by at most map.size(), and for easier usage this decrementing value
	 * is returned.
	 * <p>
	 * This implementation iterates around the specified map and uses
	 * {@link #put(int, Object, Object)}.
	 * 
	 * @param index
	 *            the list index where to add
	 * @param map
	 *            the map to add
	 * @return the difference between the index and the new index of mapping
	 *         which is put at index, or 0 if map is empty, see the description
	 * @throws NullPointerException
	 *             if the map is null
	 */
	public int putAll(final int index, final Map<? extends K, ? extends V> map) {
		final int mapSize = map.size();
		if (mapSize == 0) {
			return 0;
		}
		int i = index;
		final int newSize = (int) ((size + mapSize) / loadFactor + 1);
		ensureCapacity(calculateNewCapacity(newSize));
		for (final Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
			put(i, entry.getKey(), entry.getValue());
			if( i < size && isEqualKey(convertKey(entry.getKey()), ((HashEntry<K, V>)list.get(i)).key) )
				i++;
		}
		return mapSize - (i - index);
	}

	// -----------------------------------------------------------------------
	/**
	 * Removes the mapping of specified key from this map, also removes it from
	 * the internal list, by {@code removeMapping(..., index)}.
	 * <p>
	 * This method takes O(1) time with optimal hashing or O(n(this)) with worst
	 * hashing, plus in case the mapping exists: O(1) when {@code index>=0} else O(n(this)).
	 * It is expensive when either hashing is worst or {@code index<0}.
	 * 
	 * @param key
	 *            the key to remove
	 * @param index
	 *            the list index where to remove (see description)
	 * @return the value (can be null) mapped to the removed key, or null if key
	 *         not in map
	 */
	protected V remove(final Object key, final int index) {
		final Object convertedKey = convertKey(key);
		final int hashCode = hash(convertedKey);
		final int hIndex = hashIndex(hashCode, data.length);
		HashEntry<K, V> entry = data[hIndex];
		HashEntry<K, V> previous = null;
		while (entry != null) {
			if (entry.hashCode == hashCode && isEqualKey(convertedKey, entry.key)) {
				final V oldValue = entry.getValue();
				removeMapping(entry, hIndex, previous, index);
				return oldValue;
			}
			previous = entry;
			entry = entry.next;
		}
		return null;
	}

	/**
	 * Removes the mapping of specified key from this map, also removes it from
	 * the internal list.
	 * <p>
	 * This method takes O(1) time with optimal hashing or O(n(this)) with worst
	 * hashing, plus O(n(this)). It is expensive.
	 * <p>
	 * See {@link HashedMap_Dawn#remove(Object)}
	 * 
	 * @param key
	 *            the key to remove
	 * @return the value (can be null) mapped to the removed key, or null if key
	 *         not in map
	 */
	@Override
	public V remove(Object key) {
		return remove(key, -1);
	}

	// -----------------------------------------------------------------------
	/**
	 * Gets an iterator over the map. Changes made to the iterator affect this
	 * map.
	 * <p>
	 * A MapIterator returns the keys in the map. It also provides convenient
	 * methods to get the key and value, and set the value. It avoids the need
	 * to create an entrySet/keySet/values object. It also avoids creating the
	 * Map.Entry object.
	 * 
	 * @return the map iterator
	 */
	@Override
	public MapIterator<K, V> mapIterator() {
		if (size == 0) {
			return EmptyMapIterator.<K, V> emptyMapIterator();
		}
		return new HashMapIterator<K, V>(this);
	}

	/**
	 * MapIterator implementation.
	 */
	protected static class HashMapIterator<K, V> extends ListHashIterator<K, V>
			implements MapIterator<K, V>, ResettableIterator<K>, ListIterator<K> {

		protected HashMapIterator(final ListHashedMap<K, V> parent) {
			super(parent);
		}

		public K next() {
			return nextEntry().getKey();
		}

		public K getKey() {
			final HashEntry<K, V> current = currentEntry();
			if (current == null) {
				throw new IllegalStateException(
						AbstractHashedMap_Dawn.GETKEY_INVALID);
			}
			return current.getKey();
		}

		public V getValue() {
			final HashEntry<K, V> current = currentEntry();
			if (current == null) {
				throw new IllegalStateException(
						AbstractHashedMap_Dawn.GETVALUE_INVALID);
			}
			return current.getValue();
		}

		public V setValue(final V value) {
			final HashEntry<K, V> current = currentEntry();
			if (current == null) {
				throw new IllegalStateException(
						AbstractHashedMap_Dawn.SETVALUE_INVALID);
			}
			return current.setValue(value);
		}

		@Override
		public K previous() {
			return previousEntry().getKey();
		}

		@Override
		public void set(K e) {
			throw new UnsupportedOperationException(
					"set(Object) method is not supported");
		}

		@Override
		public void add(K e) {
			throw new UnsupportedOperationException(
					"set(Object) method is not supported");
		}

	}

	// -----------------------------------------------------------------------
	/**
	 * Gets the entrySet view of the map. Changes made to the view affect this
	 * map. To simply iterate through the entries, use {@link #mapIterator()}.
	 * 
	 * @return the entrySet view
	 */
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		if (entrySet == null) {
			entrySet = new ListEntrySet<K, V>(this);
		}
		return entrySet;
	}

	protected static class ListEntrySet<K, V> extends EntrySet<K, V> {
		/** The parent map */
		// Super has it, but it is private, and need type of this for added iterator(int)
		protected final ListHashedMap<K, V> parent;

		protected ListEntrySet(ListHashedMap<K, V> parent) {
			super(parent);
			this.parent = parent;
		};

		public ListIterator<Map.Entry<K, V>> iterator(final int index) {
			return this.parent.createEntrySetIterator(index);
		}

		@Override
		public ListIterator<Map.Entry<K, V>> iterator() {
			return parent.createEntrySetIterator();
		}
	}

	/**
	 * Creates an entry set iterator which starts at index. Subclasses can
	 * override this to return iterators with different properties.
	 * 
	 * @param index
	 *            the index where iterating is started
	 * @return the entrySet iterator
	 */
	protected ListIterator<Map.Entry<K, V>> createEntrySetIterator(final int index) {
		if (size() == 0) {
			return EmptyListIterator.<Map.Entry<K, V>> emptyListIterator();
		}
		return new EntrySetIterator<K, V>(this, index);
	}

	/**
	 * Creates an entry set iterator. Subclasses can override this to return
	 * iterators with different properties, however this calls
	 * {@code createEntrySetIterator(0)}, thus that should be overridden.
	 * 
	 * @return the entrySet iterator
	 */
	@Override
	protected ListIterator<Map.Entry<K, V>> createEntrySetIterator() {
		return createEntrySetIterator(0);
	}

	/**
	 * EntrySet iterator.
	 */
	protected static class EntrySetIterator<K, V> extends
			ListHashIterator<K, V> implements ListIterator<Map.Entry<K, V>>, ResettableIterator<Map.Entry<K, V>> {

		protected EntrySetIterator(final ListHashedMap<K, V> parent) {
			super(parent);
		}

		protected EntrySetIterator(final ListHashedMap<K, V> parent, final int startIndex) {
			super(parent, startIndex);
		}

		@Override
		public Map.Entry<K, V> next() {
			return nextEntry();
		}

		@Override
		public Entry<K, V> previous() {
			return previousEntry();
		}

		@Override
		public void set(final Entry<K, V> e) {
			if (this.lastItemIndex == -1) {
				throw new IllegalStateException(
						"must call next() or previous() before a call to set()");
			}
			this.parent.set(lastItemIndex, e);
		}

		@Override
		public void add(final Entry<K, V> e) {
			if (this.lastItemIndex == -1) {
				throw new IllegalStateException(
						"must call next() or previous() before a call to add()");
			}
			this.parent.add(lastItemIndex, e);
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * Gets the keySet view of the map. Changes made to the view affect this
	 * map. To simply iterate through the keys, use {@link #mapIterator()}.
	 * 
	 * @return the keySet view
	 */
	@Override
	public Set<K> keySet() {
		if (keySet == null) {
			keySet = new ListKeySet<K>(this);
		}
		return keySet;
	}

	protected static class ListKeySet<K> extends KeySet<K> {
		/** The parent map */
		// Super has it, but it is private, and need type of this for added iterator(int)
		protected final ListHashedMap<K, ?> parent;

		protected ListKeySet(ListHashedMap<K, ?> parent) {
			super(parent);
			this.parent = parent;
		};

		public ListIterator<K> iterator(final int index) {
			return this.parent.createKeySetIterator(index);
		}

		@Override
		public ListIterator<K> iterator() {
			return this.parent.createKeySetIterator();
		}
	}

	/**
	 * Creates a key set iterator. Subclasses can override this to return
	 * iterators with different properties.
	 * 
	 * @param index
	 *            the index where iterating is started
	 * @return the keySet iterator
	 */
	protected ListIterator<K> createKeySetIterator(final int index) {
		if (size() == 0) {
			return EmptyListIterator.<K> emptyListIterator();
		}
		return new KeySetIterator<K, V>(this);
	}

	/**
	 * Creates a key set iterator. Subclasses can override this to return
	 * iterators with different properties, however this calls
	 * {@code createKeySetIterator(0)}, thus that should be overridden.
	 * 
	 * @return the keySet iterator
	 */
	@Override
	protected ListIterator<K> createKeySetIterator() {
		return createKeySetIterator(0);
	}

	/**
	 * KeySet iterator.
	 */
	protected static class KeySetIterator<K, V> extends HashMapIterator<K, V> implements ListIterator<K> {

		protected KeySetIterator(final ListHashedMap<K, V> parent) {
			super((ListHashedMap<K, V>) parent);
		}
	}

	// -----------------------------------------------------------------------
	/**
	 * Creates a values iterator. Subclasses can override this to return
	 * iterators with different properties.
	 * 
	 * @return the values iterator
	 */
	@Override
	protected Iterator<V> createValuesIterator() {
		if (size() == 0) {
			return EmptyIterator.<V> emptyIterator();
		}
		return new ValuesIterator<V>(this);
	}

	/**
	 * Values iterator.
	 */
	protected static class ValuesIterator<V> extends
			ListHashIterator<Object, V> implements ListIterator<V>, ResettableIterator<V> {

		@SuppressWarnings("unchecked")
		protected ValuesIterator(final ListHashedMap<?, V> parent) {
			super((ListHashedMap<Object, V>) parent);
		}

		@Override
		public V next() {
			return nextEntry().getValue();
		}

		@Override
		public V previous() {
			return previousEntry().getValue();
		}

		/**
		 * This iterator does not support modification of its backing
		 * collection, and so will always throw an
		 * {@link UnsupportedOperationException} when this method is invoked.
		 * 
		 * @param o
		 *            the mapping to add
		 * @throws UnsupportedOperationException
		 *             always thrown.
		 * @see java.util.ListIterator#set
		 */
		@Override
		public void add(final V o) {
			throw new UnsupportedOperationException(
					"add(Object) method is not supported");
		}

		/**
		 * Sets the mapping under the cursor.
		 * <p>
		 * This method sets the mapping that was returned by the last call to
		 * {@link #next()} of {@link #previous()}.
		 * <p>
		 * <b>Note:</b> {@link ListIterator} implementations that support
		 * <code>add()</code> and <code>remove()</code> only allow
		 * <code>set()</code> to be called once per call to <code>next()</code>
		 * or <code>previous</code> (see the {@link ListIterator} javadoc for
		 * more details). Since this implementation does not support
		 * <code>add()</code> or <code>remove()</code>, <code>set()</code> may
		 * be called as often as desired.
		 * 
		 * @param o
		 *            the mapping to set
		 * @throws IllegalStateException
		 *             if {@link #next()} or {@link #previous()} has not been
		 *             called before {@link #set(Object)}
		 * @see java.util.ListIterator#set
		 */
		@Override
		public void set(final V o) {
			if (this.lastItemIndex == -1) {
				throw new IllegalStateException(
						"must call next() or previous() before a call to set()");
			}
			this.parent.setValue(lastItemIndex, o);
		}

	}

	// -----------------------------------------------------------------------
	/**
	 * Base Iterator
	 * Although it is a fully working class, it is marked as abstract to force
	 * the user to extend it and maybe add necessary methods.
	 */
	protected static abstract class ListHashIterator<K, V> {

		/** The parent map */
		protected final ListHashedMap<K, V> parent;
		/** The start iterator index */
		protected final int startIndex;
		/** The current iterator index */
		protected int index;
		/** The last returned mapping */
		protected HashEntry<K, V> last;
		/** The modification count expected */
		protected int expectedModCount;
		/**
		 * Holds the index of the last item returned by a call to
		 * {@code next()} or {@code previous()}. This is set to
		 * {@code -1} if neither method has yet been invoked.
		 * {@code lastItemIndex} is used to to implement the {@link #set}
		 * method.
		 */
		protected int lastItemIndex = -1;

		/**
		 * Construct a {@code ListHashIterator} that will iterate over a range of entries
		 * in the specified {@code ListHashedMap} starting from index 0.
		 * 
		 * @param parent
		 *            the parent {@code ListHashedMap} to iterate over.
		 * @throws NullPointerException
		 *             if <code>parent</code> is <code>null</code>.
		 */
		protected ListHashIterator(final ListHashedMap<K, V> parent) {
			this(parent, 0);
		}
		/**
		 * Construct a {@code ListHashIterator} that will iterate over a range of entries
		 * in the specified {@code ListHashedMap} starting from index startIndex.
		 * 
		 * @param startIndex  the start index of iteration
		 * @param parent
		 *            the parent {@code ListHashedMap} to iterate over.
		 * @throws NullPointerException
		 *             if <code>parent</code> is <code>null</code>.
		 */
		protected ListHashIterator(final ListHashedMap<K, V> parent, final int startIndex) {
			super();
			this.parent = parent;
			this.startIndex = startIndex;
			this.index = this.startIndex;
			this.expectedModCount = this.parent.modCount;
		}

		/**
		 * Returns true if there are next mappings to return from the parent.
		 * 
		 * @return true if there is a next mapping to return
		 */
		public boolean hasNext() {
			return this.index < parent.size;
		}

		/**
		 * Gets the next mapping from the parent.
		 * 
		 * @return the next mapping
		 * @throws NoSuchElementException
		 *             if there is no next mapping
		 */
		protected HashEntry<K, V> nextEntry() {
			if (this.parent.modCount != this.expectedModCount) {
				throw new ConcurrentModificationException();
			}
			if (!hasNext() )
				throw new NoSuchElementException(
						AbstractHashedMap_Dawn.NO_NEXT_ENTRY);
			this.lastItemIndex = this.index;
			this.last = (HashEntry<K, V>) this.parent.getEntry(this.index++);
			return this.last;
		}

		/**
		 * Returns true if there are previous mappings to return from the parent.
		 * 
		 * @return true if there is a previous mapping to return
		 */
		public boolean hasPrevious() {
			return this.index > 0;
		}

		/**
		 * Gets the previous mapping from the parent.
		 * 
		 * @return the previous mapping
		 * @throws NoSuchElementException
		 *             if there is no previous mapping
		 */
		public HashEntry<K, V> previousEntry() {
			if (this.parent.modCount != this.expectedModCount) {
				throw new ConcurrentModificationException();
			}
			if (!hasPrevious() )
				throw new NoSuchElementException(
						AbstractHashedMap_Dawn.NO_PREVIOUS_ENTRY);
			this.lastItemIndex = --this.index;
			this.last = (HashEntry<K, V>) this.parent.getEntry(this.index);
			return this.last;
		}

		/**
		 * Gets the current mapping from the parent.
		 * 
		 * @return the current mapping
		 */
		protected HashEntry<K, V> currentEntry() {
			return this.last;
		}

		public void remove() {
			if (this.last == null) {
				throw new IllegalStateException(
						AbstractHashedMap_Dawn.REMOVE_INVALID);
			}
			if (this.parent.modCount != this.expectedModCount) {
				throw new ConcurrentModificationException();
			}
			this.parent.remove(--this.index);
			this.last = null;
			this.expectedModCount = this.parent.modCount;
		}

		@Override
		public String toString() {
			if (this.last != null) {
				return "Iterator[" + this.last.getKey() + "=" + this.last.getValue()
						+ "]";
			}
			return "Iterator[]";
		}

		// Properties
		// -----------------------------------------------------------------------
		/**
		 * Gets the ListHashedMap that this iterator is iterating over.
		 * 
		 * @return the ListHashedMap this iterator iterates over.
		 */
		public ListHashedMap<K, V> getListHashMap() {
			return this.parent;
		}

		/**
		 * Gets the next index to be retrieved.
		 * 
		 * @return the index of the item to be retrieved next
		 */
		public int nextIndex() {
			return this.index;
		}

		/**
		 * Gets the index of the item to be retrieved.
		 * It is -1 if {@code getCurrentIndex} is 0, in this case calling
		 * {@code previousEntry} will cause {@code NoSuchElementException}.
		 * 
		 * @return the index of the item to be retrieved next
		 */
		public int previousIndex() {
			return this.index - 1;
		}

		/**
		 * Resets the iterator back to the start index.
		 */
		public void reset() {
			this.index = 0;
			this.lastItemIndex = -1;
		}

	}

	// -----------------------------------------------------------------------
	/* These methods are used when serializing ListHashSets.
	   Originally <no modifier>, but ListHashSet is in different package, thus
	   only public access is solution, and why not making these public.
	*/
	/**
	 * Gets the current capacity.
	 * @return the current capacity
	 */
	public int getCapacity() {
		return data.length;
	}

	/**
	 * Gets the current load factor.
	 * @return the current load factor
	 */
	public float getLoadFactor() {
		return loadFactor;
	}

	// -----------------------------------------------------------------------
	/**
	 * Write the map out using a custom routine.
	 */
	private void writeObject(final ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	/**
	 * Read the map in using a custom routine.
	 */
	private void readObject(final ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
	}

	// -----------------------------------------------------------------------
	/**
	 * Returns an array containing all of the mappings in this map in proper
	 * sequence (from first to last mapping).
	 * 
	 * <p>
	 * The returned array will be "safe" in that no references to it are
	 * maintained by this map. (In other words, this method must allocate a new
	 * array). The caller is thus free to modify the returned array.
	 * 
	 * <p>
	 * This method acts as bridge between array-based and collection-based APIs.
	 * <p>
	 * Applying the List interface to this class, this method should return
	 * {@code ListIterator<Entry<K, V>>}, however it would result in naming
	 * confusion. Thus this method returns {@code ListIterator<K>}, while the
	 * {@code entryListIterator} returns {@code ListIterator<Entry<K, V>>}.
	 * 
	 * @return an array containing all of the mappings in this map in proper
	 *         sequence
	 */
	public Object[] toEntryArray() {
		return list.toArray();
	}

	/**
	 * Returns an array containing all of the keys of mappings in this map in proper
	 * sequence (from first to last mapping).
	 * 
	 * <p>
	 * The returned array will be "safe" in that no references to it are
	 * maintained by this map. (In other words, this method must allocate a new
	 * array). The caller is thus free to modify the returned array.
	 * 
	 * <p>
	 * This method acts as bridge between array-based and collection-based APIs.
	 * <p>
	 * Applying the List interface to this class, this method should return
	 * {@code Entry<K, V>[]}, however it would result in naming
	 * confusion. Thus this method returns {@code <K>[]}, while the
	 * {@code toEntryArray} returns {@code Entry<K, V>[]}.
	 * 
	 * @return an array containing all of the keys of mappings in this map in proper
	 *         sequence
	 */
	public Object[] toArray() {
		Object[] r = new Object[size];
		for( int i = size - 1; i >= 0; i-- )
			r[i] = list.get(i).getKey();
		return r;
	}

	/**
	 * Returns an array containing all of the mappings in this map in proper
	 * sequence (from first to last mapping); the runtime type of the returned
	 * array is that of the specified array. If the map fits in the specified
	 * array, it is returned therein. Otherwise, a new array is allocated with
	 * the runtime type of the specified array and the size of this map.
	 * 
	 * <p>
	 * If the map fits in the specified array with room to spare (i.e., the
	 * array has more mappings than the map), the mapping in the array
	 * immediately following the end of the collection is set to {@code null}.
	 * (This is useful in determining the length of the map <i>only</i> if the
	 * caller knows that the map does not contain any null mappings. In this class it does not.)
	 * 
	 * @param a
	 *            the array into which the mappings of the map are to be
	 *            stored, if it is big enough; otherwise, a new array of the
	 *            same runtime type is allocated for this purpose.
	 * @return an array containing the mappings of the map
	 * @throws ArrayStoreException
	 *             if the runtime type of the specified array is not a supertype
	 *             of the runtime type of every mapping in this map
	 * @throws NullPointerException
	 *             if the specified array is null
	 */
	public <T> T[] toEntryArray(T[] a) {
		return list.toArray(a);
	}

	/**
	 * Returns an array containing all of the keys mappings in this map in proper
	 * sequence (from first to last mapping); the runtime type of the returned
	 * array is that of the specified array. If the map fits in the specified
	 * array, it is returned therein. Otherwise, a new array is allocated with
	 * the runtime type of the specified array and the size of this map.
	 * 
	 * <p>
	 * If the map fits in the specified array with room to spare (i.e., the
	 * array has more mappings than the map), the mapping in the array
	 * immediately following the end of the collection is set to {@code null}.
	 * (This is useful in determining the length of the map <i>only</i> if the
	 * caller knows that the map does not contain any null mappings. In this class it does not.)
	 * <p>
	 * Applying the List interface to this class, this method should return
	 * {@code ? super Entry<K, V>[]}, however it would result in naming
	 * confusion. Thus this method returns {@code <? super K>[]}, while the
	 * {@code toKeyArray} returns {@code ? super Entry<K, V>[]}.
	 * 
	 * @param a
	 *            the array into which the mappings of the map are to be
	 *            stored, if it is big enough; otherwise, a new array of the
	 *            same runtime type is allocated for this purpose.
	 * @return an array containing the keys of mappings of the map
	 * @throws ArrayStoreException
	 *             if the runtime type of the specified array is not a supertype
	 *             of the runtime type of every mapping in this map
	 * @throws NullPointerException
	 *             if the specified array is null
	 */
	@SuppressWarnings("unchecked")
	public <T> T[] toArray(T[] a) {
		T[] r = a.length >= size ? a : (T[]) java.lang.reflect.Array
				.newInstance(a.getClass().getComponentType(), size);
		for (int i = 0; i < size; i++) {
			r[i] = (T) list.get(i).getKey();
		}
		if( size < r.length )
			r[size] = null;
		return r;
	}

	public List<Entry<K, V>> toList() {
		return new ArrayList<Entry<K, V>>(list);
	}

	public Collection<Entry<K, V>> toCollection() {
		return new ArrayList<Entry<K, V>>(list);
	}

	// -----------------------------------------------------------------------
	/* In this section this class implements the List interface over the
	 * Collection interface. More exactly, it implements what possible.
	 * Unfortunately the naming in List and Map is quite lame. Probably the goal
	 * was creating the shortest methods name ever, but nobody thought of a class
	 * which would implement both interfaces, like this class. Thus when
	 * implementing (more exactly adopting) the methods, sometimes a method is
	 * renamed, or type of argument is changed, in order to have a standardized
	 * method naming.
	 * - something(...) will do something expecting key like arguments optionally returning key or value
	 * - somethingEntry(...) will do something expecting mapping like arguments optionally returning mapping
	 * - somethingValue(...) will do something expecting value like arguments optionally returning key or value
	 * The exceptions of this naming are the add(...), addAll(...), put(...),
	 * putAll(...) and set(...) methods where either the mapping<K, V> or the
	 * K and V must be specified.
	 * The following 3 methods of List conflict with Map, thus can not have them:
	@Override
	public Entry<K, V> get(final int index) { //List would return Entry, name implies K
		return getEntry(index).getKey();
	}

	@Override
	public Entry<K, V> remove(Object entry) { //List would need Entry, name implies key
		return removeFromIndex(key, -1);
	}

	@Override
	public Entry<K, V> remove(int index) { //List would return Entry, name implies K or V, here V
		return removeFromIndex(list.get(index).getKey(), index);
	}
	 */

	// -----------------------------------------------------------------------
	public K get(final int index) {
		return getEntry(index).getKey();
	}

	public Entry<K, V> getEntry(final int index) {
		return list.get(index);
	}

	public V getValue(final int index) {
		return getEntry(index).getValue();
	}

	// -----------------------------------------------------------------------
	public int indexOf(final Object key) {
		if( !containsKey(key) )
			return -1;
		if (key == null) {
			for (int i = 0; i < list.size(); i++)
				if (list.get(i) == null)
					return i;
		} else {
			for (int i = 0; i < list.size(); i++)
				if (key.equals(list.get(i).getKey()))
					return i;
		}
		return -1;
	}

	/**
	 * Returns the index of the first occurrence of the specified mapping in
	 * this list, or -1 if this list does not contain the mapping. More
	 * formally, returns the lowest index {@code i} of an e entry in this map for which
	 * {@code key.equals(e.getKey()) && value.equals(e.getValue())} , where any
	 * getKey() or getValue() can be null if the map permits it.
	 * 
	 * @param key
	 *            the key of entry to search for
	 * @param value
	 *            the value of entry to search for
	 * @return the index of the first occurrence of the specified mapping in
	 *         this list, or -1 if this list does not contain the mapping
	 */
	public int indexOf(final Object key, final Object value) {
		final int index = indexOf(key);
		if( index < 0 || (list.get(index).getValue() == null && value != null) || !isEqualValue(list.get(index).getValue(), value) )
			return -1;
		return index;
	}

	/**
	 * Returns the index of the first occurrence of the specified mapping in
	 * this list, or -1 if this list does not contain the mapping. More
	 * formally, returns the lowest index {@code i} of an e entry in this map for which
	 * {@code entry.getKey().equals(e.getKey()) && entry.getValue().equals(e.getValue())} , where any
	 * getKey() or getValue() can be null if the map permits it.
	 * 
	 * @param entry
	 *            mapping to search for
	 * @return the index of the first occurrence of the specified mapping in
	 *         this list, or -1 if this list does not contain the mapping
	 */
	public int indexOfEntry(final Object entry) {
		@SuppressWarnings("unchecked")
		Entry<K, V> entryThis = (Entry<K, V>)entry;
		if( entryThis == null )
			return -1;
		return indexOf(entryThis.getKey(), entryThis.getValue());
	}

	public int indexOfValue(final Object value) {
		if (value == null) {
			for (int i = 0; i < list.size(); i++)
				if (list.get(i) == null)
					return i;
		} else {
			for (int i = 0; i < list.size(); i++)
				if (value.equals(list.get(i).getValue()))
					return i;
		}
		return -1;
	}

	public int lastIndexOf(final Object key) {
		if( !containsKey(key) )
			return -1;
		if (key == null) {
			for (int i = list.size() - 1; i >= 0 ; i--)
				if (list.get(i) == null)
					return i;
		} else {
			for (int i = list.size() - 1; i >= 0 ; i--)
				if (key.equals(list.get(i).getKey()))
					return i;
		}
		return -1;
	}

	/**
	 * Returns the index of the last occurrence of the specified mapping in
	 * this list, or -1 if this list does not contain the mapping. More
	 * formally, returns the highest index {@code i} of an e entry in this map for which
	 * {@code key.equals(e.getKey()) && value.equals(e.getValue())} , where any
	 * getKey() or getValue() can be null if the map permits it.
	 * 
	 * @param key
	 *            the key of entry to search for
	 * @param value
	 *            the value of entry to search for
	 * @return the index of the last occurrence of the specified mapping in
	 *         this list, or -1 if this list does not contain the mapping
	 */
	public int lastIndexOf(final Object key, final Object value) {
		final int index = lastIndexOf(key);
		if( index < 0 || (list.get(index).getValue() == null && value != null) || !isEqualValue(list.get(index).getValue(), value) )
			return -1;
		return index;
	}

	/**
	 * Returns the index of the last occurrence of the specified mapping in
	 * this list, or -1 if this list does not contain the mapping. More
	 * formally, returns the highest index {@code i} of an e entry in this map for which
	 * {@code entry.getKey().equals(e.getKey()) && entry.getValue().equals(e.getValue())} , where any
	 * getKey() or getValue() can be null if the map permits it.
	 * 
	 * @param entry
	 *            mapping to search for
	 * @return the index of the last occurrence of the specified mapping in
	 *         this list, or -1 if this list does not contain the mapping
	 */
	public int lastIndexOfEntry(final Object entry) {
		@SuppressWarnings("unchecked")
		Entry<K, V> entryThis = (Entry<K, V>)entry;
		if( entryThis == null )
			return -1;
		return lastIndexOf(entryThis.getKey(), entryThis.getValue());
	}


	public int lastIndexOfValue(final Object value) {
		if (value == null) {
			for (int i = list.size() - 1; i >= 0 ; i--)
				if (list.get(i) == null)
					return i;
		} else {
			for (int i = list.size() - 1; i >= 0 ; i--)
				if (value.equals(list.get(i).getValue()))
					return i;
		}
		return -1;
	}

	// -----------------------------------------------------------------------
	/**
	 * Removes the mapping at the specified position from this list, also
	 * removes it from the map. Shifts any subsequent mappings to the left
	 * (subtracts one from their indices). Returns the value of mapping that was
	 * removed from the list.
	 * <p>
	 * This method takes O(1) time with optimal hashing or O(n(this)) with worst
	 * hashing, plus O(1). It is expensive when hashing is worst.
	 * <p>
	 * See {@link List#remove(Object)}
	 * 
	 * @param index
	 *            the index of the mapping to be removed
	 * @return value (can be null) of mapping that was removed
	 * @throws IndexOutOfBoundsException
	 *             if the index is out of range (
	 *             {@code index < 0 || index >= size()})
	 */
	public V remove(final int index) {
		return remove(list.get(index).getKey(), index);
	}

	/**
	 * Removes the mapping of specified entry from this map by finding its index
	 * with {@code indexOf(key, value)} and removing by {@code remove(index)},
	 * which also removes it from the internal list. More formally, the mapping
	 * of the lowest index {@code i} is removed such that there is an e entry in
	 * this map for which
	 * {@code key.equals(e.getKey()) && value.equals(e.getValue())} , where any
	 * getKey() or getValue() can be null if the map permits it.
	 * <p>
	 * This method takes O(1) if the key does not exist, else O(1) time with
	 * optimal hashing or O(n(this)) with worst hashing, plus O(n(this)) + O(1).
	 * It is expensive, and more expensive when hashing is worst.
	 * 
	 * @param key
	 *            the key of entry to remove
	 * @param value
	 *            the value of entry to remove
	 * @return the value (can be null) mapped to the removed mapping, or null if
	 *         mapping not in map
	 */
	public V remove(final Object key, final Object value) {
		final int index = indexOf(key, value);
		if( index < 0 )
			return null;
		return remove(index);
	}

	/**
	 * Removes the mapping of specified entry from this map by
	 * {@code remove(entry.getKey(), entry.getValue())}, which also removes it
	 * from the internal list. More formally, the entry {@code Object} is
	 * removed if entry is an {@code Entry} and there is an e entry in this map
	 * for which
	 * {@code entry.getKey().equals(e.getKey()) && entry.getValue().equals(e.getValue())}
	 * , where any getKey() or getValue() can be null if the map permits it.
	 * <p>
	 * This method takes O(1) time with optimal hashing or O(n(this)) with worst
	 * hashing, plus O(n(this)) + O(1). It is expensive, and more expensive when
	 * hashing is worst.
	 * 
	 * @param entry
	 *            the entry to remove
	 * @return the value (can be null) mapped to the removed mapping, or null if
	 *         mapping not in map
	 */
	public V removeEntry(final Object entry) {
		@SuppressWarnings("unchecked")
		Entry<K, V> entryThis = (Entry<K, V>)entry;
		if( entryThis == null )
			return null;
		return remove(entryThis.getKey(), entryThis.getValue());
	}

	/**
	 * Removes the first occurrence of the specified value from this map by
	 * finding its index with {@code indexOfValue(value)} and
	 * {@code remove(index)}, which also removes it from the internal list. More
	 * formally, it removes mapping of the lowest index {@code i} such that
	 * {@code (value==null ? list.get(i)==null : value.equals(list.get(i).getValue()))}
	 * , or does nothing if there is no such index.
	 * <p>
	 * This method takes O(1) time with optimal hashing or O(n(this)) with worst
	 * hashing, plus O(n(this)) + O(1). It is expensive, and more expensive when
	 * hashing is worst.
	 * 
	 * @param value
	 *            the value to remove
	 * @return the value (can be null) mapped to the removed mapping, or null if
	 *         value not in map
	 */
	public V removeValue(final Object value) {
		final int index = indexOfValue(value);
		if( index < 0 )
			return null;
		return remove(index);
	}

	// -----------------------------------------------------------------------
	/**
	 * Puts a key-value mapping into this map, and also sets it at the index
	 * of the internal list. If the key of mapping previously existed, then that
	 * mapping is removed from the internal list after the new mapping is set
	 * in it. It means the mapping is correctly set as it is expected from a
	 * list, but if the previous mapping had less index, then the index of new
	 * mapping is decreased by one caused by the removal.
	 * 
	 * @param index
	 *            the list index where to set
	 * @param key
	 *            the key to add
	 * @param value
	 *            the value to add
	 * @return the value previously mapped to this key, null if none
	 */
	public V set(final int index, final K key, final V value) {
		int oldIndex = -1;
		V oldValue = null;
		final Object convertedKey = convertKey(key);
		final int hashCode = hash(convertedKey);
		final int hIndex = hashIndex(hashCode, data.length);
		HashEntry<K, V> entry = data[hIndex];
		HashEntry<K, V> previous = null;
		while (entry != null) {
			if (entry.hashCode == hashCode
					&& isEqualKey(convertedKey, entry.key)) {
				oldValue = entry.getValue();
				if( index < size && isEqualKey(convertedKey, ((HashEntry<K, V>)list.get(index)).key) ) {
					updateEntry(entry, value);
					return oldValue;
				}
				oldIndex = indexOf(entry.getKey());
				removeMapping(entry, hIndex, previous);
				break;
			}
			entry = entry.next;
			previous = entry;
		}

		addMappingAtIndex(hIndex, hashCode, key, value, index, false);
		if( entry != null )
			list.remove(oldIndex);
		return oldValue;
	}

	/**
	 * Puts a key-value mapping into this map, and also sets it at the index
	 * of the internal list. If the key of mapping previously existed, then that
	 * mapping is removed from the internal list after the new mapping is set
	 * in it. It means the mapping is correctly set as it is expected from a
	 * list, but if the previous mapping had less index, then the index of new
	 * mapping is decreased by one caused by the removal.
	 * <p>It is named "set" in List interface, here it would mean set key
	 * which is nonsense, thus it is rewritten as setEntry, and returns V.
	 * 
	 * @param index
	 *            the list index where to set
	 * @param key
	 *            the key to add
	 * @param value
	 *            the value to add
	 * @return the value previously mapped to this key, null if none
	 */
	public V set(final int index, final Entry<K, V> element) {
		return set(index, element.getKey(), element.getValue());
	}

	public V setValue(final int index, final V value) {
		return list.get(index).setValue(value);
	}

	// -----------------------------------------------------------------------
	/**
	 * Checks whether the map contains the specified entry. The o {@code Object} is
	 * contained if o is an {@code Entry} and there is an e entry in this map for which
	 * {@code o.getKey().equals(e.getKey()) && o.getValue().equals(e.getValue())}
	 * , where any getKey() or getValue() can be null if the map permits it.
	 * <p>It is named "contains" in List interface, here it would mean contains key
	 * which already exists, thus it is rewritten as containsEntry.
	 * 
	 * @param o
	 *            the entry to search for
	 * @return true if the map contains the entry
	 */
	public boolean containsEntry(final Object o) {
		Entry<?, ?> entry = (Entry<?, ?>) o;
		if (entry == null)
			return false;
		final Entry<K, V> entryThis = getEntry(entry.getKey());
		if (entryThis == null)
			return false;
		return entry.getValue().equals(entryThis.getValue());
	}

	/**
	 * Returns {@code true} if this map contains all of the keys of the
	 * specified collection. If the specified collection is also a keyset of a map, this
	 * method returns {@code true} if that map is a <i>submap</i> of this map keywise.
	 * <p>
	 * This implementation iterates over the specified collection, checking each
	 * mapping returned by the iterator in turn to see if it's contained in this
	 * map. If all mappings are so contained {@code true} is returned,
	 * otherwise {@code false}.
	 * 
	 * @param c
	 *            collection containing keys to be checked for containment in this map
	 * @return {@code true} if this map contains all of the keys of the
	 *         specified collection
	 * @throws NullPointerException
	 *             if the specified collection contains one or more null
	 *             keys and this set does not permit null key (<a
	 *             href="Collection.html#optional-restrictions">optional</a>),
	 *             or if the specified collection is null
	 * @see #contains(Object)
	 */
	public boolean containsAll(final Collection<?> c) {
		for (final Object e : c)
			if (!containsKey(e))
				return false;
		return true;
	}

	/**
	 * Returns an iterator over the mappings in this map, which is the entrySet
	 * view of the map. The mappings are returned in order of internal list.
	 * Changes made to the view affect this map. To simply iterate through the
	 * keys of entries, use {@link #mapIterator()}.
	 * 
	 * @return an iterator over the mappings in this map, which is the entrySet
	 *         view
	 */
	public Iterator<Entry<K, V>> iterator() {
		return entrySet().iterator();
	}

	/**
	 * Adds the specified mapping to this map, and also inserts it as last
	 * mapping if it is not already present More formally, adds the specified
	 * mapping to this map if the map contains no mapping {@code e} such that
	 * {@code (k==null ? e.getKey()==null : k.equals(e.getKey()))} . If this map
	 * already contains the mapping, the call leaves the map unchanged and
	 * returns {@code false}. In combination with the restriction on
	 * constructors, this ensures that sets never contain duplicate mappings.
	 * 
	 * @param k
	 *            key of mapping to be added to this map
	 * @param v
	 *            value of mapping to be added to this map
	 * @return {@code true} if this map did not already contain the specified
	 *         mapping
	 * @throws NullPointerException
	 *             if the specified mapping is null, or the key of mapping is
	 *             null and this map does not permit null key
	 */
	public boolean add(final K k, final V v) {
		if( containsKey(k) )
			return false;
		put(k, v);
		return true;
	}

	/**
	 * Adds the specified mapping to this map, and also inserts it as last
	 * mapping if it is not already present. More formally, adds the specified
	 * mapping {@code e} to this map if the map contains no mapping {@code e2}
	 * such that
	 * {@code (e.getKey()==null ? e2.getKey()==null : e.getKey().equals(e2.getKey()))}
	 * . If this map already contains the mapping, the call leaves the map
	 * unchanged and returns {@code false}. In combination with the restriction
	 * on constructors, this ensures that sets never contain duplicate mappings.
	 * 
	 * @param e
	 *            mapping to be added to this map
	 * @return {@code true} if this map did not already contain the specified
	 *         mapping
	 * @throws NullPointerException
	 *             if the specified mapping is null, or the key of mapping is
	 *             null and this map does not permit null key
	 */
	public boolean add(final Entry<K, V> e) {
		return add(e.getKey(), e.getValue());
	}

	/**
	 * Adds the specified mapping to this map, and also inserts it as last
	 * mapping if it is not already present More formally, adds the specified
	 * mapping to this map if the map contains no mapping {@code e} such that
	 * {@code (k==null ? e.getKey()==null : k.equals(e.getKey()))} . If this map
	 * already contains the mapping, the call leaves the map unchanged and
	 * returns {@code false}. In combination with the restriction on
	 * constructors, this ensures that sets never contain duplicate mappings.
	 * <p>
	 * This method returns void in List interface, here it returns boolean.
	 * 
	 * @param k
	 *            key of mapping to be added to this map
	 * @param v
	 *            value of mapping to be added to this map
	 * @return {@code true} if this map did not already contain the specified
	 *         mapping
	 * @throws NullPointerException
	 *             if the specified mapping is null, or the key of mapping is
	 *             null and this map does not permit null key
	 */
	public boolean add(final int index, final K k, final V v) {
		if( containsKey(k) )
			return false;
		put(index, k, v);
		return true;
	}

	/**
	 * Adds the specified mapping to this map, and also inserts it at the
	 * specified index if it is not already present. More formally, adds the
	 * specified mapping {@code e} to this map if the map contains no mapping
	 * {@code e2} such that
	 * {@code (e.getKey()==null ? e2.getKey()==null : e.getKey().equals(e2.getKey()))}
	 * . If this map already contains the mapping, the call leaves the map
	 * unchanged and returns {@code false}. In combination with the restriction
	 * on constructors, this ensures that sets never contain duplicate mappings.
	 * <p>
	 * This method returns void in List interface, here it returns boolean.
	 * 
	 * @param index
	 *            the list index where to add
	 * @param element
	 *            mapping to be added to this map
	 * @return {@code true} if this map did not already contain the specified
	 *         mapping
	 * @throws NullPointerException
	 *             if the specified mapping is null, or the key of mapping is
	 *             null and this map does not permit null key
	 */
	public boolean add(final int index, final Entry<K, V> e) {
		return add(index, e.getKey(), e.getValue());
	}

	/**
	 * Adds all of the mappings in the specified collection to this map if
	 * they're not already present (optional operation). If the specified
	 * collection is also a set of a map, the {@code addAll} operation
	 * effectively modifies this map so that its value is the <i>union</i> of
	 * the two maps. The behavior of this operation is undefined if the
	 * specified collection is modified while the operation is in progress.
	 * <p>
	 * This implementation iterates over the specified collection, and adds each
	 * object returned by the iterator to this map, in turn.
	 * 
	 * @param c
	 *            collection containing mappings to be added to this map
	 * @return {@code true} if this map changed as a result of the call
	 * 
	 * @throws NullPointerException
	 *             if the specified collection contains one or more null
	 *             mappings and this map does not permit null mappings, or if
	 *             the specified collection is null
	 * @see #add(java.util.Map.Entry)
	 */
	public boolean addAll(final Collection<? extends Entry<K, V>> c) {
		boolean modified = false;
		for (Entry<K, V> e : c)
			if (add(e))
				modified = true;
		return modified;
	}

	/**
	 * Adds all of the key-value mappings in the specified collection to this
	 * map, and also inserts those one after the other at the specified index of
	 * the internal list if they're not already present (optional operation). If
	 * the specified collection is also a set of a map, the {@code addAll}
	 * operation effectively modifies this map so that its value is the
	 * <i>union</i> of the two maps. The behavior of this operation is undefined
	 * if the specified collection is modified while the operation is in
	 * progress.
	 * <p>
	 * This implementation iterates over the specified collection, and adds each
	 * object returned by the iterator to this map, in turn.
	 * 
	 * @param c
	 *            collection containing mappings to be added to this map
	 * @return {@code true} if this map changed as a result of the call
	 * 
	 * @throws NullPointerException
	 *             if the specified collection contains one or more null
	 *             mappings and this map does not permit null mappings, or if
	 *             the specified collection is null
	 * @see #add(int, java.util.Map.Entry)
	 */
	public boolean addAll(final int index, final Collection<? extends Entry<K, V>> c) {
		boolean modified = false;
		int i = index;
		for (Entry<K, V> e : c)
			if (add(i, e)) {
				i++;
				modified = true;
			}
		return modified;
	}

	/**
	 * Removes from this map all of its mappings whose keys are contained in the
	 * specified collection (optional operation). If the specified collection is
	 * also a keyset of a map, this operation effectively modifies this map so
	 * that its key is the <i>asymmetric map difference</i> of the two maps.
	 * <p>
	 * This implementation iterates over this map, checking each mapping
	 * returned by the iterator in turn to see if its key is contained in the
	 * specified collection. If it's not so contained, it's removed from this
	 * map with the iterator's {@code remove} method.
	 * 
	 * @param c
	 *            collection containing keys of mappings to be removed from this
	 *            map
	 * @return {@code true} if this map changed as a result of the call
	 * @throws NullPointerException
	 *             if this map contains a null key of an mapping and the
	 *             specified collection does not permit null mappings (<a
	 *             href="Collection.html#optional-restrictions">optional</a>),
	 *             or if the specified collection is null
	 * @see #remove(Object)
	 * @see #containsEntry(Object)
	 */
	public boolean removeAll(final Collection<?> c) {
		boolean modified = false;
		for( int i = size - 1; i >= 0; i-- ) {
			if (c.contains(list.get(i).getKey())) {
				remove(i);
				modified = true;
			}
		}
		return modified;
	}

	/**
	 * Retains only the mappings in this map whose keys are contained in the
	 * specified collection (optional operation). In other words, removes from
	 * this map all of its mappings whose keys are not contained in the
	 * specified collection. If the specified collection is also a keyset of a
	 * map, this operation effectively modifies this map so that its key is the
	 * <i>intersection</i> of the two maps.
	 * 
	 * <p>
	 * This implementation iterates over this map, checking each mapping
	 * returned by the iterator in turn to see if its key is contained in the
	 * specified collection. If it's not so contained, it's removed from this
	 * map with the iterator's {@code remove} method.
	 * 
	 * @param c
	 *            collection containing keys of mappings to be retained in this
	 *            map
	 * @return {@code true} if this map changed as a result of the call
	 * @throws NullPointerException
	 *             if this map contains a null key of an mapping and the
	 *             specified collection does not permit null mappings (<a
	 *             href="Collection.html#optional-restrictions">optional</a>),
	 *             or if the specified collection is null
	 * @see #remove(Object)
	 * @see #containsEntry(Object)
	 */
	public boolean retainAll(final Collection<?> c) {
		boolean modified = false;
		for( int i = size - 1; i >= 0; i-- ) {
			if (!c.contains(list.get(i).getKey())) {
				remove(i);
				modified = true;
			}
		}
		return modified;
	}

	/**
	 * Returns a list iterator over the mappings in this map (in proper
	 * sequence).
	 * 
	 * <p>
	 * The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
	 * 
	 * @return the list iterator
	 * @see #listIterator(int)
	 */
	@SuppressWarnings("unchecked")
	public ListIterator<Entry<K, V>> entryListIterator() {
		return ((ListEntrySet<K, V>) entrySet()).iterator(); //TODO TEST
	}

	/**
	 * Returns a list iterator over the mappings in this map (in proper
	 * sequence), starting at the specified index in the internal list. The
	 * specified index indicates the first mapping that would be returned by an
	 * initial call to {@link ListIterator#next next}. An initial call to
	 * {@link ListIterator#previous previous} would return the mapping with the
	 * specified index minus one.
	 * 
	 * <p>
	 * The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
	 * 
	 * @return the list iterator
	 * @throws IndexOutOfBoundsException
	 *             {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	public ListIterator<Entry<K, V>> entryListIterator(final int index) {
		return ((ListEntrySet<K, V>) entrySet()).iterator(index); //TODO TEST
	}

	/**
	 * Returns a list iterator over the keys of mappings in this map (in proper
	 * sequence).
	 * <p>
	 * Applying the List interface to this class, this method should return
	 * {@code ListIterator<Entry<K, V>>}, however it would result in naming
	 * confusion. Thus this method returns {@code ListIterator<K>}, while the
	 * {@code entryListIterator} returns {@code ListIterator<Entry<K, V>>}.
	 * 
	 * <p>
	 * The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
	 * @return the list iterator
	 * @see #keyListIterator(int)
	 */
	public ListIterator<K> listIterator() {
		return ((ListKeySet<K>) keySet()).iterator();
	}

	/**
	 * Returns a list iterator over the keys of mappings in this map (in proper
	 * sequence), starting at the specified index in the internal list. The
	 * specified index indicates the key of first mapping that would be returned
	 * by an initial call to {@link ListIterator#next next}. An initial call to
	 * {@link ListIterator#previous previous} would return the key of mapping
	 * with the specified index minus one.
	 * <p>
	 * Applying the List interface to this class, this method should return
	 * {@code ListIterator<Entry<K, V>>}, however it would result in naming
	 * confusion. Thus this method returns {@code ListIterator<K>}, while the
	 * {@code entryListIterator} returns {@code ListIterator<Entry<K, V>>}.
	 * 
	 * <p>
	 * The returned list iterator is <a href="#fail-fast"><i>fail-fast</i></a>.
	 * 
	 * @param index
	 *            the list index where to set
	 * @return the list iterator
	 * @see #keyListIterator(int)
	 */
	public ListIterator<K> listIterator(final int index) {
		return ((ListKeySet<K>) keySet()).iterator(index);
	}

	/**
	 * <p>Marked as deprecated, meaning it is not supported yet, but might be in the future.
	 * 
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	@Deprecated
	public List<Entry<K, V>> entrySubList(final int fromIndex, final int toIndex) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("entrySubList is not supported yet");
	}

	/**
	 * <p>
	 * Applying the List interface to this class, this method should return
	 * {@code List<Entry<K, V>>}, however it would result in naming
	 * confusion. Thus this method returns {@code List<K>}, while the
	 * {@code entrySubList} returns {@code List<Entry<K, V>>}.
	 * <p>Marked as deprecated, meaning it is not supported yet, but might be in the future.
	 * 
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 */
	@Deprecated
	public List<K> subList(final int fromIndex, final int toIndex) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("subList is not supported yet");
	}
}
