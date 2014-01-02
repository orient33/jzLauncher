package cn.ingenic.launcher;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Cell extends LinearLayout {

	static final String TAG="[Cell]";
	ItemInfo mItemInfo;
	ImageView mIcon;
	TextView mTitle;
	Context mContext;
	
	public Cell(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}
	public Cell(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startA();
			}
		});
	}

	void setItemInfo(ItemInfo ii){
		mItemInfo = ii;
		mIcon=(ImageView)this.findViewById(R.id.icon);
		mTitle=(TextView)findViewById(R.id.title);
	}

	void setIconTitle(Drawable i,String t){
		if (mIcon != null)
			mIcon.setImageDrawable(i);
		else 
			DB.log(TAG + "mIcon is null");
		if (mTitle != null)
			mTitle.setText(t);
		else 
			DB.log(TAG + "mTitle is null");
	}
	private void startA(){
		try {
			mContext.startActivity(mItemInfo.intent);
		} catch (Exception e) {
			showToast(e.toString());
		}
	}

	private void showToast(String msg) {
		Toast.makeText(mContext, msg, 0).show();
	}
	@Override
	public String toString(){
		return mTitle + ", " + mItemInfo.toString();
	}
}
