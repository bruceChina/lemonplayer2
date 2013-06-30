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
	 * 播放歌曲
	 * @param filename
	 * @param position
	 * @param id
	 * @throws IOException
	 */
	public void play(String filename,int position,int id) throws IOException;
	
	/**
	 * 展厅歌曲
	 * @return
	 */
	public boolean pause();
	/**
	 * 重置播放器
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
	 * 当前服务对象
	 * @return
	 */
	public Service getPlayerService();
	
	/**
	 * 上一曲
	 */
	public void preSong();
	
	/**
	 * 暂停或者播放
	 * @param view 按钮对象
	 */
	public void playState(View view);
	
	/**
	 * 下一曲
	 */
	public void nextSong();
	
}
