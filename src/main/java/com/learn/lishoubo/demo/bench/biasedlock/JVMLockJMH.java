package com.learn.lishoubo.demo.bench.biasedlock;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Created by lishoubo on 17/7/6.
 * <p>
 * java  -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDetails -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=1 -Xloggc:/tmp/logs/gc.log -XX:+PrintGCDateStamps  -XX:+UnlockDiagnosticVMOptions -XX:-DisplayVMOutput -XX:+LogVMOutput -XX:LogFile=/tmp/logs/vm.log  -XX:-DoEscapeAnalysis -XX:-EliminateLocks  -jar target/demo-1.0-SNAPSHOT.jar
 * <p>
 * 取消偏向锁
 * -XX:-UseBiasedLocking
 */
@State(Scope.Benchmark)
@Threads(1)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 20, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
public class JVMLockJMH {
    private static long value = 20000000;
    private static Object lock = new Object();

    private static int count;

    @Benchmark
    public void doRun() {
        synchronized (lock) {
            count++;
        }

    }

    public static void main(String[] args) throws RunnerException, InterruptedException {
        Thread.sleep(5_000);
        Options opt = new OptionsBuilder()
                .include(JVMLockJMH.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

}
