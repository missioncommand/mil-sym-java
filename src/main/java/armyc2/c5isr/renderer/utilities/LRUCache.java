package armyc2.c5isr.renderer.utilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Modified LRUCache implementation from: https://techblogstation.com/java/lru-cache-implementation-in-java/
 * and
 * https://www.baeldung.com/java-lru-cache
 */


/**
 * The class for LRU Cache storage and its operations
 * @param <T>
 */
public class LRUCache<T> {

        private static class LRUEntry<T>
        {

                public String key;
                public T value;
                public LRUEntry<T> next;
                public LRUEntry<T> prev;

                public LRUEntry(LRUEntry<T> prev, LRUEntry<T> next, String key, T value) {
                        this.prev = prev;
                        this.next = next;
                        this.key = key;
                        this.value = value;
                }

        }

        // Variable to store the least recently used element
        private LRUEntry<T> lruElement;

        // Variable to store the most recently used element
        private LRUEntry<T> mruElement;

        private Map<String, LRUEntry<T>> container;
        private int capacity;
        private int currentSize;

        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

        // Constructor for setting the values in instance variables
        public LRUCache(int capacity) {


            this.capacity = capacity;
                currentSize = 0;
                lruElement = new LRUEntry<T>(null, null, null, null);
                mruElement = lruElement;
                container = new ConcurrentHashMap<>();
                }

        /**
         * The get method to perform the retrieve operations on data
         * @param key String
         * @return T
         */
        public T get(String key)
        {
                lock.readLock().lock();
                try {
                        LRUEntry<T> tempLRUEntry = container.get(key);
                        if (tempLRUEntry == null) {
                                return null;
                        }
                        // In case the MRU leave the list as it is :
                        else if (tempLRUEntry.key.equals(mruElement.key)) {
                                return mruElement.value;
                        }

                        // Getting the Next and Previous Nodes
                        LRUEntry<T> nextLRUEntry = tempLRUEntry.next;
                        LRUEntry<T> prevLRUEntry = tempLRUEntry.prev;

                        // If LRU is updated at the left-most
                        if (tempLRUEntry.key.equals(lruElement.key)) {
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
                        lock.readLock().unlock();
                }

        }

        /**
         * The put method to perform the insert operations on cache
          * @param key String
         * @param value T
         */
        public void put(String key, T value)
        {
                lock.writeLock().lock();
                try {
                        if (container.containsKey(key)) {
                                return;
                        }

                        // Inserting the new Node at the right-most end position of the linked-list
                        LRUEntry<T> myLRUEntry = new LRUEntry<>(mruElement, null, key, value);
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
                        lock.writeLock().unlock();
                }
        }
}
