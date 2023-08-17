package com.leguan;

import java.util.HashMap;

/**
 * @Description：
 * @Author：ZhangHui
 * @Package：com.leguan
 * @Date: 2023/7/27
 */
public class TestHashMap {
    private static HashMap<Long, Object> objectObjectHashMap = new HashMap<>();

    public static void main(String[] args){
        for (int i = 0; i < 100000; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    objectObjectHashMap.put(System.nanoTime(), new UserKey());
                }
            }).start();
        }
    }
}

class UserKey {

    public UserKey() {
    }

    @Override
    public int hashCode() {
        return 1;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}