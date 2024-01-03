package com.leguan.reentrantlock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan
 * @Date: 2023/12/29
 */
public class ReentrantLockTestTwo {

    private static ReentrantLock lock = new ReentrantLock();
    private static int sum = 0;

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                lock.lock();
                try {
                    for (int i1 = 0; i1 < 10000; i1++) {
                        sum++;
                    }
                }finally {
                    lock.unlock();
                }
            },"thread"+i).start();
        }
        Thread.sleep(2000);
        System.out.println(sum);
    }
}
