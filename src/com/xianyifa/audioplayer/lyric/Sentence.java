package com.xianyifa.audioplayer.lyric;

import java.io.Serializable;

/**
 * 来源YOYOPlayer
 * 一个用来表示每一句歌词的类
 * 它封装了歌词的内容以及这句歌词的起始时间
 * 和结束时间，还有一些实用的方法
 * @author hadeslee
 */

public class Sentence implements Serializable{
	 	private static final long serialVersionUID = 20071125L;
	    private long fromTime;//这句的起始时间,时间是以毫秒为单位
	    private long toTime;//这一句的结束时间
	    private String content;//这一句的内容
	    private final static long DISAPPEAR_TIME = 1000L;//歌词从显示完到消失的时间
	    
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
	     * 检查某个时间是否包含在某句中间
	     * @param time 时间
	     * @return 是否包含了
	     */
	    public boolean isInTime(long time) {
	        return time >= fromTime && time <= toTime;
	    }
		
		 @Override
		public String toString() {
		        return "{" + fromTime + "(" + content + ")" + toTime + "}";
		    }
}

