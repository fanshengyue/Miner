package com.fsy.google.bean;

/**
 * Created by Mr.Zhang on 2017/11/21.
 * 光线传感器
 */

public class LightBean {
    private long systemTime;
    private float x;

    public LightBean(long systemTime, float x) {
        this.systemTime = systemTime;
        this.x = x;
    }
}
