package armyc2.c5isr.renderer.utilities;

//The Node class for doubly linked list
public class LRUEntry
{

    public String key;
    public ImageInfo value;
    public LRUEntry next;
    public LRUEntry prev;

    public LRUEntry(LRUEntry prev, LRUEntry next, String key, ImageInfo value) {
        this.prev = prev;
        this.next = next;
        this.key = key;
        this.value = value;
    }

}
