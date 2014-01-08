package cn.ingenic.launcher.wallpaper;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import cn.ingenic.launcher.R;

public class WallpaperChooser extends Activity {
	   @SuppressWarnings("unused")
	    private static final String TAG = "Launcher.WallpaperChooser";

	    @Override
	    public void onCreate(Bundle icicle) {
	        super.onCreate(icicle);
	        setContentView(R.layout.wallpaper_chooser_base);

	        Fragment fragmentView =
	                getFragmentManager().findFragmentById(R.id.wallpaper_chooser_fragment);
	        // TODO: The following code is currently not exercised. Leaving it here in case it
	        // needs to be revived again.
	        if (fragmentView == null) {
	            /* When the screen is XLarge, the fragment is not included in the layout, so show it
	             * as a dialog
	             */
	            DialogFragment fragment = WallpaperChooserDialogFragment.newInstance();
	            fragment.show(getFragmentManager(), "dialog");
	        }
	    }
}
