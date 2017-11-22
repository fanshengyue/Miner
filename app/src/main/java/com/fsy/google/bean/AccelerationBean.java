package com.fsy.google.bean;

/**
 * Created by Mr.Zhang on 2017/11/21.
 * 加速度
 */

public class AccelerationBean {
    private long systemTime;
    private float x;
    private float y;
    private float z;

    public AccelerationBean(long systemTime, float x, float y, float z) {
        this.systemTime = systemTime;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public long getSystemTime() {
        return systemTime;
    }

    public void setSystemTime(long systemTime) {
        this.systemTime = systemTime;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

}
