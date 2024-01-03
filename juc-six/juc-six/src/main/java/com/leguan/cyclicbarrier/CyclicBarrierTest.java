package com.leguan.cyclicbarrier;

import java.util.HashMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan.cyclicbarrier
 * @Date: 2024/1/3
 */
public class CyclicBarrierTest {

    private static AtomicInteger c = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {

        CyclicBarrier cyclicBarrier = new CyclicBarrier(3, () -> {
            BarrierActionTest tel = new BarrierActionTest();
            tel.start();
        });


        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            final int ii = i;
            new Thread(() -> {
                map.put(ii, ii + 1);
                try {
                    System.out.println(Thread.currentThread().getName() + "开始阻塞了");
                    cyclicBarrier.await();
                    System.out.println(Thread.currentThread().getName() + "开始被唤醒了");
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            }, "thread" + i).start();
        }
        Thread.sleep(4000);
        System.out.println(map);
    }

    static class BarrierActionTest extends Thread {
        @Override
        public void run() {
            System.out.println("唤醒后执行了第"+c.get()+"次唤醒方法");
            c.incrementAndGet();
        }
    }
}
