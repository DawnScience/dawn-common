/*
 * Copyright (c) 2013 European Molecular Biology Laboratory
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.apache.commons.collections4.map;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.collections4.MapIterator;
import org.apache.commons.collections4.collection.SynchronizedCollection;
import org.apache.commons.collections4.list.SynchronizedList;
import org.apache.commons.collections4.map.AbstractHashedMap_Dawn.HashEntry;

/**
 * Decorates another {@link ListHashedMap} to synchronize its behaviour for a
 * multi-threaded environment.
 * <p>
 * Iterators must be manually synchronized.
 * 
 * <pre>
 * synchronized (listHashedMap) {
 * 	Iterator it = listHashedMap.MapIterator();
 * 	// do stuff with iterator
 * }
 * </pre>
 * The map must be manually synchronized when iterating over any of its
 * collection views:
 * 
 * <pre>
 * List list = listHashedMap.toList();
 * synchronized (listHashedMap) {
 * 	Iterator it = list.iterator();
 * 	// do stuff with iterator
 * }
 * </pre>
 * <p>
 * This class is Serializable from Commons Collections ?.
 * 
 * @param <K>
 *            the type of key of the elements in the ListHashedMap
 * @param <V>
 *            the type of value of the elements in the ListHashedMap
 * @since ?
 * 
 * @author Gábor Náray
 */
public class SynchronizedListHashedMap<K, V> extends SynchronizedMap<K, V> {
	/** Serialization version */
	private static final long serialVersionUID = -1541154006533434695L;

	/**
	 * Factory method to create a synchronized map.
	 * 
	 * @param <T>
	 *            the type of the elements in the map
	 * @param map
	 *            the map to decorate, must not be null
	 * @return a new synchronized map
	 * @throws IllegalArgumentException
	 *             if map is null
	 * @since ?
	 */
	public static <K, V> SynchronizedListHashedMap<K, V> synchronizedListHashedMap(
			final ListHashedMap<K, V> map) {
		return new SynchronizedListHashedMap<K, V>(map);
	}

	// -----------------------------------------------------------------------
	/**
	 * Constructor that wraps (not copies).
	 * 
	 * @param map
	 *            the map to decorate, must not be null
	 * @throws IllegalArgumentException
	 *             if the map is null
	 */
	protected SynchronizedListHashedMap(final ListHashedMap<K, V> map) {
		super(map);
	}

	/**
	 * Constructor that wraps (not copies).
	 * 
	 * @param map
	 *            the map to decorate, must not be null
	 * @param lock
	 *            the lock object to use, must not be null
	 * @throws IllegalArgumentException
	 *             if the map is null
	 */
	protected SynchronizedListHashedMap(final ListHashedMap<K, V> map, final Object lock) {
		super(map);
	}

	/**
	 * Gets the map being decorated.
	 * 
	 * @return the decorated map
	 */
	protected ListHashedMap<K, V> decorated() {
		return (ListHashedMap<K, V>) super.decorated();
	}

	// -----------------------------------------------------------------------

	public ListHashedMap<K, V> clone() {
		synchronized (lock) {
			return decorated().clone();
		}
	}

	protected void addMappingAtIndex(final int hashIndex, final int hashCode,
			final K key, final V value, final int index, boolean add) {
		synchronized (lock) {
			decorated().addMappingAtIndex(hashIndex, hashCode, key, value, index, add);
		}
	}

	protected void removeMapping(final HashEntry<K, V> entry,
			final int hashIndex, final HashEntry<K, V> previous) {
		synchronized (lock) {
			decorated().removeMapping(entry, hashIndex, previous);
		}
	}

	protected void removeMapping(final HashEntry<K, V> entry,
			final int hashIndex, final HashEntry<K, V> previous, int index) {
		synchronized (lock) {
			decorated().removeMapping(entry, hashIndex, previous, index);
		}
	}
		
	public void move(final int oldIndex, final int newIndex) {
		synchronized (lock) {
			decorated().move(oldIndex, newIndex);
		}
	}

	public V put(final int index, final K key, final V value) {
		synchronized (lock) {
			return decorated().put(index, key, value);
		}
	}

