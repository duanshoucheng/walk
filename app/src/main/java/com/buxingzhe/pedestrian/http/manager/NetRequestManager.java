package com.buxingzhe.pedestrian.http.manager;

import com.buxingzhe.lib.rxjava.TransformUtils;

import java.util.Map;

import rx.Subscriber;
import rx.Subscription;

/**
 * Created by zhaishaoping on 27/03/2017.
 */
public class NetRequestManager{

    private static class SingleHolder {
        private static final NetRequestManager sInstance = new NetRequestManager();
    }

    private NetRequestManager() {
    }

    public static NetRequestManager getInstance() {
        return SingleHolder.sInstance;
    }

    //普通登陆
    public Subscription login(Map<String, String> paramsMap, Subscriber subscriber){
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .login(paramsMap)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //手机号登陆
    public Subscription loginByPhone(Map<String, String> paramsMap, Subscriber subscriber){
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .loginByPhone(paramsMap)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //修改用户信息
    public Subscription modifyUserInfo(Map<String, String> paramsMap, Subscriber subscriber) {
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .modifyUserInfo(paramsMap)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //查询用户信息
    public Subscription getUserInfo(String userId, String token, Subscriber subscriber){
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .getUserInfo(userId,token)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //用户注册
    public Subscription register(Map<String,String> paramsMap,Subscriber subscriber){
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .register(paramsMap)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //上传步行或者骑行记录
    public Subscription uploadWalkRecord(Map<String,String> paramsMap,Subscriber subscriber){
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .uploadWalkRecord(paramsMap)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //获取步行或者骑行记录
    public Subscription getWalkRecord(String recordId,Subscriber subscriber){
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .getWalkRecord(recordId)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //获取当天天气
    public Subscription getCurrentWeather(String cityName,Subscriber subscriber){
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .getCurrentWeather(cityName)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //获取历史天气
    public Subscription getHistoryWeather(String cityName,String date, Subscriber subscriber){
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .getHistoryWeather(cityName,date)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //我的跑团
    public Subscription getMineRunTeam(Map<String,String> paramsMap, Subscriber subscriber){
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .getMineRunTeam(paramsMap)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //获取用户信息
//    public Subscription getUserInfo(String userId,String token,Subscriber subscriber){
//        return RetrofitManager.getInstance()
//                .getNetRequestService()
//                .getUserInfo(userId,token)
//                .compose(TransformUtils.defaultSchedulers())
//                .subscribe(subscriber);
//    }

    //查询圈子
    public Subscription getWalkRecords(String userId,int pageNo,int pageSize,Subscriber subscriber){
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .getWalkRecords(userId,pageNo,pageSize)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //活动查询
    public Subscription getActivities(int pageNo,int pageSize,Subscriber subscriber){
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .getActivities(pageNo,pageSize)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //查询该活动下的记录
    public Subscription getWalkRecordsByActivity(String userId,String activity,int pageNo, int pageSize, Subscriber subscriber){
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .getWalkRecordsByActivity(userId,activity,pageNo,pageSize)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //步行或骑行记录点赞
    public Subscription walkRecordLike(String userId,String token,String walkRecord,Subscriber subscriber){
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .walkRecordLike(userId,token,walkRecord)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //评论步行或骑行记录
    public Subscription walkRecordComment(String userId,String c,String walkRecord,String star,Double streetStar,Double envirStar,Double safeStar,String content,Subscriber subscriber){
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .walkRecordComment(userId,walkRecord,walkRecord,star,streetStar,envirStar,safeStar,content)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //查询附近街道
    public Subscription getStreets(Map<String,String> paramsMap,Subscriber subscriber){
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .getStreets(paramsMap)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

    //查询附近标记点
    public Subscription getNearByPoints(Map<String, String> paramsMap, Subscriber subscriber) {
        return RetrofitManager.getInstance()
                .getNetRequestService()
                .getNearByPoints(paramsMap)
                .compose(TransformUtils.defaultSchedulers())
                .subscribe(subscriber);
    }

}
