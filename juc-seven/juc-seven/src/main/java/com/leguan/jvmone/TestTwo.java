package com.leguan.jvmone;

import java.lang.reflect.Method;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan.jvmone
 * @Date: 2024/1/4
 */
public class TestTwo {

    public static void main(String[] args) throws Exception {
        MyClassLoader myClassLoaderTest = new MyClassLoader("/Users/zhanghui/dev/github");
        Class<?> aClass = myClassLoaderTest.loadClass("com.leguan.jvmone.User1");
        Object o = aClass.newInstance();
        Method t1 = aClass.getDeclaredMethod("sou", null);
        t1.invoke(o, null);
        System.out.println(aClass.getClassLoader().getClass().getName());

    }
}
