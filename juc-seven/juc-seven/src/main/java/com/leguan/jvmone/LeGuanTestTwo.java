package com.leguan.jvmone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan.jvmone
 * @Date: 2024/1/5
 */
public class LeGuanTestTwo {

    public static void main(String[] args) throws InterruptedException {
        List<LeGuanTestTwo> objects = new ArrayList<>();
        while (true) {
            objects.add(new LeGuanTestTwo());
            Thread.sleep(1);
        }
    }
}
