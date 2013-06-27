/**
 * 
 */
package com.xianyifa.audioplayer.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * @author Administrator ѡ�������ļ���
 */
public class SelectFileActivity extends ListActivity {
	private List<String> items = null;
	private List<String> paths = null;
	private String rootPath = "/";//��Ŀ¼
	private String curPath = "/";//��ǰѡ���Ŀ¼
	private TextView selectPath;
	private final int FILE_RESULT_CODE = 1;
	private final static String TAG = "SelectFileActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.fileselect);
		selectPath = (TextView) findViewById(R.id.selectPath);
		Button buttonConfirm = (Button) findViewById(R.id.buttonConfirm);
		Button buttonCancel = (Button) findViewById(R.id.buttonCancel);
		//�жϴ治����SD��
		if(ExistSDCard()){
			curPath = "/sdcard";
		}
		
		buttonConfirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent data = new Intent(SelectFileActivity.this,
						MyActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("file", curPath);// ���ص�·��
				data.putExtras(bundle);
				setResult(FILE_RESULT_CODE, data);
				/*
				File file = new File(curPath);
				List<AudioFile> audioFiles = Toolbox.showAudioFile(file);
				AudioDao ad = new AudioDao(SelectFileActivity.this);
				items = new ArrayList<String>();
				paths = new ArrayList<String>();
				for (AudioFile audioFile : audioFiles) {
					items.add(audioFile.getFilePath());
					paths.add(audioFile.getFilePath());
					setListAdapter(new MyAdapter(SelectFileActivity.this, items, paths));
					ad.save(audioFile);
				}
				ad.closeDB();
				*/
				Log.i(TAG, curPath);
				finish();
			}
		});
		
		buttonCancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		getFileDir(curPath);
	}
	
	/**
	 * �ж��Ƿ����SD��
	 * @return
	 */
    private boolean ExistSDCard() {  
        if (android.os.Environment.getExternalStorageState().equals(  
          android.os.Environment.MEDIA_MOUNTED)) {  
         return true;  
        } else  
         return false;  
       }  
	
	/**
	 * ��ȡĿ¼�µ�Ŀ¼�б�
	 * @param filePath
	 */
	private void getFileDir(String filePath) {
		selectPath.setText(filePath);
		items = new ArrayList<String>();
		paths = new ArrayList<String>();
		File f = new File(filePath);
		File[] files = f.listFiles();
		if (!filePath.equals(rootPath)) {
			items.add("b1");
			paths.add(rootPath);
			items.add("b2");
			paths.add(f.getParent());
		}
		//�����������Ŀ¼
		if(files != null){
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				if(file.isDirectory()){
					items.add(file.getName());
					paths.add(file.getPath());
				}else if(file.getName().lastIndexOf(".") != -1){
					if(file.getName().substring(file.getName().lastIndexOf("."), file.getName().length()).equals(".mp3")){
						items.add(file.getName());
						paths.add(file.getPath());
					}
				}
			}
		}
		setListAdapter(new MyAdapter(SelectFileActivity.this, items, paths));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		File file = new File(paths.get(position));
		if (file.isDirectory()) {
			curPath = paths.get(position);
//			Toast toast = Toast.makeText(SelectFileActivity.this,
//					curPath, Toast.LENGTH_LONG);
//			toast.setGravity(Gravity.CENTER, 0, 0);
//			toast.show();
			getFileDir(curPath);
		} else {
			// ���Դ��ļ�
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	/**
	 * items�ṹ
	 * @author Administrator
	 *
	 */
	private class ViewHolder {
		TextView text;
		ImageView icon;
	}

	private class MyAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<String> items;
		private List<String> paths;
		private Bitmap backIcon1;  
	    private Bitmap backIcon2;  
	    private Bitmap folderIcon;  
	    private Bitmap musicIcon;  
		

		public MyAdapter(Context context, List<String> it, List<String> pa) {
			mInflater = LayoutInflater.from(context);
			items = it;
			paths = pa;
			backIcon1 = BitmapFactory.decodeResource(context.getResources(),R.drawable.back01);  
			backIcon2 = BitmapFactory.decodeResource(context.getResources(),R.drawable.scan_folder_return);  
		    folderIcon = BitmapFactory.decodeResource(context.getResources(),R.drawable.icon_folder);  
		    musicIcon = BitmapFactory.decodeResource(context.getResources(),R.drawable.img_wifi_transfering_icon);
			// super(context, items, paths);
			// TODO Auto-generated constructor stub
		}

		/*
		 * ÿ�μ���listView������� (non-Javadoc)
		 * 
		 * @see android.widget.SimpleAdapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.fileitem, null);
				holder = new ViewHolder();
				holder.text = (TextView) convertView.findViewById(R.id.text);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			File f = new File(paths.get(position).toString());
			if (items.get(position).toString().equals("b1")) {
				holder.text.setText(R.string.back_root_dir);
				holder.icon.setImageBitmap(backIcon1);
			} else if (items.get(position).toString().equals("b2")) {
				holder.text.setText(R.string.back_dir);
				holder.icon.setImageBitmap(backIcon2);
			} else {
				holder.text.setText(f.getName());
				if (f.isDirectory()) {
					 holder.icon.setImageBitmap(folderIcon);
				} else if(f.getName().substring(f.getName().lastIndexOf("."), f.getName().length()).equals(".mp3")){
					 holder.icon.setImageBitmap(musicIcon);
				}
			}

			return convertView;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return items.size();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return items.get(position);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

	}

}
