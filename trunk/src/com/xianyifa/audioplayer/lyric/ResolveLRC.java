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
	private static List<Sentence> list = new ArrayList<Sentence>();//里面装的是所有的句子
	//用于缓存的一个正则表达式对象
    private static final Pattern pattern = Pattern.compile("(?<=\\[).*?(?=\\])");
    
    private String path;//歌词路径
    private String songName;//歌曲名称
    private long songLength;//歌曲长度
	
	
	public ResolveLRC(String path,long songLength) {
		//根据传入的歌词路径取得歌名
		this.songName = path.substring(path.lastIndexOf("/")-1, path.lastIndexOf("."));
		this.songLength = songLength;
		this.path = path;
		
	}

	/*
	 * 字符流方式
	 * path  歌词路径当前只能在歌曲同目录并且名称一样
	 */
	public List<Sentence> IOChar() throws Exception{
		Log.i(TAG, "song start Resolve,this Lyric:" + path);
		File file = new File(path);
		if(!file.exists()){
			 list.add(0, new Sentence(songName, 0, songLength));
		}else{
			FileInputStream inStream = new FileInputStream(path);//传入歌词的路径
			InputStreamReader in = new InputStreamReader(inStream);
			BufferedReader br = new BufferedReader(in);//缓存区
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
	 * 字节流方式
	 */
	public List<Sentence> IOByte()throws Exception{
		FileInputStream inStream = new FileInputStream(path);
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		int len = 0;
		while((len = inStream.read(buffer)) != -1){
			outStream.write(buffer, 0, len);//写到输出流中去
		}
		byte[] data = outStream.toByteArray();//转为字节数组
		String strLRC = new String(data);
		init(strLRC);
//		System.out.println(strLRC);
		
		outStream.close();
		inStream.close();
		
		return list;
	}
	
	/**
	 * 这个方法解决了读出来的歌词乱码问题
	 * @return
	 */
	public List<Sentence> getLyricList(){
		Log.i(TAG, "song start Resolve,this Lyric:" + path);
		File file = new File(path);
		if(!file.exists()){
			 list.add(0, new Sentence(songName, 0, songLength));
		}else{
			ConvertFileCode convertFileCode = new ConvertFileCode();//读取歌词文件并装换编码输出文本字符串
			String text = convertFileCode.converfile(path);
			init(text);
		}
		return list;
	}
	
	 /**
	  * 来自YOYOPlayer
     * 最重要的一个方法，它根据读到的歌词内容
     * 进行初始化，比如把歌词一句一句分开并计算好时间
     * @param content 歌词内容
     */
    private void init(String content) {
        //如果歌词的内容为空,则后面就不用执行了
        //直接显示歌曲名就可以了
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
            //读进来以后就排序了
            Collections.sort(list, new Comparator<Sentence>() {

                @Override
				public int compare(Sentence o1, Sentence o2) {
                    return (int) (o1.getFromTime() - o2.getFromTime());
                }
            });
            //处理第一句歌词的起始情况,无论怎么样,加上歌名做为第一句歌词,并把它的
            //结尾为真正第一句歌词的开始
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
            //如果就是没有怎么办,那就只显示一句歌名了
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
	  * 来自YOYOPlayer
     * 分析这一行的内容，根据这内容
     * 以及标签的数量生成若干个Sentence对象
     * 当此行中的时间标签分布不在一起时，也要能分析出来
     * 所以更改了一些实现
     * 20080824更新
     * @param line 这一行
     */
    private void parseLine(String line) {
        if (line.equals("")) {
            return;
        }
        Matcher matcher = pattern.matcher(line);
        List<String> temp = new ArrayList<String>();
        int lastIndex = -1;//最后一个时间标签的下标
        int lastLength = -1;//最后一个时间标签的长度
        while (matcher.find()) {
            String s = matcher.group();
            int index = line.indexOf("[" + s + "]");
            if (lastIndex != -1 && index - lastIndex > lastLength + 2) {
                //如果大于上次的大小，则中间夹了别的内容在里面
                //这个时候就要分段了
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
        //如果列表为空，则表示本行没有分析出任何标签
        if (temp.isEmpty()) {
            return;
        }
        try {
            int length = lastLength + 2 + lastIndex;
            String content = line.substring(length > line.length() ? line.length() : length);
//            if (Config.getConfig().isCutBlankChars()) {
//                content = content.trim();
//            }
            //当已经有了偏移量的时候，就不再分析了
//            if (content.equals("") && offset == 0) {
//                for (String s : temp) {
//                    int of = parseOffset(s);
//                    if (of != Integer.MAX_VALUE) {
//                        offset = of;
//                        info.setOffset(offset);
//                        break;//只分析一次
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
     * 来自YOYOPlayer
     * 把如00:00.00这样的字符串转化成
     * 毫秒数的时间，比如 
     * 01:10.34就是一分钟加上10秒再加上340毫秒
     * 也就是返回70340毫秒
     * @param time 字符串的时间
     * @return 此时间表示的毫秒
     */
    private long parseTime(String time) {
        String[] ss = time.split("\\:|\\.");
        //如果 是两位以后，就非法了
        if (ss.length < 2) {
            return -1;
        } else if (ss.length == 2) {//如果正好两位，就算分秒
            try {
                //先看有没有一个是记录了整体偏移量的
//                if (offset == 0 && ss[0].equalsIgnoreCase("offset")) {
//                    offset = Integer.parseInt(ss[1]);
//                    info.setOffset(offset);
//                    System.err.println("整体的偏移量：" + offset);
//                    return -1;
//                }
                int min = Integer.parseInt(ss[0]);
                int sec = Integer.parseInt(ss[1]);
                if (min < 0 || sec < 0 || sec >= 60) {
                    throw new RuntimeException("数字不合法!");
                }
                return (min * 60 + sec) * 1000L;
            } catch (Exception exe) {
                return -1;
            }
        } else if (ss.length == 3) {//如果正好三位，就算分秒，十毫秒
            try {
                int min = Integer.parseInt(ss[0]);
                int sec = Integer.parseInt(ss[1]);
                int mm = Integer.parseInt(ss[2]);
                if (min < 0 || sec < 0 || sec >= 60 || mm < 0 || mm > 99) {
                    throw new RuntimeException("数字不合法!");
                }
                return (min * 60 + sec) * 1000L + mm * 10;
            } catch (Exception exe) {
                return -1;
            }
        } else {//否则也非法
            return -1;
        }
    }
	
}
