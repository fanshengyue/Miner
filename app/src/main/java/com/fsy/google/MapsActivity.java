package com.fsy.google;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.fsy.google.adapter.TimeListAdapter;
import com.fsy.google.bean.AccelerationBean;
import com.fsy.google.bean.GPSBean;
import com.fsy.google.bean.LightBean;
import com.fsy.google.utils.JsonUtil;
import com.fsy.google.utils.PermissionUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, ActivityCompat.OnRequestPermissionsResultCallback {
    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @BindView(R.id.bt_getdata)
    Button btGetdata;
    @BindView(R.id.cb_traffic)
    CheckBox cbTraffic;
    @BindView(R.id.bt_pop)
    Button btPop;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;
    private GoogleMap mMap;
    private Handler handler;
    private Marker mCusMarker;//自定义Marker
    private double lat = 39.1160770147;
    private double lng = 117.2135210037;
    private int frequency = 5;//传感器更新频率
    private Timer timer;
    private TimerTask task;
    private PopupWindow popupWindow;
    private LocationManager lm;
    private File file;
    private SensorManager mSensorManager;
    private String bestProvider;
    private Sensor mAccelerometer;
    private Sensor mLightSensor;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        String filePath = "/sdcard/Test/";
        file = makeFilePath(filePath, "test.json");
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // 判断GPS是否正常启动
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();
            // 返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }

        // 为获取地理位置信息时设置查询条件
        bestProvider = lm.getBestProvider(getCriteria(), true);

        // 获取位置信息
        // 如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER
        Location location = lm.getLastKnownLocation(bestProvider);
        updateView(location);
        // 监听状态
        lm.addGpsStatusListener(gpsListener);
        // 绑定监听，有4个参数
        // 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
        // 参数2，位置信息更新周期，单位毫秒
        // 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
        // 参数4，监听
        // 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新

        // 1秒更新一次，或最小位移变化超过1米更新一次；
        // 注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);



        btPop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initSpinner();
                if(popupWindow!=null&&!popupWindow.isShowing()){
                    popupWindow.showAsDropDown(btPop,0,5);
                }
            }
        });

        btGetdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateTimer();
                if (null!=mSensorManager) {
                    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI);

                    mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
                    mSensorManager.registerListener(sensorEventListener, mLightSensor, SensorManager.SENSOR_DELAY_UI);
                }
            }
        });
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        if(lat>0&&lng>0){
                            LatLng myLatLng = new LatLng(lat , lng);
                            mCusMarker = mMap.addMarker(new MarkerOptions()
                                    .position(myLatLng)
                                    .flat(true)//标记平面化
                                    .rotation(45.0f)//将标记旋转45度
                                    .icon(BitmapDescriptorFactory.defaultMarker()));
                            //                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
                        }
                        break;
                    default:
                        break;
                }
            }
        };

    }

    /**
     * 设置下拉列表
     */
    private void initSpinner() {
        ListView listView=new ListView(this);
        final List<Integer> data = new ArrayList<>();
        data.add(5);
        data.add(10);
        data.add(20);
        data.add(30);
        data.add(60);
        TimeListAdapter adapter=new TimeListAdapter(this,data);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                btPop.setText(getResources().getString(R.string.frequency)+data.get(i)+"s");
                frequency=data.get(i);
                popupWindow.dismiss();
                updateTimer();
            }
        });
        popupWindow = new PopupWindow(listView, btPop.getWidth(), ActionBar.LayoutParams.WRAP_CONTENT, true);
        // 取得popup窗口的背景图片
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.button_fliter_down);
        popupWindow.setBackgroundDrawable(drawable);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                // 关闭popup窗口
                popupWindow.dismiss();
            }
        });

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();
        Location mylocation = mMap.getMyLocation();
        if (mylocation != null && mylocation.getLatitude() > 0 && mylocation.getLongitude() > 0) {
            LatLng myLatLng = new LatLng(mylocation.getLatitude(), mylocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(myLatLng).title("Marker in MyLocation"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(myLatLng));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15f));
        }
        updateTraffic();

    }

    /**
     * 路况点击事件
     */
    public void onTrafficToggled(View view) {
        updateTraffic();
    }

    private void updateTraffic() {
        if (!checkReady()) {
            return;
        }
        mMap.setTrafficEnabled(cbTraffic.isChecked());
    }

    private boolean checkReady() {
        if (mMap == null) {
            return false;
        }
        return true;
    }
    private void updateTimer(){
        if(timer!=null&&task!=null){
            timer.cancel();
            task.cancel();
        }
        timer = new Timer();
        task = new TimerTask() {

            @Override
            public void run() {
               handler.sendEmptyMessage(1);
            }
        };
        timer.schedule(task,0,frequency*1000);
    }



    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(MapsActivity.this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

//传感器监听
    private SensorEventListener sensorEventListener=new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {



            Log.e("linc", "value size: " + sensorEvent.values.length);
            float xValue = sensorEvent.values[0];// Acceleration minus Gx on the x-axis
            float yValue = sensorEvent.values[1];//Acceleration minus Gy on the y-axis
            float zValue = sensorEvent.values[2];//Acceleration minus Gz on the z-axis


            long systemTime=System.currentTimeMillis();
            switch (sensorEvent.sensor.getType()){
                //                加速度传感器
                case Sensor.TYPE_ACCELEROMETER:
                    AccelerationBean accelerationBean=new AccelerationBean(systemTime,xValue,yValue,zValue);
                    writeTxtToFile(JsonUtil.obj2json(accelerationBean));
                    break;
                //                    光线传感器
                case Sensor.TYPE_LIGHT:
                    LightBean lightBean=new LightBean(systemTime,xValue);
                    writeTxtToFile(JsonUtil.obj2json(lightBean));
                    break;
                //                    温度传感器
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    break;
                //                    湿度传感器
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    break;
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };


    private Location mLocation;
    // 位置监听
    private LocationListener locationListener = new LocationListener() {

        /**
         * 位置信息变化时触发
         */
        public void onLocationChanged(Location location) {
            mLocation=location;
            updateView(location);
            Log.i(TAG, "时间：" + location.getTime());
            Log.i(TAG, "经度：" + location.getLongitude());
            Log.i(TAG, "纬度：" + location.getLatitude());
            Log.i(TAG, "海拔：" + location.getAltitude());
        }

        /**
         * GPS状态变化时触发
         */
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                // GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    Log.i(TAG, "当前GPS状态为可见状态");
                    break;
                // GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "当前GPS状态为服务区外状态");
                    break;
                // GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG, "当前GPS状态为暂停服务状态");
                    break;
            }
        }

        /**
         * GPS开启时触发
         */
        public void onProviderEnabled(String provider) {
            Location location = lm.getLastKnownLocation(provider);
            updateView(location);
        }

        /**
         * GPS禁用时触发
         */
        public void onProviderDisabled(String provider) {
            updateView(null);
        }

    };

    private String TAG=MapsActivity.class.getSimpleName();
    // 状态监听
    GpsStatus.Listener gpsListener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                // 第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.i(TAG, "第一次定位");
                    break;
                // 卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.i(TAG, "卫星状态改变");
                    // 获取当前状态
                    GpsStatus gpsStatus = lm.getGpsStatus(null);
                    // 获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    // 创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites()
                            .iterator();
                    int count = 0;
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite s = iters.next();
                        count++;
                    }
                    System.out.println("搜索到：" + count + "颗卫星");
                    break;
                // 定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.i(TAG, "定位启动");
                    break;
                // 定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.i(TAG, "定位结束");
                    break;
            }
        };
    };


    public static String ms2Date(long _ms){
        Date date = new Date(_ms);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    /**
     * 实时更新文本内容
     *
     * @param location
     */
    private void updateView(Location location) {
        if (location != null) {
            lat=location.getLatitude();
            lng=location.getLongitude();
            GPSBean gpsBean=new GPSBean(System.currentTimeMillis(),String.valueOf(location.getLongitude()),String.valueOf(location.getLatitude()),ms2Date(location.getTime()),String.valueOf(location.getAltitude()),String.valueOf(location.getBearing()),String.valueOf(location.getBearing()),location.getProvider(),String.valueOf(location.getAccuracy()),String.valueOf(location.getElapsedRealtimeNanos()));
            writeTxtToFile(JsonUtil.obj2json(gpsBean));

        } else {
            Toast.makeText(MapsActivity.this,"位置提供者:"+bestProvider+"     location为null",Toast.LENGTH_SHORT).show();
        }
    }


    // 将字符串写入到文本文件中
    public void writeTxtToFile(String strcontent) {
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 返回查询条件
     *
     * @return
     */
    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        // 设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        // 设置是否要求速度
        criteria.setSpeedRequired(true);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(true);
        // 设置是否需要方位信息
        criteria.setBearingRequired(true);
        // 设置是否需要海拔信息
        criteria.setAltitudeRequired(true);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }





    // 生成文件
    private   File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    private  static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(mMap!=null){
            mMap.clear();
        }
        lm.removeUpdates(locationListener);
        mSensorManager.unregisterListener(sensorEventListener);
    }
}
