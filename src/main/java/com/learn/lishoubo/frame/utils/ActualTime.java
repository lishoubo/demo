package com.learn.lishoubo.frame.utils;

import com.vdian.middleware.qps.core.utils.Time;

/**
 * Created by lishoubo on 17/6/23.
 */
public class ActualTime implements Time {
    @Override
    public long getCurrentTimeInMillis() {
        return System.currentTimeMillis();
    }
}
