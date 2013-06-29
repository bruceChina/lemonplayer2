package com.xianyifa.audioplayer.ui;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

import com.xianyifa.audioplayer.db.AudioDao;
import com.xianyifa.audioplayer.impl.BaseActivity;
import com.xianyifa.audioplayer.impl.MyPlayer;
import com.xianyifa.audioplayer.util.MyApplication;
import com.xianyifa.audioplayer.util.Toolbox;
import com.xianyifa.audioplayer.vo.AudioFile;

public class MyActivity extends Activity implements OnTouchListener,OnGestureListener,BaseActivity  {
	private final String TAG = "MyActivity";
	private final int ADDAUDIOPLAYER_ID = Menu.FIRST;
	private final int DELAUDIOPLAYER_ID = Menu.FIRST + 1;
	private final int EXITAUDIOPLAYER_ID = Menu.FIRST + 2;
	private final int SET_UP_ID = Menu.FIRST + 3;
	private final int FILE_RESULT_CODE = 1;
	public GestureDetector gestureDetector;
	protected MyPlayer myPlayerService;//播放服务对象
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        
	}

	/*
	 * 添加菜单 (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		// 退出程序
		menu.add(0, ADDAUDIOPLAYER_ID, 0, "添加歌曲").setShortcut('2', 'b').setIcon(R.drawable.ic_menu_scan);
		menu.add(0, DELAUDIOPLAYER_ID, 0, "删除歌曲").setShortcut('2', 'b').setIcon(R.drawable.operation_button_delete);
		menu.add(0, SET_UP_ID, 0, "设置").setShortcut('2', 'b').setIcon(R.drawable.ic_menu_setting);
		// .setIcon(R.drawable.exit);
		// 退出程序
		menu.add(1, EXITAUDIOPLAYER_ID, 0, "退出").setShortcut('4', 'd')
				.setIcon(R.drawable.ic_menu_exit);
		return true;
	}

	/*
	 * 处理菜单动作 (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		// 退出不在这里操作
		switch (item.getItemId()) {
		case EXITAUDIOPLAYER_ID:
			// 退出播放器
			if(getPlayerService() != null){//如果播放服务已经启动
				getPlayerService().hideNotification();//把通知栏清楚
				getPlayerService().getPlayerService().stopSelf();//停止服务
			}
			MyApplication.getInstance().exit(); //把管理器中的所有activity的退出 finish 完了会直接System.exit(0);//不用停止服务
			Log.i(TAG, "bye bye!");
			return true;
		case ADDAUDIOPLAYER_ID:
			intent  = new Intent(MyActivity.this, SelectFileActivity.class);
			//以浮窗打开新的activity
			startActivityForResult(intent, FILE_RESULT_CODE);
			return true;
		case SET_UP_ID:
//			LayoutInflater inflater = (LayoutInflater)this.getSystemService(this.LAYOUT_INFLATER_SERVICE);
//			//加载设置界面
//			View LayoutView = (View)inflater.inflate(R.layout.app_set_up, null);
//			//使用PopupWindow显示
//			PopupWindow pw = new PopupWindow(LayoutView, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
//			pw.showAtLocation(findViewById(R.id.bottom_menu), Gravity.CENTER, 0, 300);
			
			intent = new Intent(MyActivity.this, SetUpActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			
			startActivity(intent);
			
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 以startActivityForResult 打开的activity 返回的调用
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(resultCode == FILE_RESULT_CODE){
			Bundle bundle = null;
			if(data != null && (bundle = data.getExtras()) != null){
//				Toast toast = Toast.makeText(MyActivity.this,
//						bundle.getString("file"), Toast.LENGTH_LONG);
//				toast.setGravity(Gravity.CENTER, 0, 0);
//				toast.show();
				Log.i(TAG, bundle.getString("file"));
				
				/** 扫描并保持歌曲 start */
				File file = new File(bundle.getString("file"));
				List<AudioFile> audioFiles = Toolbox.showAudioFile(file);
				Log.i(TAG, bundle.getString("file")+"232");
				if(audioFiles != null){
					AudioDao ad = new AudioDao(MyActivity.this);
					for (AudioFile audioFile : audioFiles) {
						ad.save(audioFile);
					}
					ad.closeDB();
					//刷新列表
					refresh();
				}
//				AudioList al = new AudioList();
//				al.refresh();
				/** 扫描并保持歌曲 end */
				
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * 刷新列表方法
	 * @param param
	 */
	public void refresh() {
		
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onDown(android.view.MotionEvent)
	 */
	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		if (e1.getX()-e2.getX() > 30 && Math.abs(velocityX) > 0) {   
			TextView audioLyricText = (TextView)findViewById(R.id.audio_lyric);
			audioLyricText.performClick();
//          切换Activity   
//          Intent intent = new Intent(ViewSnsActivity.this, UpdateStatusActivity.class);   
//          startActivity(intent);   
//            Toast.makeText(MyActivity.this, "向左手势", Toast.LENGTH_SHORT).show();   
        } else if (e2.getX()-e1.getX() > 30 && Math.abs(velocityX) > 0) {   
        	TextView audioListText = (TextView)findViewById(R.id.audiolisttext); 
        	audioListText.performClick();
//          切换Activity   
//          Intent intent = new Intent(ViewSnsActivity.this, UpdateStatusActivity.class);   
//          startActivity(intent);   
//            Toast.makeText(MyActivity.this, "向右手势", Toast.LENGTH_SHORT).show();   
        }   
           
		return false;
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onLongPress(android.view.MotionEvent)
	 */
	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onShowPress(android.view.MotionEvent)
	 */
	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see android.view.GestureDetector.OnGestureListener#onSingleTapUp(android.view.MotionEvent)
	 */
	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
		// TODO Auto-generated method stub
		return gestureDetector.onTouchEvent(event);
	}
	
	/**
	 * 读取配置
	 */
	public HashMap<String, String> getConfig(){
		SharedPreferences shareprefernces =  getSharedPreferences("config", Context.MODE_APPEND);//取得要写入的文件对象
		String bgImg = shareprefernces.getString("bgImg", "");
		if(bgImg.equals("")){
			bgImg = "bkgs/001.jpg";
		}
		HashMap<String, String> config = new HashMap<String, String>();
		config.put("bg", bgImg);
		return config;
	}
	
	/**
	 * 保存配置文件
	 */
	public void setConfig(HashMap<String, String> config){
		SharedPreferences shareprefernces =  getSharedPreferences("config", Context.MODE_APPEND);//取得要写入的文件对象
		Editor editor = shareprefernces.edit();//取得一个编辑器
		Set<Map.Entry<String, String>> keySet = config.entrySet();
		for (Map.Entry<String, String> m : keySet) {
			editor.putString(m.getKey(), m.getValue());
		}
		
		editor.commit();//一定不要忘记吧内存中的数据提交保存
	}
	

	/**
	 * 取得播放服务对象
	 */
	@Override
	public MyPlayer getPlayerService() {
		// TODO Auto-generated method stub
		return myPlayerService;
	}
	
	@Override
	public void updateBackground(){
		
	}
	
	/**
	 * 底部按钮事件
	 */
	public void bottomMeun(){
		
	}
	
}
