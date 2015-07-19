package fb.wallpaper.chat.view.list.adapter;

import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import fb.wallpaper.chat.ConversationActivity;
import fb.wallpaper.chat.R;
import fb.wallpaper.chat.data.MessageThread;
import fb.wallpaper.chat.data.Smile;
import fb.wallpaper.chat.utils.Utils;
import fb.wallpaper.chat.view.list.adapter.AbstractListAdapter.ViewHolder;
import fb.wallpaper.chat.view.list.holder.ChatsPopupHolder;
import fb.wallpaper.chat.view.list.holder.RecentMessageThreadHolder;

public class ChatsAdapter extends AbstractListAdapter<MessageThread> {
	private static LayoutInflater inflater = null;
	private ImageLoader imageLoader = ImageLoader.getInstance();

	public ChatsAdapter(Activity activity, int viewid, List<MessageThread> threads) {
		super(activity, viewid, threads);
		inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		convertView = inflater.inflate(R.layout.grid_profile_picture, null);
		ViewHolder holder = createHolder(convertView);
		convertView.setTag(holder);
		holder.data = getItem(position);
		bindHolder(holder);
		return convertView;
	}

	protected ViewHolder createHolder(View v) {
		ImageView icon = (ImageView) v.findViewById(R.id.userImage);
		ProgressBar spinner = (ProgressBar) v.findViewById(R.id.loading);
		TextView unread = (TextView) v.findViewById(R.id.grid_bubble_text);

		ViewHolder result = new ChatsPopupHolder(icon, spinner, unread);

		FrameLayout itemWrapper = (FrameLayout) v.findViewById(R.id.profile_picture);		
		itemWrapper.setOnClickListener(new AbstractListAdapter.OnItemClickListener(result) {			
			public void onClick(View v, ViewHolder viewHolder) {
				MessageThread thread = (MessageThread) viewHolder.data;
				Intent intent = new Intent();
				intent.putExtra("userWith", thread.getUserWith());
				intent.setClass(context, ConversationActivity.class);
				((Activity) context).finish();
				context.startActivity(intent);						
			}
		});	

		return result;
	}

	protected void bindHolder(ViewHolder h) {
		ChatsPopupHolder viewHolder = (ChatsPopupHolder) h;
		MessageThread messageThread = (MessageThread) viewHolder.data;

		final ProgressBar spinner = viewHolder.getProgressBar();
		imageLoader.displayImage(messageThread.getUserWith().getProfilePictureSquare(75), viewHolder.getImageView(), Utils.DEFAULT_IMAGE_OPTIONS,
				new SimpleImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				spinner.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {						
				spinner.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				spinner.setVisibility(View.GONE);
			}
		});
		if(messageThread.getUnreadCount() != 0) {
			viewHolder.getTextView().setText(" " + messageThread.getUnreadCount() + " ");
			viewHolder.getTextView().setBackgroundResource(R.drawable.unread_bubble);
		}
	}	
}
