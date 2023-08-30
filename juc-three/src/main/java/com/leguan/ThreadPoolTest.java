package com.leguan;

import com.sun.deploy.net.HttpUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan
 * @Date: 2023/8/17
 */
public class ThreadPoolTest {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        final Random random = new Random();
        final List<Integer> list = new ArrayList<>();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        for (int i = 0; i < 100000; i++) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    list.add(random.nextInt());
                }
            });
        }

        System.out.println("时间：" + (System.currentTimeMillis() - start));
        System.out.println("大小：" + list.size());
        executorService.shutdown();



    }
}
