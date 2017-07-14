package com.buxingzhe.pedestrian.walk;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.widget.Toast;

import com.buxingzhe.lib.util.Log;
import com.buxingzhe.pedestrian.activity.BaseFragment;
import com.buxingzhe.pedestrian.application.PDApplication;
import com.buxingzhe.pedestrian.common.GlobalParams;
import com.buxingzhe.pedestrian.http.manager.NetRequestManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.Subscriber;

/**
 * Created by chinaso on 2017/5/23.
 */

public class StepCountFragment extends BaseFragment {
    protected PDApplication trackApp = null;
    protected TextView distanceTv;
    protected TextView stepCountTv;
    private LocationManager lm;
    private static final String TAG = "StepCountFragment";
    protected double distance = 0;
    private Location oldLocation;
    private RefreshThread refreshThread = null;  //手动获取位置
    //记录
    protected HourStepCache stepCache;
    private List<HourStep> stepList = new ArrayList<>();
    private int hourStepCount = 0;
    private String timeStap;
    private String bestProvider;
    private Location location;

    private String today;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trackApp = (PDApplication) getActivity().getApplicationContext();
        stepCache = new HourStepCache(getActivity());
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("HH");
        timeStap = format.format(date);
    }

    private void checkUpLoadDistance() {
        today = getActivity().getSharedPreferences("todaydate", Context.MODE_PRIVATE).getString("todaydate", null);
        if (today == null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date date=new Date();
            today=sdf.format(date);
        }else{

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            Date newDate=new Date();
            String newDay=sdf.format(newDate);
            if (!newDay .equals(today)) {
                upLoadDistance();
                SharedPreferences preferences = this.getActivity().getSharedPreferences("todaydate", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("todaydate", newDay);
                editor.commit();

                //清除按小时计算的步数

                stepCache.celarStepsList();
                stepCache.saveStepsList(stepList);
            }
        }

    }

    private void upLoadDistance() {
        double distanceUp = distance / 1000;
        int stepCount = (int) (distance / 0.4);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String s = format.format(today);
        Map<String, String> params = new HashMap<>();
        params.put("distance", distanceUp + " ");
        params.put("stepCount", stepCount + " ");
        params.put("publishDate", s);
        params.put("userId", GlobalParams.USER_ID);
        params.put("token", GlobalParams.TOKEN);
        mSubscription = NetRequestManager.getInstance().publishWalkRecord(params, new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.i("onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.i(e.getMessage());
            }

            @Override
            public void onNext(String jsonStr) {
                distance = 0;
            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();


        distance = getActivity().getSharedPreferences("stepdistance", Context.MODE_PRIVATE).getFloat("stepdistanceKey", 0);
        checkUpLoadDistance();
        lm = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        // 判断GPS是否正常启动

        if (ContextCompat.checkSelfPermission(trackApp, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getActivity(), "请开启GPS导航...", Toast.LENGTH_SHORT).show();
            // 返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }


        // 为获取地理位置信息时设置查询条件
        bestProvider = lm.getBestProvider(getCriteria(), true);
        // 获取位置信息
        // 如果不设置查询要求，getLastKnownLocation方法传入的参数为LocationManager.GPS_PROVIDER
        location = lm.getLastKnownLocation(bestProvider);
        updateView(location);
        // 监听状态
        lm.addGpsStatusListener(listener);
        // 绑定监听，有4个参数
        // 参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
        // 参数2，位置信息更新周期，单位毫秒
        // 参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
        // 参数4，监听
        // 备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新

        // 1秒更新一次，或最小位移变化超过1米更新一次；
        // 注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener);
        startRefreshThread(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        lm.removeUpdates(locationListener);
        SharedPreferences preferences = this.getActivity().getSharedPreferences("stepdistance", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("stepdistanceKey", (float) distance);
        editor.commit();
    }

    // 位置监听
    private LocationListener locationListener = new LocationListener() {


        /**
         * 位置信息变化时触发
         */
        public void onLocationChanged(Location location) {
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
            if (ActivityCompat.checkSelfPermission(trackApp, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
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

    // 状态监听
    GpsStatus.Listener listener = new GpsStatus.Listener() {
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
                    if (ActivityCompat.checkSelfPermission(trackApp, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
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
        }

        ;
    };

    /**
     * 实时更新文本内容
     *
     * @param location
     */
    private void updateView(Location location) {
        if (location != null) {
            if (oldLocation != null) {
                Double d = getDistance(oldLocation, location);
                if (d < 0.01) {
                    distance = distance + d;
                    hourStepCount = hourStepCount + (int) (d / 0.0004);
                }
                oldLocation=location;
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    distanceTv.setText((float)distance + " ");
                    int totalCount=(int)(distance/0.0004);
                    stepCountTv.setText(totalCount + " ");
                }
            });


        } else {
            // 不更新EditText对象
            /*distanceTv.setText(0 + " ");

            stepCountTv.setText(0 + " ");*/
        }

        oldLocation = location;

    }

    private double getDistance(Location locationOld, Location locationNew) {
        // 维度
        double lat1 = (Math.PI / 180) * locationOld.getLatitude();
        double lat2 = (Math.PI / 180) * locationNew.getLatitude();

        // 经度
        double lon1 = (Math.PI / 180) * locationOld.getLongitude();
        double lon2 = (Math.PI / 180) * locationOld.getLongitude();

        // 地球半径
        double R = 6371;

        // 两点间距离 km，如果想要米的话，结果*1000就可以了
        double d = Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon2 - lon1)) * R;

        return d;
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
        criteria.setSpeedRequired(false);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(false);
        // 设置是否需要方位信息
        criteria.setBearingRequired(false);
        // 设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }


    /**
     * 启动刷新线程
     *
     * @param isStart
     */
    private void startRefreshThread(boolean isStart) {

        if (refreshThread == null) {
            refreshThread = new RefreshThread();
        }

        refreshThread.refresh = isStart;

        if (isStart) {
            if (!refreshThread.isAlive()) {
                refreshThread.start();
            }
        } else {
            refreshThread = null;
        }


    }

    private class RefreshThread extends Thread {


        protected boolean refresh = true;

        public void run() {

            while (refresh) {
                if (ActivityCompat.checkSelfPermission(trackApp, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                location = lm.getLastKnownLocation(bestProvider);
                System.out.println("steps--查询location----");
                if(location!=null){
                    System.out.println("steps--查询location"+location.getLatitude()+"经度"+location.getLongitude());
                    updateView(location);
                    newHourTodo();
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
            }

        }

        private void newHourTodo() {
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("HH");
            String s = format.format(date);

            if (!timeStap.equals(s)) {
                //TODO
                if(stepCache.readStepsList()!=null){
                    stepList = stepCache.readStepsList();
                }

                HourStep hourStep = new HourStep();
                hourStep.setHour(s+"点");
                hourStep.setStepCount(hourStepCount);
                stepList.add(hourStep);
                hourStepCount = 0;
                stepCache.saveStepsList(stepList);

            }
        }

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        lm.removeUpdates(locationListener);
        lm.removeGpsStatusListener(listener);
        startRefreshThread(false);
    }
}