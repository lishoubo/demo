package com.learn.lishoubo.demo.longaddr;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by lishoubo on 17/7/3.
 */
final class AtomicLongBenchmark extends IncrementBench {
    AtomicLong counter;

    public AtomicLongBenchmark(int numThreads) {
        super(numThreads);
    }

    @Override
    protected void initializeCounter() {
        counter = new AtomicLong();
    }

    @Override
    protected void incrementCounter() {
        counter.incrementAndGet();
    }

    @Override
    protected void clearCounter() {
        counter.set(0);
    }

    @Override
    protected long getCounterValue() {
        return counter.longValue();
    }
}
