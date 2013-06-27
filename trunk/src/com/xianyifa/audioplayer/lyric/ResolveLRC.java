package com.xianyifa.audioplayer.lyric;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

import com.xianyifa.audioplayer.util.ConvertFileCode;

public class ResolveLRC {
	private final String TAG = "ResolveLRC";
	private static List<Sentence> list = new ArrayList<Sentence>();//����װ�������еľ���
	//���ڻ����һ��������ʽ����
    private static final Pattern pattern = Pattern.compile("(?<=\\[).*?(?=\\])");
    
    private String path;//���·��
    private String songName;//��������
    private long songLength;//��������
	
	
	public ResolveLRC(String path,long songLength) {
		//���ݴ���ĸ��·��ȡ�ø���
		this.songName = path.substring(path.lastIndexOf("/")-1, path.lastIndexOf("."));
		this.songLength = songLength;
		this.path = path;
		
	}

	/*
	 * �ַ�����ʽ
	 * path  ���·����ǰֻ���ڸ���ͬĿ¼��������һ��
	 */
	public List<Sentence> IOChar() throws Exception{
		Log.i(TAG, "song start Resolve,this Lyric:" + path);
		File file = new File(path);
		if(!file.exists()){
			 list.add(0, new Sentence(songName, 0, songLength));
		}else{
			FileInputStream inStream = new FileInputStream(path);//�����ʵ�·��
			InputStreamReader in = new InputStreamReader(inStream);
			BufferedReader br = new BufferedReader(in);//������
			String temp = null;
			StringBuilder strb = new StringBuilder();
			while((temp = br.readLine()) != null){
				System.out.println(temp);
				strb.append(temp).append("\n");
			}
			init(strb.toString());
//			for(Sentence s : list){
//				System.out.println(s.toString());
//			}
			
			in.close();
			inStream.close();
		}
		return list;
	}
	
	/*
	 * �ֽ�����ʽ
	 */
	public List<Sentence> IOByte()throws Exception{
		FileInputStream inStream = new FileInputStream(path);
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		int len = 0;
		while((len = inStream.read(buffer)) != -1){
			outStream.write(buffer, 0, len);//д���������ȥ
		}
		byte[] data = outStream.toByteArray();//תΪ�ֽ�����
		String strLRC = new String(data);
		init(strLRC);
//		System.out.println(strLRC);
		
		outStream.close();
		inStream.close();
		
		return list;
	}
	
	/**
	 * �����������˶������ĸ����������
	 * @return
	 */
	public List<Sentence> getLyricList(){
		Log.i(TAG, "song start Resolve,this Lyric:" + path);
		File file = new File(path);
		if(!file.exists()){
			 list.add(0, new Sentence(songName, 0, songLength));
		}else{
			ConvertFileCode convertFileCode = new ConvertFileCode();//��ȡ����ļ���װ����������ı��ַ���
			String text = convertFileCode.converfile(path);
			init(text);
		}
		return list;
	}
	
	 /**
	  * ����YOYOPlayer
     * ����Ҫ��һ�������������ݶ����ĸ������
     * ���г�ʼ��������Ѹ��һ��һ��ֿ��������ʱ��
     * @param content �������
     */
    private void init(String content) {
        //�����ʵ�����Ϊ��,�����Ͳ���ִ����
        //ֱ����ʾ�������Ϳ�����
        if (content == null || content.trim().equals("")) {
//            list.add(new Sentence(info.getFormattedName(), Integer.MIN_VALUE, Integer.MAX_VALUE));
            list.add(new Sentence(songName, Integer.MIN_VALUE, Integer.MAX_VALUE));
            return;
        }
        try {
            BufferedReader br = new BufferedReader(new StringReader(content));
            String temp = null;
            while ((temp = br.readLine()) != null) {
                parseLine(temp.trim());
            }
            br.close();
            //�������Ժ��������
            Collections.sort(list, new Comparator<Sentence>() {

                @Override
				public int compare(Sentence o1, Sentence o2) {
                    return (int) (o1.getFromTime() - o2.getFromTime());
                }
            });
            //�����һ���ʵ���ʼ���,������ô��,���ϸ�����Ϊ��һ����,��������
            //��βΪ������һ���ʵĿ�ʼ
            if (list.size() == 0) {
//                list.add(new Sentence(info.getFormattedName(), 0, Integer.MAX_VALUE));
            	list.add(new Sentence(songName, Integer.MIN_VALUE, Integer.MAX_VALUE));
                return;
            } else {
                Sentence first = list.get(0);
//                list.add(0, new Sentence(info.getFormattedName(), 0, first.getFromTime()));
                list.add(0, new Sentence(songName, 0, first.getFromTime()));
            }

            int size = list.size();
            for (int i = 0; i < size; i++) {
                Sentence next = null;
                if (i + 1 < size) {
                    next = list.get(i + 1);
                }
                Sentence now = list.get(i);
                if (next != null) {
                    now.setToTime(next.getFromTime() - 1);
                }
            }
            //�������û����ô��,�Ǿ�ֻ��ʾһ�������
            if (list.size() == 1) {
                list.get(0).setToTime(Integer.MAX_VALUE);
            } else {
                Sentence last = list.get(list.size() - 1);
//                last.setToTime(info == null ? Integer.MAX_VALUE : info.getLength() * 1000 + 1000);
                last.setToTime(songLength);
            }
        } catch (Exception ex) {
        	Log.i(TAG, ex.toString());
        }
    }
	
