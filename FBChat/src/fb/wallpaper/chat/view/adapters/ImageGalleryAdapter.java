package fb.wallpaper.chat.view.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import fb.wallpaper.chat.R;
import fb.wallpaper.chat.utils.Constants;

public class ImageGalleryAdapter extends BaseAdapter {
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;

	private String[] images;
	private LayoutInflater inflater;
	
	public ImageGalleryAdapter(Context context, String[] images) {
		options = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.ic_stub)
			.showImageForEmptyUri(R.drawable.ic_empty)
			.showImageOnFail(R.drawable.ic_error)
			.cacheInMemory(true)
			.cacheOnDisc(true)
			.bitmapConfig(Bitmap.Config.RGB_565)
			.build();
		this.images = images;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	@Override
	public int getCount() {
		return images.length;
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView = (ImageView) convertView;
		if (imageView == null) {
			imageView = (ImageView) inflater.inflate(R.layout.item_gallery_image, parent, false);
		}
		imageLoader.displayImage(images[position], imageView, options);
		return imageView;
	}
}
