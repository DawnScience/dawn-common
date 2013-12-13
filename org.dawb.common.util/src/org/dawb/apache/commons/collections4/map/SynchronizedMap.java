/*
 * Copyright (c) 2013 European Molecular Biology Laboratory
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawb.apache.commons.collections4.map;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.collection.SynchronizedCollection;
import org.dawb.apache.commons.collections4.set.SynchronizedSet;

/**
 * Decorates another {@link Map} to synchronize its behaviour for a
 * multi-threaded environment.
 * <p>
 * The map must be manually synchronized when iterating over any of its
 * collection views:
 * 
 * <pre>
 * Set set = map.keySet();
 * synchronized (map) {
 * 	Iterator it = set.iterator();
 * 	// do stuff with iterator
 * }
 * </pre>
 * <p>
 * This class is Serializable from Commons Collections ?.
 * 
 * @param <K>
 *            the type of key of the elements in the Map
 * @param <V>
 *            the type of value of the elements in the Map
 * @since ?
 * 
 * @author Gábor Náray
 */
public class SynchronizedMap<K, V> implements Map<K, V>, Serializable {
	/** Serialization version */
	private static final long serialVersionUID = -8007071591331754337L;

	/** The map to decorate */
	private final Map<K, V> map; // Backing Map
	/** The object to lock on, needed for certain views */
	protected final Object lock; // Object on which to synchronize

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
	public static <K, V> SynchronizedMap<K, V> synchronizedMap(
			final Map<K, V> map) {
		return new SynchronizedMap<K, V>(map);
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
	protected SynchronizedMap(final Map<K, V> map) {
		if (map == null)
			throw new IllegalArgumentException("Map must not be null");
		this.map = map;
		lock = this;
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
	protected SynchronizedMap(final Map<K, V> map, final Object lock) {
		if (map == null)
			throw new IllegalArgumentException("Map must not be null");
		this.map = map;
		this.lock = lock;
	}

	/**
	 * Gets the map being decorated.
	 * 
	 * @return the decorated map
	 */
	protected Map<K, V> decorated() {
		return map;
	}

	// -----------------------------------------------------------------------

	public int size() {
		synchronized (lock) {
			return map.size();
		}
	}

	public boolean isEmpty() {
		synchronized (lock) {
			return map.isEmpty();
		}
	}

	public boolean containsKey(final Object key) {
		synchronized (lock) {
			return map.containsKey(key);
		}
	}

	public boolean containsValue(final Object value) {
		synchronized (lock) {
			return map.containsValue(value);
		}
	}

	public V get(final Object key) {
		synchronized (lock) {
			return map.get(key);
		}
	}

	public V put(final K key, final V value) {
		synchronized (lock) {
			return map.put(key, value);
		}
	}

	public V remove(final Object key) {
		synchronized (lock) {
			return map.remove(key);
		}
	}

	public void putAll(final Map<? extends K, ? extends V> m) {
		synchronized (lock) {
			map.putAll(m);
		}
	}

	public void clear() {
		synchronized (lock) {
			map.clear();
		}
	}

	private transient Set<K> keySet = null;
	private transient Set<Map.Entry<K, V>> entrySet = null;
	private transient Collection<V> values = null;

	/**
	 * The map must be manually synchronized when iterating over any of its
	 * collection views:
	 * 
	 * <pre>
	 * Set set = map.keySet();
	 * synchronized (map) {
	 * 	Iterator it = set.iterator();
	 * 	// do stuff with iterator
	 * }
	 * </pre>
	 * 
	 * @return a set, which when iterated over, the user must manually synchronize on the map
	 */
	public Set<K> keySet() {
		synchronized (lock) {
			if (keySet == null)
				keySet = new SynchronizedSet<K>(map.keySet(), lock) {
					private static final long serialVersionUID = -5651988177891069107L;
				};
			return keySet;
		}
	}

	/**
	 * The map must be manually synchronized when iterating over any of its
	 * collection views:
	 * 
	 * <pre>
	 * Set set = map.entrySet();
	 * synchronized (map) {
	 * 	Iterator it = set.iterator();
	 * 	// do stuff with iterator
	 * }
	 * </pre>
	 * 
	 * @return a set, which when iterated over, the user must manually synchronize on the map
	 */
	public Set<Map.Entry<K, V>> entrySet() {
		synchronized (lock) {
			if (entrySet == null)
				entrySet = new SynchronizedSet<Map.Entry<K, V>>(map.entrySet(),
						lock) {
					private static final long serialVersionUID = -8775610895344000513L;
				};
			return entrySet;
		}
	}

	/**
	 * The map must be manually synchronized when iterating over any of its
	 * collection views:
	 * 
	 * <pre>
	 * Collection collection = map.values();
	 * synchronized (map) {
	 * 	Iterator it = collection.iterator();
	 * 	// do stuff with iterator
	 * }
	 * </pre>
	 * 
	 * @return a collection, which when iterated over, the user must manually synchronize on the map
	 */
	public Collection<V> values() {
		synchronized (lock) {
			if (values == null)
				values = new SynchronizedCollection<V>(map.values(), lock) {
					private static final long serialVersionUID = -1270473338268793685L;
				};
			return values;
		}
	}

	public boolean equals(final Object o) {
		if (this == o)
			return true;
		synchronized (lock) {
			return map.equals(o);
		}
	}

	public int hashCode() {
		synchronized (lock) {
			return map.hashCode();
		}
	}

	public String toString() {
		synchronized (lock) {
			return map.toString();
		}
	}

	private void writeObject(final ObjectOutputStream s) throws IOException {
		synchronized (lock) {
			s.defaultWriteObject();
		}
	}
}
