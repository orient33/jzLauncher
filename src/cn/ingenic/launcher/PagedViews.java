/**@author dfdun*/
package cn.ingenic.launcher;

import java.lang.reflect.Field;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.Scroller;

/**
 * 可以平滑的在水平、竖直方向滑动的ViewGroup、GridLayout；
 * 类似与Launcher的桌面滑动效果。
 * */
public class PagedViews extends GridLayout {
	static final int PAGE_LEFT = 1, PAGE_TOP = 2, PAGE_RIGHT = 3,
			PAGE_BOTTOM = 4;
	static final int SCROLL_DURATION = 1500;// 动画时间
	static final int SPEC_UNDEFINED = ViewGroup.LayoutParams.MATCH_PARENT;
	static final int SNAP_VELOCITY = 500;
	static final int STATE_STATIC = 0; // 静止状态..
	static final int STATE_SCROLLING = 1; // 正在滚动状态.x
	static final int STATE_SCROLLINGY = 2; // 正在滚动状态.y
	static final int MIN_X_SWITCH_PAGE = 30, MIN_Y_SWITCH_PAGE = 30;

	protected float mDownMotionX, mDownMotionY, mLastMotionX, mLastMotionY;

	private Scroller mScroller;
	private int mSlopDistance;// 滑动中拖动的最小像素
	private int state = STATE_STATIC;// 当前的状态, 是否在滚动...

	int mPageWidth, mPageHeight;
	int mCurrentPageX = 0, mCurrentPageY = 0;
	final int mMaxPageY;// row count 01234
	int mMaxPageX = 0; // column count 01234
	
	public PagedViews(Context context) {
		this(context, null);
	}

