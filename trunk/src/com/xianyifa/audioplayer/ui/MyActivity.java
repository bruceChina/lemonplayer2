package com.xianyifa.audioplayer.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ActionMode;
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
	protected MyPlayer myPlayerService;//���ŷ������
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        
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
		// �˳�����
		menu.add(0, ADDAUDIOPLAYER_ID, 0, "��Ӹ���").setShortcut('2', 'b').setIcon(R.drawable.ic_menu_scan);
		menu.add(0, DELAUDIOPLAYER_ID, 0, "ɾ������").setShortcut('2', 'b');
		menu.add(0, SET_UP_ID, 0, "����").setShortcut('2', 'b').setIcon(R.drawable.ic_menu_setting);
		// .setIcon(R.drawable.exit);
		// �˳�����
		menu.add(1, EXITAUDIOPLAYER_ID, 0, "�˳�").setShortcut('4', 'd')
				.setIcon(R.drawable.ic_menu_exit);
		return true;
	}

	/*
	 * ����˵����� (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		// �˳������������
		switch (item.getItemId()) {
		case EXITAUDIOPLAYER_ID:
			// �˳�������
			if(getPlayerService() != null){//������ŷ����Ѿ�����
				getPlayerService().hideNotification();//��֪ͨ�����
				getPlayerService().getPlayerService().stopSelf();//ֹͣ����
			}
			MyApplication.getInstance().exit(); //�ѹ������е�����activity���˳� finish ���˻�ֱ��System.exit(0);//����ֹͣ����
			Log.i(TAG, "bye bye!");
			return true;
		case ADDAUDIOPLAYER_ID:
			intent  = new Intent(MyActivity.this, SelectFileActivity.class);
			//�Ը������µ�activity
			startActivityForResult(intent, FILE_RESULT_CODE);
			return true;
		case SET_UP_ID:
//			LayoutInflater inflater = (LayoutInflater)this.getSystemService(this.LAYOUT_INFLATER_SERVICE);
//			//�������ý���
//			View LayoutView = (View)inflater.inflate(R.layout.app_set_up, null);
//			//ʹ��PopupWindow��ʾ
//			PopupWindow pw = new PopupWindow(LayoutView, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
//			pw.showAtLocation(findViewById(R.id.bottom_menu), Gravity.CENTER, 0, 300);
			
			intent = new Intent(MyActivity.this, SetUpActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			
			startActivity(intent);
			
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * ��startActivityForResult �򿪵�activity ���صĵ���
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
				
				/** ɨ�貢���ָ��� start */
				File file = new File(bundle.getString("file"));
				List<AudioFile> audioFiles = Toolbox.showAudioFile(file);
				AudioDao ad = new AudioDao(MyActivity.this);
				for (AudioFile audioFile : audioFiles) {
					ad.save(audioFile);
				}
				ad.closeDB();
				//ˢ���б�
				refresh();
//				AudioList al = new AudioList();
//				al.refresh();
				/** ɨ�貢���ָ��� end */
				
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * ˢ���б���
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
		if (e1.getX()-e2.getX() > 50 && Math.abs(velocityX) > 0) {   
			TextView audioLyricText = (TextView)findViewById(R.id.audio_lyric);
			audioLyricText.performClick();
//          �л�Activity   
//          Intent intent = new Intent(ViewSnsActivity.this, UpdateStatusActivity.class);   
//          startActivity(intent);   
//            Toast.makeText(MyActivity.this, "��������", Toast.LENGTH_SHORT).show();   
        } else if (e2.getX()-e1.getX() > 50 && Math.abs(velocityX) > 0) {   
        	TextView audioListText = (TextView)findViewById(R.id.audiolisttext); 
        	audioListText.performClick();
//          �л�Activity   
//          Intent intent = new Intent(ViewSnsActivity.this, UpdateStatusActivity.class);   
//          startActivity(intent);   
//            Toast.makeText(MyActivity.this, "��������", Toast.LENGTH_SHORT).show();   
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
	 * ��ȡ����
	 */
	public HashMap<String, String> getConfig(){
		HashMap<String, String> config = new HashMap<String, String>();
		config.put("bg", "bkgs/002.jpg");
		return config;
	}
	
	
	@Override
	public void setPlayerService(MyPlayer service) {
		// TODO Auto-generated method stub
		this.myPlayerService = service;
	}

	/**
	 * ȡ�ò��ŷ������
	 */
	@Override
	public MyPlayer getPlayerService() {
		// TODO Auto-generated method stub
		return myPlayerService;
	}
	
}
