package com.learn.lishoubo.demo.longaddr;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by lishoubo on 17/7/3.
 */
@State(Scope.Benchmark)
@Threads(4)
@Warmup(iterations = 5)
@Measurement(iterations = 5, time = 20, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@BenchmarkMode(Mode.Throughput)
public class AtomLongJMHBench {
    private AtomicLong counter = new AtomicLong();

    @Benchmark
    public void longAddderBenchmark() {
        counter.incrementAndGet();
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(AtomLongJMHBench.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
