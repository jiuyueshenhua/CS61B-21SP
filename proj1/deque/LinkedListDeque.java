package deque;

import java.util.Deque;

public class LinkedListDeque<ItemType> {
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

    public void addFirst(ItemType item) {
        Node new0 = new Node(item);
        new0.prev = sentinel;
        new0.next = sentinel.next;

        sentinel.next.prev = new0;

        sentinel.next = new0;
        size++;
    }

    public void addLast(ItemType item) {
        Node tail = sentinel.prev;
        Node new0 = new Node(item);
        tail.next = new0;
        new0.prev = tail;
        new0.next = sentinel;
        sentinel.prev = new0;
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

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


}
