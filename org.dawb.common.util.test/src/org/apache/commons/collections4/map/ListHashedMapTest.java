package org.apache.commons.collections4.map;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.dawb.apache.commons.collections4.map.ListHashedMap;
import org.dawb.apache.commons.collections4.map.SynchronizedListHashedMap;
import org.junit.Test;

public class ListHashedMapTest {
	protected static class A implements Serializable {
		private static final long serialVersionUID = 3842558611690899730L;
		int a;
		public A(final int v) {a=v;}
		@Override
		public boolean equals(final Object o) { if( this == o ) return true; A oThis = (A)o; if( oThis == null ) return false; return a == oThis.a; }
		@Override
		public int hashCode() { return a; }
		@Override
		public String toString() { return String.valueOf(a); }
	};
	protected static class B extends A implements Serializable {
		private static final long serialVersionUID = -3064670121652127487L;
		double b;
		public B(final double v) {super((int) v); b=v;}
		@Override
		public boolean equals(final Object o) { if( this == o ) return true; B oThis = (B)o; if( oThis == null ) return false; return super.equals(oThis) && b == oThis.b; }
		@Override
		public int hashCode() { return a ^ Double.valueOf(b).hashCode(); }
		@Override
		public String toString() { return "[" + super.toString() + ", " + String.valueOf(b) + "]"; }
	};
	protected static class P implements Serializable {
		private static final long serialVersionUID = -7964294081495107540L;
		int p;
		public P(final int v) {p=v;}
		@Override
		public boolean equals(final Object o) { if( this == o ) return true; P oThis = (P)o; if( oThis == null ) return false; return p == oThis.p; }
		@Override
		public int hashCode() { return p; }
		@Override
		public String toString() { return String.valueOf(p); }
	};
	protected static class Q extends P implements Serializable {
		private static final long serialVersionUID = -9212007566233018554L;
		String q;
		public Q(final String v) {super(v.length()); q=v;}
		@Override
		public boolean equals(final Object o) { if( this == o ) return true; Q oThis = (Q)o; if( oThis == null ) return false; return super.equals(oThis) && q.equals(oThis.q); }
		@Override
		public int hashCode() { return p ^ q.hashCode(); }
		@Override
		public String toString() { return "[" + super.toString() + ", " + String.valueOf(q) + "]"; }
	};

