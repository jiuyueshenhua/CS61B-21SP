/**
 * Array based list.
 *
 * @author Josh Hug
 */

public class AList <Type> implements List<Type> {
    /**
     * Creates an empty list.
     */
    private int size;
    private Type[] item;
    public AList() {
        item =  (Type [ ]) new Object [100];
        size = 0;
    }

    /**
     * Inserts X into the back of the list.
     */
    public void addLast(Type x) {
        item[size++]=x;
    }

    @Override
    public void addlast(Type x) {

    }

    @Override
    public Type getFirst() {
        return null;
    }

    /**
     * Returns the item from the back of the list.
     */
    public Type getLast() {
        return item[size-1];
    }

    /**
     * Gets the ith item in the list (0 is the front).
     */
    public Type get(int i) {
        return item[i];
    }

    @Override
    public void insert(Type x, int position) {

    }

    /**
     * Returns the number of items in the list.
     */
    public int size() {
        return size;
    }

    /**
     * Deletes item from back of the list and
     * returns deleted item.
     */
    public Type removeLast() {
        Type x = item[size-1];
        size--;
        return x;
    }
} 