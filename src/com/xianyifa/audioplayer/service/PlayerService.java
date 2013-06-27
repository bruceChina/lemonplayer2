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
    private MediaPlayer mediaPlayer;//ʵ����һ��������;
    private String filepath = null;//�ļ�����·��
    
    private int position;//���ŵĽ���
    private long playLength;//���ڲ������ֵĳ���
    private boolean isStop = false;
    private boolean controlPlayStop = false;//���ŷ����
    private boolean controlLyricStop = false;//���Ÿ�ʷ����
    private boolean isPlayInit = false;//�������Ƿ��ʼ��
    private List<HashMap<String, Object>> listViewData;// listView������ 
    private ControlPlay controlPlay;//���ſ����߳�
    private ControlLyric controlLyric;//���Ÿ�ʿ����߳�
    private int listId = -1;
    private boolean isShowNotification = false;
    private Notification notification;//֪ͨ����
    private CharSequence contentTitle;//֪ͨ������
    private CharSequence contentText;//֪ͨ������
    private PendingIntent contentIntent;//������ͼ
    private BroadcastReceiver receiver;//
    private NotificationManager notificationManager;//֪ͨ����
    private Handler handler;//�͸��activityͨѶ��Handler
    private Handler noticeHandler;//����֪ͨ�������ʾ
    private String showLyric = "";//��ǰ�ĸ�ʾ���
    private String lyricPath = "";//���·��
	private String oldLyricPath = "";//��һ�׸��·��
	private ResolveLRC resolveLRC;//��ʽ�����
    private static List<Sentence> list = new ArrayList<Sentence>();//����װ�������еľ���
    private Binder binder = new MyBinder();//����һ��ͨѶ�����ڷ��ظ����ã�����ͨѶ����,ͨѶ�����ڴ�����
	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "PlayerService start");
		// ȡ�õ绰����,ʵ�ֵ绰������ʱ��ֹͣ����,�Ҷϵļ���
		TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		// ע������������绰״̬,��ָ��������ִ�е���,1�����ü��������෽����2����������ͨѶ״̬(�绰���룬��ͨ�绰���Ҷϵ绰)
		telManager.listen(new TelListenr(),
				PhoneStateListener.LISTEN_CALL_STATE);
		mediaPlayer = new MediaPlayer();//ʵ����һ��������;
		controlPlay = new ControlPlay();//���ſ����߳�
		controlLyric = new ControlLyric();//���Ÿ�ʿ����߳�
		// ����һ��NotificationManager������,֪ͨ������
		notificationManager = (NotificationManager)
            PlayerService.this.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
       
		//����һ���㲥���ջ�
		receiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				Log.i(TAG, "service start play next song");
				//ֻ�н��յ����淢���Ĳ��Ź㲥�ſ�ʼ���ţ���֤����ʱ����ʾ�Ͳ���ͬ��
				MyPlayer myPlayer = (MyPlayer)binder;
				try {
					myPlayer.play(filepath, 0,listId);
				} catch (IOException e) {
					Log.e(TAG, e.toString());
				}
				
			}
		};
		//ע��㲥
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.xianyifa.AudioPlayService.CONTROL");
		this.registerReceiver(receiver, intentFilter);
		
		//֪ͨ����ʿ���
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
	 * ΪBinder���ҵ�񷽷���ֻ�������������ͨ��Binder������ͨ�����е���
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
				//��������new  Ϊ�˷�ֹ�߳�ֹͣ��,����start����Thread already started.�쳣
				controlLyric = new ControlLyric();
				controlLyric.start();
	    	}
			PlayerService.this.handler = handler;
		}

		
		@Override
		public void play(String filepath,int position,int id) throws IOException{
			setFilePath(filepath);//�ڷ��񱣴浱ǰMP3·��
			setPosition(position);//�ڷ��񱣴浱ǰMP3·��
			listId = id;
			PlayerService.this.isStop = false;
			File file = new File(filepath);
			
	    	mediaPlayer.reset();//��֮ǰ�����ö�����һ��
	    	mediaPlayer.setDataSource(file.getAbsolutePath());//���������ļ�·��
	    	mediaPlayer.prepare();//����һ��Ҫ����,��ʼ��
	    	
	    	playLength = mediaPlayer.getDuration();
	    	
	    	//�������������㲥���Ѿ�׼����Ҫ���ţ������ʼ����һ�׸����
			Intent intent = new Intent("com.xianyifa.AudioPlay.CONTROL");
			intent.putExtra("listId", listId);
			intent.putExtra("filepath", filepath);
			PlayerService.this.sendBroadcast(intent);
	    	
			//ȡ�ø��·����Ŀǰֻ����ͬĿ¼��ͬ��
			lyricPath = filepath.substring(0, filepath.lastIndexOf(".")) + ".lrc";
			File f = new File(lyricPath);
	    	mediaPlayer.start();
	    	
	    	mediaPlayer.seekTo(position);
	    	isPlayInit = true;
	    	if(!PlayerService.this.controlPlay.isAlive() && f.exists()){//ֻ�д��ڸ��
	    		controlPlay.start();
	    	}else{
	    		controlLyricStop = true;//��������ڸ�ʾͽ����߳�
	    	}
		}
		
		@Override
		public boolean pause(){
			if(mediaPlayer.isPlaying()){//������ڲ���
				mediaPlayer.pause();
				return true;
			}else{
				//ӦΪ�������ֱֹͣ��ʹ��start �����б���
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
			}else if(PlayerService.this.filepath != null){//ȷ���û��ȵ��������
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
		 * ��ʾ֪ͨ��ͼ��,�����治�ɼ���ʱ�����
		 * (non-Javadoc)
		 * @see com.xianyifa.audioplayer.impl.MyPlayer#showNotification()
		 */
		@Override
		public void showNotification(){
			
	        // ����Notification�ĸ�������
	        notification =new Notification(R.drawable.icon,
	        		(filepath != null) ? filepath.substring(filepath.lastIndexOf("/")+1, filepath.length()) : getString(R.string.no_music_playing),
	        				System.currentTimeMillis());
	        notification.flags |= Notification.FLAG_ONGOING_EVENT; // ����֪ͨ�ŵ�֪ͨ����"Ongoing"��"��������"����
	        notification.flags |= Notification.FLAG_NO_CLEAR; // �����ڵ����֪ͨ���е�"���֪ͨ"�󣬴�֪ͨ�������������FLAG_ONGOING_EVENTһ��ʹ��
	        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
	        notification.defaults = Notification.DEFAULT_LIGHTS;
	        notification.ledARGB = Color.BLUE;
	        notification.ledOnMS =5000;
	               
	        // ����֪ͨ���¼���Ϣ
	        contentTitle = getString(R.string.now_playing); // ֪ͨ������
	        contentText = (showLyric != null) 
	        		? showLyric : getString(R.string.no_music_playing); // ֪ͨ������
//	        		CharSequence contentText = "�����ֲ���"; // ֪ͨ������
	        Intent notificationIntent = new Intent(PlayerService.this, AudioList.class); // �����֪ͨ��Ҫ��ת��Activity
	        //���������Խ������home��ֹͣactivity�ڳ�֪ͨ����ʱ���ֶ��activity����
	        //Ҳ�����ٰ�������������һ��������������activity
	        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  
	        contentIntent = PendingIntent.getActivity(PlayerService.this, 0,
	                notificationIntent, 0);
	        notification.setLatestEventInfo(PlayerService.this, contentTitle, contentText,
	        		contentIntent);
	        
	        
	        // ��Notification���ݸ�NotificationManager
	        notificationManager.notify(0, notification);
	        isShowNotification = true;
		}
		
		/*
		 * ɾ��֪ͨ����ͼ��
		 * (non-Javadoc)
		 * @see com.xianyifa.audioplayer.impl.MyPlayer#hideNotification()
		 */
		@Override
		public void hideNotification(){
			// ������ɾ��֮ǰ���Ƕ����֪ͨ
				notificationManager.cancel(0);
				isShowNotification = false;
		}
		
		/*
		 * �������
		 */
		@Override
		public Service getPlayerService(){
			return PlayerService.this;
		}
		
	}
	
	/*
	 * ʵ�ֵ绰������
	 */
	private class TelListenr extends PhoneStateListener {

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			try {
				switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:// �Ҷ�
					PlayerService.this.mediaPlayer.start();
					break;
				case TelephonyManager.CALL_STATE_OFFHOOK:// ��ͨ�绰
					
					break;

				case TelephonyManager.CALL_STATE_RINGING:// �绰����
					if(PlayerService.this.mediaPlayer.isPlaying()){//������ڲ���
						PlayerService.this.mediaPlayer.pause();//��ͣ
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
	 * ���ſ����߳�,��Ҫ����
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
						//��һ�λ���û�г�ʼ�������ҵ���ǰ��listView�������Ժ����ϼӾͿ���֪���б�ĩβ
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
						//�ж�֪ͨ�ǲ�����ʾ��������֪ͨ��Ϣ�ĸ���
						if(isShowNotification){
							myPlayer.showNotification();
						}
					}
				}
		}

	}
	
	/*
	 * ���Ÿ������¿����߳�,ֻ�в��������ڲŻ�����(ͬ������ĸ��ͬ��)
	 */
	public class ControlLyric extends Thread {

		@Override
		public void run() {
			Log.i(TAG, "ControlLyric start");
			// �жϸ����Ƿ��ڲ���
				while (!controlLyricStop) {
					long milliSecond = mediaPlayer.getCurrentPosition();
					Message message = Message.obtain();// ���ں�ס�߳�ͨѶ����Ϣ����,ÿ��ͨѶ��Ҫ����
					message.obj = milliSecond+"";
					handler.sendMessage(message);
					//�ж�֪ͨ�ǲ�����ʾ��������֪ͨ��Ϣ�ĸ���
					if(isShowNotification){
						Message message1 = Message.obtain();// ���ں�ס�߳�ͨѶ����Ϣ����,ÿ��ͨѶ��Ҫ����
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
	 * data list�󶨵�����
	 * filepath ���ڲ��Ÿ�����·��
	 * filename ���ڲ��Ÿ�������
	 * return i �������ڲ��ŵĸ�����listView�����ݵ�����
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
	 * index ������listView�����ݵ�����
	 * return ���ظ����ľ���·��
	 */
	private String getFilePath(int index){
		HashMap<String,Object> audio = listViewData.get(index);
		return audio.get("filepath").toString();
	}
	
	@Override
	public void onDestroy() {
		controlPlayStop = true;//���Ʋ����߳�Ҳ����
		controlLyricStop = true;//���Ʋ����߳�Ҳ����
		//�ȴ����ſ����߳̽�����ֹͣ���ŷ���
		while(controlPlay.isAlive()){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Log.e(TAG, e.toString());
			}
			Log.i(TAG, "wait controlPlay stop");
		}
		//���ſ����߳�ֹͣ����ֹͣ������
		if(mediaPlayer != null){
			MyPlayer myPlayer = (MyBinder)binder;
			myPlayer.hideNotification();//���������ʱ��һ��Ҫ���֪ͨ��
			mediaPlayer.stop();//һ��Ҫ������ֹͣ��Ҫ��Ȼ����ֹͣ�˲��������ǻ��������
			mediaPlayer.release();//�ͷ���Դ
		}
		this.unregisterReceiver(receiver);//�������ע��
		Log.i(TAG, "PlayerService onDestroy");
		super.onDestroy();
	}

	
}
