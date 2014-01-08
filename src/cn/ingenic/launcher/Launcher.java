package cn.ingenic.launcher;

import java.util.Locale;
import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

public class Launcher extends Activity {
	static String TAG = "[Launcher]";
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
		final String locale = Locale.getDefault().toString();
		boolean localeChanged=!locale.equals(mSharedPrefs.getString(Utils.KEY_Locale, " "));
		mDB = new DB(this, Utils.DB_NAME, null, 1);
		setContentView(R.layout.launcher);
		mWorkspace = (Workspace) findViewById(R.id.workspace);
		findViewById(R.id.layer).setBackground(null);
		mWorkspace.setLauncher(this);
		mAppsDeskManager = AppsDeskManager.init(this, mWorkspace);

		if (mIsFirstLoad || localeChanged) {
			getProgessDialog().show();
			new FirstLoad().execute(1);
			new Thread("SetNoFirstLoad") {
				public void run() {
					SharedPreferences.Editor editor = mSharedPrefs.edit();
					editor.putBoolean(Utils.KEY_isFirstLoad, false);
					editor.putString(Utils.KEY_Locale, locale);
					editor.commit();
				}
			}.start();
			mIsFirstLoad = false;
		} else/* if (!mInitedUI) */{
			getProgessDialog().show();
			new LoadWorkspace().execute(1);
			mInitedUI = true;
		}/*else{
			DB.log("not first load . and already init UI");
		}*/
		DB.log("on create");
	}

	@Override
	protected void onResume() {
		super.onResume();
		DB.log("on resume");
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		DB.log("on pause");
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
	protected void onDestroy() {
		super.onDestroy();
		((ViewGroup) mWorkspace.getParent()).removeAllViews();
		mWorkspace.removeAllViews();
		mWorkspace=null;
		DB.log("on destroy");
		android.os.Process.killProcess(android.os.Process.myPid());
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
			DB.log("key BACK/HOME up,code "+keyCode);
			mWorkspace.snapToHome();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.launcher, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
		case R.id.action_settings:
			Intent i=new Intent(Intent.ACTION_VIEW);
			i.setClassName("com.android.settings", "com.android.settings.Settings");
			startActivity(i);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void startSetWallpaper() {
		final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
		Intent chooser = Intent.createChooser(pickWallpaper, "Set WallPaper");
		startActivityForResult(chooser, 10);
    }

	void appChanged(String[] packageNames, int op) {
		if (mWorkspace != null)
			mWorkspace.appChanged(packageNames, op);
	}

	static void runOnMainThreak(Runnable r){
		sUI.post(r);
	}
	public static void runOnWorkThreak(Runnable r){
		sWorker.post(r);
	}
	
	
	
	private Dialog getProgessDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setCancelable(false);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setMessage(getString(R.string.loading));
		}
		return mProgressDialog;
	}
	class FirstLoad extends AsyncTask<Integer, Void, Void>{
		@Override
		protected Void doInBackground(Integer... params) {
			long timer=System.currentTimeMillis();
			//初始化workspace，加载所有应用信息到DB
			mAppsDeskManager.initAppsIcon();
			timer=System.currentTimeMillis()-timer;
			DB.log("[Launcher]task FirstLoad use time = "+timer);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			getProgessDialog().hide();
			mWorkspace.initToHome();
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
			mWorkspace.initToHome();
		}
	}

}
