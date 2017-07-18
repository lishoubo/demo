package com.learn.lishoubo.demo.bench.frame;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CyclicBarrier;

/**
 * Created by lishoubo on 17/7/6.
 */

/**
 *@FIXME 线程模型又问题: 预热的线程和实际运行的线程应该保持一致
 */
public class BenchExecutor {
    private static Logger logger = LoggerFactory.getLogger(BenchExecutor.class);

    private final int nThreads;
    private final int warnUpTimes;
    private final int iterations;
    private final DescriptiveStatistics statistics;
    private final CyclicBarrier cyclicBarrier;

    public BenchExecutor(int nThreads, int warnUpTimes, int iterations) {
        this.nThreads = nThreads;
        this.warnUpTimes = warnUpTimes;
        this.iterations = iterations;
        this.statistics = new DescriptiveStatistics();
        this.cyclicBarrier = new CyclicBarrier(nThreads + 1);
    }

    public CyclicBarrier getCyclicBarrier() {
        return cyclicBarrier;
    }

    public void benchMark(BenchWorker benchWorker) throws Exception {
        logger.warn("sleep 5 seconds........");
        Thread.sleep(5_000);

        logger.warn("begin warm up........");
        doWarmUp(benchWorker);

        logger.warn("reset after warm up........");
        reset();

        logger.warn("do bench run........");
        doRun(benchWorker);

        logger.warn(String.format("Mean: %.0f ops/ms, stdev: %.0f ops/ms (min: %.0f, max: %.0f)\n", statistics.getMean(), statistics.getStandardDeviation(), statistics.getMin(), statistics.getMax()));
    }

    private void doWarmUp(BenchWorker benchWorker) {
        for (int i = 0; i < warnUpTimes; i++) {
            logger.warn("   warm up round:" + i);

            warmUpRound(i, benchWorker);

        }
    }

    private void doRun(BenchWorker benchWorker) {
        for (int i = 0; i < iterations; i++) {
            logger.warn("  run round:" + i);

            runRound(i, benchWorker);
        }
    }

    private void reset() {
        statistics.clear();
    }

    private void runRound(int round, BenchWorker benchWorker) {
        benchWorker.before();

        Thread[] threads = createThreads(benchWorker);

        logger.warn("nThreads:" + threads.length);

        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }


        final long startTime = System.nanoTime();
        /**
         * 同步状态
         */
        waitCyclicBarrier();

        /**
         * 等待结束
         */
        waitCyclicBarrier();

        final long endTime = System.nanoTime();

        final double durationMs = (endTime - startTime) / 1000000.0;
        final double operationsPerMillisecond = benchWorker.operationCount() / durationMs;
        logger.warn(String.format("run. round:" + round + " %6.0f ops/ms (%.0f ms)\n", operationsPerMillisecond, durationMs));

        statistics.addValue(operationsPerMillisecond);
        benchWorker.end();

        join(threads);

    }

    private void warmUpRound(int round, final BenchWorker benchWorker) {
        benchWorker.before();

        Thread[] threads = createThreads(new Runnable() {

            @Override
            public void run() {
                benchWorker.warmUp();
            }
        });


        for (int i = 0; i < threads.length; i++) {
            threads[i].start();
        }


        final long startTime = System.nanoTime();
        /**
         * 同步状态
         */
        waitCyclicBarrier();

        /**
         * 等待结束
         */
        waitCyclicBarrier();

        final long endTime = System.nanoTime();

        final double durationMs = (endTime - startTime) / 1000000.0;
        final double operationsPerMillisecond = benchWorker.operationCount() / durationMs;
        logger.warn(String.format("warnUp. round:" + round + ". %6.0f ops/ms (%.0f ms)\n", operationsPerMillisecond, durationMs));

        statistics.addValue(operationsPerMillisecond);
        benchWorker.end();

        join(threads);
    }

    private void join(Thread[] threads) {
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                logger.error("join exception", e);
            }
        }
    }

    private Thread[] createThreads(Runnable runnable) {
        Thread[] threads = new Thread[nThreads];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(runnable, "bench-thread");
        }
        return threads;
    }

    private void waitCyclicBarrier() {
        try {
            cyclicBarrier.await();
        } catch (Exception e) {
            logger.error("cyclicBarrier interrupted.", e);
        }

    }
}
