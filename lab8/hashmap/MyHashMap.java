package hashmap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A hash table-backed Map implementation. Provides amortized constant time
 * access to elements via get(), remove(), and put() in the best case.
 * <p>
 * Assumes null keys will never be inserted, and does not resize down upon remove().
 *
 * @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            Node m = (Node) obj;
            return this.key.equals(m.key);
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    private int num = 0;
    private double loadFactor;
    // You should probably define some more!

    /**
     * Constructors
     */
    public MyHashMap() {
        this(16, 0.75);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, 0.75);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad     maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        buckets = createTable(initialSize);
        loadFactor = maxLoad;
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     * <p>
     * The only requirements of a hash table bucket are that we can:
     * 1. Insert items (`add` method)
     * 2. Remove items (`remove` method)
     * 3. Iterate through items (`iterator` method)
     * <p>
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     * <p>
     * Override this method to use different data structures as
     * the underlying bucket type
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new HashSet<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] B = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            B[i] = createBucket();
        }
        return B;
    }


    // Your code won't compile until you do so!

    @Override
    public void clear() {
        buckets = createTable(16);
        num = 0;
    }

    @Override
    public int size() {
        return num;
    }

    private int getIndex(K key, int len) {
        return Math.floorMod(key.hashCode(), len);
    }

    private void addHelper(Collection<Node>[] B, K key, V value) {
        int id = getIndex(key, B.length);
        Iterator<Node> iter = B[id].iterator();
        while (iter.hasNext()) {
            Node cur = iter.next();
            if (cur.key.equals(key)) {
                cur.value = value;
                return;
            }
        }
        B[id].add(new Node(key, value));
    }

    @Override
    public void put(K key, V value) {
        if (!containsKey(key)) {
            num++;
        }
        addHelper(buckets, key, value);

        if (nowLoad() > loadFactor) {
            resize(buckets.length * 2);
        }
    }

    private double nowLoad() {
        return (double) num / buckets.length;
    }

    private void resize(int newSize) {
        Collection<Node>[] B2 = createTable(newSize);
        for (int i = 0; i < buckets.length; i++) {
            Iterator<Node> iter = buckets[i].iterator();
            while (iter.hasNext()) {
                Node cur = iter.next();
                addHelper(B2, cur.key, cur.value);
            }
        }
        buckets = B2;

    }


    @Override
    public boolean containsKey(K key) {
        int id = getIndex(key, buckets.length);
        Iterator<Node> iter = buckets[id].iterator();
        while (iter.hasNext()) {
            Node cur = iter.next();
            if (cur.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        int id = getIndex(key, buckets.length);
        Iterator<Node> iter = buckets[id].iterator();
        while (iter.hasNext()) {
            Node cur = iter.next();
            if (cur.key.equals(key)) {
                return cur.value;
            }
        }
        return null;
    }

    @Override
    public Set<K> keySet() {
        Set<K> S = new HashSet<>();
        for (Collection<Node> cur : buckets) {
            Iterator<Node> iter = cur.iterator();
            while (iter.hasNext()) {
                S.add(iter.next().key);
            }
        }
        return S;
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    @Override
    public V remove(K key, V value) {
        V val = get(key);
        if (val != null && val.equals(value)) {
            return remove(key);
        }
        return null;
    }

    @Override
    public V remove(K key) {
        Collection<Node> b = buckets[getIndex(key, buckets.length)];
        for (Node n : b) {
            if (n.key.equals(key)) {
                V t = n.value;
                b.remove(n);
                num--;
                return t;
            }
        }
        return null;
    }

    private void print() {
        for (Collection<Node> cur : buckets) {
            Iterator<Node> iter = cur.iterator();
            while (iter.hasNext()) {
                Node x = iter.next();
                System.out.print(x.key + " :  " + x.value + "  ");
            }
            if (!cur.isEmpty()) {
                System.out.println();
            }

        }
    }

    private static void main(String[] args) {

    }

}
