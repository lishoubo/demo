package com.learn.lishoubo.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Created by lishoubo on 17/1/6.
 */
public class DubboDemoServiceImp implements DubboDemoService {
    static Logger logger = LoggerFactory.getLogger(DubboDemoServiceImp.class);
    Random random = new Random();

    @Override
    public String hello(String name, int sleepWhile) {
        long begin = System.currentTimeMillis();
        try {
            Thread.sleep(random.nextInt(sleepWhile));
        } catch (InterruptedException e) {

        }
        logger.info("|{}", (System.currentTimeMillis() - begin));
        return "hello," + name;
    }
}
