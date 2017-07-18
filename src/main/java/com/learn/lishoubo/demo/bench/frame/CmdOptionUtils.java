package com.learn.lishoubo.demo.bench.frame;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by lishoubo on 17/7/6.
 */
public class CmdOptionUtils {
    private static Logger logger = LoggerFactory.getLogger(CmdOptionUtils.class);


    public static int getIntOption(CommandLine commandLine, String option, String defaultValue) {
        String optionValue = commandLine.getOptionValue(option, defaultValue);
        logger.warn("cmd option. name:{}, value:{}, default:{}", option, optionValue, defaultValue);
        return Integer.parseInt(optionValue);
    }
}
