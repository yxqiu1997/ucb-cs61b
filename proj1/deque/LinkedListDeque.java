package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Iterable<T>, Deque<T> {

    private final Node<T> sentinel = new Node<>(null, null, null);

    private int size;

    public LinkedListDeque() {
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        size = 0;
    }

    @Override
    public void addFirst(T item) {
        sentinel.next = new Node<>(item, sentinel, sentinel.next);
        sentinel.next.next.prev = sentinel.next;
        ++size;
    }

    @Override
    public void addLast(T item) {
        sentinel.prev = new Node<>(item, sentinel.prev, sentinel);
        sentinel.prev.prev.next = sentinel.prev;
        ++size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Node<T> pointer = sentinel.next;
        if (pointer == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; ++i) {
            sb.append(pointer.getItem().toString()).append(" ");
            pointer = pointer.next;
        }
        System.out.println(sb);
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        T item = sentinel.next.item;
        sentinel.next = sentinel.next.next;
        sentinel.next.prev = sentinel;
        --size;
        return item;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        T item = sentinel.prev.item;
        sentinel.prev = sentinel.prev.prev;
        sentinel.prev.next = sentinel;
        --size;
        return item;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index > size - 1) {
            return null;
        }
        Node<T> pointer = sentinel.next;
        for (int i = 0; i < size; ++i) {
            if (i == index) {
                return pointer.item;
            }
            pointer = pointer.next;
        }
        return null;
    }

    public T getRecursive(int index) {
        if (index < 0 || index > size - 1) {
            return null;
        }
        return getRecursiveHelper(index, sentinel.next);
    }

    private T getRecursiveHelper(int index, Node<T> currentNode) {
        if (index == 0) {
            return currentNode.item;
        }
        return getRecursiveHelper(index - 1, currentNode.next);
    }

    public Iterator<T> iterator() {
        return new LinkedListDequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Deque)) {
            return false;
        }
        Deque deque = (Deque) o;
        if (deque.size() != size) {
            return false;
        }
        Node pointer = sentinel.next;
        for (int i = 0; i < size; ++i) {
            if (!pointer.item.equals(deque.get(i))) {
                return false;
            }
            pointer = pointer.next;
        }
        return true;
    }

    private static class Node<T> {

        private final T item;

        private Node<T> prev;

        private Node<T> next;

        Node(T item, Node<T> prev, Node<T> next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }

        public T getItem() {
            return item;
        }

        public Node<T> getPrev() {
            return prev;
        }

        public void setPrev(Node<T> prev) {
            this.prev = prev;
        }

        public Node<T> getNext() {
            return next;
        }

        public void setNext(Node<T> next) {
            this.next = next;
        }

        @Override
        public String toString() {
            return this.item == null ? "null" : item.toString();
        }
    }

    private class LinkedListDequeIterator implements Iterator<T> {

        private Node<T> pointer;

        LinkedListDequeIterator() {
            pointer = sentinel.next;
        }

        public boolean hasNext() {
            return pointer != sentinel;
        }

        public T next() {
            T item = pointer.item;
            pointer = pointer.next;
            return item;
        }
    }
}
