/**
 * 
 */
package com.xianyifa.audioplayer.impl;

/**
 * @author Administrator
 *
 */
public interface BaseActivity {
	
	/**
	 * 设置播放服务对象
	 */
	public void setPlayerService(MyPlayer service);
	
	/**
	 * 取得播放服务对象
	 */
	public MyPlayer getPlayerService();
	
}
