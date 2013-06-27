package com.xianyifa.audioplayer.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.xianyifa.audioplayer.impl.MyPlayer;
import com.xianyifa.audioplayer.lyric.ResolveLRC;
import com.xianyifa.audioplayer.lyric.Sentence;
import com.xianyifa.audioplayer.ui.AudioList;
import com.xianyifa.audioplayer.ui.R;

public class PlayerService extends Service {
	private final String TAG = "PlayerService";
    private MediaPlayer mediaPlayer;//实例化一个播放器;
    private String filepath = null;//文件绝对路径
    
    private int position;//播放的进度
    private long playLength;//正在播放音乐的长度
    private boolean isStop = false;
    private boolean controlPlayStop = false;//播放否控制
    private boolean controlLyricStop = false;//播放歌词否控制
    private boolean isPlayInit = false;//播放器是否初始化
    private List<HashMap<String, Object>> listViewData;// listView的数据 
    private ControlPlay controlPlay;//播放控制线程
    private ControlLyric controlLyric;//播放歌词控制线程
    private int listId = -1;
    private boolean isShowNotification = false;
    private Notification notification;//通知对象
    private CharSequence contentTitle;//通知栏标题
    private CharSequence contentText;//通知栏内容
    private PendingIntent contentIntent;//待定意图
    private BroadcastReceiver receiver;//
    private NotificationManager notificationManager;//通知管理
    private Handler handler;//和歌词activity通讯的Handler
    private Handler noticeHandler;//控制通知栏歌词显示
    private String showLyric = "";//当前的歌词句子
    private String lyricPath = "";//歌词路径
	private String oldLyricPath = "";//上一首歌词路径
	private ResolveLRC resolveLRC;//歌词解析器
    private static List<Sentence> list = new ArrayList<Sentence>();//里面装的是所有的句子
    private Binder binder = new MyBinder();//创建一个通讯，用于返回给调用，建立通讯桥梁,通讯都基于次桥梁
	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "PlayerService start");
		// 取得电话服务,实现电话进来的时候停止播放,挂断的继续
		TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// 注册监听，监听电话状态,并指定触发后执行的类,1、调用监听处理类方法，2、监听到的通讯状态(电话进入，接通电话，挂断电话)
		telManager.listen(new TelListenr(),
				PhoneStateListener.LISTEN_CALL_STATE);
		mediaPlayer = new MediaPlayer();//实例化一个播放器;
		controlPlay = new ControlPlay();//播放控制线程
		controlLyric = new ControlLyric();//播放歌词控制线程
		// 创建一个NotificationManager的引用,通知管理器
		notificationManager = (NotificationManager)
            PlayerService.this.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
       
		//创建一个广播接收机
		receiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(TAG, "service start play next song");
				//只有接收到界面发出的播放广播才开始播放，保证界面时间显示和播放同步
				MyPlayer myPlayer = (MyPlayer)binder;
				try {
					myPlayer.play(filepath, 0,listId);
				} catch (IOException e) {
					Log.e(TAG, e.toString());
				}
				
			}
		};
		//注册广播
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.xianyifa.AudioPlayService.CONTROL");
		this.registerReceiver(receiver, intentFilter);
		
		//通知栏歌词控制
		noticeHandler = new Handler(){

			@Override
			public void handleMessage(Message msg) {
				long milliSecond = Long.parseLong((String)msg.obj);
				
				if(!lyricPath.equals(oldLyricPath)){
					resolveLRC = new ResolveLRC(lyricPath, playLength);
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
					
					if(isShowNotification){
						MyPlayer myPlayer = (MyPlayer)binder;
						myPlayer.showNotification();
						isShowNotification = true;
					}
					
				super.handleMessage(msg);
			}
			
		};
		
		return binder;
	}
	
	
	
	public void setFilePath(String filepath){
		this.filepath = filepath;
	}
	
	public void setPosition(int position){
		this.position = position;
	}
	
	
	/*
	 * 为Binder添加业务方法，只有在这里面才能通过Binder建立的通道进行调用
	 */
	private final class MyBinder extends Binder implements MyPlayer{
		
		@Override
		public void setFilePath(String filepath){
			PlayerService.this.setFilePath(filepath);
		}
		
		@Override
		public void setListViewData(List<HashMap<String, Object>> listViewData){
			PlayerService.this.listViewData = listViewData;
		}
		
		@Override
		public void setHandler(Handler handler){
			if(!PlayerService.this.controlLyric.isAlive() && handler != null && filepath != null){
				//这里重新new  为了防止线程停止过,二次start出现Thread already started.异常
				controlLyric = new ControlLyric();
				controlLyric.start();
	    	}
			PlayerService.this.handler = handler;
		}

		
		@Override
		public void play(String filepath,int position,int id) throws IOException{
			setFilePath(filepath);//在服务保存当前MP3路径
			setPosition(position);//在服务保存当前MP3路径
			listId = id;
			PlayerService.this.isStop = false;
			File file = new File(filepath);
			
	    	mediaPlayer.reset();//把之前的设置都重置一下
	    	mediaPlayer.setDataSource(file.getAbsolutePath());//设置音乐文件路径
	    	mediaPlayer.prepare();//缓存一定要调用,初始化
	    	
	    	playLength = mediaPlayer.getDuration();
	    	
	    	//发出换播歌曲广播，已经准备好要播放，界面初始化下一首歌界面
			Intent intent = new Intent("com.xianyifa.AudioPlay.CONTROL");
			intent.putExtra("listId", listId);
			intent.putExtra("filepath", filepath);
			PlayerService.this.sendBroadcast(intent);
	    	
			//取得歌词路径，目前只能在同目录，同名
			lyricPath = filepath.substring(0, filepath.lastIndexOf(".")) + ".lrc";
			File f = new File(lyricPath);
	    	mediaPlayer.start();
	    	
	    	mediaPlayer.seekTo(position);
	    	isPlayInit = true;
	    	if(!PlayerService.this.controlPlay.isAlive() && f.exists()){//只有存在歌词
	    		controlPlay.start();
	    	}else{
	    		controlLyricStop = true;//如果不存在歌词就结束线程
	    	}
		}
		
		@Override
		public boolean pause(){
			if(mediaPlayer.isPlaying()){//如果是在播放
				mediaPlayer.pause();
				return true;
			}else{
				//应为如果按了停止直接使用start 继续叫报错
				if(!PlayerService.this.isStop){
					mediaPlayer.start();
				}
				return false;
			}
		}
		
		@Override
		public void reset()throws IOException{
			if(mediaPlayer.isPlaying()){
				mediaPlayer.seekTo(0);
			}else if(PlayerService.this.filepath != null){//确保用户先点击过播放
				play(PlayerService.this.filepath,0,listId);
			}
		}
		
		@Override
		public void stop(){
			if(mediaPlayer.isPlaying()){
				mediaPlayer.stop();
				PlayerService.this.isStop = true;
			}
		}
		
		@Override
		public int getPosition(){
			return mediaPlayer.getCurrentPosition();
		}
		
		@Override
		public String getFilePath(){
			return PlayerService.this.filepath;
		}
		
		@Override
		public MediaPlayer getMediaPlayer(){
			return mediaPlayer;
		}
		
		@Override
		public int getListId(){
			return listId;
		}
		
		@Override
		public boolean getIsPlayInit(){
			return isPlayInit;
		}
		
		@Override
		public long getPlayLength(){
			return playLength;
		}
		
		@Override
		public boolean isShowNotification(){
			return isShowNotification;
		}
		
		/*
		 * 显示通知栏图标,当界面不可见的时候调用
		 * (non-Javadoc)
		 * @see com.xianyifa.audioplayer.impl.MyPlayer#showNotification()
		 */
		@Override
		public void showNotification(){
			
	        // 定义Notification的各种属性
	        notification =new Notification(R.drawable.icon,
	        		(filepath != null) ? filepath.substring(filepath.lastIndexOf("/")+1, filepath.length()) : getString(R.string.no_music_playing),
	        				System.currentTimeMillis());
	        notification.flags |= Notification.FLAG_ONGOING_EVENT; // 将此通知放到通知栏的"Ongoing"即"正在运行"组中
	        notification.flags |= Notification.FLAG_NO_CLEAR; // 表明在点击了通知栏中的"清除通知"后，此通知不清除，经常与FLAG_ONGOING_EVENT一起使用
	        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
	        notification.defaults = Notification.DEFAULT_LIGHTS;
	        notification.ledARGB = Color.BLUE;
	        notification.ledOnMS =5000;
	               
	        // 设置通知的事件消息
	        contentTitle = getString(R.string.now_playing); // 通知栏标题
	        contentText = (showLyric != null) 
	        		? showLyric : getString(R.string.no_music_playing); // 通知栏内容
//	        		CharSequence contentText = "无音乐播放"; // 通知栏内容
	        Intent notificationIntent = new Intent(PlayerService.this, AudioList.class); // 点击该通知后要跳转的Activity
	        //添加这里可以解决当按home键停止activity在冲通知进入时出现多个activity对象
	        //也就是再按返回是跳到另一个还是这个界面的activity
	        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
	        contentIntent = PendingIntent.getActivity(PlayerService.this, 0,
	                notificationIntent, 0);
	        notification.setLatestEventInfo(PlayerService.this, contentTitle, contentText,
	        		contentIntent);
	        
	        
	        // 把Notification传递给NotificationManager
	        notificationManager.notify(0, notification);
	        isShowNotification = true;
		}
		
		/*
		 * 删除通知栏的图标
		 * (non-Javadoc)
		 * @see com.xianyifa.audioplayer.impl.MyPlayer#hideNotification()
		 */
		@Override
		public void hideNotification(){
			// 启动后删除之前我们定义的通知
				notificationManager.cancel(0);
				isShowNotification = false;
		}
		
		/*
		 * 服务对象
		 */
		@Override
		public Service getPlayerService(){
			return PlayerService.this;
		}
		
	}
	
	/*
	 * 实现电话监听类
	 */
	private class TelListenr extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			try {
				switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:// 挂断
					PlayerService.this.mediaPlayer.start();
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:// 接通电话
					
					break;

				case TelephonyManager.CALL_STATE_RINGING:// 电话进入
					if(PlayerService.this.mediaPlayer.isPlaying()){//如果是在播放
						PlayerService.this.mediaPlayer.pause();//暂停
					}
					break;
				default:
					break;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.i(TAG, e.toString());
			}
			super.onCallStateChanged(state, incomingNumber);
		}

	}

	/*
	 * 播放控制线程,主要换个
	 */
	public class ControlPlay extends Thread {

		@Override
		public void run() {
				while (!controlPlayStop) {
					try {
						Thread.sleep(800);
					} catch (InterruptedException e) {
						Log.e(TAG, e.toString());
					}
					long milliSecond = mediaPlayer.getCurrentPosition();
					if((playLength - milliSecond) <= 1100){
						//第一次换歌没有初始化，先找到当前在listView的索引以后往上加就可以知道列表末尾
						if(listId == -1){
							listId = getListId(filepath);
						}
						if(listId < (listViewData.size() - 1)){
							listId += 1;
						}else{
							listId = 0;
						}
						filepath = getFilePath(listId);
						Log.i(TAG, listId+"------"+filepath+"----"+listViewData.size());
						MyPlayer myPlayer = (MyBinder)binder;
						
						try {
							Log.i(TAG, "service waiting 2 Second play next song");
							Thread.sleep(2000);
							myPlayer.play(filepath, 0,listId);
						} catch (Exception e) {
							Log.e(TAG, e.toString());
						}
						//判断通知是不是显示，做出对通知信息的更改
						if(isShowNotification){
							myPlayer.showNotification();
						}
					}
				}
		}

	}
	
	/*
	 * 播放歌曲更新控制线程,只有播放器存在才会启动(同步界面的歌词同步)
	 */
	public class ControlLyric extends Thread {

		@Override
		public void run() {
			Log.i(TAG, "ControlLyric start");
			// 判断歌曲是否还在播放
				while (!controlLyricStop) {
					long milliSecond = mediaPlayer.getCurrentPosition();
					Message message = Message.obtain();// 用于和住线程通讯的消息容器,每次通讯都要创建
					message.obj = milliSecond+"";
					handler.sendMessage(message);
					//判断通知是不是显示，做出对通知信息的更改
					if(isShowNotification){
						Message message1 = Message.obtain();// 用于和住线程通讯的消息容器,每次通讯都要创建
						message1.obj = milliSecond+"";
						noticeHandler.sendMessage(message1);
					}
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						Log.e(TAG, e.toString());
					}
						
				}
				Log.i(TAG, "ControlLyric stop");
		}

	}
	
	
	/*
	 * data list绑定的数据
	 * filepath 正在播放歌曲的路径
	 * filename 正在播放歌曲名称
	 * return i 返回正在播放的歌曲在listView绑定数据的索引
	 */
	private int getListId(String filepath){
		int i = 0;
		for(HashMap<String,Object> audio : listViewData){
			if(audio.get("filepath").equals(filepath)){
				break;
			}
			i++;
		}
		return i;
	}
	/*
	 * index 歌曲在listView绑定数据的索引
	 * return 返回歌曲的绝对路径
	 */
	private String getFilePath(int index){
		HashMap<String,Object> audio = listViewData.get(index);
		return audio.get("filepath").toString();
	}
	
	@Override
	public void onDestroy() {
		controlPlayStop = true;//控制播放线程也结束
		controlLyricStop = true;//控制播放线程也结束
		//等待播放控制线程结束才停止播放服务
		while(controlPlay.isAlive()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Log.e(TAG, e.toString());
			}
			Log.i(TAG, "wait controlPlay stop");
		}
		//播放控制线程停止后在停止播放器
		if(mediaPlayer != null){
			MyPlayer myPlayer = (MyBinder)binder;
			myPlayer.hideNotification();//服务结束的时候一定要清楚通知栏
			mediaPlayer.stop();//一定要在这里停止，要不然服务停止了播放器还是会继续播放
			mediaPlayer.release();//释放资源
		}
		this.unregisterReceiver(receiver);//解除监听注册
		Log.i(TAG, "PlayerService onDestroy");
		super.onDestroy();
	}

	
}
