package com.xianyifa.audioplayer.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.xianyifa.audioplayer.db.AudioDao;
import com.xianyifa.audioplayer.impl.MyPlayer;
import com.xianyifa.audioplayer.service.PlayerService;
import com.xianyifa.audioplayer.util.MyApplication;
import com.xianyifa.audioplayer.util.Toolbox;

public class AudioList extends MyActivity {
	private final String TAG = "AudioList";
	private String filepath = null;//当前播放音乐文件绝对路径
	private String longClickFilePath;//长按的音乐文件绝对路径
	private Integer longClickFileId;//长按的音乐文件在数据库中的id
	private int position = 0;//当前播放位置
	protected MyPlayer myPlayerService;//播放服务对象
	private MyServiceConnection conn;
	private Intent service;// 音乐播放服务意图
	private ListView listView;
	private List<HashMap<String, Object>> data;// listView的数据
	private long audioLength;// 播放音乐长度
	private SimpleAdapter adapter;// ListView适配器
	private int listId = -1;// 当前播放的音乐在listView的索引
	private int onListId = -1;// 上一首播放的音乐在listView的索引
	private MediaPlayer mediaPlayer;// 服务的播放器
	private Handler handler;// 用于主线程和子线程的通讯
	private ControlPlayTime controlPlayTime;// 音乐时间更新控制线程
	private boolean controlPlayStop = false;//控制音乐时间更新控制线程结束
	private boolean isStop = false;// 标识播放器是否暂停
	private boolean isPause = false;// 标识activity是否是在暂停恢复
	private boolean isExit = false;//当用户点击退出按钮为true
	private boolean isAudioLengthShow = false;//判断界面的歌曲长度是否更新在UI了
	private int widthPixels;//设备屏幕宽度像素
	private BroadcastReceiver receiver;//广播监听
//	private final int ADDAUDIOPLAYER_ID = Menu.FIRST;
//	private final int DELAUDIOPLAYER_ID = Menu.FIRST + 1;
	private final int EXITAUDIOPLAYER_ID = Menu.FIRST + 2;
	private final int DELETE_DIALOG = 1;
	private final int PAGE_SIZE = 100;//默认开始读取多少行
	private ImageView progressBar;//音乐播放进度条控件 
	private HashMap<String, String> config;//程序配置参数

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audiolist);
		MyApplication.getInstance().addActivity(this); //把当前activity放到activity管理器
		showListView();
		//取得系统参数
		config = getConfig();
		//手势切换activity start
		gestureDetector = new GestureDetector(AudioList.this);
		ListView audiolist = (ListView)findViewById(R.id.audiolist);
		audiolist.setOnTouchListener(this);
		audiolist.setLongClickable(true);
		//手势切换activity end
		//设置背景图片 start
		String bgPath = config.get("bg");
		try {
			Log.i(TAG, bgPath);
			InputStream assetFile = getAssets().open(bgPath);//以字节流读取
			BitmapDrawable bitmap = (BitmapDrawable)Drawable.createFromStream(assetFile, bgPath);
			LinearLayout audioListView = (LinearLayout)findViewById(R.id.audioListView);//取得界面
			audioListView.setBackgroundDrawable(bitmap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, e.toString());
			e.printStackTrace();
		}
		//设置背景图片 end
		
		//获取屏幕的宽度像素
		DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthPixels = dm.widthPixels;
		
        //设置歌曲列表标题的宽度
        TextView audioListText = (TextView)findViewById(R.id.audiolisttext);
        LayoutParams laParaContent = audioListText.getLayoutParams();
		laParaContent.width = widthPixels/2;//占屏幕的一半
		audioListText.setLayoutParams(laParaContent);
		//当前选中的标题高亮
