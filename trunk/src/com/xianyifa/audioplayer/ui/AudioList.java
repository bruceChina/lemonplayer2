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
	private String filepath = null;//��ǰ���������ļ�����·��
	private String longClickFilePath;//�����������ļ�����·��
	private Integer longClickFileId;//�����������ļ������ݿ��е�id
	private int position = 0;//��ǰ����λ��
	protected MyPlayer myPlayerService;//���ŷ������
	private MyServiceConnection conn;
	private Intent service;// ���ֲ��ŷ�����ͼ
	private ListView listView;
	private List<HashMap<String, Object>> data;// listView������
	private long audioLength;// �������ֳ���
	private SimpleAdapter adapter;// ListView������
	private int listId = -1;// ��ǰ���ŵ�������listView������
	private int onListId = -1;// ��һ�ײ��ŵ�������listView������
	private MediaPlayer mediaPlayer;// ����Ĳ�����
	private Handler handler;// �������̺߳����̵߳�ͨѶ
	private ControlPlayTime controlPlayTime;// ����ʱ����¿����߳�
	private boolean controlPlayStop = false;//��������ʱ����¿����߳̽���
	private boolean isStop = false;// ��ʶ�������Ƿ���ͣ
	private boolean isPause = false;// ��ʶactivity�Ƿ�������ͣ�ָ�
	private boolean isExit = false;//���û�����˳���ťΪtrue
	private boolean isAudioLengthShow = false;//�жϽ���ĸ��������Ƿ������UI��
	private int widthPixels;//�豸��Ļ�������
	private BroadcastReceiver receiver;//�㲥����
//	private final int ADDAUDIOPLAYER_ID = Menu.FIRST;
//	private final int DELAUDIOPLAYER_ID = Menu.FIRST + 1;
	private final int EXITAUDIOPLAYER_ID = Menu.FIRST + 2;
	private final int DELETE_DIALOG = 1;
	private final int PAGE_SIZE = 100;//Ĭ�Ͽ�ʼ��ȡ������
	private ImageView progressBar;//���ֲ��Ž������ؼ� 
	private HashMap<String, String> config;//�������ò���

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audiolist);
		MyApplication.getInstance().addActivity(this); //�ѵ�ǰactivity�ŵ�activity������
		showListView();
		//ȡ��ϵͳ����
		config = getConfig();
		//�����л�activity start
		gestureDetector = new GestureDetector(AudioList.this);
		ListView audiolist = (ListView)findViewById(R.id.audiolist);
		audiolist.setOnTouchListener(this);
		audiolist.setLongClickable(true);
		//�����л�activity end
		//���ñ���ͼƬ start
		String bgPath = config.get("bg");
		try {
			Log.i(TAG, bgPath);
			InputStream assetFile = getAssets().open(bgPath);//���ֽ�����ȡ
			BitmapDrawable bitmap = (BitmapDrawable)Drawable.createFromStream(assetFile, bgPath);
			LinearLayout audioListView = (LinearLayout)findViewById(R.id.audioListView);//ȡ�ý���
			audioListView.setBackgroundDrawable(bitmap);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, e.toString());
			e.printStackTrace();
		}
		//���ñ���ͼƬ end
		
		//��ȡ��Ļ�Ŀ������
		DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthPixels = dm.widthPixels;
		
        //���ø����б����Ŀ��
        TextView audioListText = (TextView)findViewById(R.id.audiolisttext);
        LayoutParams laParaContent = audioListText.getLayoutParams();
		laParaContent.width = widthPixels/2;//ռ��Ļ��һ��
		audioListText.setLayoutParams(laParaContent);
		//��ǰѡ�еı������
