package com.learn.lishoubo.demo;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lishoubo on 17/1/6.
 */
public class Provider {
    public static void main(String[] args) throws IOException, InterruptedException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "spring-context.xml", "dubbo-provider.xml"
        );
        context.start();
        context.registerShutdownHook();
        new CountDownLatch(1).await();
    }
}