	public void putAll(int index, final Map<? extends K, ? extends V> map) {
		synchronized (lock) {
			decorated().putAll(index, map);
		}
	}

	protected V remove(Object key, final int listIndex) {
		synchronized (lock) {
			return decorated().remove(key, listIndex);
		}
	}

	/**
	 * Iterators must be manually synchronized.
	 * 
	 * <pre>
	 * synchronized (listHashedMap) {
	 * 	Iterator it = listHashedMap.MapIterator();
	 * 	// do stuff with iterator
	 * }
	 * </pre>
	 * 
	 * @return an iterator that must be manually synchronized on the list
	 */
	public MapIterator<K, V> mapIterator() {
		return decorated().mapIterator();
	}

	public Object[] toEntryArray() {
		synchronized (lock) {
			return decorated().toEntryArray();
		}
	}

	public Object[] toArray() {
		synchronized (lock) {
			return decorated().toArray();
		}
	}

	public <T> T[] toEntryArray(T[] a) {
		synchronized (lock) {
			return decorated().toEntryArray(a);
		}
	}

	public <T> T[] toArray(T[] a) {
		synchronized (lock) {
			return decorated().toArray(a);
		}
	}

	/**
	 * The map must be manually synchronized when iterating over any of its
	 * collection views:
	 * 
	 * <pre>
	 * List list = listHashedMap.toList();
	 * synchronized (listHashedMap) {
	 * 	Iterator it = collection.iterator();
	 * 	// do stuff with iterator
	 * }
	 * </pre>
	 * 
	 * @return a collection, which when iterated over, the user must manually synchronize on the listHashedMap
	 */
	public List<Entry<K, V>> toList() {
		synchronized (lock) {
			return new SynchronizedList<Entry<K, V>>(decorated().toList(), lock) {
				private static final long serialVersionUID = 6428796759602579547L;
				;
			};
		}
	}

	/**
	 * The map must be manually synchronized when iterating over any of its
	 * collection views:
	 * 
	 * <pre>
	 * Collection collection = listHashedMap.toCollection();
	 * synchronized (listHashedMap) {
	 * 	Iterator it = collection.iterator();
	 * 	// do stuff with iterator
	 * }
	 * </pre>
	 * 
	 * @return a collection, which when iterated over, the user must manually synchronize on the listHashedMap
	 */
	public Collection<Entry<K, V>> toCollection() {
		synchronized (lock) {
			return new SynchronizedCollection<Entry<K, V>>(decorated().toCollection(), lock) {
				private static final long serialVersionUID = -3950738471945390250L;
			};
		}
	}

	public K get(final int index) {
		synchronized (lock) {
			return decorated().get(index);
		}
	}

	public Entry<K, V> getEntry(final int index) {
		synchronized (lock) {
			return decorated().getEntry(index);
		}
	}

	public V getValue(final int index) {
		synchronized (lock) {
			return decorated().getValue(index);
		}
	}

	public int indexOf(final Object key) {
		synchronized (lock) {
			return decorated().indexOf(key);
		}
	}

	public int indexOf(final Object key, final Object value) {
		synchronized (lock) {
			return decorated().indexOf(key, value);
		}
	}

	public int indexOfEntry(final Object entry) {
		synchronized (lock) {
			return decorated().indexOfEntry(entry);
		}
	}

	public int indexOfValue(final Object value) {
		synchronized (lock) {
			return decorated().indexOfValue(value);
		}
	}

	public int lastIndexOf(final Object key) {
		synchronized (lock) {
			return decorated().lastIndexOf(key);
		}
	}

	public int lastIndexOf(final Object key, final Object value) {
		synchronized (lock) {
			return decorated().lastIndexOf(key, value);
		}
	}

	public int lastIndexOfEntry(final Object entry) {
		synchronized (lock) {
			return decorated().lastIndexOfEntry(entry);
		}
	}

	public int lastIndexOfValue(final Object value) {
		synchronized (lock) {
			return decorated().lastIndexOfValue(value);
		}
	}

	public V remove(int index) {
		synchronized (lock) {
			return decorated().remove(index);
		}
	}

	public V remove(final Object key, final Object value) {
		synchronized (lock) {
			return decorated().remove(key, value);
		}
	}

