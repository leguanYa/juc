package com.leguan.countdownlatch;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan.countdownlatch
 * @Date: 2024/1/3
 */
public class CountDownLatchTestTwo {

    private static AtomicInteger sum = new AtomicInteger();


    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(5);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                sum.incrementAndGet();
                countDownLatch.countDown();
            }, "thread" + i).start();
        }
        countDownLatch.await();
        long end = System.currentTimeMillis();
        System.out.println("主线程:在所有任务运行完成后，进行结果sum汇总:" + sum + "执行耗时为：" + (end - start) + "ms");

    }
}
