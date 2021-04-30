import java.io.*;
import java.util.concurrent.*;

public final class MyCacheImplementation {

    /**
     * A doubly-linked-list implementation to save objects into the hashmap as
     * key-value pari.
     *
     */

    /**
     * The maximum number of bits that can be cached, should be set during
     * instantiation time.
     */
    private final int maxCapacity;

    /**
     * The current number of bits that can be cached, should be set during
     * instantiation time.
     */
    private int currentCapacity;

    /**
     * Use {@linkplain ConcurrentHashMap} here to maintain the cache of objects.
     * Also this offers thread safe access of the cache.
     */
    private ConcurrentHashMap<String, Node> map;

    /**
     * A key-value representation of the cache object identified by a cache key.
     * This is actually a doubly-linked list which maintains the most recently
     * accessed objects (read/write) at the tail-end and the least read objects at
     * the head.
     */
    private Node head, tail;

    public MyCacheImplementation(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.currentCapacity = 0;
        map = new ConcurrentHashMap<>(maxCapacity);
    }

    /**
     * Removes a node from the head position doubly-linked list.
     * 
     * @param node
     */
    private void removeNode(Node node) {
        if (node == null)
            return;

        if (node.getPrev() != null) {
            node.getPrev().setNext(node.getNext());
        } else {
            head = node.getNext();
        }

        if (node.getNext() != null) {
            node.getNext().setPrev(node.getPrev());
        } else {
            tail = node.getPrev();
        }
    }

    /**
     * Offers a node to the tail-end of the doubly-linked list because it was
     * recently read or written.
     * 
     * @param node
     */
    private void updateTail(Node node) {
        if (node == null)
            return;
        if (head == null) {
            head = tail = node;
        } else {
            tail.setNext(node);
            node.setPrev(tail);
            node.setNext(null);
            tail = node;
        }
    }

