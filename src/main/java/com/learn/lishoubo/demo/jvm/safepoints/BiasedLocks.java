package com.learn.lishoubo.demo.jvm.safepoints;

import java.util.concurrent.locks.LockSupport;

/**
 * Created by lishoubo on 17/7/5.
 */
public class BiasedLocks {

    /**
     * java -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDetails -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1 -Xloggc:/tmp/logs/gc.log -XX:+PrintGCDateStamps  -XX:+UnlockDiagnosticVMOptions -XX:-DisplayVMOutput -XX:+LogVMOutput -XX:LogFile=/tmp/logs/vm.log -jar target/demo-1.0-SNAPSHOT.jar
     */
    private static synchronized void contend() {
        LockSupport.parkNanos(1000);
    }

    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(5_000);

        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    BiasedLocks.contend();
                }
            }).start();
        }
    }

}
