/**
 * 
 */
package com.xianyifa.audioplayer.vo;

import java.io.Serializable;

/**
 * 音乐文件路径实体类
 * @author Administrator
 *
 */
public class AudioFile implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2382436324256614815L;
	private Integer id;
	private String filePath;
	private String fileName;
	private String cover;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getFilePath() {
		return filePath;
	}
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getCover() {
		return cover;
	}
	public void setCover(String cover) {
		this.cover = cover;
	}
	
	
}
