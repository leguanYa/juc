package com.leguan.jvmone;

import sun.misc.Launcher;

import java.net.URL;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan.jvmone
 * @Date: 2024/1/4
 */
public class ZhangTestOne {
    public static void main(String[] args) {
        System.out.println(String.class.getClassLoader());
        System.out.println((com.sun.crypto.provider.DESKeyFactory.class.getClassLoader()));
        System.out.println(ZhangTestOne.class.getClassLoader());


        System.out.println();
        ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();
        ClassLoader extClassloader = appClassLoader.getParent();
        ClassLoader bootstrapLoader = extClassloader.getParent();
        System.out.println("the appClassLoader : " + appClassLoader);
        System.out.println("the extClassloader : " + extClassloader);
        System.out.println("the bootstrapLoader : " + bootstrapLoader);


//        System.out.println();
//        System.out.println("bootstrapLoader加载以下文件：");
//        URL[] urls = Launcher.getBootstrapClassPath().getURLs();
//        for (int i = 0; i < urls.length; i++) {
//            System.out.println(urls[i]);
//
//        }
//        System.out.println();
//        System.out.println("extClassloader加载以下文件：");
//        System.out.println(System.getProperty("java.ext.dirs"));
//        System.out.println();
//        System.out.println("appClassLoader加载以下文件：");
//        System.out.println(System.getProperty("java.class.path"));
    }
}
