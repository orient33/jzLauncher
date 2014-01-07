package cn.ingenic.launcher;

import java.util.HashMap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import cn.ingenic.launcher.appchange.AppInstallReceiver;
import cn.ingenic.launcher.home.Clock;

public class Workspace extends PagedViews implements ViewGroup.OnHierarchyChangeListener {
	private static final String TAG="[workspace]";
	/** String is cellLayout index ,eg 1-0 is (1,0)Screen 
	 * 保存这个workspace的所有子View，的索引、*/
	static HashMap<String, CellLayout> mAllCellLayout=new HashMap<String, CellLayout>(); 
	Context mContext;
	Launcher mLauncher;
	static int homeX = 1, homeY = 0;
	
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
//		DB.log("[Workspace] add child. cellLayout=" + key);
	}

	@Override
	public void onChildViewRemoved(View parent, View child) {
		CellLayout cl = (CellLayout) child;
		String key = getKeyForCellLayout(cl);
		if (mAllCellLayout.containsKey(key))
			mAllCellLayout.remove(key);
		DB.log("[Workspace] remove child. cellLayout=" + key);
	}

	void setLauncher(Launcher l){
		mLauncher = l;
	}
	void snapToHome() {
		smoothScrollToPage(homeX, homeY);
	}
	void snapToCellLayout(CellLayout cl){
		smoothScrollToPage(cl.x, cl.y);
	}
	void initToHome(){
		scrollTo(homeX*mPageWidth,homeY*mPageHeight);
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
//		DB.log("add column : "+col+",w="+mPageWidth+",h="+mPageHeight);
		for (int i = 0; i < mMaxPageY; i++) {// 依据行数添加屏数
			ViewGroup.LayoutParams lp=new ViewGroup.LayoutParams(mPageWidth, mPageHeight);
			
			CellLayout cell = (CellLayout) View.inflate(mContext,
					R.layout.celllayout, null);
			cell.x = col;
			cell.y = i;
			if (homeX == col && homeY == i) {
				Clock clock = (Clock)View.inflate(mContext, R.layout.home_colck, null);
				cell.addView(clock, lp);
			}
			addView(cell,lp);
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
//			DB.log(TAG+"add " + " cell x=" + cl.x + ",y=" + cl.y + " ; "
//					+ cell.mItemInfo.title);
			cl.addView(cell);
		}
	}

	void appChanged(String[] packageNames,int op) {
		int N = getChildCount();
		for (int p = 0; p < packageNames.length; p++){
			boolean done = false;
			for (int i = 0; i < N; i++) {
				if (done)	//	若已正确处理 packageName[p]，则退出这层
					break;
				CellLayout cl = (CellLayout) getChildAt(i);
				switch (op) {
				case AppInstallReceiver.OP_add:
					if (cl.addCellForPackage(packageNames[p]))
						done = true;
					break;
				case AppInstallReceiver.OP_remove:
					if(cl.removeCellForPackage(packageNames[p]))
						done = true;
					break;
				case AppInstallReceiver.OP_update:
					if (cl.updateCellForPackage(packageNames[p]))
						done = true;
					break;
				}
			}
		}
	}

	private String getKeyForCellLayout(CellLayout cl) {
		return cl.x + "-" + cl.y;
	}

}
