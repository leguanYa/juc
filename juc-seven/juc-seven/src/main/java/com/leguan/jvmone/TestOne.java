package com.leguan.jvmone;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan.jvmone
 * @Date: 2024/1/4
 */
public class TestOne {

    static {
        System.out.println("------load TestOne-----");
    }

    public static void main(String[] args) {
        A a = new A();
        System.out.println("------load test-----");
//        B b = null;
        B b = new B();
    }
}

class A {
    static {
        System.out.println("------load A-----");
    }
    public A() {
        System.out.println("------init A-----");
    }
}
class B {
    static {
        C c = new C();
        System.out.println("------load B-----");
    }
    public B() {
        System.out.println("------init B-----");
    }
}

class C {
    static {
        System.out.println("------load C-----");
    }
    public C() {
        System.out.println("------init C-----");
    }
}