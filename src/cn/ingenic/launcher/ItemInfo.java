package cn.ingenic.launcher;
/*
 * Copyright (C) 2013  Ingenic.cn
 *
 */


import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Represents an item in the launcher.
 * 代表桌面上的一个条目信息：一个app icon或者widget
 */
class ItemInfo {
    
    static final int NO_ID = -1;
    
    /**
     * The id in the settings database for this item
     */
    long id = NO_ID;
    
    /**
     * 条目类型： app icon还是widget
     */
    int itemType;
    
    /**
     * The id of the container that holds this item. For the desktop, this will be 
     * {@link DBHepler.Favorites#CONTAINER_DESKTOP}. For the all applications folder it
     * will be {@link #NO_ID} (since it is not stored in the settings DB). 
     */
    long container = NO_ID;
    
    /**
     * indicates the screen in which the shortcut appears.
     * 指示着这个条目在哪一屏，
     */
    int screen = -1;
    
    /**
     * Indicates the X position of the associated cell.
     * 指示这个条目的 x坐标
     */
    int cellX = -1;

    /**
     * Indicates the Y position of the associated cell.
     * 指示这个条目的 y坐标
     */
    int cellY = -1;

    /**
     * Indicates the X cell span.
     * 指示这个条目横向 占据了 几格
     */
    int spanX = 1;

    /**
     * Indicates the Y cell span.
     * 指示这个条目竖向 占据了 几格
     */
    int spanY = 1;

    /**
     * The position of the item in a drag-and-drop operation.
     * 拖拽中的位置,目前不用
     */
    int[] dropPos = null;

    String title;
    String packageName,activityName;
    Intent intent;
    Drawable icon;
    
    ItemInfo() {
    }
    ItemInfo(int s,String t,String pn,Drawable ic){
		screen = s;
		title = t;
		packageName = pn;
		icon = ic;
    }
    ItemInfo(ItemInfo info) {
        id = info.id;
        cellX = info.cellX;
        cellY = info.cellY;
        spanX = info.spanX;
        spanY = info.spanY;
        screen = info.screen;
        itemType = info.itemType;
        container = info.container;
    }

    /** Returns the package name that the intent will resolve to, or an empty string if
     *  none exists. */
    static String getPackageName(Intent intent) {
        if (intent != null) {
            String packageName = intent.getPackage();
            if (packageName == null && intent.getComponent() != null) {
                packageName = intent.getComponent().getPackageName();
            }
            if (packageName != null) {
                return packageName;
            }
        }
        return "";
    }

	/** 屏数 和 celllayout的位置的转化 */
	static void screenToXY(int screen, int xy[]) {
		xy[0] = screen % 1000;
		xy[1] = screen / 1000;
	}
	/** 屏数 和 celllayout的位置的转化 */
	static int xyToScreen(int x, int y) {
		return x + y * 1000;
	}

    static byte[] flattenBitmap(Bitmap bitmap) {
        // Try go guesstimate how much space the icon will take when serialized
        // to avoid unnecessary allocations/copies during the write.
        int size = bitmap.getWidth() * bitmap.getHeight() * 4;
        ByteArrayOutputStream out = new ByteArrayOutputStream(size);
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            Log.w("Favorite", "Could not write icon");
            return null;
        }
    }

//    static void writeBitmap(ContentValues values, Bitmap bitmap) {
//        if (bitmap != null) {
//            byte[] data = flattenBitmap(bitmap);
//            values.put(DBHepler.Favorites.ICON, data);
//        }
//    }

    /**
     * It is very important that sub-classes implement this if they contain any references
     * to the activity (anything in the view hierarchy etc.). If not, leaks can result since
     * ItemInfo objects persist across rotation and can hence leak by holding stale references
     * to the old view hierarchy / activity.
     */
    void unbind() {
    }

    @Override
    public String toString() {
        return "Item(id=" + this.id + " type=" + this.itemType + " container=" + this.container
            + " screen=" + screen + " cellX=" + cellX + " cellY=" + cellY + " spanX=" + spanX
            + " spanY=" + spanY +  " dropPos=" + dropPos + ")";
    }
}
