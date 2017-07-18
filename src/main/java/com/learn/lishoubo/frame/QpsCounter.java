package com.learn.lishoubo.frame;

import com.vdian.middleware.qps.core.utils.Time;
import com.vdian.middleware.qps.core.utils.VdianRollingNumber;
import com.vdian.middleware.qps.core.utils.VdianRollingNumberEvent;

import java.io.Serializable;

/**
 * Created by lishoubo on 17/6/22.
 */
public class QpsCounter implements Serializable {
    private static final long serialVersionUID = 8537573401763461929L;

    private transient VdianRollingNumber vdianRollingNumber;

    public QpsCounter(Time time, int sampleTimeInMilliseconds, int sampleNumberOfBuckets) {
        vdianRollingNumber = new VdianRollingNumber(time, sampleTimeInMilliseconds, sampleNumberOfBuckets);
    }

    public QpsCounter(int sampleTimeSeconds, int sampleNumberOfBuckets) {
        vdianRollingNumber = new VdianRollingNumber(sampleTimeSeconds * 1000, sampleNumberOfBuckets);
    }

    /**
     * 增加一次请求计数,不区分成功或者失败
     */
    public void incrementSlowCount() {
        vdianRollingNumber.increment(VdianRollingNumberEvent.SLOW_COUNT);
    }

    public long slowCount() {
        return vdianRollingNumber.getRollingSum(VdianRollingNumberEvent.SLOW_COUNT);
    }

    /**
     * 增加一次请求计数,不区分成功或者失败
     */
    public void incrementCount() {
        vdianRollingNumber.increment(VdianRollingNumberEvent.COUNT);
    }

    public long count() {
        return vdianRollingNumber.getRollingSum(VdianRollingNumberEvent.COUNT);
    }

    /**
     * 增加一次失败计数
     */
    public void incrementFail() {
        vdianRollingNumber.increment(VdianRollingNumberEvent.FAILURE);
    }

    public long fail() {
        return vdianRollingNumber.getRollingSum(VdianRollingNumberEvent.FAILURE);
    }

    /**
     * 增加一次业务失败计数
     */
    public void incrementBizFail() {
        vdianRollingNumber.increment(VdianRollingNumberEvent.BIZ_FAILURE);
    }

    public long bizFail() {
        return vdianRollingNumber.getRollingSum(VdianRollingNumberEvent.BIZ_FAILURE);
    }

    /**
     * 增加一次超时计数
     */
    public void incrementTimeout() {
        vdianRollingNumber.increment(VdianRollingNumberEvent.TIMEOUT);
    }

    public long timeout() {
        return vdianRollingNumber.getRollingSum(VdianRollingNumberEvent.TIMEOUT);
    }

    /**
     * 增加一次总的RT
     */
    public void increaseRT(long timeMs) {
        vdianRollingNumber.add(VdianRollingNumberEvent.RT, timeMs);
    }

    /**
     * 增加一次业务RT
     */
    public void increaseBizRT(long timeMs) {
        vdianRollingNumber.add(VdianRollingNumberEvent.BIZ_RT, timeMs);
    }

    /**
     * 获取最近一次采用的请求数
     */
    public long getLastSampleCount() {
        return vdianRollingNumber.getValueOfLatestBucket(VdianRollingNumberEvent.COUNT);
    }

    /**
     * 平均RT
     */
    public long avgRT() {
        final long count = vdianRollingNumber.getRollingSum(VdianRollingNumberEvent.COUNT);
        final long totalTime = vdianRollingNumber.getRollingSum(VdianRollingNumberEvent.RT);
        if (count > 0) {
            return totalTime / count;
        } else {
            return totalTime;
        }
    }

    /**
     * 平均RT
     */
    public long avgBizRT() {
        final long count = vdianRollingNumber.getRollingSum(VdianRollingNumberEvent.COUNT);
        final long totalTime = vdianRollingNumber.getRollingSum(VdianRollingNumberEvent.BIZ_RT);
        if (count > 0) {
            return totalTime / count;
        } else {
            return totalTime;
        }
    }
}
