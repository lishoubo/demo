package com.learn.lishoubo.demo.bench.biasedlock;

import com.learn.lishoubo.demo.bench.frame.BenchWorker;
import com.learn.lishoubo.demo.bench.frame.CmdOptionUtils;
import com.learn.lishoubo.demo.bench.frame.BenchExecutor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

import java.util.concurrent.CyclicBarrier;

/**
 * Created by lishoubo on 17/7/6.
 *
 * java -server -XX:+PrintGCApplicationStoppedTime -XX:+PrintGCDetails -XX:+PrintSafepointStatistics -XX:PrintSafepointStatisticsCount=2 -Xloggc:/tmp/logs/gc.log -XX:+PrintGCDateStamps  -XX:+UnlockDiagnosticVMOptions -XX:-DisplayVMOutput -XX:+LogVMOutput -XX:LogFile=/tmp/logs/vm.log -jar target/demo-1.0-SNAPSHOT.jar
 *
 * 取消偏向锁
 * -XX:-UseBiasedLocking
 */
public class JVMLockBench extends BenchWorker {
    public static long value = 20000000;
    private static Object lock = new Object();
    private static int count;

    public JVMLockBench(CyclicBarrier barrier) {
        super(barrier);
    }

    @Override
    protected void doRun() {
        for (int i = 0; i < value; i++) {
            synchronized (lock) {
                count++;
            }
        }

    }

    @Override
    public double operationCount() {
        return value;
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
        benchExecutor.benchMark(new JVMLockBench(benchExecutor.getCyclicBarrier()));
    }

}
