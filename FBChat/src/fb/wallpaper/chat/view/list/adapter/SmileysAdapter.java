package fb.wallpaper.chat.view.list.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import fb.wallpaper.chat.R;
import fb.wallpaper.chat.data.Smile;

public class SmileysAdapter extends BaseAdapter {
	private Activity activity;
	private static LayoutInflater inflater = null;

	public static final Smile[] IMAGES = new Smile[] { 
		/*new Smile(":)", R.drawable.smile_glad), 
		new Smile(" :(", R.drawable.smile_satisfied),*/
		new Smile(" :)", R.drawable.fb_smile),
		new Smile(" :(", R.drawable.fb_frown),
		new Smile(" :P", R.drawable.fb_tounge),
		new Smile(" :D", R.drawable.fb_grin),
		new Smile(" :O", R.drawable.fb_gasp),
		new Smile(" ;)", R.drawable.fb_wink),
		new Smile(" 8)", R.drawable.fb_glasses),
		new Smile(" 8|", R.drawable.fb_sunglasses),
		new Smile(" >:(", R.drawable.fb_grumpy),
		new Smile(" :/", R.drawable.fb_unsure),
		new Smile(" :'(", R.drawable.fb_cry),
		new Smile(" 3:)", R.drawable.fb_devil),
		new Smile(" O:)", R.drawable.fb_angel),
		new Smile(" :*", R.drawable.fb_kiss),
		new Smile(" <3", R.drawable.fb_heart),
		new Smile(" ^_^", R.drawable.fb_kiki),
		new Smile(" -_-", R.drawable.fb_squint),
		new Smile(" o.O", R.drawable.fb_confused),
		new Smile(" >:O", R.drawable.fb_upset),
		new Smile(" :v", R.drawable.fb_pacman),
		new Smile(" :3", R.drawable.fb_curlylips),
		new Smile(" :|]", R.drawable.fb_robot),
		new Smile(" :putnam:", R.drawable.fb_putnam),
		new Smile(" (^^^)", R.drawable.fb_shark),
		new Smile(" <(\")", R.drawable.fb_penguin),
		new Smile(" (Y)", R.drawable.fb_thumb),
		};	

	public SmileysAdapter(Activity act) {
		activity = act;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return IMAGES.length;
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
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.grid_row, null);
			holder.imageView = (ImageView) convertView.findViewById(R.id.imageView1);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.imageView.setImageResource(IMAGES[position].getResourceId());
		return convertView;
	}

	public static class ViewHolder {
		public ImageView imageView;
	}
}
