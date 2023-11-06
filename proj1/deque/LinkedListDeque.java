package deque;


import java.util.Iterator;

public class LinkedListDeque<ItemType> implements Deque<ItemType> {
    private class Node {
        protected ItemType item;
        protected Node next;
        protected Node prev;

        Node(ItemType i) {
            this.item = i;
        }

        Node() {
            next = null;
            prev = null;
        }
    }

    private int size;
    private Node sentinel;

    public LinkedListDeque() {
        size = 0;
        sentinel = new Node();
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
    }

    @Override
    public void addFirst(ItemType item) {
        Node new0 = new Node(item);
        new0.prev = sentinel;
        new0.next = sentinel.next;

        sentinel.next.prev = new0;

        sentinel.next = new0;
        size++;
    }

    @Override
    public void addLast(ItemType item) {
        Node tail = sentinel.prev;
        Node new0 = new Node(item);
        tail.next = new0;
        new0.prev = tail;
        new0.next = sentinel;
        sentinel.prev = new0;
        size++;
    }


    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        Node cur = sentinel.next;
        for (int i = 0; i < size; i++) {
            System.out.print(cur.item);
            if (i != size) {
                System.out.print(" ");
            } else {
                System.out.println();
            }
            cur = cur.next;
        }
    }

    @Override
    public ItemType removeFirst() {
        if (size == 0) {
            return null;
        }
        Node sec = sentinel.next.next;
        Node fir = sentinel.next;
        sentinel.next = sec;
        sec.prev = sentinel;
        size--;
        return fir.item;
    }

    @Override
    public ItemType removeLast() {
        if (size == 0) {
            return null;
        }
        Node lastSec = sentinel.prev.prev;
        Node last = sentinel.prev;
        lastSec.next = sentinel;
        sentinel.prev = lastSec;
        size--;
        return last.item;
    }

    @Override
    public ItemType get(int index) {
        if (index < 0 || index >= size) {
            return null;
        }
        Node cur = sentinel.next;
        int id = 0;
        while (id != index) {
            id++;
            cur = cur.next;
        }
        return cur.item;
    }

    public ItemType getRecursive(int index) {
        return getHelper(index, sentinel.next);
    }

    private ItemType getHelper(int index, Node n) {
        if (index < 0 || index >= size) {
            return null;
        }
        if (index == 0) {
            return n.item;
        }
        return getHelper(index - 1, n.next);
    }

    private class DequeIterator implements Iterator<ItemType> {
        Node cur = sentinel.next;

        @Override
        public boolean hasNext() {
            return cur != sentinel;
        }

        @Override
        public ItemType next() {
            ItemType x = cur.item;
            cur = cur.next;
            return x;
        }
    }

    public Iterator<ItemType> iterator() {
        return new DequeIterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Deque) {
            if (((Deque<?>) o).size() == this.size()) {
                for (int i = 0; i < this.size(); i++) {
                    if (((Deque<?>) o).get(i) != this.get(i)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }


}
