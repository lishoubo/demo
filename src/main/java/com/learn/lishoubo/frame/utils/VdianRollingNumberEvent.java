package com.learn.lishoubo.frame.utils;

/**
 * Created by lishoubo on 17/6/22.
 */
public enum VdianRollingNumberEvent {
    /**
     * 总的请求
     */
    COUNT(1),
    /**
     * 慢请求
     */
    SLOW_COUNT(1),
    /**
     * 失败请求
     */
    FAILURE(1),
    /**
     * 业务失败请求
     */
    BIZ_FAILURE(1),
    /**
     * 超时请求
     */
    TIMEOUT(1),
    /**
     * 总耗时
     */
    RT(1),
    /**
     * 业务耗时
     */
    BIZ_RT(1),
    ;
    private final int type;

    VdianRollingNumberEvent(int type) {
        this.type = type;
    }

    public boolean isCounter() {
        return type == 1;
    }

    public boolean isMaxUpdater() {
        return type == 2;
    }
}
