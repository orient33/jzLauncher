package cn.ingenic.launcher;

import java.util.ArrayList;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DB extends SQLiteOpenHelper {
	final String Table=Utils.TABLE_FAVORITES;
	Context mContext;

	static final class Favorites implements BaseColumns {
		static final String TITLE = "title";				// 1
		static final String INTENT = "intent";				// 2
		static final String SCREEN = "screen";				// 3
		static final String CELLX = "cellX";				// 4
		static final String CELLY = "cellY";				// 5
		static final String SPANX = "spanX";				// 6
		static final String SPANY = "spanY";				// 7
		static final String ITEM_TYPE = "itemType";			// 8
			static final int ITEM_TYPE_APPLICATION = 0;
			static final int ITEM_TYPE_SHORTCUT = 1;
		static final String APPWIDGET_ID = "appWidgetId";	// 9
		static final String ICON_TYPE = "iconType";			// 10
			static final int ICON_TYPE_RESOURCE = 0;
			static final int ICON_TYPE_BITMAP = 1;
		static final String ICON_PACKAGE = "iconPackage";	// 11
		static final String ICON_RESOURCE = "iconResource";	// 12
		static final String ICON = "icon";					// 13
		static final String PACKAGENAME = "packageName";	// 14
		static final String ACTIVITYNAME="activityName";	// 15
		static final String URI = "uri";					// 16
	}
	
	public DB(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql="CREATE TABLE if not exists "+Table+" ( "+
				"_id INTEGER PRIMARY KEY," +
                "title TEXT," +				// 1 title
                "intent TEXT," +			// 2 intent
                "screen INTEGER," +			// 3 screen
                "cellX INTEGER," +			// 4
                "cellY INTEGER," +			// 5
                "spanX INTEGER," +			// 6
                "spanY INTEGER," +			// 7
                "itemType INTEGER," +		// 8
                "appWidgetId INTEGER NOT NULL DEFAULT -1," +	// 9
                "iconType INTEGER," +		// 10
                "iconPackage TEXT," +		// 11
                "iconResource TEXT," +		// 12
                "icon BLOB," +				// 13
                "packageName TEXT,"+		// 14
                "activityName TEXT,"+ 		// 15
                "uri TEXT )" ;				// 16
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}
	
	void clearDB(){
		SQLiteDatabase d=this.getWritableDatabase();
		String sql="delete  from "+Table +" where cellX <> -1;";
		try{
		d.execSQL(sql);
		}catch(android.database.SQLException e){
			log(""+e.toString());
		}
	}
	
	void queryFavs(ArrayList<ItemInfo> items,PackageManager pm) {
		SQLiteDatabase d=this.getReadableDatabase();
		Cursor c=d.query(Table, null, null, null, null, null, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			ItemInfo ii=new ItemInfo();
			ii.screen=c.getInt(c.getColumnIndex(Favorites.SCREEN));
			ii.cellX=c.getInt(c.getColumnIndex(Favorites.CELLX));
			ii.cellY=c.getInt(c.getColumnIndex(Favorites.CELLY));
			ii.title=c.getString(c.getColumnIndex(Favorites.TITLE));
//			ii.intent=c.getString(c.getColumnIndex(Favorites.INTENT));
			ii.packageName=c.getString(c.getColumnIndex(Favorites.PACKAGENAME));
			ii.activityName=c.getString(c.getColumnIndex(Favorites.ACTIVITYNAME));
			Intent intent = new Intent(Intent.ACTION_MAIN);
			ComponentName cn =new ComponentName(ii.packageName,ii.activityName);
			intent.setComponent(cn);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ii.intent=intent;
			try {
				ii.icon=pm.getActivityIcon(cn);
			} catch (NameNotFoundException e) {
				log("[Error] queryFavs() "+e.toString());
			}
			items.add(ii);
			c.moveToNext();
		}
		c.close();
	}
	
	void insertToDB(ContentValues cv){
		SQLiteDatabase d=this.getWritableDatabase();
		d.insert(Table, null, cv);
		d.close();
	}

	static void log(String msg){
		Log.d("sw2df", msg);
	}
}
