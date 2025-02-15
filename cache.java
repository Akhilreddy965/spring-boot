import java.util.concurrent.*;
import java.util.*;

class ExpiringCache<K, V> {
    private final ConcurrentHashMap<K, CacheItem<V>> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final long expirationTimeMillis;

    public ExpiringCache(long expirationTimeMillis) {
        this.expirationTimeMillis = expirationTimeMillis;
        // Schedule cache cleanup every 10 minutes
        scheduler.scheduleAtFixedRate(this::cleanupExpiredItems, expirationTimeMillis, expirationTimeMillis, TimeUnit.MILLISECONDS);
    }

    public void put(K key, V value) {
        CacheItem<V> item = new CacheItem<>(value, System.currentTimeMillis() + expirationTimeMillis);
        cache.put(key, item);
    }

    public V get(K key) {
        CacheItem<V> item = cache.get(key);
        if (item != null && item.isExpired()) {
            cache.remove(key); // Remove expired item
            return null;
        }
        return item != null ? item.getValue() : null;
    }

    public void remove(K key) {
        cache.remove(key);
    }

    public void clear() {
        cache.clear();
    }

    private void cleanupExpiredItems() {
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<K, CacheItem<V>> entry : cache.entrySet()) {
            if (entry.getValue().getExpirationTime() < currentTime) {
                cache.remove(entry.getKey());
            }
        }
    }

    // Helper class for cache items
    private static class CacheItem<V> {
        private final V value;
        private final long expirationTime;

        public CacheItem(V value, long expirationTime) {
            this.value = value;
            this.expirationTime = expirationTime;
        }

        public V getValue() {
            return value;
        }

        public long getExpirationTime() {
            return expirationTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expirationTime;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        ExpiringCache<String, String> cache = new ExpiringCache<>(5000); // 5 seconds expiration

        cache.put("key1", "value1");
        System.out.println("Initial value: " + cache.get("key1")); // Should print "value1"

        // Wait 6 seconds for the cache to expire
        Thread.sleep(6000);
        System.out.println("After expiration: " + cache.get("key1")); // Should print "null"
    }
}
