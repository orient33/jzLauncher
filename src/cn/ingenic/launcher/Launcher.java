package cn.ingenic.launcher;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.view.WindowManager;

public class Launcher extends Activity {

	static int SCREEN_COUNT = 0;//screen的列数
	boolean mIsFirstLoad;
	boolean mInitedUI;
	private static final HandlerThread sWorkerThread = new HandlerThread("launcher-loader");
    static {
        sWorkerThread.start();
    }
    private static final Handler sWorker = new Handler(sWorkerThread.getLooper());
    private static final Handler sUI = new Handler();
	private static ProgressDialog mProgressDialog;
	SharedPreferences mSharedPrefs;
	Application mApp;
	Workspace mWorkspace;
	DB mDB;
	AppsDeskManager mAppsDeskManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSharedPrefs = getSharedPreferences(Utils.PREF, 0);
		mApp = getApplication();
		mIsFirstLoad = mSharedPrefs.getBoolean(Utils.KEY_isFirstLoad, true);
		
		mDB = new DB(this, "launcher.db", null, 1);
		setContentView(R.layout.launcher);
		mWorkspace = (Workspace) findViewById(R.id.workspace);
		findViewById(R.id.layer).setBackground(null);
		mAppsDeskManager = AppsDeskManager.init(this, mWorkspace);

		if (mIsFirstLoad) {
			getProgessDialog().show();
			new FirstLoad().execute(1);
			new Thread("SetNoFirstLoad") {
				public void run() {
					SharedPreferences.Editor editor = mSharedPrefs.edit();
					editor.putBoolean(Utils.KEY_isFirstLoad, false);
					editor.commit();
				}
			}.start();
			mIsFirstLoad = false;
		} else if (!mInitedUI) {
			getProgessDialog().show();
			new LoadWorkspace().execute(1);
			mInitedUI = true;
		}else{
			;
		}
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		((ViewGroup) mWorkspace.getParent()).removeAllViews();
		mWorkspace.removeAllViews();
		mWorkspace=null;
		DB.log("on destroy");
	}


	@Override
	protected void onPause() {
		super.onPause();
		DB.log("on pause");
	}


	@Override
	protected void onResume() {
		super.onResume();
		DB.log("on resume");
	}

	@Override
	protected void onStop() {
		super.onStop();
		DB.log("on stop");
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}
    @Override
     public boolean dispatchKeyEvent(KeyEvent event) {
         if (event.getAction() == KeyEvent.ACTION_DOWN) {
             switch (event.getKeyCode()) {
                 case KeyEvent.KEYCODE_HOME:
                     return true;
             }
         } else if (event.getAction() == KeyEvent.ACTION_UP) {
             switch (event.getKeyCode()) {
                 case KeyEvent.KEYCODE_HOME:
                	 DB.log("HOME key .up");
                     return true;
             }
         }
 
         return super.dispatchKeyEvent(event);
     }
    


	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK||keyCode==KeyEvent.KEYCODE_HOME){
			DB.log("key BACK/HOME up");
			mWorkspace.snapToHome();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	static void runOnMainThreak(Runnable r){
		sUI.post(r);
	}
	static void runOnWorkThreak(Runnable r){
		sWorker.post(r);
	}
	
	
	private Dialog getProgessDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setMessage("Loading...");
		}
		return mProgressDialog;
	}
	class FirstLoad extends AsyncTask<Integer, Void, Void>{
		long timer;
		@Override
		protected Void doInBackground(Integer... params) {
			timer=System.currentTimeMillis();
			//初始化workspace，加载所有应用信息到DB
			mAppsDeskManager.initAppsIcon();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			getProgessDialog().hide();
			timer=System.currentTimeMillis()-timer;
			DB.log("[Launcher]task FirstLoad use time = "+timer);
		}
	}

	class LoadWorkspace extends AsyncTask<Integer, Void, Void> {

		@Override
		protected Void doInBackground(Integer... params) {
			long timer = System.currentTimeMillis();
			mAppsDeskManager.loadItemsFromDB();
			timer = System.currentTimeMillis() - timer;
			DB.log("[Launcher]task LoadWorkspace use time = " + timer);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			getProgessDialog().hide();
		}
	}
}