	@Test
	public void basicSingleThread() {
		SynchronizedListHashedMap<A,P> lhm = SynchronizedListHashedMap.synchronizedListHashedMap(new ListHashedMap<A,P>());
		assertNotNull(lhm);
		assertNull(lhm.put(new A(1), new P(1)));
		assertNull(lhm.put(new A(11), new P(111)));
		assertNull(lhm.put(new A(5), new P(7)));
		assertNull(lhm.put(new B(25.74), new Q("carrot")));
		assertNull(lhm.put(new B(53.42), new Q("beetroot")));
		assertEquals(lhm.size(), 5);
		assertEquals(lhm.getValue(2), lhm.get(new A(5)));
		assertEquals(new P(7), lhm.put(new A(5), new P(23))); //Reputs A(5) to the end of list
		assertEquals(lhm.size(), 5);
		assertEquals(lhm.getEntry(4).getValue(), lhm.get(new A(5)));
		assertEquals(new P(23), lhm.put(2, new A(5), new P(35))); //Reputs A(5) to index 2
		assertEquals(lhm.size(), 5);
		assertEquals(lhm.getValue(2), lhm.get(new A(5)));
		assertEquals(new P(35), lhm.remove(new A(5)));
		assertEquals(lhm.size(), 4);
		File serFile = null;
		try {
			serFile = File.createTempFile("lhmtest", ".ser");
			serFile.deleteOnExit();
		} catch (IOException e) {
			fail("Could not create the file for serializing the object because " + e.getMessage());
		}
		FileOutputStream fileOut;
		try {
			fileOut = new FileOutputStream(serFile);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(lhm);
			out.close();
			fileOut.close();
		} catch (FileNotFoundException e) {
			fail("Could not serialize the object because " + e.getMessage());
		} catch (IOException e) {
			fail("Could not serialize the object because " + e.getMessage());
		}
		SynchronizedListHashedMap<A, P> lhmDes = null;
		try {
			FileInputStream fileIn = new FileInputStream(serFile);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			lhmDes = (SynchronizedListHashedMap<A, P>) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException e) {
			fail("Could not deserialize the object because " + e.getMessage());
		} catch (ClassNotFoundException e) {
			fail("Could not deserialize the object because " + e.getMessage());
		}
		assertEquals(lhmDes.size(), 4);
		ListHashedMap<A,P> lhmClone = lhm.clone();
		assertNotNull(lhmClone);
		assertSame(lhm.get(1), lhmClone.get(1)); //Due to shallow copy
		assertNotSame(lhm.getEntry(1), lhmClone.getEntry(1)); //At least entries are deep copied
		assertTrue(lhm.containsAll(lhmClone.keySet()));
		assertFalse(lhm.addAll(2, lhmClone.entrySet())); //Adding existing mappings does nothing
		assertEquals(lhm.size(), 4);
		assertEquals(new P(111), lhm.setValue(1, new P(109))); //Changing value of mapping at 1
		assertEquals(lhm.getValue(1), new P(109));
		assertEquals(lhm.indexOf(lhm.get(1)), 1); //Index check
		assertEquals(lhm.indexOfValue(new P(109)), 1); //Index check
		assertEquals(lhm.lastIndexOf(lhm.get(1)), 1); //Index check
		assertFalse(lhm.retainAll(lhmClone.keySet())); //Retaining the same set does nothing
		Object[] array1 = lhm.toArray();
		assertEquals(array1.length, lhm.size());
		A[] array2 = new A[lhm.size()];
		Object[] array3 = lhm.toArray(array2);
		assertSame(array3, array2); //Because array2 was enough large to hold the result
		assertEquals(array3.length, lhm.size());
		array2 = new A[lhm.size() - 1];
		array3 = lhm.toArray(array2);
		assertNotSame(array3, array2); //Because array2 was not enough large to hold the result
		assertEquals(array3.length, lhm.size());
		array2 = new A[lhm.size() + 1];
		array2[lhm.size()] = new A(77);
		array3 = lhm.toArray(array2);
		assertSame(array3, array2); //Because array2 was enough large to hold the result
		assertEquals(array3.length, lhm.size() + 1);
		assertNull(array3[lhm.size()]); //Got the null after size amount of mappings
		assertFalse(lhm.add(new A(11), new P(107))); //Can not add, it exists
		assertEquals(lhm.size(), 4);
		assertTrue(lhm.add(new B(22), new Q("apple")));
		assertEquals(lhm.size(), 5);
		assertTrue(lhm.add(3, new B(33), new Q("peach")));
		assertEquals(lhm.size(), 6);
		assertTrue(lhm.removeAll(lhm.keySet()));
		assertEquals(lhm.size(), 0);
//		ListHashedMap<B,Q> lhmBQ = new ListHashedMap<B,Q>(lhm);
//		ListHashedMap<A,P> lhm5 = new ListHashedMap<A,P>(lhm4);
	}

	public static void main(String[] args) {
		ListHashedMapTest lhmt = new ListHashedMapTest();
		lhmt.basicSingleThread();
//		for( A e : lhm.keySet() ) { System.out.println("Looping keySet: " + e.toString()); }
//		for( P e : lhm.values() ) { System.out.println("Looping values: " + e.toString()); }
//		for( Map.Entry<A, P> e : lhm.entrySet() ) { System.out.println("Looping entrySet: " + e.toString()); }
//		MapIterator<A, P> it = lhm.mapIterator();
//		while( it.hasNext() ) { System.out.println("Looping mapIterator: " + it.next().toString()); }
	}
}
