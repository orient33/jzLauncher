package cn.ingenic.launcher;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;

/**
 * 存储一些固定的值的帮助类
 * */
public class Utils {

	/** shared_pref name*/
	static final String PREF="launch.xml";
	static final String KEY_isFirstLoad="Is_First_Load";
	
	/** DB authority  */
//	static final String AUTHORITY = "cn.ingenic.launcher.settings";
	/** DB tables's name  */
    static final String TABLE_FAVORITES = "favorites";
//    static final String PARAMETER_NOTIFY = "notify";

    
    
    /**  draw a icon on background, For  like MIUI's icon <br>
     *  为app图标添加背景图片，类似小米桌面
     *  background is larger than the icon    (dfdun add;)*/
    public static Bitmap mergeTwoImg(Bitmap background,Bitmap icon){
        int w=background.getWidth(), h=background.getHeight(),
         ww=icon.getWidth(),hh=icon.getHeight();
        Bitmap newb= Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas c=new Canvas(newb);
        c.drawBitmap(background, 0, 0, null);
        c.drawBitmap(icon, (w-ww)/2, (h-hh)/2, null);
        c.save(Canvas.ALL_SAVE_FLAG);
        c.restore();
        return newb;
    }
    
    /**
     * resize a bitmap ; (dfdun add)
     * 调整图片的大小
     * @param src  the bitmap need to resize
     * @param b   if b > 1 ,larger ,if(b<1) littler
     * */
    public static Bitmap scaleBitmap(Bitmap src ,float b){
        int width = src.getWidth(), height=src.getHeight();
        Matrix m = new Matrix();
        m.postScale(b, b);
        return Bitmap.createBitmap(src, 0, 0, width, height, m, true);
    }
}
