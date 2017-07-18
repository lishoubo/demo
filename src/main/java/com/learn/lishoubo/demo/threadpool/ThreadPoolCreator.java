package com.learn.lishoubo.demo.threadpool;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by lishoubo on 17/6/27.
 */
public class ThreadPoolCreator {
    private volatile Executor executor;

    public void start() {
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    createNewPool();
                    sleepAWhile(1000);
                }
            }
        }.start();
    }

    public void createNewPool() {
        executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 2; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    sleepAWhile(1000);
                }
            });
        }
    }

    private static void sleepAWhile(int sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {

        }
    }
}
