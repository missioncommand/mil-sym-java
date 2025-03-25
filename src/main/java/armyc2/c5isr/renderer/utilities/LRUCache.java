package armyc2.c5isr.renderer.utilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Modified LRUCache implementation from: https://techblogstation.com/java/lru-cache-implementation-in-java/
 * and
 * https://www.baeldung.com/java-lru-cache
 */


//The class for LRU Cache storage and its operations
public class LRUCache {

// Variable to store the least recently used element
private LRUEntry lruElement;

// Variable to store the most recently used element
private LRUEntry mruElement;

private Map<String, LRUEntry> container;
private int capacity;
private int currentSize;

private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

// Constructor for setting the values in instance variables
public LRUCache(int capacity) {


    this.capacity = capacity;
        this.currentSize = 0;
        lruElement = new LRUEntry(null, null, null, null);
        mruElement = lruElement;
        container = new ConcurrentHashMap<String, LRUEntry>();
        }

// The get method to perform the retrieve operations on data
public ImageInfo get(String key)
{
        this.lock.readLock().lock();
        try {
                LRUEntry tempLRUEntry = container.get(key);
                if (tempLRUEntry == null) {
                        return null;
                }
                // In case the MRU leave the list as it is :
                else if (tempLRUEntry.key == mruElement.key) {
                        return mruElement.value;
                }

                // Getting the Next and Previous Nodes
                LRUEntry nextLRUEntry = tempLRUEntry.next;
                LRUEntry prevLRUEntry = tempLRUEntry.prev;

                // If LRU is updated at the left-most
                if (tempLRUEntry.key == lruElement.key) {
                        nextLRUEntry.prev = null;
                        lruElement = nextLRUEntry;
                }

                // In case we are in the middle, we are required to update the items before and
                // after our item
                else if (tempLRUEntry.key != mruElement.key) {
                        prevLRUEntry.next = nextLRUEntry;
                        nextLRUEntry.prev = prevLRUEntry;
                }

                // And here we are finally moving our item to MRU
                tempLRUEntry.prev = mruElement;
                mruElement.next = tempLRUEntry;
                mruElement = tempLRUEntry;
                mruElement.next = null;

                return tempLRUEntry.value;
        }
        finally
        {
                this.lock.readLock().unlock();
        }

        }

// The put method to perform the insert operations on cache

        public void put(String key, ImageInfo value)
        {
                this.lock.writeLock().lock();
                try {
                        if (container.containsKey(key)) {
                                return;
                        }

                        // Inserting the new Node at the right-most end position of the linked-list
                        LRUEntry myLRUEntry = new LRUEntry(mruElement, null, key, value);
                        mruElement.next = myLRUEntry;
                        container.put(key, myLRUEntry);
                        mruElement = myLRUEntry;

                        // Deleting the entry of position left-most of LRU cache and also updating the
                        // LRU pointer
                        if (currentSize == capacity) {
                                container.remove(lruElement.key);
                                lruElement = lruElement.next;
                                lruElement.prev = null;
                        }

                        // Updating the size of container for the firstly added entry and updating the
                        // LRU pointer
                        else if (currentSize < capacity) {
                                if (currentSize == 0) {
                                        lruElement = myLRUEntry;
                                }
                                currentSize++;
                        }
                }
                finally
                {
                        this.lock.writeLock().unlock();
                }
        }
}