	 /**
	  * ����YOYOPlayer
     * ������һ�е����ݣ�����������
     * �Լ���ǩ�������������ɸ�Sentence����
     * �������е�ʱ���ǩ�ֲ�����һ��ʱ��ҲҪ�ܷ�������
     * ���Ը�����һЩʵ��
     * 20080824����
     * @param line ��һ��
     */
    private void parseLine(String line) {
        if (line.equals("")) {
            return;
        }
        Matcher matcher = pattern.matcher(line);
        List<String> temp = new ArrayList<String>();
        int lastIndex = -1;//���һ��ʱ���ǩ���±�
        int lastLength = -1;//���һ��ʱ���ǩ�ĳ���
        while (matcher.find()) {
            String s = matcher.group();
            int index = line.indexOf("[" + s + "]");
            if (lastIndex != -1 && index - lastIndex > lastLength + 2) {
                //��������ϴεĴ�С�����м���˱������������
                //���ʱ���Ҫ�ֶ���
                String content = line.substring(lastIndex + lastLength + 2, index);
                for (String str : temp) {
                    long t = parseTime(str);
                    if (t != -1) {
                        list.add(new Sentence(content, t));
                    }
                }
                temp.clear();
            }
            temp.add(s);
            lastIndex = index;
            lastLength = s.length();
        }
        //����б�Ϊ�գ����ʾ����û�з������κα�ǩ
        if (temp.isEmpty()) {
            return;
        }
        try {
            int length = lastLength + 2 + lastIndex;
            String content = line.substring(length > line.length() ? line.length() : length);
//            if (Config.getConfig().isCutBlankChars()) {
//                content = content.trim();
//            }
            //���Ѿ�����ƫ������ʱ�򣬾Ͳ��ٷ�����
//            if (content.equals("") && offset == 0) {
//                for (String s : temp) {
//                    int of = parseOffset(s);
//                    if (of != Integer.MAX_VALUE) {
//                        offset = of;
//                        info.setOffset(offset);
//                        break;//ֻ����һ��
//                    }
//                }
//                return;
//            }
            for (String s : temp) {
                long t = parseTime(s);
                if (t != -1) {
                    list.add(new Sentence(content, t));
                }
            }
        } catch (Exception exe) {
        }
    }
    
    /**
     * ����YOYOPlayer
     * ����00:00.00�������ַ���ת����
     * ��������ʱ�䣬���� 
     * 01:10.34����һ���Ӽ���10���ټ���340����
     * Ҳ���Ƿ���70340����
     * @param time �ַ�����ʱ��
     * @return ��ʱ���ʾ�ĺ���
     */
    private long parseTime(String time) {
        String[] ss = time.split("\\:|\\.");
        //��� ����λ�Ժ󣬾ͷǷ���
        if (ss.length < 2) {
            return -1;
        } else if (ss.length == 2) {//���������λ���������
            try {
                //�ȿ���û��һ���Ǽ�¼������ƫ������
//                if (offset == 0 && ss[0].equalsIgnoreCase("offset")) {
//                    offset = Integer.parseInt(ss[1]);
//                    info.setOffset(offset);
//                    System.err.println("�����ƫ������" + offset);
//                    return -1;
//                }
                int min = Integer.parseInt(ss[0]);
                int sec = Integer.parseInt(ss[1]);
                if (min < 0 || sec < 0 || sec >= 60) {
                    throw new RuntimeException("���ֲ��Ϸ�!");
                }
                return (min * 60 + sec) * 1000L;
            } catch (Exception exe) {
                return -1;
            }
        } else if (ss.length == 3) {//���������λ��������룬ʮ����
            try {
                int min = Integer.parseInt(ss[0]);
                int sec = Integer.parseInt(ss[1]);
                int mm = Integer.parseInt(ss[2]);
                if (min < 0 || sec < 0 || sec >= 60 || mm < 0 || mm > 99) {
                    throw new RuntimeException("���ֲ��Ϸ�!");
                }
                return (min * 60 + sec) * 1000L + mm * 10;
            } catch (Exception exe) {
                return -1;
            }
        } else {//����Ҳ�Ƿ�
            return -1;
        }
    }
	
}
