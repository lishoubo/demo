package com.learn.lishoubo.demo.bench.longadder;

import com.learn.lishoubo.demo.bench.frame.BenchWorker;
import com.learn.lishoubo.demo.bench.frame.BenchExecutor;
import com.learn.lishoubo.demo.bench.frame.CmdOptionUtils;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by lishoubo on 17/7/6.
 */
public class AtomLongBench extends BenchWorker {
    private AtomicLong atomicLong = new AtomicLong();
    private static long value = 20000000;

    public AtomLongBench(CyclicBarrier barrier) {
        super(barrier);
    }

    @Override
    public void before() {
        atomicLong.set(0);
    }

    @Override
    protected void doRun() {
        for (int i = 0; i < value; i++) {
            atomicLong.incrementAndGet();
        }
    }

    @Override
    public double operationCount() {
        return atomicLong.get();
    }


    public static void main(String[] args) throws Exception {
        Options opts = new Options();
        opts.addOption("n", true, "nThreads");
        opts.addOption("w", true, "warnUp times");
        opts.addOption("r", true, "run iterations");

        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = parser.parse(opts, args);

        int nThreads = CmdOptionUtils.getIntOption(cmd, "n", "1"),
                warmUpTimes = CmdOptionUtils.getIntOption(cmd, "w", "1"),
                runIterations = CmdOptionUtils.getIntOption(cmd, "r", "1");

        BenchExecutor benchExecutor = new BenchExecutor(nThreads, warmUpTimes, runIterations);
        benchExecutor.benchMark(new AtomLongBench(benchExecutor.getCyclicBarrier()));
    }


}
