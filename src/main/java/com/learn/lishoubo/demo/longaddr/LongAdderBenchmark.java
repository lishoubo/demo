package com.learn.lishoubo.demo.longaddr;


import com.learn.lishoubo.frame.utils.LongAdder;

/**
 * Created by lishoubo on 17/7/3.
 */
public class LongAdderBenchmark extends IncrementBench {
    LongAdder counter;

    public LongAdderBenchmark(int numThreads) {
        super(numThreads);
    }

    @Override
    protected void initializeCounter() {
        counter = new LongAdder();
    }

    @Override
    protected void incrementCounter() {
        counter.increment();
    }

    @Override
    protected void clearCounter() {
        counter.reset();
    }

    @Override
    protected long getCounterValue() {
        return counter.longValue();
    }

    public static void main(String[] args) {
        LongAdderBenchmark longAdderBenchmark = new LongAdderBenchmark(2);
        try {
            longAdderBenchmark.benchmark();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