//		audioListText.setTextColor(Color.parseColor("#000000"));
		ImageView listImageView = (ImageView)findViewById(R.id.select_list);
		LayoutParams laParaContentSelect = listImageView.getLayoutParams();
		laParaContentSelect.width = widthPixels/2;//占屏幕的一半
		listImageView.setLayoutParams(laParaContentSelect);//设置宽度

		ImageView lyricImageView = (ImageView)findViewById(R.id.select_lyric);
		LayoutParams laParaContentSelect2 = lyricImageView.getLayoutParams();
		laParaContentSelect2.width = widthPixels/2;//占屏幕的一半
		lyricImageView.setLayoutParams(laParaContentSelect2);//设置宽度
		
		listImageView.setBackgroundColor(Color.parseColor("#ffffff"));
		//设置标题 end
		
		progressBar = (ImageView)findViewById(R.id.progress_bar);//取得进度条在底部按钮处显示
        //为未选中的注册点击事件
		TextView audioLyricText = (TextView)findViewById(R.id.audio_lyric);
		audioLyricText.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AudioList.this, AudioLyric.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				if(filepath == null){
					filepath = "noPlay";
				}
				Log.i(TAG, filepath);
				intent.putExtra("filepath", filepath);
				startActivity(intent);
			}
		});
		
		
		/** 为底部按钮增加事件  start */
		ImageButton preSong = (ImageButton)findViewById(R.id.bottom_pre_song);
		ImageButton nextSong = (ImageButton)findViewById(R.id.bottom_next_song);
		ImageButton pauseSong = (ImageButton)findViewById(R.id.bottom_pause_song);
		
		pauseSong.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(myPlayerService != null){
					MediaPlayer mediaPlayer = myPlayerService.getMediaPlayer();
					if(mediaPlayer != null){
						if(mediaPlayer.isPlaying()){
							myPlayerService.pause();
							v.setBackgroundResource(R.drawable.but_icon_play_song);
						}else{
							if(filepath != null && !filepath.equals("")){
								AudioList.this.position = myPlayerService.getPosition();
							}else{
								//播放第一条或上次的继续播放
								HashMap<String, Object> item = (HashMap<String, Object>)listView.getItemAtPosition(0);
								filepath = item.get("filepath").toString();
								listId = 0;
								AudioList.this.position = 0;
							}
							Log.i(TAG, filepath+"|"+AudioList.this.position);
								try {
									myPlayerService.play(filepath, AudioList.this.position, listId);
									v.setBackgroundResource(R.drawable.but_icon_pause_song);
									// 判断线程是否活动状态
									if (!controlPlayTime.isAlive()) {
										audioLength = mediaPlayer.getDuration();//第一次还是要在这里取一次，防止广播接收到更改为它赋值慢于此线程启动导致audioLength为0
										controlPlayTime.start();// 第一次执行播放开始线程
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							
						}
					}
				}else{
					//播放服务没有启动
				}
			}
		});
		
		/** 上一曲 */
		preSong.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//只有在播放状态上一曲按钮才有效
				MediaPlayer mediaPlayer = myPlayerService.getMediaPlayer();
				Log.i(TAG, "prev song");
				if(mediaPlayer.isPlaying()){
					HashMap<String, Object> item = (HashMap<String, Object>)listView.getItemAtPosition(listId-1);
					listId = listId-1;
					if(item == null){
						item = (HashMap<String, Object>)listView.getItemAtPosition(listView.getCount()-1);
						listId = listView.getCount()-1;
					}
					filepath = item.get("filepath").toString();
					
					AudioList.this.position = 0;
					try {
						myPlayerService.play(filepath, AudioList.this.position, listId);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ImageButton pauseSong = (ImageButton)findViewById(R.id.bottom_pause_song);
					pauseSong.setBackgroundResource(R.drawable.but_icon_pause_song);
				}
			}
		});
		
		/** 下一曲 */
		nextSong.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//只有在播放状态上一曲按钮才有效
				MediaPlayer mediaPlayer = myPlayerService.getMediaPlayer();
				Log.i(TAG, "next song");
				if(mediaPlayer.isPlaying()){
					HashMap<String, Object> item;
					if((listId+1) < listView.getCount()){
						item = (HashMap<String, Object>)listView.getItemAtPosition(listId+1);
						listId = listId+1;
					}else{
						item = (HashMap<String, Object>)listView.getItemAtPosition(0);
						listId = 0;
					}
					filepath = item.get("filepath").toString();
					
					AudioList.this.position = 0;
					try {
						myPlayerService.play(filepath, AudioList.this.position, listId);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ImageButton pauseSong = (ImageButton)findViewById(R.id.bottom_pause_song);
					pauseSong.setBackgroundResource(R.drawable.but_icon_pause_song);
				}
			}
		});
		/** 为底部按钮增加时间   end */
		
		Log.i(TAG, "onCreate");
	}
	
	/*
	 * 监听服务换歌广播
	 */
	private void regReceiver(){
		//创建一个广播监听
		receiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				listId = intent.getIntExtra("listId", listId);
				// 当是服务换歌是在这里更新UI显示
				HashMap<String, Object> playItem = (HashMap<String, Object>) listView.getItemAtPosition(listId);
				if(onListId != -1){
					HashMap<String, Object> item = (HashMap<String, Object>) listView.getItemAtPosition(onListId);
					item.put("audioTime", "");
					item.put("playTime", "");
				}
				audioLength = myPlayerService.getPlayLength();
				String time = Toolbox.lengthTime(audioLength);
				playItem.put("audioTime", "/" + time);
				playItem.put("playTime", "0:00");
				onListId = listId;//不要忘了这部要不然时间不会跳动
				adapter.notifyDataSetChanged();
			}
			
		};
		
		//注册广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.xianyifa.AudioPlay.CONTROL");//要拦截的动作
		this.registerReceiver(receiver, intentFilter);
	}
	
	/*
	 * 把数据绑定listview，并在界面显示
	 */
	private void showListView(){
		Log.i(TAG, "showListView");
		data = new ArrayList<HashMap<String, Object>>();
//		data = Toolbox.showCatalog(file);
		AudioDao ad = new AudioDao(AudioList.this);
		data = ad.getAudioFile(1, PAGE_SIZE);
		// 取得listview
		listView = (ListView) findViewById(R.id.audiolist);
		adapter = new MyAdapter(AudioList.this, data,
				R.layout.audiolistitem, new String[] {"id", "filename", "filepath",
						"playTime", "audioTime" }, new int[] {R.id.id, R.id.audioname,
						R.id.audiopath, R.id.audioplaytime, R.id.audiotime });
		listView.setAdapter(adapter);

		// 创建播放控制线程
		controlPlayTime = new ControlPlayTime();// 取得播放时间控制线程
		
		// 创建线程通讯监听
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				String message = (String) msg.obj;
//				//如果message是数字,就是发过来的歌曲长度，不是则是转换后的播放时间点
				if(message.matches("[0-9]+")){
					//这是服务在播放，用户重新回到activity界面时更新UI显示当前播放歌曲信息
					HashMap<String, Object> item = (HashMap<String, Object>) listView.getItemAtPosition(listId);
					item.put("audioTime", "/"+Toolbox.lengthTime(Long.parseLong(message)));
				}else{
						HashMap<String, Object> item = (HashMap<String, Object>) listView.getItemAtPosition(listId);
						item.put("playTime", message);
				}
				//根据通知不停更新进度条
				progressBar.setBackgroundResource(R.drawable.slide_img_handle);
				//修改进度条长度
				LayoutParams laParaContent = progressBar.getLayoutParams();
				laParaContent.width = getprogressBarSize();
				progressBar.setLayoutParams(laParaContent);
				
				adapter.notifyDataSetChanged();
				super.handleMessage(msg);
			}
		};
		

		// 为listView注册单击事件
		listView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView v = (ListView) parent;
				HashMap<String, Object> item = (HashMap<String, Object>) v.getItemAtPosition(position);
				// 上个播放的文件在ListView的位置
