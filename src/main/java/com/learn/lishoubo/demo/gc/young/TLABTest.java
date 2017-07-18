package com.learn.lishoubo.demo.gc.young;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by lishoubo on 17/7/14.
 *
 * java -XX:+PrintGCApplicationConcurrentTime -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDetails -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1 -Xloggc:/tmp/logs/gc.log -XX:+PrintGCDateStamps -verbose:gc -XX:+UnlockDiagnosticVMOptions -XX:-DisplayVMOutput -XX:+LogVMOutput -XX:LogFile=/tmp/logs/vm.log -server -Xms512m -Xmx512m -Xmn128m -XX:+UseConcMarkSweepGC  -XX:+DoEscapeAnalysis  -jar target/demo-1.0-SNAPSHOT.jar
 */
public class TLABTest {
    private static final int total = 10000 * 10000 * 10000 * 10000;
    private static final int nThreads = 10;

    private static class Foo {
        private int x;
        private static int counter;

        public Foo() {
            x = (++counter);
        }
    }

    public static void main(String[] args) {
        final CyclicBarrier cyclicBarrier = new CyclicBarrier(nThreads + 1);

        for (int n = 0; n < nThreads; n++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    waitBarrier(cyclicBarrier);

                    for (int i = 0; i < total; ++i) {
                        Foo foo = new Foo();
                    }

                    waitBarrier(cyclicBarrier);
                }
            }).start();
        }
        waitBarrier(cyclicBarrier);

        long begin = System.currentTimeMillis();

        waitBarrier(cyclicBarrier);

        System.out.println("time:" + (System.currentTimeMillis() - begin));
    }

    private static void waitBarrier(CyclicBarrier cyclicBarrier) {
        try {
            cyclicBarrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
