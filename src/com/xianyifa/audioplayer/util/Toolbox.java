package com.xianyifa.audioplayer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.xianyifa.audioplayer.vo.AudioFile;

public class Toolbox {
	// �����ļ�Ŀ¼��ѯ����
    //����һ���Ծ���·�����������Ŀ¼�µ������ļ�·��
    public static List<HashMap<String, Object>> showCatalog(File file) {
        //System.out.println(file.getName());
    	List<HashMap<String, Object>> fileList = new ArrayList<HashMap<String, Object>>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {//�ж��Ƿ���Ŀ¼
                    showCatalog(f);
                } else {
//                    System.out.println(f.getName());//����ļ����ļ�������
                    if(f.getName().substring(f.getName().lastIndexOf("."), f.getName().length()).equals(".mp3")){
                    	HashMap<String, Object> item = new HashMap<String, Object>();
                    	item.put("filepath",f.getAbsolutePath());//�������·��
                    	item.put("filename",f.getName());//�������·��
                    	item.put("playTime","");//�������·��
                    	item.put("audioTime","");//�������·��
                    	fileList.add(item);
                    }
                }
            }
        }
        return fileList;
    }
    
    /**
     * ɨ��ָ���ļ����µ���Ƶ�ļ�
     * @param file
     * @return
     */
    public static List<AudioFile> showAudioFile(File file) {
        //System.out.println(file.getName());
    	List<AudioFile> fileList = new ArrayList<AudioFile>();
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {//�ж��Ƿ���Ŀ¼
                	showAudioFile(f);
                } else {
                    if(f.getName().substring(f.getName().lastIndexOf("."), f.getName().length()).equals(".mp3")){
                    	AudioFile audioFile = new AudioFile();
                    	audioFile.setFilePath(f.getAbsolutePath());//�������·��
                    	audioFile.setFileName(f.getName());//�������·��
                    	audioFile.setCover("");
                    	fileList.add(audioFile);
                    }
                }
            }
        }
        return fileList;
    }
    
    /*
     * ��ʽ����ǰ����ʱ���ʱ��
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
     * ����ʱ��
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
