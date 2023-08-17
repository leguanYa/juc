package com.leguan;

import java.util.HashMap;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan
 * @Date: 2023/7/7
 */
public class Main {

    private boolean flag = true;
    private Integer count = 0;

    public void refresh() {
        flag = false;
        System.out.println(Thread.currentThread().getName() + "变更flag属性" + flag);
    }

    public void load() {
        System.out.println(Thread.currentThread().getName() + "开始执行循环累加");
        while (flag) {
            count++;
        }
        System.out.println(Thread.currentThread().getName() + "开始执行循环累加结束count=" + count);
    }

    public static void main(String[] args) throws InterruptedException {
//        Main test = new Main();
//        new Thread(test::load, "线程A").start();
//        Thread.sleep(1000);
//        new Thread(test::refresh, "线程B").start();

        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put(1,1);
        objectObjectHashMap.put(1,2);
    }
}