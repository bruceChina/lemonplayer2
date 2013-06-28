package com.xianyifa.audioplayer.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xianyifa.audioplayer.impl.MyPlayer;
import com.xianyifa.audioplayer.lyric.ResolveLRC;
import com.xianyifa.audioplayer.lyric.Sentence;
import com.xianyifa.audioplayer.service.PlayerService;
import com.xianyifa.audioplayer.util.MyApplication;

public class AudioLyric extends MyActivity {
	private final String TAG = "AudioLyric";
	private int widthPixels;//手机屏幕宽度
	protected MyPlayer myPlayerService;//播放服务对象
	private MyServiceConnection conn;
	private Intent service;// 音乐播放服务意图
	private Handler handler;
	private BroadcastReceiver receiver;//监听广播
	private String lyricPath = "";//歌词路径
	private String oldLyricPath = "";//上一首歌词路径
	private ResolveLRC resolveLRC;//歌词解析器
	private static List<Sentence> list = new ArrayList<Sentence>();//里面装的是所有的句子
	private final int EXITAUDIOPLAYER_ID = Menu.FIRST + 2;
	private HashMap<String, String> config;//系统参数
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audiolyric);
		
		MyApplication.getInstance().addActivity(this); //把当前activity放到activity管理器
		
		//手势切换activity start
		gestureDetector = new GestureDetector(AudioLyric.this);
		LinearLayout audioLyricLayout = (LinearLayout)findViewById(R.id.audio_lyric_layout);
		audioLyricLayout.setOnTouchListener(this);
		audioLyricLayout.setLongClickable(true);
		//手势切换activity end
		
		//背景
		updateBackground();
		
		//获取屏幕的宽度像素
		DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthPixels = dm.widthPixels;
		//设置歌曲列表标题的宽度
        TextView audioListText = (TextView)findViewById(R.id.audiolisttext);
        LayoutParams laParaContent = audioListText.getLayoutParams();
		laParaContent.width = widthPixels/2;//占屏幕的一半
		audioListText.setLayoutParams(laParaContent);
		
		TextView audioLyricText = (TextView)findViewById(R.id.audio_lyric);
		//当前选中的标题高亮
