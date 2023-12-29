package com.leguan;

import org.openjdk.jol.info.ClassLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan
 * @Date: 2023/12/20
 */
public class BiasedLockingTest {

    public static void main(String[] args) throws Exception {
        //休眠五秒创建的对象为偏向锁状态
        Thread.sleep(5000);
        List<A> list = new ArrayList<>();
        new Thread(() -> {
            //创建50个对象
            for (int i = 0; i < 50; i++) {
                A lock = new A();
                synchronized (lock) {
                    list.add(lock);
                }
            }
            //线程保活
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "thread1").start();
        Thread.sleep(3000);
        System.out.println("打印线程1的第20个对象信息\n" + ClassLayout.parseInstance(list.get(19)).toPrintable());


        new Thread(() -> {
            for (int i = 0; i < 40; i++) {
                A lock = list.get(i);
                String name = Thread.currentThread().getName();
                if ((i >= 18 && i <= 23) || i >= 38) {
                    System.out.println(name + "第" + (i + 1) + "次加锁准备中\n" + ClassLayout.parseInstance(lock).toPrintable());
                }
                synchronized (lock) {
                    if ((i >= 18 && i <= 23) || i >= 38) {
                        System.out.println(name + "第" + (i + 1) + "次加锁执行中\n" + ClassLayout.parseInstance(lock).toPrintable());
                    }
                }
            }
            try {
                Thread.sleep(100000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "thread2").start();


        System.out.println("打印的第11个对象信息\n" + ClassLayout.parseInstance(list.get(10)).toPrintable());
        System.out.println("打印的第20个对象信息\n" + ClassLayout.parseInstance(list.get(19)).toPrintable());
        System.out.println("打印的第21个对象信息\n" + ClassLayout.parseInstance(list.get(20)).toPrintable());
        System.out.println("打印的第41个对象信息\n" + ClassLayout.parseInstance(list.get(41)).toPrintable());
        System.out.println("批量重偏向时新创建的对象\n" + ClassLayout.parseInstance(new A()).toPrintable());


        Thread.sleep(3000);
        new Thread(() -> {
            for (int i = 0; i < 50; i++) {
                A lock = list.get(i);
                String name = Thread.currentThread().getName();
                if ((i >= 18 && i <= 23) || (i >= 38 && i <= 42)) {
                    System.out.println(name + "第" + (i + 1) + "次加锁准备中\n" + ClassLayout.parseInstance(lock).toPrintable());
                }
                synchronized (lock) {
                if ((i >= 18 && i <= 23) || (i >= 38 && i <= 42)) {
                        System.out.println(name + "第" + (i + 1) + "次加锁执行中\n" + ClassLayout.parseInstance(lock).toPrintable());
                    }
                }
            }
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "thread3").start();
        Thread.sleep(4000);
        System.out.println("批量重撤销后新创建的对象\n" + ClassLayout.parseInstance(new A()).toPrintable());
    }

//    public static void main(String[] args) throws Exception {
//        //延时产生可偏向对象
//        Thread.sleep(5000);
//
//        //创造100个偏向线程t1的偏向锁
//        List<A> listA = new ArrayList<>();
//        Thread t1 = new Thread(() -> {
//            for (int i = 0; i < 100; i++) {
//                A a = new A();
//                synchronized (a) {
//                    listA.add(a);
//                }
//            }
//            try {
//                //为了防止JVM线程复用，在创建完对象后，保持线程t1状态为存活
//                Thread.sleep(100000000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//        t1.start();
//
//        //睡眠3s钟保证线程t1创建对象完成
//        Thread.sleep(3000);
//        System.out.println("打印t1线程，list中第20个对象的对象头：");
//        System.out.println((ClassLayout.parseInstance(listA.get(19)).toPrintable()));
//
//        //创建线程t2竞争线程t1中已经退出同步块的锁
//        Thread t2 = new Thread(() -> {
//            //这里面只循环了30次！！！
//            for (int i = 0; i < 30; i++) {
//                A a = listA.get(i);
//                synchronized (a) {
//                    //分别打印第19次和第20次偏向锁重偏向结果
//                    if (i == 18 || i == 19) {
//                        System.out.println("第" + (i + 1) + "次偏向结果");
//                        System.out.println((ClassLayout.parseInstance(a).toPrintable()));
//                    }
//                }
//            }
//            try {
//                Thread.sleep(10000000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        });
//        t2.start();
//
////        Thread.sleep(3000);
////        out.println("打印list中第11个对象的对象头：");
////        out.println((ClassLayout.parseInstance(listA.get(10)).toPrintable()));
////        out.println("打印list中第26个对象的对象头：");
////        out.println((ClassLayout.parseInstance(listA.get(25)).toPrintable()));
////        out.println("打印list中第41个对象的对象头：");
////        out.println((ClassLayout.parseInstance(listA.get(40)).toPrintable()));
//    }


}

class A {

}
