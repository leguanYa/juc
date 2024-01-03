package com.leguan.reentrantlock;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan
 * @Date: 2023/12/29
 */
public class ReentrantLockTest {

    public static void main(String[] args) {
        ReentrantLock lock = new ReentrantLock();
        Condition condition = lock.newCondition();

        new Thread(() -> {
            lock.lock();
            String name = Thread.currentThread().getName();
            System.out.println(name+"开始处理任务");
            try {
                condition.await();
                System.out.println(name+"处理任务完成");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }, "thread1").start();


        new Thread(() -> {
            lock.lock();
            String name = Thread.currentThread().getName();
            System.out.println(name+"开始处理任务");
            try {
                Thread.sleep(2000);
                condition.signal();
                System.out.println(name+"处理任务完成");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }
        }, "thread2").start();
    }
}
