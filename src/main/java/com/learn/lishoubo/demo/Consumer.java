package com.learn.lishoubo.demo;

import com.learn.lishoubo.demo.service.DubboDemoService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by lishoubo on 17/1/6.
 */
public class Consumer {
    public static void main(String[] args) throws IOException, InterruptedException {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
                "spring-context.xml", "dubbo-consumer.xml"
        );
        context.start();
        context.registerShutdownHook();

        DubboDemoService dubboDemoService = context.getBean(DubboDemoService.class);
        System.out.println(dubboDemoService.hello("test", 100));
        new CountDownLatch(1).await();
    }
}
