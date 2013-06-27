package com.xianyifa.audioplayer.util;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*
 * 实现所有activity退出
 */
public class MyApplication extends Application {
	private final String TAG = "MyApplication";
	private List<Activity> activityList = new LinkedList<Activity>();
	private static MyApplication instance;

	private MyApplication() {
	}

	// 单例模式中获取唯一的MyApplication实例
	public static MyApplication getInstance() {
		if (null == instance) {
			instance = new MyApplication();
		}
		return instance;

	}

	// 添加Activity到容器中
	public void addActivity(Activity activity) {
		if(!activityList.contains(activity)){
			activityList.add(activity);
		}
	}

	public void removeActivity(Activity activity){
		activityList.remove(activity);
	}
	
	// 遍历所有Activity并finish

	public void exit() {
		//停止服务
		for (Activity activity : activityList) {
			Log.i(TAG, activity.getClass().getName());
			activity.finish();
		}
		System.exit(0);

	}
}
