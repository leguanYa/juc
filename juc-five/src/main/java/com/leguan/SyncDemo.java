package com.leguan;

import org.openjdk.jol.info.ClassLayout;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan
 * @Date: 2023/10/23
 */
public class SyncDemo {


//*开启编向锁:
//*-XX:+UseBiasedLocking -XX:BiasedLockingStartupDelay=0
//*关闭稿向锁:关闭之后程序默认会直接进入----
//*----------------------->>>>>>>>轻量级锁状态。
//* -XX:-UseBiasedLocking
//-XX:+UseBiasedLocking -XX:BiasedLockingStartupDelay=0

    public static void main(String[] args) throws InterruptedException {
        Object o = new Object();
        System.out.println("one----" + ClassLayout.parseInstance(o).toPrintable());
//        Thread.sleep(5000);
//        Object o1 = new Object();
//        System.out.println("two----" + ClassLayout.parseInstance(o1).toPrintable());
//        synchronized (o1) {
//            System.out.println("two----" + ClassLayout.parseInstance(o1).toPrintable());
//        }
////        Thread.sleep(5000);
//        o1.hashCode();
//        System.out.println("two----" + ClassLayout.parseInstance(o1).toPrintable());
//        new Thread(()-> {
//            for (int i = 0; i < 2; i++) {
//                synchronized (o) {
//                    System.out.println("第" + i + "次循环\n" + ClassLayout.parseInstance(o).toPrintable());
//                }
//            }
//        }, "thread1").start();

    }
}
