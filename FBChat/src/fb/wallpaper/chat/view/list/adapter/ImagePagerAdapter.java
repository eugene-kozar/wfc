package fb.wallpaper.chat.view.list.adapter;

import java.io.IOException;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import fb.wallpaper.chat.R;

public class ImagePagerAdapter extends PagerAdapter {

	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;

	private String[] images;
	private LayoutInflater inflater;
	private Context context;

	public ImagePagerAdapter(Context context, String[] images) {
		this.context = context;
		options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.ic_empty)
				.showImageOnFail(R.drawable.ic_error)
				.resetViewBeforeLoading(true).cacheOnDisc(true)
				.imageScaleType(ImageScaleType.EXACTLY)
				.bitmapConfig(Bitmap.Config.RGB_565)
				.displayer(new FadeInBitmapDisplayer(300)).build();

		this.images = images;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}

	@Override
	public void finishUpdate(View container) {
	}

	@Override
	public int getCount() {
		return images.length;
	}

	@Override
	public Object instantiateItem(ViewGroup view, int position) {
		final int pos = position;
		View imageLayout = inflater.inflate(R.layout.item_pager_image, view,
				false);
		ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
		
		imageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Toast.makeText(context, "Background changed", Toast.LENGTH_SHORT).show();
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
				sharedPref.edit().putString("background", images[pos]).commit();
				Intent intent = new Intent("FBChatBackgroundChanged");
				LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(context);
	    		broadcaster.sendBroadcast(intent);
	    		if(sharedPref.getBoolean("home_screen", false)) {
	    			WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
	    			try {
		    			wallpaperManager.setResource(Integer.parseInt(images[pos].substring(images[pos].lastIndexOf("/")+1)));
	    			    } catch (IOException e) {
	    			    	e.printStackTrace();
	    			    }
	    			}
				}
		});
		
		final ProgressBar spinner = (ProgressBar) imageLayout
				.findViewById(R.id.loading);

		imageLoader.displayImage(images[position], imageView, options,
				new SimpleImageLoadingListener() {
					@Override
					public void onLoadingStarted(String imageUri, View view) {
						spinner.setVisibility(View.VISIBLE);
					}

					@Override
					public void onLoadingFailed(String imageUri, View view,
							FailReason failReason) {
						String message = null;
						switch (failReason.getType()) {
						case IO_ERROR:
							message = "Input/Output error";
							break;
						case DECODING_ERROR:
							message = "Image can't be decoded";
							break;
						case NETWORK_DENIED:
							message = "Downloads are denied";
							break;
						case OUT_OF_MEMORY:
							message = "Out Of Memory error";
							break;
						case UNKNOWN:
							message = "Unknown error";
							break;
						}
						Toast.makeText(context, message,
								Toast.LENGTH_SHORT).show();

						spinner.setVisibility(View.GONE);
					}

					@Override
					public void onLoadingComplete(String imageUri, View view,
							Bitmap loadedImage) {
						spinner.setVisibility(View.GONE);
					}
				});

		((ViewPager) view).addView(imageLayout, 0);
		return imageLayout;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view.equals(object);
	}

	@Override
	public void restoreState(Parcelable state, ClassLoader loader) {
	}

	@Override
	public Parcelable saveState() {
		return null;
	}

	@Override
	public void startUpdate(View container) {
	}
}
