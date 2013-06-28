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
	private int widthPixels;//�ֻ���Ļ���
	protected MyPlayer myPlayerService;//���ŷ������
	private MyServiceConnection conn;
	private Intent service;// ���ֲ��ŷ�����ͼ
	private Handler handler;
	private BroadcastReceiver receiver;//�����㲥
	private String lyricPath = "";//���·��
	private String oldLyricPath = "";//��һ�׸��·��
	private ResolveLRC resolveLRC;//��ʽ�����
	private static List<Sentence> list = new ArrayList<Sentence>();//����װ�������еľ���
	private final int EXITAUDIOPLAYER_ID = Menu.FIRST + 2;
	private HashMap<String, String> config;//ϵͳ����
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.audiolyric);
		
		MyApplication.getInstance().addActivity(this); //�ѵ�ǰactivity�ŵ�activity������
		
		//�����л�activity start
		gestureDetector = new GestureDetector(AudioLyric.this);
		LinearLayout audioLyricLayout = (LinearLayout)findViewById(R.id.audio_lyric_layout);
		audioLyricLayout.setOnTouchListener(this);
		audioLyricLayout.setLongClickable(true);
		//�����л�activity end
		
		//����
		updateBackground();
		
		//��ȡ��Ļ�Ŀ������
		DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        widthPixels = dm.widthPixels;
		//���ø����б����Ŀ��
        TextView audioListText = (TextView)findViewById(R.id.audiolisttext);
        LayoutParams laParaContent = audioListText.getLayoutParams();
		laParaContent.width = widthPixels/2;//ռ��Ļ��һ��
		audioListText.setLayoutParams(laParaContent);
		
		TextView audioLyricText = (TextView)findViewById(R.id.audio_lyric);
		//��ǰѡ�еı������
//		audioLyricText.setBackgroundColor(Color.parseColor("#000000"));
//		audioLyricText.setTextColor(Color.parseColor("#000000"));
		ImageView lyricImageView = (ImageView)findViewById(R.id.select_lyric);
		LayoutParams laParaContentSelect = lyricImageView.getLayoutParams();
		laParaContentSelect.width = widthPixels/2;//ռ��Ļ��һ��
		lyricImageView.setLayoutParams(laParaContentSelect);//���ÿ��
		
		ImageView listImageView = (ImageView)findViewById(R.id.select_list);
		LayoutParams laParaContentSelect2 = listImageView.getLayoutParams();
		laParaContentSelect2.width = widthPixels/2;//ռ��Ļ��һ��
		listImageView.setLayoutParams(laParaContentSelect2);//���ÿ��
		
		lyricImageView.setBackgroundColor(Color.parseColor("#ffffff"));
		//���ñ���  end
		
		audioListText.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(AudioLyric.this, AudioList.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			}
		});
		
		showLyric();//��ʾ���
		
		//ȥ�ĸ������洫�����Ĳ���
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
	 * �������񻻸�㲥
	 */
	private void regReceiver(){
		//����һ���㲥����
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
		//ע��㲥
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("com.xianyifa.AudioPlay.CONTROL");
		this.registerReceiver(receiver, intentFilter);
	}
	
	private void showLyric(){
		//����Ƹ�������ͬ����ʾ���߳�ͨѶ
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
	 * ��Ӳ˵� (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
//			if(myPlayerService != null){//������ŷ����Ѿ�����
//				myPlayerService.hideNotification();//��֪ͨ�����
//			}
//			MyApplication.getInstance().exit(); //�ѹ������е�����activity���˳� finish ���˻�ֱ��System.exit(0);//����ֹͣ����
//			Log.i(TAG, "bye bye!");
//			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		service = new Intent(this, PlayerService.class);// ͨ����ͼȡ�ò��ŷ���
		// ��������1����2�������ط���һ��ͨ����Activity��serviceͨѶ��ͨ��ͨ��ͨѶ�ģ�����ͨ����һ���ӿ�Ҫʵ��
		// 3������1�Զ�����
		conn = new MyServiceConnection();
		this.bindService(service, conn, BIND_AUTO_CREATE);
		regReceiver();//ע��㲥����
		Log.i(TAG, "Lyric onResume");
		
		if(MyApplication.getInstance().isReloadBackground()){
			updateBackground();
		}
		
		super.onResume();
	}

	@Override
	protected void onPause() {
		this.unbindService(conn);
		this.unregisterReceiver(receiver);//����㲥����
		Log.i(TAG, "Lyric onPause");
		super.onPause();
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
			myPlayerService = (MyPlayer) service;
			
//			setPlayerService((MyPlayer)service);
			myPlayerService.setHandler(handler);
			// �ж��߳��Ƿ�״̬���������ַ����ڲ���  ���������������ʱ���߳�
			Log.i(TAG, "Lyric onServiceConnected");
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
	 * ���±���
	 */
	@Override
	public void updateBackground(){
		//���ñ���ͼƬ start
		config = getConfig();
		String bgPath = config.get("bg");
		try {
			Log.i(TAG, bgPath);
			InputStream assetFile = getAssets().open(bgPath);//���ֽ�����ȡ
			BitmapDrawable bitmap = (BitmapDrawable)Drawable.createFromStream(assetFile, bgPath);
			LinearLayout audioLyricLayout = (LinearLayout)findViewById(R.id.audio_lyric_layout);//ȡ�ý���
			audioLyricLayout.setBackgroundDrawable(bitmap);
			MyApplication.getInstance().setReloadBackground(false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, e.toString());
//			e.printStackTrace();
		}
		//���ñ���ͼƬ end
	}
	
}
