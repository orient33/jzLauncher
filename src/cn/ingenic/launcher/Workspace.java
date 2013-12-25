package cn.ingenic.launcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Workspace extends PagedViews {

	/** String is cellLayout index ,eg 1-0 is (1,0)Screen */
	static HashMap<String, CellLayout> mAllCellLayout=new HashMap<String, CellLayout>(); 
	ArrayList<ResolveInfo> mAllCellLayoutAppInfo;
	Context mContext;
	
	public Workspace(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public Workspace(Context context, AttributeSet attrs, int def) {
		super(context, attrs, def);
		mContext=context;
	}
	
	void snapToHome(){
		super.smoothScrollToPage(0, 0);//:TODO
	}

	void initAllAppInfo(List<ResolveInfo> ris){
		if (mAllCellLayoutAppInfo == null) {
			mAllCellLayoutAppInfo = new ArrayList<ResolveInfo>();
			for (ResolveInfo ri : ris)
				mAllCellLayoutAppInfo.add(ri);
		}
	}

	void addShortcutToScreen(final ItemInfo ai){
		int size=getChildCount();
		CellLayout cl = null;
		if (mAllCellLayout.containsKey(ai.screen+"-"+0))
			cl = mAllCellLayout.get(ai.screen+"-"+0);
		else
			for (int i = 0; i < size; i++) {
				CellLayout temp = (CellLayout) getChildAt(i);
				mAllCellLayout.put(temp.x + "-" + temp.y, temp);
				if (temp.y == 0 && temp.x == ai.screen) {
					cl = temp;
					break;
				}
			}
		View tv = View.inflate(mContext, R.layout.app_item, null);
		((ImageView)tv.findViewById(R.id.icon)).setImageDrawable(ai.icon);
		((TextView)tv.findViewById(R.id.title)).setText(ai.title);
		tv.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startA(ai.intent);
			}
		});
		if (cl != null) {
			DB.log("[workspace]add "+" cell x="+cl.x+",y="+cl.y+" ; "+ai.title);
			cl.addView(tv);
		}
	}
	void showToast(String msg){
		Toast.makeText(mContext, msg, 0).show();
	}
	void startA(Intent i){
		try{
		mContext.startActivity(i);
		}catch(Exception e){
			showToast(e.toString());
		}
	}
	
}
