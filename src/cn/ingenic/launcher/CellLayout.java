package cn.ingenic.launcher;

import java.util.HashMap;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

public class CellLayout extends GridLayout implements ViewGroup.OnHierarchyChangeListener {
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
				c.layout(ll, tt, ll+mCellWidth, tt+mCellHeight);
			}
		}
	}
	@Override
	public void onChildViewAdded(View parent, View child) {
		if (child instanceof Cell) {
			String key = getKeyForCell((Cell) child);
			if (!mCells.containsKey(key))
				mCells.put(key, (Cell) child);
		}
	}

	@Override
	public void onChildViewRemoved(View parent, View child) {
		if (child instanceof Cell) {
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
			}
			
		}else {
			DB.log(TAG+" child is not Cell");
		}
	}

	private String getKeyForCell(Cell c){
		return c.mItemInfo.screen+"-"+c.mItemInfo.cellX+"-"+c.mItemInfo.cellY;
	}
	public String toString(){
		return "CellLayout{x=" + x + ",y=" + y + "}";
	}
}
