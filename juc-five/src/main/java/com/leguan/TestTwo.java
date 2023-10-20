package com.leguan;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan
 * @Date: 2023/10/17
 */
public class TestTwo {
    private static int count = 0;
    public synchronized static void add() {
        count++;
    }
    public synchronized static void sub(){
        count--;
    }
    public static void main(String[] args) throws InterruptedException {
        new Thread(()->{
            for (int i = 0; i < 50000; i++) {add();}
        }).start();
        new Thread(()->{
            for (int i = 0; i < 50000; i++) {sub();}
        }).start();
        Thread.sleep(1000);
        System.out.println(count);
    }
}