//		audioListText.setTextColor(Color.parseColor("#000000"));
		ImageView listImageView = (ImageView)findViewById(R.id.select_list);
		LayoutParams laParaContentSelect = listImageView.getLayoutParams();
		laParaContentSelect.width = widthPixels/2;//ռ��Ļ��һ��
		listImageView.setLayoutParams(laParaContentSelect);//���ÿ��

		ImageView lyricImageView = (ImageView)findViewById(R.id.select_lyric);
		LayoutParams laParaContentSelect2 = lyricImageView.getLayoutParams();
		laParaContentSelect2.width = widthPixels/2;//ռ��Ļ��һ��
		lyricImageView.setLayoutParams(laParaContentSelect2);//���ÿ��
		
		listImageView.setBackgroundColor(Color.parseColor("#ffffff"));
		//���ñ��� end
		
		progressBar = (ImageView)findViewById(R.id.progress_bar);//ȡ�ý������ڵײ���ť����ʾ
        //Ϊδѡ�е�ע�����¼�
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
		
		
		/** Ϊ�ײ���ť�����¼�  start */
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
								//���ŵ�һ�����ϴεļ�������
								HashMap<String, Object> item = (HashMap<String, Object>)listView.getItemAtPosition(0);
								filepath = item.get("filepath").toString();
								listId = 0;
								AudioList.this.position = 0;
							}
							Log.i(TAG, filepath+"|"+AudioList.this.position);
								try {
									myPlayerService.play(filepath, AudioList.this.position, listId);
									v.setBackgroundResource(R.drawable.but_icon_pause_song);
									// �ж��߳��Ƿ�״̬
									if (!controlPlayTime.isAlive()) {
										audioLength = mediaPlayer.getDuration();//��һ�λ���Ҫ������ȡһ�Σ���ֹ�㲥���յ�����Ϊ����ֵ���ڴ��߳���������audioLengthΪ0
										controlPlayTime.start();// ��һ��ִ�в��ſ�ʼ�߳�
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							
						}
					}
				}else{
					//���ŷ���û������
				}
			}
		});
		
		/** ��һ�� */
		preSong.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//ֻ���ڲ���״̬��һ����ť����Ч
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
		
		/** ��һ�� */
		nextSong.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//ֻ���ڲ���״̬��һ����ť����Ч
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
		/** Ϊ�ײ���ť����ʱ��   end */
		
		Log.i(TAG, "onCreate");
	}
	
	/*
	 * �������񻻸�㲥
	 */
	private void regReceiver(){
		//����һ���㲥����
		receiver = new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				listId = intent.getIntExtra("listId", listId);
				// ���Ƿ��񻻸������������UI��ʾ
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
				onListId = listId;//��Ҫ�����ⲿҪ��Ȼʱ�䲻������
				adapter.notifyDataSetChanged();
			}
			
		};
		
		//ע��㲥
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.xianyifa.AudioPlay.CONTROL");//Ҫ���صĶ���
		this.registerReceiver(receiver, intentFilter);
	}
	
	/*
	 * �����ݰ�listview�����ڽ�����ʾ
	 */
	private void showListView(){
		Log.i(TAG, "showListView");
		data = new ArrayList<HashMap<String, Object>>();
//		data = Toolbox.showCatalog(file);
		AudioDao ad = new AudioDao(AudioList.this);
		data = ad.getAudioFile(1, PAGE_SIZE);
		// ȡ��listview
		listView = (ListView) findViewById(R.id.audiolist);
		adapter = new MyAdapter(AudioList.this, data,
				R.layout.audiolistitem, new String[] {"id", "filename", "filepath",
						"playTime", "audioTime" }, new int[] {R.id.id, R.id.audioname,
						R.id.audiopath, R.id.audioplaytime, R.id.audiotime });
		listView.setAdapter(adapter);

		// �������ſ����߳�
		controlPlayTime = new ControlPlayTime();// ȡ�ò���ʱ������߳�
		
		// �����߳�ͨѶ����
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				String message = (String) msg.obj;
//				//���message������,���Ƿ������ĸ������ȣ���������ת����Ĳ���ʱ���
				if(message.matches("[0-9]+")){
					//���Ƿ����ڲ��ţ��û����»ص�activity����ʱ����UI��ʾ��ǰ���Ÿ�����Ϣ
					HashMap<String, Object> item = (HashMap<String, Object>) listView.getItemAtPosition(listId);
					item.put("audioTime", "/"+Toolbox.lengthTime(Long.parseLong(message)));
				}else{
						HashMap<String, Object> item = (HashMap<String, Object>) listView.getItemAtPosition(listId);
						item.put("playTime", message);
				}
				//����֪ͨ��ͣ���½�����
				progressBar.setBackgroundResource(R.drawable.slide_img_handle);
				//�޸Ľ���������
				LayoutParams laParaContent = progressBar.getLayoutParams();
				laParaContent.width = getprogressBarSize();
				progressBar.setLayoutParams(laParaContent);
				
				adapter.notifyDataSetChanged();
				super.handleMessage(msg);
			}
		};
		

		// ΪlistViewע�ᵥ���¼�
		listView.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				ListView v = (ListView) parent;
				HashMap<String, Object> item = (HashMap<String, Object>) v.getItemAtPosition(position);
				// �ϸ����ŵ��ļ���ListView��λ��
