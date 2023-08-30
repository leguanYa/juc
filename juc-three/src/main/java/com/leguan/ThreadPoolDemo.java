package com.leguan;

import java.util.concurrent.*;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan
 * @Date: 2023/8/29
 */
public class ThreadPoolDemo {
    public static void main(String[] args) {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(10);
//        scheduledThreadPoolExecutor.execute();
        ExecutorService executorService = Executors.newCachedThreadPool();
        ExecutorService executorService1 = Executors.newFixedThreadPool(10);
        ExecutorService executorService2 = Executors.newSingleThreadExecutor();

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10, 20, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(10));
        for (int i = 0; i < 100; i++) {
            threadPoolExecutor.submit(new MyTask(i));
        }
    }
}


class MyTask implements Runnable {

    int i = 0;

    public MyTask(int i) {
        this.i = i;
    }

    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName() + "第" + i + "个项目");
        try {

            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
