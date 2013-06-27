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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
	private HashMap<String, String> config;//程序配置参数
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		MyApplication.getInstance().addActivity(this); //把当前activity放到activity管理器
		setContentView(R.layout.select_bg_view);
		
		showGridViewData();
		
		
	}
	
	/***
	 * 显示数据GridView
	 */
	private void showGridViewData(){
//		data = new ArrayList<HashMap<String, Object>>();
		data = getBackgroundDate();
		GridView bgGridView = (GridView)findViewById(R.id.bg_img_gridView);
		bgGridView.setAdapter(new MyAdapter(SelectBackgroundActivity.this, data));
		

		
		//点击事件
		bgGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position,
					long id) {
				// TODO Auto-generated method stub
				
				GridView gv = (GridView)parent;
				HashMap<String, Object> item = (HashMap<String, Object>)gv.getItemAtPosition(position);
				String path = item.get("path").toString();
				Log.i(TAG, path);
				//写入配置文件
				HashMap<String, String> cofig = new HashMap<String, String>();
				cofig.put("bgImg", path);
				setConfig(cofig);
				SelectBackgroundActivity.this.finish();
			}

			
			
		});
	}
	
	/**
	 * 读取背景目录的图片
	 */
	private ArrayList<HashMap<String, Object>> getBackgroundDate(){
		ArrayList<HashMap<String, Object>> data = null;
		try {
			String path = "bkgs";
			data = new ArrayList<HashMap<String, Object>>();
			String[] strFiles = getAssets().list(path);
			
			//循环读取
			HashMap<String, Object> bg;
			for(int i = 0;i < strFiles.length;i++){
				bg = new HashMap<String, Object>();
				
				InputStream assetFile = getAssets().open(path+"/"+strFiles[i]);//以字节流读取
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
	 * 自定义适配器
	 */
	private class MyAdapter extends BaseAdapter{
		private Context context;
		private ArrayList<HashMap<String, Object>> data;
		private String bgPath;
		
		public MyAdapter(Context context,ArrayList<HashMap<String, Object>> data){
			config = getConfig();
			this.context = context;
			this.data = data;
			this.bgPath = config.get("bg");
			Log.i(TAG, "MyAdapter"+data.size());
		}
		
		/* (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int index) {
			// TODO Auto-generated method stub
			return data.get(index);
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		/* (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			Log.i(TAG, "getView");
//			ImageView imageView;
//			if (convertView == null) { 
//                imageView = new ImageView(context); 
//                imageView.setLayoutParams(new GridView.LayoutParams(150, 250));//设置ImageView对象布局 
//                imageView.setAdjustViewBounds(false);//设置边界对齐 
//                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);//设置刻度的类型 
//                imageView.setPadding(8, 8, 8, 8);//设置间距 
//            }  
//            else { 
//                imageView = (ImageView) convertView; 
//            } 
//            imageView.setImageDrawable((BitmapDrawable)data.get(position).get("bitmap"));
//            return imageView; 
			
            
            convertView = LayoutInflater.from(SelectBackgroundActivity.this.getApplicationContext()).inflate(R.layout.select_bg_item, null);
            ImageView imageView = (ImageView)convertView.findViewById(R.id.bgItemImg);
//            imageView.setLayoutParams(new GridView.LayoutParams(150, 250));//设置ImageView对象布局 
//	          imageView.setAdjustViewBounds(false);//设置边界对齐 
//	          imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);//设置刻度的类型 
//	          imageView.setPadding(8, 8, 8, 8);//设置间距 
            
            
            imageView.setImageDrawable((BitmapDrawable)data.get(position).get("bitmap"));
            
            if(data.get(position).get("path").equals(bgPath)){
            	ImageView imageViewCheck = (ImageView)convertView.findViewById(R.id.bgItemCheck);
            	imageViewCheck.setVisibility(View.VISIBLE);//可见
//            	imageViewCheck.setImageResource(R.drawable.icon_add_checked);
            }
            
            return convertView;
            
		}
		
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		MyApplication.getInstance().removeActivity(this);
		super.onDestroy();
	}
	
	
}
