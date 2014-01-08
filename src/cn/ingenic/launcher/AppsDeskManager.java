package cn.ingenic.launcher;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class AppsDeskManager {
	private static AppsDeskManager sInstance;
	
	public static boolean APPS_DESK_MODE = true; 
	
	public static int CELL_COUNT_X = 2;
	public static int CELL_COUNT_Y = 2;
	public static int CELL_COUNT = CELL_COUNT_X * CELL_COUNT_Y;//每屏放置的图标个数
	public static int DEFAULT_PAGE = 1;
	public static final int AppScreenStartX=2;//横向第三屏开始为app,前两屏为 Left,Home屏
	public static final int AppScreenStartY=0;//竖向第一屏开始为app
	public static final int ExtraX_Screen_size=2;// 0 is Left, 1 is Home page. 
	
	private final static String PREF_APPS_DESK = "pref_apps_desk";
	private final static String KEY_SCREEN_COUNT = "screen_count";
	
	private Application mApp;
	private Launcher mLauncher;
	private Workspace mWorkSpace;
	private PackageManager mPm;
	
	private AppsDeskManager(Launcher launcher, Workspace workspace){
		mLauncher = launcher;
		mApp = mLauncher.getApplication();
		mWorkSpace = workspace;
		mPm = launcher.getPackageManager();
//		loadConfig();
	}
	
	public static AppsDeskManager init(Launcher launcher, Workspace workspace){
		if(sInstance == null){
			sInstance = new AppsDeskManager(launcher, workspace);
		}
		return sInstance;
	}
	public static AppsDeskManager getInstance(){
		return sInstance;
	}
	
//	public static void release(){
//		sInstance.releaseAll();
//		sInstance = null;
//	}
	
/*	private void releaseAll(){
		mWorkSpace = null;
		mLauncher = null;
		mApp = null;
	}*/
	
/*	private void loadConfig(){
		//load screen
		mWorkSpace.removeAllViews();
		int count = mLauncher.SCREEN_COUNT;
		for (int i = 0; i < count; i++) {
			LayoutInflater inflate = LayoutInflater.from(mLauncher);
			CellLayout cell = (CellLayout) inflate.inflate(R.layout.workspace_screen, null);
			if(i == EXTRAS_MAIN_SCREEN){
				LinearLayout weatherTime = new ClockWeatherWidget(mLauncher);
				CellLayout.LayoutParams params = new CellLayout.LayoutParams(0, 0, CELL_COUNT_X, CELL_COUNT_Y);
				int childId = LauncherModel.getCellLayoutChildId(LauncherSettings.Favorites.CONTAINER_DESKTOP,
						i, 0, 0, CELL_COUNT_X, CELL_COUNT_Y);
				cell.addViewToCellLayout(weatherTime, 0, childId, params, true);
				cell.lock();
			}
			mWorkSpace.addView(cell);
		}
	}*/
	
	ArrayList<ItemInfo> mItems=new ArrayList<ItemInfo>();
	private List<ResolveInfo> queryAllApp(){
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return mPm.queryIntentActivities(mainIntent, 0);
	}
	
	public void initAppsIcon(){
        List<ResolveInfo> apps = queryAllApp();
		log(" query apps size = "+apps.size());
		mLauncher.mDB.clearDB();
		/**计算应用图标需要的屏数目*/
		int need_screen_count = apps.size() / CELL_COUNT + ((apps.size() % CELL_COUNT == 0) ? 0 : 1);
		// add screen
		final int count =need_screen_count + ExtraX_Screen_size;
		Runnable r = new Runnable(){
			public void run() {
				for (int j = 0; j < count; j++)
					mWorkSpace.addOneColumn(j);
			}};
		Launcher.runOnMainThreak(r);
		Launcher.SCREEN_COUNT += count;
		SharedPreferences pref = mApp.getSharedPreferences(PREF_APPS_DESK, 0);
		SharedPreferences.Editor edit = pref.edit();
		edit.putInt(KEY_SCREEN_COUNT, Launcher.SCREEN_COUNT);
		edit.commit();
		//load first screen
//		mApp.getLauncherProvider().loadDefaultFavorites();
		
		//add icon
        for(int i = 0; i < apps.size(); i++){
			addAppsInfoFromRI(apps.get(i), i);
        }
        addItemsToUI();
		/*mWorkSpace.initAllAppInfo(apps);*/
	}

	public void loadItemsFromDB(){
		if(mItems.size()==0){
			log("loadItemsFromDB, but mItems is  empty..So re-load from DB");
			mLauncher.mDB.queryFavs(mItems, mPm);	
		}
		log("loadItemsFromDB, mItems size="+mItems.size());
        addItemsToUI();
	}
	
	private void addItemsToUI(){

        Runnable r=new Runnable(){
			@Override
			public void run() {
				
				if(mWorkSpace.getChildCount()==0){
					int count = mItems.size() / CELL_COUNT
							+ ((mItems.size() % CELL_COUNT == 0) ? 0 : 1)
							+ ExtraX_Screen_size;
					for (int j = 0; j < count; j++){
						mWorkSpace.addOneColumn(j);
					}
					log("addItemsToUI 11 child empty, inflate now.workspace size = "+mWorkSpace.getChildCount());
				}else{
					log("addItemsToUI 22 celllayout size = "+mWorkSpace.getChildCount());
				}
				
				for(ItemInfo ai: mItems){
					Cell cell = ItemInfo2Cell(ai);
					mWorkSpace.addCell(cell);
				}
				mWorkSpace.invalidate();
				log("add app OVER");
			}};
		Launcher.runOnMainThreak(r);
	}

	/**根据ItemInfo初始化一个Cell实例并返回*/
	private Cell ItemInfo2Cell(ItemInfo ii) {
		Cell cell = (Cell) View.inflate(mLauncher, R.layout.cell, null);
		cell.setItemInfo(ii);
		cell.setIconTitle(ii.icon, ii.title);
		return cell;
	}
	
	private Intent getLaunchIntent(ResolveInfo reInfo,ItemInfo ii){
		if(reInfo == null){
			return null;
		}
		String packageName = reInfo.activityInfo.packageName;
		String name = reInfo.activityInfo.name;
		ComponentName cn = new ComponentName(packageName,name);
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setComponent(cn);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ii.activityName = name;
		return intent;
	}
	
	private void addAppsInfoFromRI(ResolveInfo info,int i){
		int screen = ExtraX_Screen_size + i / CELL_COUNT, //计入额外的屏,home屏,left屏
				cellX = i % CELL_COUNT % CELL_COUNT_X,
				cellY = i % CELL_COUNT / CELL_COUNT_X;
		ResolveInfo2ItemInfo(info,screen,cellX,cellY,null);
	}
	
	/**根据ResolveInfo、screen、x、y封装一个ItemInfo，添加到mItems
	 * 若不是初始时，还需添加对应app icon到相应屏
	 * */
	private void ResolveInfo2ItemInfo(ResolveInfo info ,int screen,int x,int y,CellLayout cl){
		final PackageManager pm = mPm;
	    ContentValues cv=new ContentValues(14);
	    ItemInfo ii = new ItemInfo();
	    String title = (String) info.loadLabel(pm);
		Intent intent = getLaunchIntent(info, ii);
	    cv.put(DB.Favorites.TITLE, title);
	    cv.put(DB.Favorites.SCREEN, screen);
	    cv.put(DB.Favorites.INTENT, intent.toString());
	    cv.put(DB.Favorites.CELLX, x);
	    cv.put(DB.Favorites.CELLY, y);
	    cv.put(DB.Favorites.PACKAGENAME, info.activityInfo.packageName);
	    cv.put(DB.Favorites.ACTIVITYNAME, info.activityInfo.name);
	    cv.put(DB.Favorites.URI, intent.toUri(0));
	    cv.put(DB.Favorites.ITEM_TYPE, DB.Favorites.ITEM_TYPE_APPLICATION);
        cv.put(DB.Favorites.ICON_TYPE, DB.Favorites.ICON_TYPE_RESOURCE);
		Drawable icon = info.loadIcon(pm);
//	    ii = new ItemInfo(i/CELL_COUNT, title, info.activityInfo.packageName, icon);
		ii.screen = screen;
		ii.title = title;
		ii.packageName = info.activityInfo.packageName;
		ii.icon = icon;
		ii.cellX = x;
		ii.cellY = y;
		ii.intent = intent;
        mItems.add(ii);
		if (cl != null) // 若不是初始化的时候(新安装apk)，需要添加Cell到对应屏
			addCellToCL(cl,ii);
		addShortcutToDb(cv);
	}
	private void addCellToCL(final CellLayout cl,ItemInfo ii){
		final Cell cell = ItemInfo2Cell(ii);
		Runnable r = new Runnable(){
			public void run() {
				cl.snapToThis();
				cl.addView(cell);
			}
		};
		Launcher.runOnMainThreak(r);
	} 
	
	private void addShortcutToDb(ContentValues cv){
            mLauncher.mDB.insertToDB(cv);
	}
	/**添加app图标时，屏不够，需要先添加屏，再添加app图标
	 * 在 work线程 调用
	 * @param col 添加的屏的列 索引
	 * @param packageName 添加的应用的包名
	 * */
	void addOneColAndApp(final int col,final String packageName){
//		log("CellLayout不够,添加celllayout后再添加cell..col="+col);
		final int screen = col;//
		final Runnable addCell = new Runnable() {
			public void run() {
				int index=mWorkSpace.getChildCount()-mWorkSpace.mMaxPageY+AppScreenStartY;
				CellLayout cl = (CellLayout) mWorkSpace.getChildAt(index);
				addForPackage(cl, packageName, screen, 0, 0);
			}
		};
		
		final Runnable addCL = new Runnable() {
			public void run() {
				mWorkSpace.addOneColumn(col);
				Launcher.runOnWorkThreak(addCell);
			}
		};
		Launcher.runOnMainThreak(addCL);
	}
	
	/**新安装 卸载了 更新了 应用
	 * @param packageNames 卸载的应用的包名
	 * @param op  操作类型 {@link #cn.ingenic.launcher.appchanged.AppInstallReceiver}
	 * */
	public void appChanged(String[] packageNames, int op) {
		if(op==2)
		logP("op:"+op,packageNames);
		mLauncher.appChanged(packageNames, op);
	}
	
	static void deleteForPackage(String packageName){
		if(sInstance!=null){
			sInstance.mLauncher.mDB.deleteForPackage(packageName);
		}
	}
	/**检索出packageName为pn的信息，并结合screen,x,y组装成ItemInfo
	 * 并封装成Cell，然后添加到对应屏;同时插入DB一条对应信息<br>
	 * 在 work线程调用
	 * @param pn 检索的包名
	 * @param screen 可添加的screen
	 * @param x 屏的横坐标 
	 * @param y 屏的竖坐标
	 * */
	static void addForPackage(CellLayout cl,String pn, int screen, int x, int y) {
		if (sInstance == null)
			return;
		 List<ResolveInfo> apps = sInstance.queryAllApp();
		 for(ResolveInfo ri : apps){
			 if(pn.equals(ri.activityInfo.packageName)){
				 sInstance.ResolveInfo2ItemInfo(ri,screen,x,y,cl);
				 break;//:TODO 若一个app中含多个launch的activity，需另处理
			 }
		 }
	}

	private void logP(String op,String[] packageNames){
		int N = packageNames.length;
		String log="";
		for(int i=0;i<N;i++)
			log += packageNames[i];
		Toast.makeText(mApp, op+ " update apk\n"+log, Toast.LENGTH_LONG).show();
	}
	
	private void log(String log){
		Log.i("sw2df", "[AppsDeskManager]"+log);
	}
}