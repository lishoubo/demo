package com.learn.lishoubo.demo.longaddr;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lishoubo on 17/6/22.
 */
public class Bench {
    private static Logger logger = LoggerFactory.getLogger(Bench.class);

    public static void main(String[] args) throws Exception {
        Options opts = new Options();
        opts.addOption("n", true, "nThreads");
        opts.addOption("c", true, "class");

        CommandLineParser parser = new DefaultParser();

        CommandLine cmd = parser.parse(opts, args);

        int nThreads = 1;
        String clazz = null;

        if (cmd.hasOption("n")) {
            nThreads = NumberUtils.toInt(cmd.getOptionValue("n"), 1);
        }
        if (cmd.hasOption("c")) {
            clazz = cmd.getOptionValue("c");
        }

        logger.warn("nThreads:{}", nThreads);

        final IncrementBench incrementBench = getIncrementBench(clazz, nThreads);
        incrementBench.benchmark();
        new CountDownLatch(1).await();
    }

    private static IncrementBench getIncrementBench(String clazz, int nThreads) {
        try {
            Class<?> aClass = Class.forName("com.learn.lishoubo.demo.longaddr." + clazz);
            Constructor<?> constructor = aClass.getDeclaredConstructor(int.class);
            Object o = constructor.newInstance(nThreads);
            return (IncrementBench) o;
        } catch (Exception e) {
            logger.error("new clazz:{}", clazz, e);
        }
        return null;
    }

}

