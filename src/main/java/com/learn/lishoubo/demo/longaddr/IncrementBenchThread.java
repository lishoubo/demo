package com.learn.lishoubo.demo.longaddr;

/**
 * Created by lishoubo on 17/7/3.
 */
public class IncrementBenchThread implements Runnable{
    protected IncrementBench controller;

    IncrementBenchThread(IncrementBench controller) {
        this.controller = controller;
    }

    public void run() {
        controller.waitForBarrier();
        for (int i = 0; i < controller.addCount; i++) {
            controller.incrementCounter();
        }
        controller.waitForBarrier();
    }
}
