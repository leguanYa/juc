package com.leguan.jvmone;

import org.openjdk.jol.info.ClassLayout;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan.jvmone
 * @Date: 2024/1/9
 */
public class JOLDemo {
    public static void main(String[] args) {
        ClassLayout classLayout = ClassLayout.parseInstance(new Object());
        System.out.println(classLayout.toPrintable());

        ClassLayout classLayout2 = ClassLayout.parseInstance(new int[]{});
        System.out.println(classLayout2.toPrintable());


        ClassLayout classLayout3 = ClassLayout.parseInstance(new ADemo());
        System.out.println(classLayout3.toPrintable());
    }
}

class ADemo {
    private int id;
    private String name;
    private byte b;
    private Object o;
}
