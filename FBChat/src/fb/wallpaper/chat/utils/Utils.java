package fb.wallpaper.chat.utils;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import fb.wallpaper.chat.R;
import android.graphics.Bitmap;
import android.util.Log;

public class Utils {
	public static void logg(String message) {
		Log.i(Constants.TAG, message);
	}

	public static final DisplayImageOptions DEFAULT_IMAGE_OPTIONS = new DisplayImageOptions.Builder()
			.showImageForEmptyUri(R.drawable.ic_empty)
			.showImageOnFail(R.drawable.ic_error).resetViewBeforeLoading(true)
			.cacheOnDisc(true).imageScaleType(ImageScaleType.EXACTLY)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.displayer(new FadeInBitmapDisplayer(300)).build();
}
