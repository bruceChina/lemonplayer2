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
	 * ���ò��ŷ������
	 */
	public void setPlayerService(MyPlayer service);
	
	/**
	 * ȡ�ò��ŷ������
	 */
	public MyPlayer getPlayerService();
	
}
