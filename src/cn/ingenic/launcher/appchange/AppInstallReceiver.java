package cn.ingenic.launcher.appchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import cn.ingenic.launcher.AppsDeskManager;
import cn.ingenic.launcher.Launcher;
/**
 * 接受app安装、卸载、更新的广播
 * */
public class AppInstallReceiver extends BroadcastReceiver {

	public static final int OP_none = 0;
	public static final int OP_add = 1;
	public static final int OP_update = 2;
	public static final int OP_remove = 3;
	Context mContext;
	AppsDeskManager mAppsDeskManager;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();
		if (mAppsDeskManager == null) {
			mAppsDeskManager = AppsDeskManager.getInstance();
		}
		
		if (Intent.ACTION_PACKAGE_CHANGED.equals(action)
				|| Intent.ACTION_PACKAGE_REMOVED.equals(action)
				|| Intent.ACTION_PACKAGE_ADDED.equals(action)) {
			final String packageName = intent.getData().getSchemeSpecificPart();
			final boolean replacing = intent.getBooleanExtra(
					Intent.EXTRA_REPLACING, false);
			log("receiver : "+action+"; pkg:"+packageName+"; replacing:"+replacing);
			if (packageName == null || packageName.length() == 0) {
				return; // they sent us a bad intent
			}
			int op = OP_none;
			if (Intent.ACTION_PACKAGE_CHANGED.equals(action))
				op = OP_update;
			else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
				if (!replacing)
					op = OP_remove;
			} else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				if (!replacing)
					op = OP_add;
				else
					op = OP_update;
			}
			if (op != OP_none) {
				PackageUpdate r = new PackageUpdate(op,
						new String[] { packageName });
				packageUpdated(r);
			}
		}
	}

	void packageUpdated(Runnable r){
		Launcher.runOnWorkThreak(r);
	}

	class PackageUpdate implements Runnable {
		final int mOp;
		final String[] mPackages;

		public PackageUpdate(int op, String[] packs) {
			mOp = op;
			mPackages = packs;
		}

		public void run() {
			if (mPackages.length > 0 && mAppsDeskManager != null) {
				mAppsDeskManager.appChanged(mPackages, mOp);
			}
		}
	}
	
	private void log(String log){
		Log.i("sw2df", "[appinstallReceiver]"+log);
	}
}
