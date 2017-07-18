package com.learn.lishoubo.demo.bench.biasedlock;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by lishoubo on 17/7/6.
 */
public class JVMLockMain {
    public static void main(String[] args) throws Exception {
        Thread.sleep(10_000);

        final CyclicBarrier barrier = new CyclicBarrier(2);
        final JUCLockBench lockBench = new JUCLockBench(barrier);

        new Thread(new Runnable() {
            @Override
            public void run() {
                /**
                 * warm up
                 */
                lockBench.doRun();

                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }

                long begin = System.currentTimeMillis();
                lockBench.doRun();
                long end = System.currentTimeMillis();
                final double durationMs = (end - begin) / 1000000.0;
                final double operationsPerMillisecond = lockBench.operationCount() / durationMs;

                System.out.println("operationsPerMillisecond:" + operationsPerMillisecond);
                try {
                    barrier.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        }).start();


        barrier.await();


        barrier.await();

    }
}
