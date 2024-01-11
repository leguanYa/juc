package com.leguan.jvmone;

import java.io.FileInputStream;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan.jvmone
 * @Date: 2024/1/4
 */
public class MyClassLoader extends ClassLoader {

    private String classPath;

    public MyClassLoader(String classPath) {
        this.classPath = classPath;
    }

    private byte[] loadByte(String name) throws Exception {
        String path = name.replace(".", "/").concat(".class");
        FileInputStream fis = new FileInputStream(classPath + "/" + path);
        int len = fis.available();
        byte[] data = new byte[len];
        fis.read(data);
        fis.close();
        return data;

    }

    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] data = loadByte(name);
            //defineClass将一个字节数组转为Class对象，这个字节数组是class文件读取后最终的字节 数组。
            return defineClass(name, data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException();

        }
    }

}