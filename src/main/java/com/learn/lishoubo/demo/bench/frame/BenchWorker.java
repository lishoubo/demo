package com.learn.lishoubo.demo.bench.frame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CyclicBarrier;

/**
 * Created by lishoubo on 17/7/6.
 */
public abstract class BenchWorker implements WarmupRunnable {
    protected static Logger logger = LoggerFactory.getLogger(BenchWorker.class);

    private final CyclicBarrier barrier;

    public BenchWorker(CyclicBarrier barrier) {
        this.barrier = barrier;
    }


    @Override
    public void run() {
        logger.warn("ThreadId:" + Thread.currentThread().getId());
        try {
            barrier.await();
            doRun();
            barrier.await();
        } catch (Throwable throwable) {
            logger.error("do run throwable.", throwable);
        }
    }

    @Override
    public void warmUp() {
        run();
    }

    public void before() {

    }

    protected abstract void doRun();

    public void end() {

    }

    /**
     * 运行次数
     *
     * @return
     */
    public abstract double operationCount();
}
