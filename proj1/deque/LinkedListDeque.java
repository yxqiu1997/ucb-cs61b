package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Iterable<T>, Deque<T> {

    private final Node<T> head = new Node<>(null, null, null);

    private int size;

    public LinkedListDeque() {
        head.next = head;
        head.prev = head;
        size = 0;
    }

    public LinkedListDeque(T item) {
        head.next = new Node<>(item, head, head);
        head.prev = head.next;
        size = 1;
    }

    @Override
    public void addFirst(T item) {
        head.next = new Node<>(item, head, head.next);
        head.next.next.prev = head.next;
        ++size;
    }

    @Override
    public void addLast(T item) {
        head.prev = new Node<>(item, head.prev, head);
        head.prev.prev.next = head.prev;
        ++size;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Node<T> pointer = head.next;
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
        T item = head.next.item;
        head.next = head.next.next;
        head.next.prev = head;
        --size;
        return item;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        T item = head.prev.item;
        head.prev = head.prev.prev;
        head.prev.next = head;
        --size;
        return item;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index > size - 1) {
            return null;
        }
        Node<T> pointer = head.next;
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
        return getRecursiveHelper(index, head.next);
    }

    public T getRecursiveHelper(int index, Node<T> currentNode) {
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
        if (!(o instanceof LinkedListDeque)) {
            return false;
        }
        LinkedListDeque<?> deque = (LinkedListDeque<?>) o;
        if (deque.size != size) {
            return false;
        }
        for (int i = 0; i < size; ++i) {
            if (deque.get(i) != get(i)) {
                return false;
            }
        }
        return true;
    }

    private static class Node<T> {

        private final T item;

        private Node<T> prev;

        private Node<T> next;

        public Node(T item, Node<T> prev, Node<T> next) {
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

        public LinkedListDequeIterator() {
            pointer = head.next;
        }

        public boolean hasNext() {
            return pointer == head;
        }

        public T next() {
            T item = pointer.item;
            pointer = pointer.next;
            return item;
        }
    }
}
