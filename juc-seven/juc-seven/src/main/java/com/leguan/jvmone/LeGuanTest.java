package com.leguan.jvmone;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan.jvmone
 * @Date: 2024/1/5
 */
public class LeGuanTest {

    public int compute() {
        int a = 1;
        int b = 2;
        int c = (a + b) * 10;
        return c;
    }

    public static void main(String[] args) {
        LeGuanTest leGuanTest = new LeGuanTest();
        int compute = leGuanTest.compute();
        System.out.println(compute);
    }
}
