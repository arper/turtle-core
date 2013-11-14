package org.arper.turtle.internal;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

public class TLLockingContext {

    public TLLockingContext(int concurrencyLevel) {
        locks = CacheBuilder.newBuilder()
                .concurrencyLevel(concurrencyLevel)
                .weakKeys()
                .build(new CacheLoader<Object, Lock>() {
                    @Override
                    public Lock load(Object key) throws Exception {
                        return new ReentrantLock();
                    }
                });
    }

    private final LoadingCache<Object, Lock> locks;

    public Lock getLock(Object o) {
        try {
            return locks.get(o);
        } catch (ExecutionException e) {
            throw Throwables.propagate(e);
        }
    }

}
