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
	 * ȡ������������
	 * @return
	 */
	private SQLiteDatabase getDB(){
		return dbOpenHelper.getReadableDatabase();
	}
	
	/**
	 * �ر����ݿ�
	 */
	public void closeDB(){
		if(db != null){
			db.close();
		}
	}
	
	/**
	 * ��������
	 * @param audioFile Ҫд���������Ϣ
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
		             Toast.makeText(con, "����ʧ�ܣ�", Toast.LENGTH_SHORT).show();
				}finally{
					closeDB();
				}
		}
	}
	
	/**
	 * ɾ��
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
	 * ȡ�����ݿ��������м�¼
	 * @param startRow ��ʼ�ļ�¼
	 * @param resultRow ��ѯ������
	 * @return ���ط��ϵļ�¼��
	 */
	public List<HashMap<String, Object>> getAudioFile(Integer startRow, Integer resultRow){
		db = getDB();
		String sql = "SELECT * FROM audio LIMIT ?,?";
		Cursor cursor = db.rawQuery(sql, new String[]{startRow.toString(), resultRow.toString()});
		List<HashMap<String, Object>> audios = new ArrayList<HashMap<String, Object>>();
		//���α��ƶ�����һ����¼
		while(cursor.moveToNext()){
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put("id",cursor.getInt(cursor.getColumnIndex("id")));//���ݿ��е�ID
			item.put("filepath",cursor.getString(cursor.getColumnIndex("filePath")));//�������·��
        	item.put("filename",cursor.getString(cursor.getColumnIndex("fileName")));//����
        	item.put("playTime","");//
        	item.put("audioTime","");//
			
			audios.add(item);
		}
		return audios;
	}
	
	/**
	 * �жϼ�¼�Ƿ����
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
