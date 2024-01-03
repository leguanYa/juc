package com.leguan.countdownlatch;

import java.util.concurrent.CountDownLatch;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan.countdownlatch
 * @Date: 2024/1/3
 */
public class CountDownLatchTest {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        for (int i = 0; i < 5; i++) {
            new Thread(() -> {
                try {
                    countDownLatch.await();
                    System.out.println(Thread.currentThread().getName()+"开始执行");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, "thread"+i).start();
        }
        Thread.sleep(3000);
        countDownLatch.countDown();
    }
}
