package com.xianyifa.audioplayer.util;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

/*
 * ʵ������activity�˳�
 */
public class MyApplication extends Application {
	private final String TAG = "MyApplication";
	private List<Activity> activityList = new LinkedList<Activity>();
	private Activity topActivity;//��ǰ���еĵ�activity
	private boolean isReloadBackground = false;
	private static MyApplication instance;
	private boolean playState = false;//���ŵ�״̬

	private MyApplication() {
	}

	// ����ģʽ�л�ȡΨһ��MyApplicationʵ��
	public static MyApplication getInstance() {
		if (null == instance) {
			instance = new MyApplication();
		}
		return instance;

	}

	public boolean isPlayState() {
		return playState;
	}

	public void setPlayState(boolean playState) {
		this.playState = playState;
	}

	public boolean isReloadBackground() {
		return isReloadBackground;
	}

	public void setReloadBackground(boolean isReloadBackground) {
		this.isReloadBackground = isReloadBackground;
	}

	public Activity getTopActivity() {
		return topActivity;
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
