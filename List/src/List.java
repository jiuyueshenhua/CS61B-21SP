public interface List<Item> {

    public void addlast(Item x);

    public Item getFirst();

    public Item getLast();

    public Item removeLast();

    public Item get(int i);

    public void insert(Item x, int position);

    public int size();
    default public void print(List l){
        for(int i=0;i<size();i++){
            System.out.print(get(i)+" ");
        }
        System.out.println();
    }
}
