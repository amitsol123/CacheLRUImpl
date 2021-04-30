public class Node {
    private byte[] value;
    private String key;
    private Node next, prev;

    public Node(String key, byte[] value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Node getNext() {
        return next;
    }

    public Node getPrev() {
        return prev;
    }

    public byte[] getValue() {
        return value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