//				HashMap<String, Object> Befitem = null;
//				if (listId > -1) {
//					Befitem = (HashMap<String, Object>) v.getItemAtPosition(listId);
//				}
//				
				listId = position;// ����listView����
//				onListId = listId;
				
				filepath = item.get("filepath").toString();// ȡ������·��
				File file = new File(filepath);
				if(!file.isFile()){
					item = (HashMap<String, Object>) v.getItemAtPosition(position+1);
					if(item == null){
						myPlayerService.pause();// ��ͣ��
					}else{
						listId = position + 1;
						filepath = item.get("filepath").toString();// ȡ������·��
					}
					//�����ݿ���Ӧ�ļ�¼ɾ��
					AudioDao ad = new AudioDao(AudioList.this);
					ad.delete(Integer.parseInt(item.get("id").toString()));
					refresh();
				}
				String playerFileName = myPlayerService.getFilePath();
				
				if (mediaPlayer.isPlaying()) {// ������ڲ���
					if (playerFileName.equals(filepath)) {// ���������·�������ڲ��ŵ�·��һ��
						myPlayerService.pause();// ��ͣ��
						isStop = true;
					} else {
						try {
							myPlayerService.play(filepath,
									AudioList.this.position,listId);
						} catch (IOException e) {
							Log.i(TAG, e.toString());
						}
					}
				} else {// ��������ڲ���
					if (isStop && playerFileName.equals(filepath)) {// �ж��ǲ���ֹͣ״̬�������󲥷ŵ���ͬһ���ļ�
						myPlayerService.pause();//�������������������,����Ӧ���ǲ���
					} else {// ������ͣ״̬�ĵ��ò���,��������ͣ��������Ĳ���ͬһ�������ļ�
						try {
							myPlayerService.play(filepath,
									AudioList.this.position,listId);

							// �ж��߳��Ƿ�״̬
							if (!controlPlayTime.isAlive()) {
								audioLength = mediaPlayer.getDuration();//��һ�λ���Ҫ������ȡһ�Σ���ֹ�㲥���յ�����Ϊ����ֵ���ڴ��߳���������audioLengthΪ0
								controlPlayTime.start();// ��һ��ִ�в��ſ�ʼ�߳�
							}
							
						} catch (IOException e) {
							Log.i(TAG, e.toString());
						}
					}
				}
			}

		});
		
		//Ϊlistview�������Ĳ˵�
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
			
			@Override
			public void onCreateContextMenu(ContextMenu menu, View v,
					ContextMenuInfo menuInfo) {
				//����ͼ��
				menu.setHeaderIcon(R.drawable.content_menu_ico);
				//���ñ���
				menu.setHeaderTitle(R.string.choice_action);
				//���ò˵�
				//����
				menu.add(R.string.player).setOnMenuItemClickListener(new OnMenuItemClickListener() {
					
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						// TODO Auto-generated method stub
						return true;
					}
				});
				//ɾ��
				menu.add(R.string.delete).setOnMenuItemClickListener(new OnMenuItemClickListener() {
					
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						showDialog(DELETE_DIALOG);//��ʾ��ʾ��
						return true;
					}
				});
				
			}
		});
		
		//ΪListView����һ��item��������
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
	 * ˢ���б���
	 * @param param
	 */
	@Override
	public void refresh() {
		// TODO Auto-generated method stub
		data = new ArrayList<HashMap<String, Object>>();
		AudioDao ad = new AudioDao(AudioList.this);
		data = ad.getAudioFile(1, PAGE_SIZE);
		// ȡ��listview
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
		 * ÿ�μ���listView�������
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
			
			//�����¸�ֵ����ʾ�հ�?λ��ԭ��:getView���ǰ�ÿ�����ݰ󶨵�����Ĺ��̣�����������Ҫ��ֵ
			fileNameText.setText(((HashMap<String, Object>)listView.getItemAtPosition(position)).get("filename").toString());
			filePathText.setText(((HashMap<String, Object>)listView.getItemAtPosition(position)).get("filepath").toString());
			playTimeText.setText(((HashMap<String, Object>)listView.getItemAtPosition(position)).get("playTime").toString());
			fileTimeText.setText(((HashMap<String, Object>)listView.getItemAtPosition(position)).get("audioTime").toString());
			if(listId == position){
				//��Ҫ��int  �����ܴ���ɫ��ʮ���ƴ���
				fileNameText.setTextColor(Color.parseColor("#3197FF"));
				playTimeText.setTextColor(Color.parseColor("#3197FF"));
				fileTimeText.setTextColor(Color.parseColor("#3197FF"));
				
				//���ò���ͼ��
				playIcon.setBackgroundResource(R.drawable.list_pause_state);
//				LayoutParams playIconParams = (LayoutParams)playIcon.getLayoutParams();
//				playIconParams.FILL_PARENT = 
//				playIcon.setLayoutParams(params);
				
//				progressBarText.setBackgroundColor(Color.parseColor("#000000"));
//				progressBar.setBackgroundResource(R.drawable.slide_img_handle);
				//�޸Ľ���������
//				LayoutParams laParaContent = (LayoutParams)progressBar.getLayoutParams();
//				laParaContent.width = getprogressBarSize();
//				progressBar.setLayoutParams(laParaContent);
//				progressBarText.setWidth(0);//��������Ĳ���
				//�����ļ�����TextView��ȷ�ֹ̫����Ӱ��ʱ����ʾ��λ��
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
	 * ����������ĳߴ�
	 */
	private int getprogressBarSize(){
		double proportion = (double)mediaPlayer.getCurrentPosition()/(double)myPlayerService.getPlayLength();
		int px = (int)(widthPixels * proportion);
		return px;
	}
	
	/*
	 * ��Ϊϵͳ�ڴ治�㱻�ݻ� (non-Javadoc)
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
	 * ��Ϊϵͳ�ڴ治�㱻�ݻ� (non-Javadoc)
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
	 * ��ͣ��Activity (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		controlPlayStop = true;//���Ʋ����߳�Ҳ����
		if (myPlayerService != null && !isExit) {
			myPlayerService.showNotification();
			isPause = true;
		}else{
			Log.i(TAG, "wait controlPlayTmie stop");
			//�ȴ����ſ����߳̽�����ֹͣ���ŷ���,��ִ���˳���ʱ����б�Ҫ������ֹͣ
			while(controlPlayTime.isAlive()){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Log.e(TAG, e.toString());
				}
			}
		}
		
		unbindService(conn);//��������
		this.unregisterReceiver(receiver);//��������㲥
		Log.i(TAG, "onPause");
		super.onPause();
	}

	/*
	 * ���»���,��տ���������� (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		//ֻ������ͣ�ָ������Ż�ִ��
		if (isPause && (myPlayerService != null)) {
			myPlayerService.hideNotification();
			listId = myPlayerService.getListId();//���ѵ�ʱ��Ҳ�����ڷ���ȡ�õ�ǰ�Ĳ���ListView���������ڷ�����Ϊ�Ĳ�����һ�׵�ʱ��Ҳ�ܸ���
			isPause = false;
		}
		
		
		//1.1������onResumeע��󶨲��ŷ���
		service = new Intent(this, PlayerService.class);// ͨ����ͼȡ�ò��ŷ���
		// ��������1����2�������ط���һ��ͨ����Activity��serviceͨѶ��ͨ��ͨ��ͨѶ�ģ�����ͨ����һ���ӿ�Ҫʵ��
		// 3������1�Զ�����
		conn = new MyServiceConnection();
		
		this.startService(service);// ��ʹ�ô��������ڰ�
		this.bindService(service, conn, BIND_AUTO_CREATE);
		
		//ע��㲥����
		regReceiver();
		
		Log.i(TAG, "onResume");
		super.onResume();
	}


	/*
	 * ��Ӳ˵� (non-Javadoc)
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
	 * ����˵����� (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case EXITAUDIOPLAYER_ID:
			// �˳�������
//			MyApplication.getInstance().exit(); //�ѹ������е�����activity���˳� finish
//			this.stopService(service);
//			Log.i(TAG, "bye bye!");
//			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * ʵ��ͨ��
	 */
	private final class MyServiceConnection implements ServiceConnection {

		/*
		 * ���ӷ�����÷��� service Ϊbinder ͨѶ������ (non-Javadoc)
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
			onListId = listId;//��ǰ���Ÿ�����listView��������
			filepath = myPlayerService.getFilePath();
			
			mediaPlayer = myPlayerService.getMediaPlayer();// ȡ�÷����еĲ�����
			// �ж��߳��Ƿ�״̬���������ַ����ڲ���  ���������������ʱ���߳�
			if (myPlayerService.getIsPlayInit()) {
				audioLength = myPlayerService.getPlayLength();//��ǰ���Ÿ����ĳ���
				if(!controlPlayTime.isAlive()){
					// ȡ�ò���ʱ������̣߳������������»���¶���ʼ�̻߳��޷����ѣ�δ֪ԭ��
					controlPlayTime = new ControlPlayTime();
					controlPlayStop = false;
					controlPlayTime.start();// ��һ��ִ�в��ſ�ʼ�߳�
				}
			}
			//��֪ͨ���д���
			if(myPlayerService.isShowNotification()){
				myPlayerService.hideNotification();
			}
			
			Log.i(TAG, "onServiceConnected");
		}

		/*
		 * �Ͽ�������÷��� (non-Javadoc)
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
	 * ����ʱ����¿����߳�,ֻ�в��������ڲŻ�����
	 */
	public class ControlPlayTime extends Thread {

		@Override
		public void run() {
			Log.i(TAG, "ControlPlayTime start");
			//�̸߳������ͷ���handler��������UI���ŵ������ܳ���
			Message message1 = Message.obtain();
			message1.obj = audioLength+"";//audioLength��long��Ҫת���ַ�������
			handler.sendMessage(message1);
			// �жϸ����Ƿ��ڲ���
				while (!controlPlayStop) {
					long milliSecond = mediaPlayer.getCurrentPosition();
					String time = Toolbox.formatTime(milliSecond);
					Message message = Message.obtain();// ���ں�ס�߳�ͨѶ����Ϣ����,ÿ��ͨѶ��Ҫ����
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
	 * ��������ȷ�ϴ���
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
					//ȷ��ִ�в���
//					Toast toast = Toast.makeText(AudioList.this,
//							"Ҫɾ�����ǣ�"+longClickFileId, Toast.LENGTH_LONG);
//					toast.setGravity(Gravity.CENTER, 0, 0);
//					toast.show();
					AudioDao ad = new AudioDao(AudioList.this);
					ad.delete(longClickFileId);
					ad.closeDB();
					refresh();
					removeDialog(DELETE_DIALOG);//�ɴ����ĵ���ɾ������ɾ���´δ�������ͬһ�����󣬵�����Ϣ���ݲ���
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