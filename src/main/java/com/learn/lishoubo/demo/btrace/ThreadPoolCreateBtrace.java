package com.learn.lishoubo.demo.btrace;

import com.sun.btrace.annotations.BTrace;
import com.sun.btrace.annotations.Export;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.Self;

import static com.sun.btrace.BTraceUtils.Threads.jstack;

/**
 * Created by lishoubo on 17/6/28.
 */
@BTrace
public class ThreadPoolCreateBtrace {
    @Export
    static long counter;

    @OnMethod(
            clazz = "java.util.concurrent.Executors",
            method = "defaultThreadFactory"
    )
    public static void onDefaultThreadFactory(@Self Object self) {
        if (counter < 10) {
            jstack();
            counter++;
        }
    }
}
