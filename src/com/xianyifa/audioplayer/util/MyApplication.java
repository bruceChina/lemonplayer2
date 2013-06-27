package com.xianyifa.audioplayer.util;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*
 * ʵ������activity�˳�
 */
public class MyApplication extends Application {
	private final String TAG = "MyApplication";
	private List<Activity> activityList = new LinkedList<Activity>();
	private static MyApplication instance;

	private MyApplication() {
	}

	// ����ģʽ�л�ȡΨһ��MyApplicationʵ��
	public static MyApplication getInstance() {
		if (null == instance) {
			instance = new MyApplication();
		}
		return instance;

	}

	// ���Activity��������
	public void addActivity(Activity activity) {
		if(!activityList.contains(activity)){
			activityList.add(activity);
		}
	}

	public void removeActivity(Activity activity){
		activityList.remove(activity);
	}
	
	// ��������Activity��finish

	public void exit() {
		//ֹͣ����
		for (Activity activity : activityList) {
			Log.i(TAG, activity.getClass().getName());
			activity.finish();
		}
		System.exit(0);

	}
}
