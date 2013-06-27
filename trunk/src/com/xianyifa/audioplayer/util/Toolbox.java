package com.xianyifa.audioplayer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.xianyifa.audioplayer.vo.AudioFile;

public class Toolbox {
	// 调用文件目录查询方法
    //方法一；以绝对路径输出给定的目录下的所有文件路径
    public static List<HashMap<String, Object>> showCatalog(File file) {
        //System.out.println(file.getName());
    	List<HashMap<String, Object>> fileList = new ArrayList<HashMap<String, Object>>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {//判断是否是目录
                    showCatalog(f);
                } else {
//                    System.out.println(f.getName());//输出文件或文件夹名称
                    if(f.getName().substring(f.getName().lastIndexOf("."), f.getName().length()).equals(".mp3")){
                    	HashMap<String, Object> item = new HashMap<String, Object>();
                    	item.put("filepath",f.getAbsolutePath());//输出绝对路径
                    	item.put("filename",f.getName());//输出绝对路径
                    	item.put("playTime","");//输出绝对路径
                    	item.put("audioTime","");//输出绝对路径
                    	fileList.add(item);
                    }
                }
            }
        }
        return fileList;
    }
    
    /**
     * 扫面指定文件夹下的音频文件
     * @param file
     * @return
     */
    public static List<AudioFile> showAudioFile(File file) {
        //System.out.println(file.getName());
    	List<AudioFile> fileList = new ArrayList<AudioFile>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {//判断是否是目录
                	showAudioFile(f);
                } else {
                    if(f.getName().substring(f.getName().lastIndexOf("."), f.getName().length()).equals(".mp3")){
                    	AudioFile audioFile = new AudioFile();
                    	audioFile.setFilePath(f.getAbsolutePath());//输出绝对路径
                    	audioFile.setFileName(f.getName());//输出绝对路径
                    	audioFile.setCover("");
                    	fileList.add(audioFile);
                    }
                }
            }
        }
        return fileList;
    }
    
    /*
     * 格式化当前播放时间点时间
     */
    public static String formatTime(long milliSecond){
    	int minute = (int)(milliSecond/1000)/60;
    	int second = (int)(milliSecond/1000)%60;
		String sec = second+"";
		if(second<10){
			sec = "0"+second;
		}
		String time = minute+":"+sec;
    	return time;
    }
    
    /*
     * 求总时长
     */
    public static String lengthTime(long milliSecond){
    	int minute = (int)(milliSecond/1000)/60;
    	int second = (int)(milliSecond/1000)%60;
    	if(milliSecond%1000 > 500){
    		second += 1;
    		if(second == 60){
    			minute += 1;
    			second = 0;
    		}
    	}
		String sec = second+"";
		if(second<10){
			sec = "0"+second;
		}
		String time = minute+":"+sec;
    	return time;
    }
    
}
