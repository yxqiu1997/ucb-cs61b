package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
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
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;

    private static final int DEFAULT_INITIAL_SIZE = 16;

    private static final double DEFAULT_LOAD_FACTOR = 0.75;

    private int size = 0;

    private double maxLoad;

    /** Constructors */
    public MyHashMap() {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, DEFAULT_LOAD_FACTOR);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.maxLoad = maxLoad;
        this.buckets = createTable(initialSize);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = new Collection[tableSize];
        for (int i = 0; i < tableSize; ++i) {
            table[i] = createBucket();
        }
        return table;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    @Override
    public void clear() {
        this.buckets = createTable(DEFAULT_INITIAL_SIZE);
        this.size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return getNode(key) != null;
    }

    private Node getNode(K key) {
        return getNode(key, getIndex(key));
    }

    private Node getNode(K key, int index) {
        for (Node node : buckets[index]) {
            if (key.equals(node.key)) {
                return node;
            }
        }
        return null;
    }

    private int getIndex(K key) {
        return getIndex(key, buckets);
    }

    private int getIndex(K key, Collection<Node>[] table) {
        int keyHashCode = key.hashCode();
        return Math.floorMod(keyHashCode, table.length);
    }

    @Override
    public V get(K key) {
        Node node = getNode(key);
        return node == null ? null : node.value;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        int index = getIndex(key);
        Node node = getNode(key, index);
        if (node != null) {
            node.value = value;
            return;
        }
        node = createNode(key, value);
        buckets[index].add(node);
        ++size;
        if (isMaxLoaded()) {
            resize(buckets.length * 2);
        }
    }

    private boolean isMaxLoaded() {
        return size * 1.0 / buckets.length > maxLoad;
    }

    private void resize(int capacity) {
        Collection<Node>[] table = createTable(capacity);
        Iterator<Node> iterator = new MyHashMapNodeIterator();
        while (iterator.hasNext()) {
            Node node = iterator.next();
            int index = getIndex(node.key, table);
            table[index].add(node);
        }
        buckets = table;
    }

    @Override
    public Set<K> keySet() {
        Set<K> set = new HashSet<>();
        for (K key : this) {
            set.add(key);
        }
        return set;
    }

    @Override
    public V remove(K key) {
        int index = getIndex(key);
        Node node = getNode(key, index);
        if (node == null) {
            return null;
        }
        --size;
        buckets[index].remove(node);
        return node.value;
    }

    @Override
    public V remove(K key, V value) {
        int index = getIndex(key);
        Node node = getNode(key, index);
        if (node == null || !value.equals(node.value)) {
            return null;
        }
        --size;
        buckets[index].remove(node);
        return node.value;
    }

    @Override
    public Iterator<K> iterator() {
        return new MyHashMapIterator();
    }

    private class MyHashMapIterator implements Iterator<K> {

        private final Iterator<Node> nodeIterator = new MyHashMapNodeIterator();

        public boolean hasNext() {
            return nodeIterator.hasNext();
        }

        public K next() {
            return nodeIterator.next().key;
        }
    }

    private class MyHashMapNodeIterator implements Iterator<Node> {

        private final Iterator<Collection<Node>> bucketsIterator = Arrays.stream(buckets).iterator();

        private int remainingNodesNum = size;

        private Iterator<Node> nodeIterator;

        public boolean hasNext() {
            return remainingNodesNum > 0;
        }

        public Node next() {
            if (nodeIterator == null || !nodeIterator.hasNext()) {
                Collection<Node> buckets = bucketsIterator.next();
                while(buckets.size() == 0) {
                    buckets = bucketsIterator.next();
                }
                nodeIterator = buckets.iterator();
            }
            --remainingNodesNum;
            return nodeIterator.next();
        }
    }
}
