package com.leguan.semaphore;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan.semaphore
 * @Date: 2024/1/2
 */
public class SemaphoreTest {

    private static final Semaphore semaphore = new Semaphore(5);

    private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(50));


    public static void exec() {
        try {
            semaphore.acquire();
            System.out.println("执行到exec方法");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            semaphore.release();
        }
    }
    public static void main(String[] args) throws InterruptedException {
        for (; ;) {
            Thread.sleep(200);
            pool.execute(SemaphoreTest::exec);
        }

    }
}
