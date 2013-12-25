package cn.ingenic.launcher;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridLayout;

public class CellLayout extends GridLayout {
	/** 指示此 屏 所在的坐标 */
	int x, y;
	
	
	public CellLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public CellLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
}
