package com.leguan;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan
 * @Date: 2023/8/30
 */
public class ScheduledThreadPoolExecutorDemo {

    public static void main(String[] args) {
        ScheduledThreadPoolExecutor executor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(5);
        Task task = new Task(100);
        System.out.println("create:"+task.getI());
//        executor.schedule(task, 2, TimeUnit.SECONDS);
//        executor.scheduleWithFixedDelay(task, 0, 2, TimeUnit.SECONDS);// 任务+延迟
        executor.scheduleAtFixedRate(task, 0, 4, TimeUnit.SECONDS);//任延迟取最大值，稳定定时
//        定时处理器处理的时候需要注意抛异常的问题，如果异常自己没有捕获到，那么就不会继续执行了
    }
}

class Task implements Runnable {

    int i = 0;

    public Task(int i) {
        this.i = i;
    }

    public int getI() {
        return i;
    }

    @Override
    public void run() {
        System.out.println(new Date().getSeconds());
        System.out.println(Thread.currentThread().getName() + "第" + i + "个项目");
        try {

            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

