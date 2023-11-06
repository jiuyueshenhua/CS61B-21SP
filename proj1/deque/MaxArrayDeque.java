package deque;


import java.util.Comparator;

public class MaxArrayDeque<ItemType> extends ArrayDeque<ItemType> {
    Comparator<ItemType> cmpor;

    public MaxArrayDeque(Comparator<ItemType> c) {
        super();
        cmpor = c;
    }

    public ItemType max() {
        if (isEmpty() || cmpor == null) {
            return null;
        }
        ItemType cur = get(0);
        for (int i = 0; i < size(); i++) {
            if (cmpor.compare(cur, get(i)) < 0) {
                cur=get(i);
            }
        }
        return cur;
    }

    public ItemType max(Comparator<ItemType> c){
        if (isEmpty() ) {
            return null;
        }
        ItemType cur = get(0);
        for (int i = 0; i < size(); i++) {
            if (c.compare(cur, get(i)) < 0) {
                cur = get(i);
            }
        }
        return cur;
    }


}