	public PagedViews(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PagedViews(Context context, AttributeSet attrs, int def) {
		super(context, attrs, def);
		Resources res = getResources();
		DisplayMetrics dm = res.getDisplayMetrics();
		final ViewConfiguration conf = ViewConfiguration.get(context);
		mSlopDistance = conf.getScaledTouchSlop();// 8dp
		state = STATE_STATIC;
		mCurrentPageX = mCurrentPageY = 0;
		int h = dm.heightPixels/*240*/, w = dm.widthPixels;/*240*/
		log("(ScreenSize)DisplayMetrics : width=" + w + ", height=" + h );

		mMaxPageY = getRowCount();
		mPageWidth = w;
		mPageHeight = h - getStatusBarHeight();
		mScroller = new Scroller(context);

	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b){
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			CellLayout child=(CellLayout)getChildAt(i);
			int ll = child.x*mPageWidth, tt = child.y*mPageHeight;
			child.layout(ll, tt, ll+mPageWidth, tt+mPageHeight);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		if(this.getChildCount()<1){
			return false;
		}
		if (action == MotionEvent.ACTION_MOVE && state != STATE_STATIC) {
			// MOVE 且 非静止 下，返回true，使事件不传递给子view，而是本View处理(onTouchEent)
			return true;
		}
		final float x = ev.getX(), y = ev.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mLastMotionY = y;
			state = STATE_STATIC;
			final int xDist=Math.abs(mScroller.getFinalX()-mScroller.getCurrX());
			final int yDist=Math.abs(mScroller.getFinalY()-mScroller.getCurrY());
			if(mScroller.isFinished()||xDist<mSlopDistance||yDist<mSlopDistance){
				mScroller.abortAnimation();
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (state == STATE_STATIC) {
				// 静止时,根据move的位移决定是否设置滚动状态
				checkScrolling(x, y);
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			enableCache(false);
			state = STATE_STATIC;
			break;
		}
		return state != STATE_STATIC;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		final float x = ev.getX(), y = ev.getY();
		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = mDownMotionX = x;
			mLastMotionY = mDownMotionY = y;
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			break;
		case MotionEvent.ACTION_MOVE: {
			if(getChildCount()<1) break;
			if (state == STATE_STATIC) {
				checkScrolling(x, y);
			} else if (state == STATE_SCROLLING) {
				int dx = (int) (mLastMotionX - x);
				if (getScrollX() < 0
						|| getScrollX() > getChildAt(getChildCount() - mMaxPageY)
								.getLeft()) {
					dx /= 4;// 越界时,位移减少
				}
				scrollBy(dx, 0);
			} else if (state == STATE_SCROLLINGY) {
				int dy = (int) (mLastMotionY - y);
				if (getScrollY() < 0
						|| getScrollY() > getChildAt(mMaxPageY - 1).getTop()) {
					dy /= 4;// 越界时,位移减少
				}
				scrollBy(0, dy);
			}
			mLastMotionX = x;
			mLastMotionY = y;
			break;
		}
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (state == STATE_SCROLLING) {
				int startX = getScrollXByPage(mCurrentPageX);
				int whichPage = mCurrentPageX;
				int xSpace = getWidth() / 8;
				if (getScrollX() < startX - xSpace) {
					whichPage = Math.max(0, whichPage - 1);
				} else if (getScrollX() > startX + xSpace) {
					whichPage = Math.min(mMaxPageX - 1, whichPage + 1);
				}
//				log("up , currentx=" + mCurrentPageX + ", whichpage="
//						+ whichPage+",state="+state);
				smoothScrollToPage(whichPage, mCurrentPageY);
			} else if (state == STATE_SCROLLINGY) {
				int startY = getScrollYByPage(mCurrentPageY);
				int whichPage = mCurrentPageY;
				int ySpace = getHeight() / 8;
				if (getScrollY() < startY - ySpace) {
					whichPage = Math.max(0, whichPage - 1);
				} else if (getScrollY() > startY + ySpace) {
					whichPage = Math.min(mMaxPageY - 1, whichPage + 1);
				}
//				log("up , currenty=" + mCurrentPageY + ", whichpage="
//						+ whichPage+",yspace="+ySpace);
				smoothScrollToPage(mCurrentPageX, whichPage);
			} else {// make sure show Page normally.
				smoothScrollToPage(mCurrentPageX, mCurrentPageY);
			}
			state = STATE_STATIC;
			break;
		default:
			return super.onTouchEvent(ev);
		}
		return true;
	}

	// 和 scroller 协作的关键，
	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset() && !mScroller.isFinished()) {
			int oldx = getScrollX(), oldy = getScrollY();
			int x = mScroller.getCurrX(), y = mScroller.getCurrY();
			if (oldx != x || oldy != y)
				scrollTo(x, y);
			// log("compute scroll: ox"+oldx+",oy="+oldy+"; x="+x+",y="+y);
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	/**
	 * 向 (row,column)添加一个child，未测。慎用。
	 * */
	void addView(int row, int column, View v) {
		if (row > 0 && row < mMaxPageY) {
			int index = row + column * mMaxPageY;
			removeViewAt(index);
			addView(v, index);
		} else if (row == 0) {
			addView(v);
			for (int i = 1; i < row; i++)
				addView(v);// :TODO should be null view
		}
	}
	
	void addMaxPageX(){
		mMaxPageX++;
		log("mMaxPageX = "+mMaxPageX);
	}
	
	int getScreenColumnCount(){
//		int r=getChildCount()%mMaxPageY;
//		if (r != 0)
//			log("getScreenColumnCount % =="+r);
		return getChildCount()/mMaxPageY;
	}
	
	private void log(String s) {
		Log.i("sw2df", "[PagedViews]" + s);
	}
	/**
	 * 获取页面(pagex,yyyyy)的左边缘、水平位置，
	 * @param pagex 页面的横坐标
	 * @see #android.view.View.getScrollX()*/
	private int getScrollXByPage(int pagex) {
		return pagex * mPageWidth - getPagePaddingX();
	}
	/**
	 * 获取页面(xxxxx,pagey)的左边缘、水平位置，
	 * @param pagey 页面的竖坐标
	 * @see #android.view.View.getScrollY()*/
	private int getScrollYByPage(int pagey) {
		return pagey * mPageHeight - getPagePaddingY();
	}

	/** 获取页面的水平padding */
	private int getPagePaddingX() {
		return (getMeasuredWidth() - mPageWidth) >> 1;
	}
	/** 获取页面的竖直padding */
	private int getPagePaddingY() {
		return (getMeasuredHeight() - mPageHeight) >> 1;
	}
	/**
	 * 平滑的滚动到页面(pagex,pagey)
	 * @param pagex 目标页面的横坐标
	 * @param pagey 目标页面的竖坐标
	 * */
	void smoothScrollToPage(int pagex, int pagey) {
		if (pagex < 0 || pagey < 0 || pagex > mMaxPageX - 1
				|| pagey > mMaxPageY - 1)
			return;
		if (!mScroller.isFinished())
			return;
		enableCache(true);
		boolean isSame = (pagex == mCurrentPageX && mCurrentPageY == pagey);
		View focused = getFocusedChild();
		if (!isSame && focused != null && focused == getCurrentDisplayView()) {
			focused.clearFocus();
		}
		int duration = SCROLL_DURATION;
		int startx = getScrollX(), starty = getScrollY();
		if (isSame) {
			int x = getScrollXByPage(pagex), y = getScrollYByPage(pagey);
			int dx = x - startx, dy = y - starty;
			if (dx != 0 && dy != 0) {
				log("[ERROR]this is not normal.dx != 0 && dy != 0");
				if (dx >= dy) {
					duration = duration * Math.abs(dx) / mPageWidth;
				} else {
					duration = duration * Math.abs(dy) / mPageHeight;
				}
				mScroller.startScroll(startx, starty, dx, dy, duration);
			} else if (dx != 0) {
				if (Math.abs(dx) < mPageWidth) {
					duration = duration * Math.abs(dx) / mPageWidth;
				}
				mScroller.startScroll(startx, starty, dx, 0, duration);
			} else if (dy != 0) {
				if (Math.abs(dy) < mPageHeight) {
					duration = duration * Math.abs(dy) / mPageHeight;
				}
				mScroller.startScroll(startx, starty, 0, dy, duration);
			} else if (dx == 0 && dy == 0) {
				log("[ERROR]this is not normal. dx == 0 && dy == 0");
				return;
			}

		} else if (pagex != mCurrentPageX) {
			mCurrentPageX = pagex;
			int x = getScrollXByPage(pagex);
			int dx = x - startx;
			if (Math.abs(dx) < mPageWidth) {
				duration = duration * Math.abs(dx) / mPageWidth;
			}
			mScroller.startScroll(startx, starty, dx, 0, duration);
		} else if (pagey != mCurrentPageY) {
			mCurrentPageY = pagey;
			int y = getScrollYByPage(pagey);
			int dy = y - starty;
			if (Math.abs(dy) < mPageHeight) {
				duration = duration * Math.abs(dy) / mPageHeight;
			}
			mScroller.startScroll(startx, starty, 0, dy, duration);
		}
		ViewCompat.postInvalidateOnAnimation(this);
	}
	/** 开始滑动时设置允许使用缓存。结束时取消缓存
	 * @param enable  是否启用缓存*/
	private void enableCache(boolean enable) {
		setChildrenDrawingCacheEnabled(enable);
		setChildrenDrawnWithCacheEnabled(enable);
	}

	/**
	 * 根据点(mLastMotionX,mLastMotionY)和点(x,y)之间的位移确认滚动的状态和方向<br>
	 * 设置成员变量
	 */
	private void checkScrolling(float x, float y) {
		float diffx = Math.abs(x - mLastMotionX);
		float diffy = Math.abs(y - mLastMotionY);
		// log("check scroll , diffx="+diffx+" ; diffy="+diffy);
		if (diffx > diffy && diffx > mSlopDistance) {
			state = STATE_SCROLLING;
			enableCache(true);
			mLastMotionX = x;
			mLastMotionY = y;
		} else if (diffy > diffx && diffy > mSlopDistance) {
			state = STATE_SCROLLINGY;
			enableCache(true);
			mLastMotionX = x;
			mLastMotionY = y;
		}
	}

	private View getCurrentDisplayView() {
		return getChildAt(mCurrentPageX * mMaxPageY + mCurrentPageY);
	}
	
	private int getStatusBarHeight(){
		Class<?> c = null;
		Object obj = null;
		Field field = null;
		int x = 0, sbar = 0;
		try {
		    c = Class.forName("com.android.internal.R$dimen");
		    obj = c.newInstance();
		    field = c.getField("status_bar_height");
		    x = Integer.parseInt(field.get(obj).toString());
		    sbar = getResources().getDimensionPixelSize(x);
		    DB.log("get status bar height OK, =="+sbar);
		    return sbar;
		} catch (Exception e1) {
		    DB.log("get status bar height fail");
		    e1.printStackTrace();
		} 
		return 0;
	}
}
