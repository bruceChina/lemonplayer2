/**
 * 
 */
package com.xianyifa.audioplayer.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.xianyifa.audioplayer.util.MyApplication;

/**
 * @author Administrator
 *
 */
public class SetUpActivity extends PreferenceActivity {
	private final String TAG = "SetUpActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.app_set_up);
		addPreferencesFromResource(R.xml.app_set_up);
		MyApplication.getInstance().addActivity(this); //把当前activity放到activity管理器
		
		
		
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
		SharedPreferences contentPreference = preference.getSharedPreferences(); 
		Log.i(TAG, "onPreferenceTreeClick:"+preference.getKey()+":"+contentPreference.getBoolean(preference.getKey(), false));
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}
	
}