    /**
     * Adds a new object to the cache. If the cache size has reached it's capacity,
     * then the least recently accessed object will be evicted.
     * 
     * @param key
     * @param value
     */
    public byte[] put(String key) {
        System.out.println("**************Try to read new file to cache**************");
        try {
            File file = new File(key);
            FileInputStream fis;
            fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            if (data.length <= this.maxCapacity) {
                while (currentCapacity + data.length > maxCapacity) {
                    this.currentCapacity -= head.getValue().length;
                    map.remove(head.getKey());
                    removeNode(head);
                }
                this.currentCapacity += data.length;
                Node node = new Node(key, data);
                updateTail(node);
                map.put(key, node);
            }
            return data;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fetches an object from the cache (could be null if no such mapping exists).
     * If the object is found in the cache, then it will be moved to the tail-end of
     * the doubly-linked list to indicate that it was recently accessed.
     * 
     * @param key
     */
    public byte[] get(String key) {
        Node node = map.get(key);
        removeNode(node);
        updateTail(node);
        return node != null ? node.getValue() : null;
    }

    /**
     * Fetches an object from the cache (could be null if no such mapping exists).
     * If the object is found in the cache, then it will be moved to the tail-end of
     * the doubly-linked list to indicate that it was recently accessed. If the If
     * object is NOT found in the cache, then it will add the file to the cache and
     * and locate it to the tail-end of the doubly-linked list to indicate that it
     * was recently accessed.
     * 
     * @param key
     * @param value
     */
    public byte[] getFileContent(String key) {
        byte[] demandedFile = this.get(key);
        return demandedFile != null ? demandedFile : put(key);
    }

    /**
     * Utility function to print the cache objects.
     */
    public void printCache() {
        Node curr = head;
        System.out.print("LRU -> ");
        while (curr != null) {
            System.out.print(curr.getValue() + " -> ");
            curr = curr.getNext();
        }
        System.out.print("<- RU");
        System.out.println();
    }

    /**
     * Create a cache instance that can store 80 bytes. => Read the contents of
     * 10BytesFile.txt from disk and store it in memory. => Read the contents of
     * 50BytesFile.txt from disk and store it in memory. => Read the contents of
     * 10BytesFile.txt from disk and store it in memory. => Read the contents of
     * 30BytesFile.txt from disk and store it in memory. Since we already have 60
     * bytes of memory used in the cache and adding another 30 bytes will pass the
     * 80 bytes limit we set, the content of 50BytesFile will be removed from memory
     * as it is the file that was least recently used. => Read the contents of
     * 100BytesFile.txt from disk but do not store it in memory since it is too
     * large.
     * 
     */
    public static void test1() {
        MyCacheImplementation cache = new MyCacheImplementation(80);

        byte[] b1 = cache.getFileContent("test1/10BytesFile.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b2 = cache.getFileContent("test1/50BytesFile.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b3 = cache.getFileContent("test1/10BytesFile.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b4 = cache.getFileContent("test1/30BytesFile.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b5 = cache.getFileContent("test1/100BytesFile.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);
    }

    /**
     *
     * Create a cache instance that can store 60 bytes. => Read 6 different file the
     * contents of 10 Bytes from disk and store it in memory. => Read the contents
     * of 30BytesFile.txt from disk and store it in memory. Since we already have 60
     * bytes of memory used in the cache and adding another 30 bytes will pass the
     * 60 bytes limit we set, the content of three LRU 10 bytes files will be
     * removed from memory.
     * 
     */
    public static void test2() {
        MyCacheImplementation cache = new MyCacheImplementation(60);

        byte[] b1 = cache.getFileContent("test2/10BytesFile1.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b2 = cache.getFileContent("test2/10BytesFile2.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b3 = cache.getFileContent("test2/10BytesFile3.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b4 = cache.getFileContent("test2/10BytesFile4.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b5 = cache.getFileContent("test2/10BytesFile5.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b6 = cache.getFileContent("test2/10BytesFile6.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b7 = cache.getFileContent("test2/30BytesFile.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);
    }

    /**
     * Create a cache instance that can store 10 bytes. => Read the contents 6
     * different of 0 Bytes files from disk and store it in memory. => Read the
     * contents of 10BytesFile1.txt from disk and store it in memory. => Read the
     * contents of 0BytesFile1.txt from cache and update LRU. => 0BytesFile2.txt
     * from cache and update LRU. => Read the contents of 10BytesFile2.txt from disk
     * and store it in memory. Since we already have 10 bytes of memory used in the
     * cache and adding another 10 bytes will pass the 10 bytes limit we set, the
     * content of 4 0 Bytes files and the 10BytesFile1.txt will be removed from
     * memory as it is the files that was least recently used. => Read the contents
     * of 10BytesFile2.txt from disk from disk and store it in memory.
     * 
     */
    public static void test3() {
        MyCacheImplementation cache = new MyCacheImplementation(10);

        byte[] b1 = cache.getFileContent("test3/0BytesFile1.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b2 = cache.getFileContent("test3/0BytesFile2.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b3 = cache.getFileContent("test3/0BytesFile3.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b4 = cache.getFileContent("test3/0BytesFile4.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b5 = cache.getFileContent("test3/0BytesFile5.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b6 = cache.getFileContent("test3/0BytesFile6.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b7 = cache.getFileContent("test3/10BytesFile1.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b8 = cache.getFileContent("test3/0BytesFile1.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b9 = cache.getFileContent("test3/0BytesFile2.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b10 = cache.getFileContent("test3/10BytesFile2.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);
    }

    /**
     * Create a cache instance that can store 00 bytes. =>Try and FAIL to Read the
     * contents of NOTEXIST.txt from disk => Read the contents of 10BytesFile.txt
     * from disk and store it in memory.
     * 
     */
    public static void test4() {
        MyCacheImplementation cache = new MyCacheImplementation(80);

        byte[] b1 = cache.getFileContent("test1/NOTEXIST.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

        byte[] b2 = cache.getFileContent("test1/10BytesFile.txt");
        cache.printCache();
        System.out.println(cache.currentCapacity);

    }

    /**
     * Runner program to test the LRU cache
     * 
     * @param args
     */
    public static void main(String[] args) {
        test1();
        test2();
        test3();
        test4();
    }

}
