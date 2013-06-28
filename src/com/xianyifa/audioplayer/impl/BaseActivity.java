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
	 * 取得播放服务对象
	 */
	public MyPlayer getPlayerService();
	
	/**
	 * 更新activity背景
	 */
	public void updateBackground();
	
}