//		audioLyricText.setBackgroundColor(Color.parseColor("#000000"));
//		audioLyricText.setTextColor(Color.parseColor("#000000"));
		ImageView lyricImageView = (ImageView)findViewById(R.id.select_lyric);
		LayoutParams laParaContentSelect = lyricImageView.getLayoutParams();
		laParaContentSelect.width = widthPixels/2;//占屏幕的一半
		lyricImageView.setLayoutParams(laParaContentSelect);//设置宽度
		
		ImageView listImageView = (ImageView)findViewById(R.id.select_list);
		LayoutParams laParaContentSelect2 = listImageView.getLayoutParams();
		laParaContentSelect2.width = widthPixels/2;//占屏幕的一半
		listImageView.setLayoutParams(laParaContentSelect2);//设置宽度
		
		lyricImageView.setBackgroundColor(Color.parseColor("#ffffff"));
		//设置标题  end
		
		audioListText.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AudioLyric.this, AudioList.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		
		showLyric();//显示歌词
		
		//去的歌曲界面传过来的参数
		Intent intent = getIntent();
		String filepath = intent.getStringExtra("filepath");
		if(filepath != null && !filepath.equals("noPlay")){
			Log.i(TAG, "Lyric:"+filepath);
			lyricPath = filepath.substring(0, filepath.lastIndexOf(".")) + ".lrc";
		}else{
//			lyricPath = "nolyeic.lrc";
			lyricPath = null;
		}
		
		Log.i(TAG, "Lyric onCreate");
	}
	
	/*
	 * 监听服务换歌广播
	 */
	private void regReceiver(){
		//创建一个广播监听
		receiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				String filepath = intent.getStringExtra("filepath");
				if(filepath != null){
					lyricPath = filepath.substring(0, filepath.lastIndexOf(".")) + ".lrc";
				}else{
					lyricPath = null;
				}
			}
			
		};
		//注册广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.xianyifa.AudioPlay.CONTROL");
		this.registerReceiver(receiver, intentFilter);
	}
	
	private void showLyric(){
		//与控制歌词与歌曲同步显示的线程通讯
		handler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				File f = new File(lyricPath);
				if(f.exists()){
					long milliSecond = Long.parseLong((String)msg.obj);
					String showLyric = "";
					if(lyricPath != null && !lyricPath.equals(oldLyricPath)){
						resolveLRC = new ResolveLRC(lyricPath, myPlayerService.getPlayLength());
						try {
							list.clear();
							list = resolveLRC.getLyricList();
						} catch (Exception e) {
							Log.e(TAG, e.toString());
						}
						oldLyricPath = lyricPath;
					}
					for(Sentence s : list){
						if(s.isInTime(milliSecond)){
							showLyric = s.getContent();
							break;
						}
					}
					
					TextView lyricText = (TextView)findViewById(R.id.show_lyric);
					lyricText.setText(showLyric);
				}
				super.handleMessage(msg);
			}
			
		};
		
		
	}
	
	/*
	 * 添加菜单 (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}

	/*
	 * 处理菜单动作 (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case EXITAUDIOPLAYER_ID:
			// 退出播放器
//			if(myPlayerService != null){//如果播放服务已经启动
//				myPlayerService.hideNotification();//把通知栏清楚
//			}
//			MyApplication.getInstance().exit(); //把管理器中的所有activity的退出 finish 完了会直接System.exit(0);//不用停止服务
//			Log.i(TAG, "bye bye!");
//			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		service = new Intent(this, PlayerService.class);// 通过意图取得播放服务
		// 驱动服务，1、，2、激活后回返回一个通道，Activity和service通讯是通过通道通讯的，服务通道是一个接口要实现
		// 3、常量1自动创建
		conn = new MyServiceConnection();
		this.bindService(service, conn, BIND_AUTO_CREATE);
		regReceiver();//注册广播监听
		Log.i(TAG, "Lyric onResume");
		
		if(MyApplication.getInstance().isReloadBackground()){
			updateBackground();
		}
		
		super.onResume();
	}

	@Override
	protected void onPause() {
		this.unbindService(conn);
		this.unregisterReceiver(receiver);//解除广播监听
		Log.i(TAG, "Lyric onPause");
		super.onPause();
	}



	/*
	 * 实现通道
	 */
	private final class MyServiceConnection implements ServiceConnection {

		/*
		 * 链接服务调用方法 service 为binder 通讯的桥梁 (non-Javadoc)
		 * 
		 * @see
		 * android.content.ServiceConnection#onServiceConnected(android.content
		 * .ComponentName, android.os.IBinder)
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// Binder binder = (Binder)service;
			myPlayerService = (MyPlayer) service;
			
//			setPlayerService((MyPlayer)service);
			myPlayerService.setHandler(handler);
			// 判断线程是否活动状态，并且音乐服务在播放  在这里就启动更新时间线程
			Log.i(TAG, "Lyric onServiceConnected");
		}

		/*
		 * 断开服务调用方法 (non-Javadoc)
		 * 
		 * @see
		 * android.content.ServiceConnection#onServiceDisconnected(android.content
		 * .ComponentName)
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
			myPlayerService = null;
			Log.i(TAG, "Lyric onServiceDisconnected");
		}

	}
	

	@Override
	protected void onDestroy() {
		Log.i(TAG, "Lyric onDestroy");
		MyApplication.getInstance().removeActivity(this);
		super.onDestroy();
	}

	@Override
	public MyPlayer getPlayerService() {
		// TODO Auto-generated method stub
		return myPlayerService;
	}
	
	/**
	 * 更新背景
	 */
	@Override
	public void updateBackground(){
		//设置背景图片 start
		config = getConfig();
		String bgPath = config.get("bg");
		try {
			Log.i(TAG, bgPath);
			InputStream assetFile = getAssets().open(bgPath);//以字节流读取
			BitmapDrawable bitmap = (BitmapDrawable)Drawable.createFromStream(assetFile, bgPath);
			LinearLayout audioLyricLayout = (LinearLayout)findViewById(R.id.audio_lyric_layout);//取得界面
			audioLyricLayout.setBackgroundDrawable(bitmap);
			MyApplication.getInstance().setReloadBackground(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, e.toString());
//			e.printStackTrace();
		}
		//设置背景图片 end
	}
	
}
