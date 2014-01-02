package cn.ingenic.launcher;

import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class Workspace extends PagedViews implements ViewGroup.OnHierarchyChangeListener {
	private static final String TAG="[workspace]";
	/** String is cellLayout index ,eg 1-0 is (1,0)Screen 
	 * 保存这个workspace的所有子View，的索引、*/
	static HashMap<String, CellLayout> mAllCellLayout=new HashMap<String, CellLayout>(); 
	Context mContext;
	
	public Workspace(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public Workspace(Context context, AttributeSet attrs, int def) {
		super(context, attrs, def);
		mContext=context;
		this.setOnHierarchyChangeListener(this);
	}

	@Override
	public void onChildViewAdded(View parent, View child) {
		if (!(child instanceof CellLayout))
			throw new IllegalArgumentException(
					"Workspace only has CellLayout children");
		CellLayout cl = (CellLayout) child;
		cl.setClickable(true);
		String key = getKeyForCellLayout(cl);
		if (!mAllCellLayout.containsKey(key))
			mAllCellLayout.put(key, cl);
		DB.log("[Workspace] add child. cellLayout=" + key);
	}

	@Override
	public void onChildViewRemoved(View parent, View child) {
		CellLayout cl = (CellLayout) child;
		String key = getKeyForCellLayout(cl);
		if (mAllCellLayout.containsKey(key))
			mAllCellLayout.remove(key);
		DB.log("[Workspace] remove child. cellLayout=" + key);
	}

	void snapToHome() {
		super.smoothScrollToPage(0, 0);// :TODO
	}

	@Override
	public void addView(View child) {
		if (!(child instanceof CellLayout))
			throw new IllegalArgumentException("Workspace only add CellLayout");
		super.addView(child);
	}

	/** 根据行数，添加一列的屏 
	 * @param col 列的索引值 */
	void addOneColumn(int col){
		DB.log("add column : "+col);
		for (int i = 0; i < mMaxPageY; i++) {// 依据行数添加屏数
			CellLayout cell = (CellLayout) View.inflate(mContext,
					R.layout.celllayout, null);
			cell.x = col;
			cell.y = i;
			addView(cell);
		}
		super.addMaxPageX();
	}
	
	void addCell(Cell cell){
		CellLayout cl=null;
		if (mAllCellLayout.containsKey(cell.mItemInfo.screen + "-" + 0))
			cl = mAllCellLayout.get(cell.mItemInfo.screen + "-" + 0);
		else
			DB.log(TAG+" celllayout in not enough for cell: "+cell);
		if(cl !=null){
			DB.log(TAG+"add " + " cell x=" + cl.x + ",y=" + cl.y + " ; "
					+ cell.mItemInfo.title);
			cl.addView(cell);
		}
	}

	private String getKeyForCellLayout(CellLayout cl) {
		return cl.x + "-" + cl.y;
	}

}