//				HashMap<String, Object> Befitem = null;
//				if (listId > -1) {
//					Befitem = (HashMap<String, Object>) v.getItemAtPosition(listId);
//				}
//				
				listId = position;// 保存listView索引
//				onListId = listId;
				
				filepath = item.get("filepath").toString();// 取得音乐路径
				File file = new File(filepath);
				if(!file.isFile()){
					item = (HashMap<String, Object>) v.getItemAtPosition(position+1);
					if(item == null){
						myPlayerService.pause();// 暂停它
					}else{
						listId = position + 1;
						filepath = item.get("filepath").toString();// 取得音乐路径
					}
					//把数据库相应的记录删除
					AudioDao ad = new AudioDao(AudioList.this);
					ad.delete(Integer.parseInt(item.get("id").toString()));
					refresh();
				}
				String playerFileName = myPlayerService.getFilePath();
				
				if (mediaPlayer.isPlaying()) {// 如果正在播放
					if (playerFileName.equals(filepath)) {// 而且请求的路径和现在播放的路径一样
						myPlayerService.pause();// 暂停它
						isStop = true;
					} else {
						try {
							myPlayerService.play(filepath,
									AudioList.this.position,listId);
						} catch (IOException e) {
							Log.i(TAG, e.toString());
						}
					}
				} else {// 如果不是在播放
					if (isStop && playerFileName.equals(filepath)) {// 判断是不是停止状态并且请求播放的是同一个文件
						myPlayerService.pause();//这里的情况基本不会出现,这里应该是播放
					} else {// 不是暂停状态的调用播放,或者是暂停但是请求的不是同一个音乐文件
						try {
							myPlayerService.play(filepath,
									AudioList.this.position,listId);

							// 判断线程是否活动状态
							if (!controlPlayTime.isAlive()) {
								audioLength = mediaPlayer.getDuration();//第一次还是要在这里取一次，防止广播接收到更改为它赋值慢于此线程启动导致audioLength为0
								controlPlayTime.start();// 第一次执行播放开始线程
							}
							
						} catch (IOException e) {
							Log.i(TAG, e.toString());
						}
					}
				}
			}

		});
		
		//为listview创建上文菜单
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				//设置图标
				menu.setHeaderIcon(R.drawable.content_menu_ico);
				//设置标题
				menu.setHeaderTitle(R.string.choice_action);
				//设置菜单
				//播放
				menu.add(R.string.player).setOnMenuItemClickListener(new OnMenuItemClickListener() {
					
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						// TODO Auto-generated method stub
						return true;
					}
				});
				//删除
				menu.add(R.string.delete).setOnMenuItemClickListener(new OnMenuItemClickListener() {
					
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						showDialog(DELETE_DIALOG);//显示提示框
						return true;
					}
				});
				
			}
		});
		
		//为ListView创建一个item长按监听
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView v = (ListView)parent;
				HashMap<String, Object> item = (HashMap<String, Object>)v.getItemAtPosition(position);
				longClickFilePath = item.get("filepath").toString();
				longClickFileId = Integer.parseInt(item.get("id").toString());
				
				listView.showContextMenu();
				return true;
			}
			
		});
		
	}
	

	/**
	 * 刷新列表方法
	 * @param param
	 */
	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		data = new ArrayList<HashMap<String, Object>>();
		AudioDao ad = new AudioDao(AudioList.this);
		data = ad.getAudioFile(1, PAGE_SIZE);
		// 取得listview
		listView = (ListView) findViewById(R.id.audiolist);
		adapter = new MyAdapter(AudioList.this, data,
				R.layout.audiolistitem, new String[] {"id", "filename", "filepath",
						"playTime", "audioTime" }, new int[] {R.id.id, R.id.audioname,
						R.id.audiopath, R.id.audioplaytime, R.id.audiotime });
		Log.i(TAG, "refresh AudioList");
		listView.setAdapter(adapter);
	}
	
	
	private class MyAdapter extends SimpleAdapter{

		
		public MyAdapter(Context context, List<? extends Map<String, ?>> data,
				int resource, String[] from, int[] to) {
			super(context, data, resource, from, to);
			// TODO Auto-generated constructor stub
		}
		/*
		 * 每次加载listView都会调用
		 * (non-Javadoc)
		 * @see android.widget.SimpleAdapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(AudioList.this.getApplicationContext()).inflate(R.layout.audiolistitem, null);
			TextView fileNameText = (TextView)convertView.findViewById(R.id.audioname);
			TextView filePathText = (TextView)convertView.findViewById(R.id.audiopath);
			TextView playTimeText = (TextView)convertView.findViewById(R.id.audioplaytime);
			TextView fileTimeText = (TextView)convertView.findViewById(R.id.audiotime);
			TextView playIcon = (TextView)convertView.findViewById(R.id.list_play_icon);
//			TextView progressBarText = (TextView)convertView.findViewById(R.id.progress_bar);
			
			//比重新给值将显示空白?位置原因:getView就是把每天数据绑定到界面的过程，所以在这里要赋值
			fileNameText.setText(((HashMap<String, Object>)listView.getItemAtPosition(position)).get("filename").toString());
			filePathText.setText(((HashMap<String, Object>)listView.getItemAtPosition(position)).get("filepath").toString());
			playTimeText.setText(((HashMap<String, Object>)listView.getItemAtPosition(position)).get("playTime").toString());
			fileTimeText.setText(((HashMap<String, Object>)listView.getItemAtPosition(position)).get("audioTime").toString());
			if(listId == position){
				//他要求传int  但不能传颜色的十进制代码
				fileNameText.setTextColor(Color.parseColor("#3197FF"));
				playTimeText.setTextColor(Color.parseColor("#3197FF"));
				fileTimeText.setTextColor(Color.parseColor("#3197FF"));
				
				//设置播放图标
				playIcon.setBackgroundResource(R.drawable.list_pause_state);
//				LayoutParams playIconParams = (LayoutParams)playIcon.getLayoutParams();
//				playIconParams.FILL_PARENT = 
//				playIcon.setLayoutParams(params);
				
//				progressBarText.setBackgroundColor(Color.parseColor("#000000"));
//				progressBar.setBackgroundResource(R.drawable.slide_img_handle);
				//修改进度条长度
//				LayoutParams laParaContent = (LayoutParams)progressBar.getLayoutParams();
//				laParaContent.width = getprogressBarSize();
//				progressBar.setLayoutParams(laParaContent);
//				progressBarText.setWidth(0);//用这个更改不了
				//更改文件名的TextView宽度防止太长而影响时间显示的位置
//				LayoutParams laParaContent1 = (LayoutParams)fileNameText.getLayoutParams();
//				laParaContent1.width = (int)(widthPixels * 0.6);
//				fileNameText.setLayoutParams(laParaContent1);
			}else{
				fileNameText.setTextColor(Color.parseColor("#000000"));
			}
			return convertView;
		}
		
	}
	
	/*
	 * 计算进度条的尺寸
	 */
	private int getprogressBarSize(){
		double proportion = (double)mediaPlayer.getCurrentPosition()/(double)myPlayerService.getPlayLength();
		int px = (int)(widthPixels * proportion);
		return px;
	}
	
	/*
	 * 因为系统内存不足被摧毁 (non-Javadoc)
	 * 
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		this.position = savedInstanceState.getInt("position");
		this.filepath = savedInstanceState.getString("filepath");
		Log.i(TAG, "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
	}

	/*
	 * 因为系统内存不足被摧毁 (non-Javadoc)
	 * 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if(myPlayerService != null){
			outState.putInt("position", myPlayerService.getPosition());
			outState.putString("filepath", myPlayerService.getFilePath());
		}
		Log.i(TAG, "onSaveInstanceState");
		super.onSaveInstanceState(outState);
	}

	/*
	 * 暂停了Activity (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		controlPlayStop = true;//控制播放线程也结束
		if (myPlayerService != null && !isExit) {
			myPlayerService.showNotification();
			isPause = true;
		}else{
			Log.i(TAG, "wait controlPlayTmie stop");
			//等待播放控制线程结束才停止播放服务,是执行退出的时候才有必要等他先停止
			while(controlPlayTime.isAlive()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, e.toString());
				}
			}
		}
		
		unbindService(conn);//解除服务绑定
		this.unregisterReceiver(receiver);//解除监听广播
		Log.i(TAG, "onPause");
		super.onPause();
	}

	/*
	 * 重新唤起,或刚开启都会调用 (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		//只有在暂停恢复过来才会执行
		if (isPause && (myPlayerService != null)) {
			myPlayerService.hideNotification();
			listId = myPlayerService.getListId();//唤醒的时候也重新在服务取得当前的播放ListView索引，当在服务人为的播放下一首的时候也能更新
			isPause = false;
		}
		
		
		//1.1改用在onResume注册绑定播放服务
		service = new Intent(this, PlayerService.class);// 通过意图取得播放服务
		// 驱动服务，1、，2、激活后回返回一个通道，Activity和service通讯是通过通道通讯的，服务通道是一个接口要实现
		// 3、常量1自动创建
		conn = new MyServiceConnection();
		
		this.startService(service);// 先使用创建服务在绑定
		this.bindService(service, conn, BIND_AUTO_CREATE);
		
		//注册广播监听
		regReceiver();
		
		Log.i(TAG, "onResume");
		super.onResume();
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
//			MyApplication.getInstance().exit(); //把管理器中的所有activity的退出 finish
//			this.stopService(service);
//			Log.i(TAG, "bye bye!");
//			return true;
		}
		return super.onOptionsItemSelected(item);
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
			Log.i(TAG, "onServiceConnected");
			myPlayerService = (MyPlayer) service;
//			setPlayerService((MyPlayer)service);
			myPlayerService.setListViewData(data);
			listId = myPlayerService.getListId();
			onListId = listId;//当前播放歌曲的listView数据索引
			filepath = myPlayerService.getFilePath();
			
			mediaPlayer = myPlayerService.getMediaPlayer();// 取得服务中的播放器
			// 判断线程是否活动状态，并且音乐服务在播放  在这里就启动更新时间线程
			if (myPlayerService.getIsPlayInit()) {
				audioLength = myPlayerService.getPlayLength();//当前播放歌曲的长度
				if(!controlPlayTime.isAlive()){
					// 取得播放时间控制线程，不在这里重新获得新对象开始线程会无法唤醒，未知原因
					controlPlayTime = new ControlPlayTime();
					controlPlayStop = false;
					controlPlayTime.start();// 第一次执行播放开始线程
				}
			}
			//对通知进行处理
			if(myPlayerService.isShowNotification()){
				myPlayerService.hideNotification();
			}
			
			Log.i(TAG, "onServiceConnected");
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
			Log.i(TAG, "onServiceDisconnected");
		}

	}

	/*
	 * 播放时间更新控制线程,只有播放器存在才会启动
	 */
	public class ControlPlayTime extends Thread {

		@Override
		public void run() {
			Log.i(TAG, "ControlPlayTime start");
			//线程刚启动就发给handler让他更新UI播放的音乐总长度
			Message message1 = Message.obtain();
			message1.obj = audioLength+"";//audioLength是long行要转换字符串传递
			handler.sendMessage(message1);
			// 判断歌曲是否还在播放
				while (!controlPlayStop) {
					long milliSecond = mediaPlayer.getCurrentPosition();
					String time = Toolbox.formatTime(milliSecond);
					Message message = Message.obtain();// 用于和住线程通讯的消息容器,每次通讯都要创建
					message.obj = time;
					handler.sendMessage(message);

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Log.e(TAG, e.toString());
					}
						
				}
				Log.i(TAG, "ControlPlayTime stop");
		}

	}
	
	/*
	 * 创建弹出确认窗口
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int, android.os.Bundle)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DELETE_DIALOG:
			return new AlertDialog.Builder(AudioList.this)
			.setTitle(R.string.prompt).setMessage(getString(R.string.verify_del)
					+"\""+ longClickFilePath.substring(longClickFilePath.lastIndexOf("/")+1, longClickFilePath.length())+"\"?")
			.setPositiveButton(R.string.verify, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//确定执行操作
//					Toast toast = Toast.makeText(AudioList.this,
//							"要删除的是："+longClickFileId, Toast.LENGTH_LONG);
//					toast.setGravity(Gravity.CENTER, 0, 0);
//					toast.show();
					AudioDao ad = new AudioDao(AudioList.this);
					ad.delete(longClickFileId);
					ad.closeDB();
					refresh();
					removeDialog(DELETE_DIALOG);//吧创建的弹出删除，不删除下次创建还是同一个对象，导致消息内容不变
				}
			}).setNegativeButton(R.string.cancel, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					removeDialog(DELETE_DIALOG);
				}
			}).create();

		default:
			return null;
		}
	}
	
	@Override
	protected void onDestroy() {
//		unbindService(conn);
//		this.stopService(service);
		Log.i(TAG, "AudioList onDestroy");
		MyApplication.getInstance().removeActivity(this);
		super.onDestroy();
	}

	@Override
	public MyPlayer getPlayerService() {
		// TODO Auto-generated method stub
		return myPlayerService;
	}	
	

}