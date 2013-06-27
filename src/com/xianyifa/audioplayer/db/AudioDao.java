/**
 * 
 */
package com.xianyifa.audioplayer.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.xianyifa.audioplayer.vo.AudioFile;

/**
 * @author Administrator
 *
 */
public class AudioDao {
	private final String TAG = "AudioDao";
	private DBOpenHelper dbOpenHelper;
	private SQLiteDatabase db;
	private Context con;
	
	public AudioDao(Context context){
		this.dbOpenHelper = new DBOpenHelper(context);
		this.con = context;
	}
	
	/**
	 * 取得数据量连接
	 * @return
	 */
	private SQLiteDatabase getDB(){
		return dbOpenHelper.getReadableDatabase();
	}
	
	/**
	 * 关闭数据库
	 */
	public void closeDB(){
		if(db != null){
			db.close();
		}
	}
	
	/**
	 * 音乐增加
	 * @param audioFile 要写入的音乐信息
	 */
	public void save(AudioFile audioFile){
		db = getDB();
		if(audioFile.getFilePath().length() <= 100 && !isExist(audioFile.getFilePath())){
				try {
					String sql = "INSERT INTO audio(filePath, fileName, cover) values(?,?,?)";
					db.execSQL(sql, new Object[]{audioFile.getFilePath(), audioFile.getFileName(), audioFile.getCover()});
				} catch (Exception e) {
					// TODO: handle exception
					 Log.i(TAG, "insert failed"+e);
		             Toast.makeText(con, "插入失败！", Toast.LENGTH_SHORT).show();
				}finally{
					closeDB();
				}
		}
	}
	
	/**
	 * 删除
	 */
	public void delete(Integer id){
		db = getDB();
		try{
			String sql = " DELETE FROM audio WHERE id = ?";
			db.execSQL(sql, new Object[]{id});
		}catch(SQLException e){
			Log.i(TAG, "delete failed"+e.toString());
		}finally{
			closeDB();
		}
	}
	
	/**
	 * 取得数据库音乐所有记录
	 * @param startRow 开始的记录
	 * @param resultRow 查询的行数
	 * @return 返回符合的记录数
	 */
	public List<HashMap<String, Object>> getAudioFile(Integer startRow, Integer resultRow){
		db = getDB();
		String sql = "SELECT * FROM audio LIMIT ?,?";
		Cursor cursor = db.rawQuery(sql, new String[]{startRow.toString(), resultRow.toString()});
		List<HashMap<String, Object>> audios = new ArrayList<HashMap<String, Object>>();
		//把游标移动到第一条记录
		while(cursor.moveToNext()){
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put("id",cursor.getInt(cursor.getColumnIndex("id")));//数据库中的ID
			item.put("filepath",cursor.getString(cursor.getColumnIndex("filePath")));//输出绝对路径
        	item.put("filename",cursor.getString(cursor.getColumnIndex("fileName")));//名称
        	item.put("playTime","");//
        	item.put("audioTime","");//
			
			audios.add(item);
		}
		return audios;
	}
	
	/**
	 * 判断记录是否存在
	 */
	public boolean isExist(String filePath){
		boolean b = false;
		db = getDB();
		String sql = "SELECT COUNT(*) FROM audio WHERE filepath = ?";
		Cursor cursor = db.rawQuery(sql, new String[]{filePath});
		cursor.moveToFirst();
		if(cursor.getLong(0) > 0){
			b = true;
		}
		return b;
	}
	
}
