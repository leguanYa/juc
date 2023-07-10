package com.leguan;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan
 * @Date: 2023/7/7
 */
public class Test {

    private boolean flag = true;
    private int count = 0;

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
        Test test = new Test();
        new Thread(test::load, "线程A").start();
        Thread.sleep(1000);
        new Thread(test::refresh, "线程B").start();
    }
}
