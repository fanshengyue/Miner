package com.miner.bean;

/**
 * Created by Mr.Zhang on 2017/11/21.
 * GPS信息
 */

public class GPSBean {

//    经度
    private String longitude;
//    纬度
    private String latitude;
//    时间
    private String time;
//    海拔
    private String altitude;
//    速度
    private String speed;
//    方向
    private String bearing;
//    位置提供者
    private String provider;
//    准确性
    private String accuracy;
//    计时器
    private String elapsedRealtimeNanos;


    public GPSBean() {
    }

    public GPSBean( String longitude, String latitude, String time, String altitude, String speed, String bearing, String provider, String accuracy, String elapsedRealtimeNanos) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.time = time;
        this.altitude = altitude;
        this.speed = speed;
        this.bearing = bearing;
        this.provider = provider;
        this.accuracy = accuracy;
        this.elapsedRealtimeNanos = elapsedRealtimeNanos;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getBearing() {
        return bearing;
    }

    public void setBearing(String bearing) {
        this.bearing = bearing;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    public String getElapsedRealtimeNanos() {
        return elapsedRealtimeNanos;
    }

    public void setElapsedRealtimeNanos(String elapsedRealtimeNanos) {
        this.elapsedRealtimeNanos = elapsedRealtimeNanos;
    }


}
