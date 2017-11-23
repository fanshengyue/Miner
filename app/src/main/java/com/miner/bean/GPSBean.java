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
//    海拔
    private String altitude;
//    速度
    private String speed;
//    方向
    private String bearing;



    public GPSBean() {
    }

    public GPSBean( String longitude, String latitude, String altitude, String speed, String bearing) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.speed = speed;
        this.bearing = bearing;

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
}
