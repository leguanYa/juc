package com.leguan;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan
 * @Date: 2023/12/20
 */
public class LockEliminationTest {

    /**
     * 锁消除
     *  ‐XX:+EliminateLocks 开启锁消除(jdk8默认开启）
     *  -XX:-EliminateLocks 关闭锁消除
     * @param s1
     * @param s2
     */
    public void append(String s1, String s2) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(s1).append(s2);
    }



//    public static void main(String[] args) {
//        LockEliminationTest lockEliminationTest = new LockEliminationTest();
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < 1000000; i++) {
//            lockEliminationTest.append("1","2");
//        }
//        long end = System.currentTimeMillis();
//        System.out.println("执行时间："+ (end-start)+"ms");
//    }


    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                Object o = new Object();
                synchronized (o) {
                    // 业务逻辑
                }
            }).start();
        }
    }
}
