package com.example.Commerce.cache;

import com.example.Commerce.aspects.PerformanceMonitoringAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
@Slf4j
public class CacheManager {

    private static final int MAX_CACHE_SIZE = 1000;
    private static final long TTL_SECONDS = 300; // 5 minutes
    
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final PerformanceMonitoringAspect performanceMonitor;

    public CacheManager(PerformanceMonitoringAspect performanceMonitor) {
        this.performanceMonitor = performanceMonitor;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Supplier<T> supplier) {
        CacheEntry entry = cache.get(key);
        
        if (entry != null && !entry.isExpired()) {
            performanceMonitor.recordCacheHit(key);
            log.info("Cache HIT: {}", key);
            return (T) entry.value;
        }
        
        performanceMonitor.recordCacheMiss(key);
        log.info("Cache MISS: {}", key);
        
        T value = supplier.get();
        
        evictIfNeeded();
        cache.put(key, new CacheEntry(value, Instant.now().getEpochSecond() + TTL_SECONDS));
        log.info("Cache PUT: {} (value={})", key, value != null ? "present" : "null");
        
        return value;
    }

    public void invalidate(String key) {
        cache.remove(key);
        log.info("Cache INVALIDATE: {}", key);
    }
    
    private void evictIfNeeded() {
        if (cache.size() >= MAX_CACHE_SIZE) {
            String oldestKey = cache.entrySet().stream()
                .min((e1, e2) -> Long.compare(e1.getValue().expiresAt, e2.getValue().expiresAt))
                .map(e -> e.getKey())
                .orElse(null);
            if (oldestKey != null) {
                cache.remove(oldestKey);
                log.info("Cache EVICT: {}", oldestKey);
            }
        }
    }
    
    private static class CacheEntry {
        final Object value;
        final long expiresAt;
        
        CacheEntry(Object value, long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
        
        boolean isExpired() {
            return Instant.now().getEpochSecond() > expiresAt;
        }
    }
}
