package cn.ingenic.launcher.home;

import java.util.Calendar;
import java.util.TimeZone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.AttributeSet;
import android.view.View;
import cn.ingenic.launcher.DB;
import cn.ingenic.launcher.PagedViews;
import cn.ingenic.launcher.R;

public class Clock extends View {
	  //时钟盘，分针、秒针、时针对象
	final Bitmap mBmpDial, mBmpHour, mBmpMinute, mBmpSecond;

    final BitmapDrawable bmdHour, bmdMinute, bmdSecond, bmdDial;

    Paint mPaint;

	final int mWidth, mHeigh;
	final int centerX, centerY;
	final int availableWidth, availableHeight;
	int mTempWidth, mTempHeigh;

    Time mt=new Time();
    final Handler tickHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			refresh();
			sendEmptyMessage(1);
		}
    	
    };
	public Clock(Context context, AttributeSet attrs){
		this(context,attrs,0);
	}
	public Clock(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		BitmapFactory.Options opt=new BitmapFactory.Options();
		opt.inDensity=120;
        mBmpHour = BitmapFactory.decodeResource(getResources(),
                R.drawable.widget_clock_hour_01);
        bmdHour = new BitmapDrawable(mBmpHour);

        mBmpMinute = BitmapFactory.decodeResource(getResources(),
                R.drawable.widget_clock_minute_01);
        bmdMinute = new BitmapDrawable(mBmpMinute);

        mBmpSecond = BitmapFactory.decodeResource(getResources(),
                R.drawable.widget_clock_second_01);
        bmdSecond = new BitmapDrawable(mBmpSecond);

        mBmpDial = BitmapFactory.decodeResource(getResources(),
                R.drawable.clock_01,opt);
        bmdDial = new BitmapDrawable(mBmpDial);
        mWidth = mBmpDial.getWidth();
        mHeigh = mBmpDial.getHeight();
        DB.log("clock w="+mWidth+", h=="+mHeigh);
        availableWidth=PagedViews.mPageWidth;
        availableHeight=PagedViews.mPageHeight;
        centerX = availableWidth / 2;
        centerY = availableHeight / 2;

        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        tickHandler.sendEmptyMessage(1);
	}

	private final void refresh() {
		postInvalidate();
	};

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mt.setToNow();
        int hour = mt.hour;
        int minute = mt.minute;
        int second = mt.second;
        float hourRotate = hour * 30.0f + minute / 60.0f * 30.0f;
        float minuteRotate = minute * 6.0f;
        float secondRotate = second * 6.0f;

        boolean scaled = false;

        if (availableWidth < mWidth || availableHeight < mHeigh) {
            scaled = true;
            float scale = Math.min((float) availableWidth / (float) mWidth,
                    (float) availableHeight / (float) mHeigh);
            canvas.save();
            canvas.scale(scale, scale, centerX, centerY);
        }

        bmdDial.setBounds(centerX - (mWidth / 2), centerY - (mHeigh / 2),
                centerX + (mWidth / 2), centerY + (mHeigh / 2));
        bmdDial.draw(canvas);

        mTempWidth = bmdHour.getIntrinsicWidth();
        mTempHeigh = bmdHour.getIntrinsicHeight();
        canvas.save();
        canvas.rotate(hourRotate, centerX, centerY);
        bmdHour.setBounds(centerX - (mTempWidth / 2), centerY
                - (mTempHeigh / 2), centerX + (mTempWidth / 2), centerY
                + (mTempHeigh / 2));
        bmdHour.draw(canvas);

        canvas.restore();

        mTempWidth = bmdMinute.getIntrinsicWidth();
        mTempHeigh = bmdMinute.getIntrinsicHeight();
        canvas.save();
        canvas.rotate(minuteRotate, centerX, centerY);
        bmdMinute.setBounds(centerX - (mTempWidth / 2), centerY
                - (mTempHeigh / 2), centerX + (mTempWidth / 2), centerY
                + (mTempHeigh / 2));
        bmdMinute.draw(canvas);

        canvas.restore();

        mTempWidth = bmdSecond.getIntrinsicWidth();
        mTempHeigh = bmdSecond.getIntrinsicHeight();
        canvas.rotate(secondRotate, centerX, centerY);
        bmdSecond.setBounds(centerX - (mTempWidth / 2), centerY
                - (mTempHeigh / 2), centerX + (mTempWidth / 2), centerY
                + (mTempHeigh / 2));
        bmdSecond.draw(canvas);

        if (scaled) {
            canvas.restore();
        }
    }
}
