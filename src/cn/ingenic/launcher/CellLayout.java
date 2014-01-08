package cn.ingenic.launcher;

import java.util.HashMap;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import cn.ingenic.launcher.home.Clock;

public class CellLayout extends GridLayout{
	private static String TAG="[CellLayout]";
	/** 指示此 屏 所在的坐标 */
	int x, y;
	/** cell 横竖向的个数*/
	int mCountX = AppsDeskManager.CELL_COUNT_X,
		mCountY = AppsDeskManager.CELL_COUNT_Y;
	/** cell的宽高 */
	private int mCellWidth, mCellHeight;
	/** 其子cell的所有索引 */
	private HashMap<String , Cell> mCells=new HashMap<String,Cell>();

	public CellLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public CellLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a=context.obtainStyledAttributes(attrs,R.styleable.CellLayout);
		mCellWidth = a.getDimensionPixelSize(R.styleable.CellLayout_cellWidth, Utils.CellDefalutWidth);
		mCellHeight=a.getDimensionPixelSize(R.styleable.CellLayout_cellHeight, Utils.CellDefalutHeight);
		a.recycle();
		// A ViewGroup usually does not draw, but CellLayout needs to draw a rectangle
		// to show the user where a dragged item will land when dropped.
		setWillNotDraw(false);
		setClipToPadding(false);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b){
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			View child=getChildAt(i);
			if(child instanceof Cell){
				Cell c = (Cell)child;
				int ll=c.mItemInfo.cellX * mCellWidth,tt=c.mItemInfo.cellY * mCellHeight;
				int spanx=mCellWidth*c.mItemInfo.spanX,spany=mCellHeight*c.mItemInfo.spanY;
				c.layout(ll, tt, ll+spanx, tt+spany);
			}else if(child instanceof Clock){
				int w=mCellWidth * AppsDeskManager.CELL_COUNT_X,h=
						mCellHeight * AppsDeskManager.CELL_COUNT_Y;
				child.layout(0, 0, w, h);
			}
		}
	}

	@Override
	public void removeView(View child){
		super.removeView(child);
		if(child instanceof Cell){
			String key = getKeyForCell((Cell) child);
			if (mCells.containsKey(key))
				mCells.remove(key);
		}
	} 
	
	@Override
	public void addView(View child) {
		if(child instanceof Cell){
			Cell c = (Cell)child;
			int temp[]={0,0};
			ItemInfo.screenToXY(c.mItemInfo.screen, temp);
			if(temp[0]!=this.x || temp[1]!=this.y){
				// 检查屏是否正确
				DB.log(TAG+"add view. cell screen. is error");
				return;
			}
			String key = getKeyForCell((Cell) child);
			if(mCells.containsKey(key)){
				//检查cell的位置是否已添加
				DB.log(TAG+"add view. cell x,y is already op");
			}else{
//				ViewGroup.LayoutParams lp=child.getLayoutParams();// is null
//				DB.log(TAG+"add view. cell w="+child.getMeasuredWidth()+",h="+child.getMeasuredHeight());// both is 0.
//				DB.log(TAG+"add view. cell w="+child.getWidth()+",h="+child.getHeight());// both is 0.
				ViewGroup.LayoutParams lp=new ViewGroup.LayoutParams(mCellWidth,mCellHeight);
				super.addView(child, lp);
				if (!mCells.containsKey(key))
					mCells.put(key, c);
			}
			
		}else {
			DB.log(TAG+" child is not Cell");
		}
	}
	/**滑动到这个屏； 应从UI线程调用
	 * */
	void snapToThis(){
		((Workspace)this.getParent()).snapToCellLayout(this);
	}
	/**删除对应包名的cell子视图*/
	boolean removeCellForPackage(String packageName){
		int N = getChildCount();
		for (int i = 0; i < N; i++){
			View child = getChildAt(i);
			if(!(child instanceof Cell))//若此子视图不是cell，表示不是用来放置app的
				break;
			final Cell cell = (Cell)child;
			if(cell.mItemInfo.packageName.equals(packageName)){
				//找到了对应的cell图标
				final CellLayout parent=this;
				Runnable r=new Runnable(){
					public void run() {
						parent.snapToThis();
						parent.removeView(cell);
					}
				};
				Launcher.runOnMainThreak(r);
				AppsDeskManager.deleteForPackage(packageName);
				return true;
			}
		}
		return false;
	}
	
	/**添加对应包名的cell子视图*/
	boolean addCellForPackage(String packageName){
		int N = getChildCount();
		if (N < AppsDeskManager.CELL_COUNT && isAllAppScreen()) {//若此屏未满且是app屏
			int screen = ItemInfo.xyToScreen(x, y);
			int itemXY[] = { 0, 0 };
			findEmptyCell(screen, itemXY);
//			DB.log("找到celllayout来添加cell , x="+x+", y="+y+"; cellx="+itemXY[0]+",celly="+itemXY[1]);
			AppsDeskManager.addForPackage(this,packageName, screen, itemXY[0], itemXY[1]);
			return true;
		}
		return false;
	}
	boolean updateCellForPackage(String packageName){
		return false;
	}
	
	boolean isAllAppScreen(){
		return y == AppsDeskManager.AppScreenStartY
				&& x >= AppsDeskManager.AppScreenStartX;
	}

	private void findEmptyCell(int screen, int[] xy){
		int ii=0,jj=0;
		String key="";
		for (jj = 0; jj < AppsDeskManager.CELL_COUNT_Y; jj++)
			for (ii = 0; ii < AppsDeskManager.CELL_COUNT_X; ii++) {
				key = screen + "-" + ii + "-" + jj;
				if (!mCells.containsKey(key)) {
					xy[0] = ii;
					xy[1] = jj;
					return;
				}
			}
		throw new IllegalArgumentException("line=160:计算错误，不应有这种情况");
	}

	private String getKeyForCell(Cell c){
		return c.mItemInfo.screen+"-"+c.mItemInfo.cellX+"-"+c.mItemInfo.cellY;
	}
	public String toString(){
		return "CellLayout{x=" + x + ",y=" + y + "}";
	}
}
