package deque;


import java.util.Iterator;

public class ArrayDeque<ItemType> implements Deque<ItemType> {
    private int size, nextFirst, nextLast;
    private ItemType[] items;

    private static final int CHECK_BIG_SIZE = 8;

    public ArrayDeque() {
        size = 0;
        nextFirst = 0;
        nextLast = 1;

        items = (ItemType[]) new Object[8];
    }

    @Override
    public int size() {
        return size;
    }


    private int moveLeft(int i) {
        return (i - 1 + items.length) % items.length;
    }

    private int moveRight(int i) {

        return (i + 1 + items.length) % items.length;
    }

    @Override
    public void printDeque() {

        int i = moveRight(nextFirst);
        while (i != nextLast) {
            System.out.print(items[i]);
            i = moveRight(i);
            if (i == nextLast) {
                System.out.println();
            } else {
                System.out.print(" ");
            }
        }
    }


    private void resize(int length) {
        ItemType[] n = (ItemType[]) new Object[length];

        int curid = moveRight(nextFirst);
        int i = 1;
        while (curid != nextLast) {
            n[i] = items[curid];
            i++;
            curid = moveRight(curid);
        }
        nextFirst = 0;
        nextLast = i;
        items = n;
    }

    private boolean isTooMore() {
        return items.length > CHECK_BIG_SIZE && size() * 4 < items.length;
    }

    private boolean isFull() {
        return nextFirst == nextLast;
    }

    @Override
    public void addFirst(ItemType item) {
        if (isFull()) {
            resize(items.length * 2);
        }
        items[nextFirst] = item;
        nextFirst = moveLeft(nextFirst);
        size++;
    }

    @Override
    public void addLast(ItemType item) {
        if (isFull()) {
            resize(items.length * 2);
        }
        items[nextLast] = item;
        nextLast = moveRight(nextLast);
        size++;
    }

    @Override
    public ItemType removeLast() {
        if (isEmpty()) {
            return null;
        }
        nextLast = moveLeft(nextLast);
        ItemType item = items[nextLast];
        items[nextLast] = null;
        size--;
        if (isTooMore()) {
            resize(items.length / 2);
        }
        return item;
    }

    @Override
    public ItemType removeFirst() {
        if (isEmpty()) {
            return null;
        }
        nextFirst = moveRight(nextFirst);
        ItemType item = items[nextFirst];
        items[nextFirst] = null;
        size--;
        if (isTooMore()) {
            resize(items.length / 2);
        }
        return item;
    }

    @Override
    public ItemType get(int index) {
        if (index < 0 || index >= size()) {
            return null;
        }
        return items[(moveRight(nextFirst) + index) % items.length];
    }

    private class DequeIterator implements Iterator<ItemType> {
        int cur = moveRight(nextFirst);

        @Override
        public boolean hasNext() {
            return cur != nextLast;
        }

        @Override
        public ItemType next() {
            ItemType x = items[cur];
            cur = moveRight(cur);
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
                    if (!((Deque<?>) o).get(i).equals(this.get(i))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public double usage() {
        return (double) size() / items.length;
    }

    public void ppr() {
        System.out.println(size() + "  " + items.length);
    }

}
