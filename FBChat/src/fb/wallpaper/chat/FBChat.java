package fb.wallpaper.chat;

import android.app.Application;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class FBChat extends Application {
	
	public void onCreate() {
		ConfigureLog4J.configure();
		ImageLoaderConfiguration config = ImageLoaderConfiguration.createDefault(this);
		ImageLoader.getInstance().init(config);
	}
}