	public V removeEntry(Object entry) {
		synchronized (lock) {
			return decorated().removeEntry(entry);
		}
	}

	public V removeValue(Object value) {
		synchronized (lock) {
			return decorated().removeValue(value);
		}
	}

	public V set(final int index, final K key, final V value) {
		synchronized (lock) {
			return decorated().set(index, key, value);
		}
	}

	public V set(final int index, final Entry<K, V> element) {
		synchronized (lock) {
			return decorated().set(index, element);
		}
	}

	public V setValue(final int index, final V value) {
		synchronized (lock) {
			return decorated().setValue(index, value);
		}
	}

	public boolean containsEntry(final Object o) {
		synchronized (lock) {
			return decorated().containsEntry(o);
		}
	}

	public boolean containsAll(final Collection<?> c) {
		synchronized (lock) {
			return decorated().containsAll(c);
		}
	}

	/**
	 * Iterators must be manually synchronized.
	 * 
	 * <pre>
	 * synchronized (listHashedMap) {
	 * 	Iterator it = listHashedMap.iterator();
	 * 	// do stuff with iterator
	 * }
	 * </pre>
	 * 
	 * @return an iterator that must be manually synchronized on the list
	 */
	public Iterator<Entry<K, V>> iterator() {
		return decorated().iterator();
	}

	public boolean add(final K k, final V v) {
		synchronized (lock) {
			return decorated().add(k, v);
		}
	}

	public boolean add(final Entry<K, V> e) {
		synchronized (lock) {
			return decorated().add(e);
		}
	}

	public boolean add(final int index, final K k, final V v) {
		synchronized (lock) {
			return decorated().add(index, k, v);
		}
	}

	public boolean add(final int index, final Entry<K, V> element) {
		synchronized (lock) {
			return decorated().add(index, element);
		}
	}

	public boolean addAll(final Collection<? extends Entry<K, V>> c) {
		synchronized (lock) {
			return decorated().addAll(c);
		}
	}

	public boolean addAll(final int index, final Collection<? extends Entry<K, V>> c) {
		synchronized (lock) {
			return decorated().addAll(index, c);
		}
	}

	public boolean removeAll(final Collection<?> c) {
		synchronized (lock) {
			return decorated().removeAll(c);
		}
	}

	public boolean retainAll(final Collection<?> c) {
		synchronized (lock) {
			return decorated().retainAll(c);
		}
	}

	/**
	 * Iterators must be manually synchronized.
	 * 
	 * <pre>
	 * synchronized (listHashedMap) {
	 * 	Iterator it = listHashedMap.entryListIterator();
	 * 	// do stuff with iterator
	 * }
	 * </pre>
	 * 
	 * @return an iterator that must be manually synchronized on the list
	 */
	public ListIterator<Entry<K, V>> entryListIterator() {
		return decorated().entryListIterator();
	}

	/**
	 * Iterators must be manually synchronized.
	 * 
	 * <pre>
	 * synchronized (listHashedMap) {
	 * 	Iterator it = listHashedMap.entryListIterator(0);
	 * 	// do stuff with iterator
	 * }
	 * </pre>
	 * 
	 * @return an iterator that must be manually synchronized on the list
	 */
	public ListIterator<Entry<K, V>> entryListIterator(final int index) {
		return decorated().entryListIterator(index);
	}

	/**
	 * Iterators must be manually synchronized.
	 * 
	 * <pre>
	 * synchronized (listHashedMap) {
	 * 	Iterator it = listHashedMap.listIterator();
	 * 	// do stuff with iterator
	 * }
	 * </pre>
	 * 
	 * @return an iterator that must be manually synchronized on the list
	 */
	public ListIterator<K> listIterator() {
		return decorated().listIterator();
	}

	/**
	 * Iterators must be manually synchronized.
	 * 
	 * <pre>
	 * synchronized (listHashedMap) {
	 * 	Iterator it = listHashedMap.listIterator(0);
	 * 	// do stuff with iterator
	 * }
	 * </pre>
	 * 
	 * @return an iterator that must be manually synchronized on the list
	 */
	public ListIterator<K> listIterator(final int index) {
		return decorated().listIterator(index);
	}

}
