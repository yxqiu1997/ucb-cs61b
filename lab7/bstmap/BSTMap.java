package bstmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K,V> {

    private BSTNode root;

    private int size;

    public BSTMap() {
        this.size = 0;
    }

    @Override
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    public boolean containsKey(K key) {
        return getNodeByKey(root, key) != null;
    }

    private BSTNode getNodeByKey(BSTNode node, K key) {
        if (node == null || key == null) {
            return null;
        }
        int cmp = key.compareTo(node.getKey());
        if (cmp < 0) {
            return getNodeByKey(node.getLeft(), key);
        } else if (cmp > 0) {
            return getNodeByKey(node.getRight(), key);
        }
        return node;
    }

    @Override
    public V get(K key) {
        BSTNode node = getNodeByKey(root, key);
        return node == null ? null : node.getValue();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        root = put(root, key, value);
        ++size;
    }

    private BSTNode put(BSTNode node, K key, V value) {
        if (node == null) {
            return new BSTNode(key, value);
        }
        int cmp = key.compareTo(node.getKey());
        if (cmp < 0) {
            node.setLeft(put(node.getLeft(), key, value));
        } else if (cmp > 0) {
            node.setRight(put(node.getRight(), key, value));
        } else {
            node.setValue(value);
        }
        return node;
    }

    @Override
    public Set<K> keySet() {
        Set<K> keySet = new HashSet<>();
        addKeys(root, keySet);
        return keySet;
    }

    private void addKeys(BSTNode node, Set<K> keySet) {
        if (node == null) {
            return;
        }
        keySet.add(node.getKey());
        addKeys(node.getLeft(), keySet);
        addKeys(node.getRight(), keySet);
    }

    @Override
    public V remove(K key) {
        V value = null;
        if (containsKey(key)) {
            value = get(key);
            root = remove(root, key);
            --size;
        }
        return value;
    }

    private BSTNode remove(BSTNode node, K key) {
        if (node == null || key == null) {
            return null;
        }
        int cmp = key.compareTo(node.getKey());
        if (cmp < 0) {
            node.setLeft(remove(node.getLeft(), key));
        } else if (cmp > 0) {
            node.setRight(remove(node.getRight(), key));
        } else {
            if (node.getLeft() == null) {
                return node.getRight();
            }
            if (node.getRight() == null) {
                return node.getLeft();
            }
            BSTNode origin = node;
            node = getMinNode(node.getRight());
            node.setLeft(origin.getLeft());
            node.setRight(remove(origin.getRight(), node.getKey()));
        }
        return node;
    }

    private BSTNode getMinNode(BSTNode node) {
        if (node.getLeft() == null) {
            return node;
        }
        return getMinNode(node.getLeft());
    }

    @Override
    public V remove(K key, V value) {
        V target = null;
        if (containsKey(key)) {
            target = get(key);
            if (value.equals(target)) {
                root = remove(root, key);
                --size;
            }
        }
        return target;
    }

    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    public void printInOrder() {
        printInOrder(root);
    }

    private void printInOrder(BSTNode node) {
        if (node == null) {
            return;
        }
        printInOrder(node.getLeft());
        System.out.println(node.getKey().toString() + " -> " + node.getValue().toString());
        printInOrder(node.getRight());
    }

    private class BSTNode {
        private final K key;

        private V value;

        private BSTNode left;

        private BSTNode right;

        public BSTNode(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        public BSTNode getLeft() {
            return left;
        }

        public void setLeft(BSTNode left) {
            this.left = left;
        }

        public BSTNode getRight() {
            return right;
        }

        public void setRight(BSTNode right) {
            this.right = right;
        }
    }
}
