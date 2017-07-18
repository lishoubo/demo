package com.learn.lishoubo.demo.bench.frame;

/**
 * Created by lishoubo on 17/7/6.
 */
public interface WarmupRunnable extends Runnable {

    /**
     * 执行预热的动作
     */
    void warmUp();
}
