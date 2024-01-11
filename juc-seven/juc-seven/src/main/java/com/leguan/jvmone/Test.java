package com.leguan.jvmone;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan.jvmone
 * @Date: 2024/1/4
 */
public class Test {

    private static ZhangTestOne testOne = new ZhangTestOne();

    public void t1() {
        System.out.println("测试");
    }


    public static void main(String[] args) throws ClassNotFoundException {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        systemClassLoader.loadClass("java.lang.String");
    }
}
