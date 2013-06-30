package com.xianyifa.audioplayer.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import android.app.Service;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.View;

public interface MyPlayer {
	/**
	 * ���Ÿ���
	 * @param filename
	 * @param position
	 * @param id
	 * @throws IOException
	 */
	public void play(String filename,int position,int id) throws IOException;
	
	/**
	 * չ������
	 * @return
	 */
	public boolean pause();
	/**
	 * ���ò�����
	 * @throws IOException
	 */
	public void reset() throws IOException;
	
	public void stop();
	
	public void setFilePath(String filepath);
	
	public void showNotification();
	
	public void hideNotification();
	
	public int getPosition();
	
	public String getFilePath();
	
	public MediaPlayer getMediaPlayer();
	
	public void setListViewData(List<HashMap<String, Object>> listViewData);
	
	public int getListId();
	
	public boolean getIsPlayInit();
	
	public long getPlayLength();
	
	public boolean isShowNotification();
	
	public void setHandler(Handler handler);
	/**
	 * ��ǰ�������
	 * @return
	 */
	public Service getPlayerService();
	
	/**
	 * ��һ��
	 */
	public void preSong();
	
	/**
	 * ��ͣ���߲���
	 * @param view ��ť����
	 */
	public void playState(View view);
	
	/**
	 * ��һ��
	 */
	public void nextSong();
	
}
