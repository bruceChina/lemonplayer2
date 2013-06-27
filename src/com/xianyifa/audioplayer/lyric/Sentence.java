package com.xianyifa.audioplayer.lyric;

import java.io.Serializable;

/**
 * ��ԴYOYOPlayer
 * һ��������ʾÿһ���ʵ���
 * ����װ�˸�ʵ������Լ�����ʵ���ʼʱ��
 * �ͽ���ʱ�䣬����һЩʵ�õķ���
 * @author hadeslee
 */

public class Sentence implements Serializable{
	 	private static final long serialVersionUID = 20071125L;
	    private long fromTime;//������ʼʱ��,ʱ�����Ժ���Ϊ��λ
	    private long toTime;//��һ��Ľ���ʱ��
	    private String content;//��һ�������
	    private final static long DISAPPEAR_TIME = 1000L;//��ʴ���ʾ�굽��ʧ��ʱ��
	    
	    public Sentence(String content, long fromTime, long toTime) {
	        this.content = content;
	        this.fromTime = fromTime;
	        this.toTime = toTime;
	    }

	    public Sentence(String content, long fromTime) {
	        this(content, fromTime, 0);
	    }

	    public Sentence(String content) {
	        this(content, 0, 0);
	    }

		public long getFromTime() {
			return fromTime;
		}

		public void setFromTime(long fromTime) {
			this.fromTime = fromTime;
		}

		public long getToTime() {
			return toTime;
		}

		public void setToTime(long toTime) {
			this.toTime = toTime;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}
	    
		/**
	     * ���ĳ��ʱ���Ƿ������ĳ���м�
	     * @param time ʱ��
	     * @return �Ƿ������
	     */
	    public boolean isInTime(long time) {
	        return time >= fromTime && time <= toTime;
	    }
		
		 @Override
		public String toString() {
		        return "{" + fromTime + "(" + content + ")" + toTime + "}";
		    }
}

