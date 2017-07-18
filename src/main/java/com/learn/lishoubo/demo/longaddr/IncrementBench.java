package com.learn.lishoubo.demo.longaddr;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by lishoubo on 17/7/3.
 */
public abstract class IncrementBench {
    protected static Logger logger = LoggerFactory.getLogger(IncrementBench.class);
    protected final int addCount = 60000000;
    private final int runCount = 20;

    private final int numThreads;
    private final CyclicBarrier barrier;

    private final Thread[] threads;

    private final DescriptiveStatistics statistics;

    public IncrementBench(int numThreads) {
        this.numThreads = numThreads;
        barrier = new CyclicBarrier(numThreads + 1);
        threads = new Thread[numThreads];
        statistics = new DescriptiveStatistics(runCount);
    }

    protected abstract void initializeCounter();

    protected abstract void incrementCounter();

    protected abstract void clearCounter();

    protected abstract long getCounterValue();


    private IncrementBenchThread createThread() {
        return new IncrementBenchThread(this);
    }

    public void benchmark() throws Exception {
        logger.warn("Performing " + NumberFormat.getInstance().format(addCount) + " increments with " + numThreads + " threads");

        logger.warn("Warming up (" + runCount + " rounds)");
        /**
         * 预热
         */
        doRuns();

        // Clear measurements
        statistics.clear();

        logger.warn("Benchmark runs (" + runCount + " rounds)");
        doRuns();

        logger.warn(String.format("Mean: %.0f ops/ms, stdev: %.0f ops/ms (min: %.0f, max: %.0f)\n", statistics.getMean(), statistics.getStandardDeviation(), statistics.getMin(), statistics.getMax()));
    }

    private void doRuns() throws Exception {
        initializeCounter();

        for (int runNumber = 0; runNumber < runCount; runNumber++) {
            logger.warn("Iteration {}: ", runNumber);
            clearCounter();

            createAndStartThreads();
            long startTime = System.nanoTime();

            /**
             * 同步开始
             */
            waitForBarrier();

            /**
             * 等待结束
             */
            waitForBarrier();

            long finalValue = getCounterValue();

            long endTime = System.nanoTime();
            join();

            double duration = (endTime - startTime) / 1000000.0;
            double operationsPerMillisecond = finalValue / duration;
            logger.warn(String.format("%6.0f ops/ms (%.0f ms)\n", operationsPerMillisecond, duration));
            statistics.addValue(operationsPerMillisecond);

            Thread.sleep(500);
        }
    }

    private void createAndStartThreads() throws Exception {
        for (int i = 0; i < numThreads; i++) {
            IncrementBenchThread thread = createThread();
            threads[i] = new Thread(thread);
            threads[i].start();
        }
    }

    /**
     * 同步线程各个状态
     */
    protected void waitForBarrier() {
        try {
            barrier.await();
        } catch (Exception ex) {
            logger.error("exception", ex);
        }
    }

    private void join() {
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (Exception ex) {
                logger.error("exception", ex);
            }
        }
    }
}
