/**
 * 
 */
package com.xianyifa.audioplayer.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.xianyifa.audioplayer.util.MyApplication;

/**
 * @author Administrator
 *
 */
public class SelectBackgroundActivity extends MyActivity {
	private final String TAG = "SelectBackgroundActivity";
	private ArrayList<HashMap<String, Object>> data;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		MyApplication.getInstance().addActivity(this); //�ѵ�ǰactivity�ŵ�activity������
		setContentView(R.layout.select_bg_view);
		
		showGridViewData();
		
		
	}
	
	/***
	 * ��ʾ����GridView
	 */
	private void showGridViewData(){
//		data = new ArrayList<HashMap<String, Object>>();
		data = getBackgroundDate();
		GridView bgGridView = (GridView)findViewById(R.id.bg_img_gridView);
		bgGridView.setAdapter(new MyAdapter(SelectBackgroundActivity.this, data));
		
	}
	
	/**
	 * ��ȡ����Ŀ¼��ͼƬ
	 */
	private ArrayList<HashMap<String, Object>> getBackgroundDate(){
		ArrayList<HashMap<String, Object>> data = null;
		try {
			String path = "bkgs";
			data = new ArrayList<HashMap<String, Object>>();
			String[] strFiles = getAssets().list(path);
			
			//ѭ����ȡ
			HashMap<String, Object> bg;
			for(int i = 0;i < strFiles.length;i++){
				bg = new HashMap<String, Object>();
				
				InputStream assetFile = getAssets().open(path+"/"+strFiles[i]);//���ֽ�����ȡ
				BitmapDrawable bitmap = (BitmapDrawable)Drawable.createFromStream(assetFile, path+"/"+strFiles[i]);
				bg.put("path", path+"/"+strFiles[i]);
				bg.put("bitmap", bitmap);
				data.add(bg);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		Log.i(TAG, "getBackgroundDate:"+data.size());
		return data;
	}
	
	/**
	 * �Զ���������
	 */
	private class MyAdapter extends BaseAdapter{
		private Context context;
		private ArrayList<HashMap<String, Object>> data;
		
		public MyAdapter(Context context,ArrayList<HashMap<String, Object>> data){
			
//			super(context, data, resource, from, to);
			this.context = context;
			this.data = data;
			Log.i(TAG, "MyAdapter"+data.size());
		}
		
		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			Log.i(TAG, "getView");
			ImageView imageView;
			if (convertView == null) { 
                imageView = new ImageView(context); 
                imageView.setLayoutParams(new GridView.LayoutParams(75, 75));//����ImageView���󲼾� 
                imageView.setAdjustViewBounds(false);//���ñ߽���� 
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);//���ÿ̶ȵ����� 
                imageView.setPadding(8, 8, 8, 8);//���ü�� 
            }  
            else { 
                imageView = (ImageView) convertView; 
            } 
//            imageView.setImageResource((BitmapDrawable)data.get(position).get("bitmap"));//ΪImageView����ͼƬ��Դ 
            imageView.setImageDrawable((BitmapDrawable)data.get(position).get("bitmap"));
            return imageView; 
			
		}
		
	}
	
	
}
