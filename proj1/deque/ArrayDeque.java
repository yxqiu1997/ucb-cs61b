package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Iterable<T>, Deque<T> {

    private T[] items = (T[]) new Object[8];

    private int size;

    private int nextFirst;

    private int nextLast;

    public ArrayDeque() {
        size = 0;
        nextFirst = 3;
        nextLast = 4;
    }

    public ArrayDeque(T item) {
        items[3] = item;
        size = 1;
        nextFirst = 2;
        nextLast = 4;
    }

    @Override
    public void addFirst(T item) {
        items[nextFirst] = item;
        ++size;
        --nextFirst;
        if (nextFirst == -1) {
            resize(size * 2);
        }
    }

    @Override
    public void addLast(T item) {
        items[nextLast] = item;
        ++size;
        ++nextLast;
        if (nextLast == items.length) {
            resize(size * 2);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < size; ++i) {
            if (items[i] != null) {
                sb.append(items[i]).append(" ");
            }
        }
        System.out.println(sb);
    }

    @Override
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }
        ++nextFirst;
        T item = items[nextFirst];
        items[nextFirst] = null;
        --size;
        shrinkSize();
        return item;
    }

    @Override
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        --nextLast;
        T item = items[nextLast];
        items[nextLast] = null;
        --size;
        shrinkSize();
        return item;
    }

    public void shrinkSize() {
        if (isEmpty()) {
            resize(8);
        } else if (items.length > size * 4 && size >= 4) {
            resize(size * 2);
        }
    }

    public void resize(int newSize) {
        T[] newItems = (T[]) new Object[newSize];
        int newNextFirst = Math.abs(size - newSize) / 2;
        System.arraycopy(items, nextFirst + 1, newItems, newNextFirst, size);
        items = newItems;
        nextFirst = newNextFirst - 1;
        nextLast = newNextFirst + size;
    }

    @Override
    public T get(int index) {
        if (index < 0 || index > size - 1) {
            return null;
        }
        return items[nextFirst + index + 1];
    }

    public Iterator<T> iterator() {
        return new ArrayDequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ArrayDeque)) {
            return false;
        }
        ArrayDeque<?> deque = (ArrayDeque<?>) o;
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

    private class ArrayDequeIterator implements Iterator<T> {

        private int index;

        public ArrayDequeIterator() {
            index = 0;
        }

        public boolean hasNext() {
            return index < size;
        }

        public T next() {
            T item = get(index);
            ++index;
            return item;
        }
    }

}
