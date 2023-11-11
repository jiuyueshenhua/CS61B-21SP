package timingtest;
public class VengefulSLList<ItemType> extends SLList<ItemType> {
    SLList<ItemType> deletedItems;
    public  VengefulSLList() {
        deletedItems = new SLList<ItemType>();
    }
    @Override
    public ItemType removeLast() {
        ItemType x =super.removeLast();
        deletedItems.addLast(x);
        return x;
    }

    public void printLostItems() {
        deletedItems.print();
    }

}
