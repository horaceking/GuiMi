 package com.guimi;
import java.util.List;
import java.util.Vector;

import com.guimi.entities.MyOutfit;
import com.guimi.myadapters.MyMatchListItemAdapter;
import com.guimi.myviews.UploadDialog;
import com.guimi.sqlite.GuiMiDB;
import com.guimi.uploadPhoto.HandlePic;
import com.guimi.uploadPhoto.combineUtil.Bimp;
import com.guimi.util.DensityUtil;
import com.guimi.util.LocalAsyncImageLoader;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class MyMatchesActivity extends Activity {
	public final String TAG = "com.guimi.MyMatchesActivity";
	private Handler UIhandler;
	private ListView list1;
	private ListView list2;
	private MyMatchListItemAdapter adapter1;
	private MyMatchListItemAdapter adapter2;
	private LinearLayout loadbar;
	private boolean showActionBar = true;
	private boolean someThreadRuning = false;
	private float listWidth = 0; 
	private List<MyOutfit> myOutfits;
	private GuiMiDB guiMiDB;
	private HandlePic handlePic;
	private LocalAsyncImageLoader localAsyncImageLoader;                                  
	
	
	@SuppressLint("HandlerLeak")
	@Override 
	public void onCreate(Bundle savedInstanceState) {  
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_matches);
		handlePic = new HandlePic("GuiMi", this);
		guiMiDB = GuiMiDB.getInstance(this);
		try {
			myOutfits = guiMiDB.getOutfits();
			if(myOutfits == null){
				Log.i("myOutfit:",""+myOutfits);
				Toast toast = Toast.makeText(this,
						"没有任何搭配哦~", Toast.LENGTH_SHORT);
				//e.printStackTrace();
				toast.show();
			/*}else{
			for(int i = 0;i<myOutfits.size();i++){
				Log.i("db", myOutfits.get(i).getOutfitUrl());
			}*/}
		} catch (SQLiteException e) {
			Toast toast = Toast.makeText(this,
					"没有任何搭配哦,sql~", Toast.LENGTH_SHORT);
			Log.i("myOutfit:",""+myOutfits);
			e.printStackTrace();
			toast.show();
		}
		findViewById(R.id.dlist_container).setOnTouchListener(new OnTouchListener() { 
		     @Override 
	        public boolean onTouch(View view, MotionEvent m) {
		    	 Log.i(TAG,"LinearLayout:onTouch action="+m.getAction()+"  isscroll:"+adapter1.isScroll());
//		    	 if(MotionEvent.ACTION_DOWN == m.getAction()){
//		     		adapter1.setScroll(true);
//		     		adapter2.setScroll(true);
		     	if(MotionEvent.ACTION_UP == m.getAction()){
		     		adapter1.setScroll(false);
		     		adapter2.setScroll(false);
		     	}
		    	 list1.dispatchTouchEvent(m);
		    	 list2.dispatchTouchEvent(m);
		    	 return true;
		     } 
	     });
		
		
		
		
		list1 = (ListView)findViewById(R.id.list1);
		list2 = (ListView)findViewById(R.id.list2);
		
		list1.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				//if the holder view clicked, do nothing
				if(arg2-1>=adapter1.matches.size())
					return;
				
				Log.i("bbb","list1:on item click "+ arg2);
				Intent intent = new Intent(MyMatchesActivity.this,ShowMyOutfitActitivity.class);
				MyOutfit thisOutfit = adapter1.matches.get(arg2-1);
				
				intent.putExtra("this_myOutfitUrl", thisOutfit.getOutfitUrl());
				intent.putExtra("isPublished", thisOutfit.isPublished());
				intent.putExtra("this_myOutfitId", thisOutfit.getOutfitID());
				startActivity(intent);
			}
			
		});
		list2.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				//if the holder view clicked, do nothing
				if(arg2-1>=adapter2.matches.size())
					return;
				
				//open detail page
				Intent intent = new Intent(MyMatchesActivity.this,ShowMyOutfitActitivity.class);
				MyOutfit thisOutfit = adapter2.matches.get(arg2-1);
				
				intent.putExtra("this_myOutfitUrl", thisOutfit.getOutfitUrl());
				intent.putExtra("isPublished", thisOutfit.isPublished());
				intent.putExtra("this_myOutfitId", thisOutfit.getOutfitID());
				startActivity(intent);
			}
			
		});
		
		
		//Add headview to listview
		LinearLayout header = new LinearLayout(this);
		AbsListView.LayoutParams lp  = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT,80);
		header.setLayoutParams(lp);
		list1.addHeaderView(header);
		list2.addHeaderView(header);
		
		
		//Hide the actionbar when scrolling
		list1.setOnScrollListener(new OnScrollListener() {
			@Override
		    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		    	
		    }

		    @Override
		    public void onScrollStateChanged(AbsListView view, int scrollState) {
		    	if(OnScrollListener.SCROLL_STATE_IDLE == scrollState){
		    		showActionBar = true;
					if (!someThreadRuning) {
						someThreadRuning = true;
						Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								if (showActionBar)
									MyMatchesActivity.this.getActionBar()
											.show();
								someThreadRuning = false;
							}
						}, 1000);
					}
		    	}else if(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == scrollState&&showActionBar){
		    		MyMatchesActivity.this.getActionBar().hide();
		    		showActionBar = false;
		    	}
		    }
		});
		
		loadbar = (LinearLayout)findViewById(R.id.load_status);
		loadbar.setVisibility(View.VISIBLE);
		adapter1 = new MyMatchListItemAdapter(this, 0);
		adapter2 = new MyMatchListItemAdapter(this, 1);
		
		
		// because the following constructions spend too much time,
		// so the UI keep nothing for several seconds
		// to avoid this, I use another thread to run it here:
		UIhandler = new Handler() {
			public void handleMessage(Message msg) {
				// ////////////////////////////////////////////
				Log.i("aaa", "start handle message ");
				super.handleMessage(msg);
				PageData data = (PageData) msg.obj;
				adapter1.addItems(data.getFirst());
				adapter2.addItems(data.getSecond());
				float height = data.getPlaceHolderViewHeight();
				if(height>0){
					adapter2.setPlaceHolderView(height);
				}else if(data.getPlaceHolderViewHeight()<0){
					adapter1.setPlaceHolderView(-height);
				}
				// ////////////////////////////////////////////
				loadbar.setVisibility(View.GONE);
			}
		};
		
		new LoadNewPageTask().start();

		list1.setAdapter(adapter1);
		list2.setAdapter(adapter2);
		

		listWidth = adapter1.listWidth;
		localAsyncImageLoader = new LocalAsyncImageLoader();
		adapter1.setLocalAsyncImageLoader(localAsyncImageLoader);
		adapter2.setLocalAsyncImageLoader(localAsyncImageLoader);
		adapter1.setListView(list1);
		adapter2.setListView(list2);
		
		if(myOutfits == null)
			getActionBar().setTitle("共有0张搭配");
		else 
			getActionBar().setTitle("共有"+myOutfits.size()+"张搭配");
		//getActionBar().setTitle("共有12张搭配");
		getActionBar().setHomeButtonEnabled(true);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		adapter1.setScroll(false);
		adapter2.setScroll(false);
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.my_matches, menu);
		return true;
	}
	
	// load the next page from the database
		private class LoadNewPageTask extends Thread {
			@Override
			public void run() {
				// connect to database
				Vector<MyOutfit> page = new Vector<MyOutfit>();
				try {
					Thread.sleep(2000);
					//testInitPage(page);
					initPage(page);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dispatchItems(page);
			}

		}

		//initialize one page just for test
		/*private void testInitPage(Vector<MyOutfit> page){
			
			for(int i = 0 ; i < 20 ; i++){
				page.add(new MyOutfit("url"+i,(int) (Math.random()*600+100),(int) (Math.random()*600+200)));
			}
			
		}*/
		private void initPage(Vector<MyOutfit> page){
			if(myOutfits == null)
				return;
			for(int i = 0;i<myOutfits.size();i++){
				MyOutfit outfit = myOutfits.get(i);
				page.add(outfit);
			}
		}
		
		//dispatch items to list
		private void dispatchItems(Vector<MyOutfit> page){
			PageData data = new PageData();
			float increasement = 0;
			float height1 = adapter1.getHeight();
			float height2 = adapter2.getHeight();
			MyOutfit item;
			for (int i = 0; i < page.size(); i++) {
				item = page.get(i);
				increasement = item.getPictureHeight()*listWidth/item.getPictureWidth()+DensityUtil.dip2px(this, 20);
				Log.i(TAG,"heigth1:"+height1+"     height2:"+  height2 +"increasement:"+increasement);
				if (height1 > height2) {
					data.putInSecond(item);
					height2 += increasement;
				} else {
					data.putInFirst(item);
					height1 += increasement;
				}
				
			}
			
			data.setPlaceHolderViewHeight(height1 - height2);
			Message msg = UIhandler.obtainMessage();
			msg.obj = data;
			UIhandler.sendMessage(msg);
			
		}
		private class PageData {
			private Vector<MyOutfit> left;
			private Vector<MyOutfit> right;
			private float placeHolderViewHeight=0;
			
			private void setPlaceHolderViewHeight(float height){
				placeHolderViewHeight = height;
			}
			
			private float getPlaceHolderViewHeight() {
				return placeHolderViewHeight;
			}
			
			private PageData() {
				left = new Vector<MyOutfit>();
				right = new Vector<MyOutfit>();
			}

			public void putInFirst(MyOutfit item) {
				left.add(item);
			}

			public void putInSecond(MyOutfit item) {
				right.add(item);
			}

			public Vector<MyOutfit> getFirst() {
				return left;
			}

			public Vector<MyOutfit> getSecond() {
				return right;
			}

		}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.action_add_collocation: 
			Dialog dialog = new UploadDialog(this,R.layout.upload_matches_dialog,
					new View.OnClickListener(){

						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							switch (v.getId()) {
							case R.id.upload_single_from_local:
								handlePic.dispatchTakePictureIntent(HandlePic.ACTION_GET_LOCAL_PHOTO);
								break;
							case R.id.upload_single_from_camera:
								handlePic.dispatchTakePictureIntent(HandlePic.ACTION_TAKE_PHOTO);
								break;
							case R.id.upload_single_from_chest:
								getIntent().removeExtra(CombinePicActivity.EXTRA_IMAGE_LIST);
								Bimp.drr.clear();
								Bimp.bmp.clear();
								Intent intent = new Intent(
										MyMatchesActivity.this,
										CombinePicActivity.class);
								startActivity(intent);
								//finish();
								break;
							}
						}
				
			});
			dialog.show();
			break;
		case android.R.id.home:
			this.finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case HandlePic.ACTION_GET_LOCAL_PHOTO:{
			if(resultCode == RESULT_OK){
				String urlString = handlePic.getmCurrentPhotoPath();
				handlePic.galleryAddPic();
				guiMiDB.addMyOutfit(urlString,null,false);
				Intent intent = new Intent(this,MyMatchesActivity.class);
				startActivity(intent);
				finish();
			}
			
		}break;
		case HandlePic.ACTION_TAKE_PHOTO:{
			if(resultCode == RESULT_OK){
				String urlString = handlePic.getmCurrentPhotoPath();
				Log.e("urlString",urlString);
				handlePic.galleryAddPic();
				guiMiDB.addMyOutfit(urlString,null,false);
				Intent intent = new Intent(this,MyMatchesActivity.class);
				startActivity(intent);
				finish();
			}
			
		}break;
		default:
			break;
		}
	}

	
}
