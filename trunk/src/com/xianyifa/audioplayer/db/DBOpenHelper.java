/**
 * 
 */
package com.xianyifa.audioplayer.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Administrator
 *
 */
public class DBOpenHelper extends SQLiteOpenHelper {
	private static final String DATABASENAME = "audiofile.db";//数据库文件名称
	private static final int DATABASEVERSION = 1;//数据库版本
	
	/**
	 * @param context
	 * @param name
	 * @param factory
	 * @param version
	 */
	public DBOpenHelper(Context context) {
		super(context, DATABASENAME, null, DATABASEVERSION);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String sql = "CREATE TABLE audio (id integer primary key autoincrement, filePath varchar(100), fileName varchar(50), cover varchar(100))";
		db.execSQL(sql);
	}

	/* (non-Javadoc)
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS person");
		onCreate(db);
	}
	
	

}
