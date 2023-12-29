package com.leguan;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan
 * @Date: 2023/12/22
 */
public class EscapeTest {
    /**
     * 1 -XX:+DoEscapeAnalysis //表示开启逃逸分析 (jdk1.8默认开启）
     * 2 -XX:-DoEscapeAnalysis //表示关闭逃逸分析。
     * 3 -XX:+EliminateAllocations //开启标量替换(默认打开)
     * 4 -XX:-EliminateAllocations //关闭标量替换
     * 5 -XX:+EliminateLocks //开启锁消除(jdk1.8默认开启）
     */
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 500000; i++) {
            test2();
        }
        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 标量替换
     */
    public static void test() {
        Point point = new Point(1, 2);
        int x = 1;
        int y = 2;
        System.out.println("ponit x:" + point.getX() + "point y:" + point.getY());
    }

    public static String test2() {
        Point point = new Point();
        return point.toString();
    }
}

class Point {
    int x;
    int y;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point() {

    }

    @Override
    public String toString() {
        return "Point{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}


