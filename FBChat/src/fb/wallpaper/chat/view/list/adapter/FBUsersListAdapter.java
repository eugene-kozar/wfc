package fb.wallpaper.chat.view.list.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import fb.wallpaper.chat.ConversationActivity;
import fb.wallpaper.chat.R;
import fb.wallpaper.chat.data.FBUser;
import fb.wallpaper.chat.utils.Utils;
import fb.wallpaper.chat.view.list.adapter.AbstractListAdapter.ViewHolder;
import fb.wallpaper.chat.view.list.holder.SimpleListItemHolder;

public class FBUsersListAdapter extends AbstractListAdapter<FBUser> {
	private List<FBUser> srcData;
	private static int onlineCount;
	private static int offlineCount;
	private static int allCount;
	private Context context;
	private int viewId;
	private LayoutInflater inflater;
	private boolean needOfflineSeparator;
	private boolean noMoreSeparators;


	private ImageLoader imageLoader = ImageLoader.getInstance();

	public FBUsersListAdapter(Context context, int viewid, List<FBUser> objects) {
		super(context, viewid, objects);
		this.srcData = objects;
		this.context = context;
		this.viewId = viewid;
		onlineCount = 0;
		offlineCount = 0;
		inflater = LayoutInflater.from(context);
		needOfflineSeparator = false;
		noMoreSeparators = false;
	}

	@Override
	protected ViewHolder createHolder(View v) {
		ImageView icon = (ImageView) v.findViewById(R.id.listitem_icon);
		TextView text = (TextView) v.findViewById(R.id.listitem_text);
		ProgressBar spinner = (ProgressBar) v.findViewById(R.id.loading);
		ImageView presence = (ImageView) v.findViewById(R.id.userPresence);
		TextView status = (TextView) v.findViewById(R.id.listitem_status);
		ViewHolder result = new SimpleListItemHolder(text, icon, spinner, presence, status);
		

		v.setOnClickListener(new AbstractListAdapter.OnItemClickListener(result) {			
			public void onClick(View v, ViewHolder viewHolder) {
				FBUser user = (FBUser) viewHolder.data;
				Intent intent = new Intent();
				intent.putExtra("userWith", user);
				intent.putExtra("fromFriends", true);
				intent.setClass(context, ConversationActivity.class);
				context.startActivity(intent);						
			}
		});	

		return result;
	}

	@Override
	protected void bindHolder(ViewHolder h) {
		SimpleListItemHolder viewHolder = (SimpleListItemHolder) h;
		FBUser fbUser = (FBUser) viewHolder.data;

		//viewHolder.getImageView().setImageResource(R.drawable.com_facebook_profile_default_icon);
		viewHolder.getTextView().setText(fbUser.getName());

		if ("available".equals(fbUser.getPresence())) {
			viewHolder.getUserPresence().setImageResource(R.drawable.circle_green);
			viewHolder.getStatus().setText("Available");
		} else {
			viewHolder.getUserPresence().setImageResource(R.drawable.circle_red);
			viewHolder.getStatus().setText("Offline");
		}


		final ProgressBar spinner = viewHolder.getProgressBar();
		imageLoader.displayImage(fbUser.getProfilePictureSquare(70), viewHolder.getImageView(), Utils.DEFAULT_IMAGE_OPTIONS,
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

	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		if("Name".equals(sharedPref.getString("buddies_sort", "Availability"))) {
			if(position == 0) {
				view = inflater.inflate(R.layout.list_friend_category, null);
				TextView tv = (TextView) view.findViewById(R.id.separator);
				tv.setText("Facebook friends "+ onlineCount + "/" + allCount);
				noMoreSeparators = true;
				return fillView(view, position);
			}
		} else {
			noMoreSeparators = false;
		}
		
		if(needOfflineSeparator) {
			if(noMoreSeparators) {
				view = inflater.inflate(viewId, null);
				return fillView(view, position);
			} else {
				view = inflater.inflate(R.layout.list_friend_category, null);
				TextView tv = (TextView) view.findViewById(R.id.separator);
				tv.setText("Offline " + offlineCount);
				return fillView(view, position);
			}
		}
		
		if(!"available".equals(((FBUser) getItem(position)).getOnlinePresence()) && (position == 0)) {
			if(noMoreSeparators) {
				view = inflater.inflate(viewId, null);
				return fillView(view, position);
			} else {
				view = inflater.inflate(R.layout.list_friend_category, null);
				TextView tv = (TextView) view.findViewById(R.id.separator);
				tv.setText("Offline " + offlineCount);
				return fillView(view, position);
			}
		}

		if("available".equals(((FBUser) getItem(position)).getOnlinePresence())) {
			if(position == 0) {
				if(noMoreSeparators) {
					view = inflater.inflate(viewId, null);
					return fillView(view, position);
				} else {
					view = inflater.inflate(R.layout.list_friend_category, null);
					TextView tv = (TextView) view.findViewById(R.id.separator);
					tv.setText("Facebook friends " + onlineCount + "/" + allCount);
					return fillView(view, position);
				}
			} else {
				view = inflater.inflate(viewId, null);
				return fillView(view, position);
			}
		} 
		
		if(position != allCount && position != 0) {
			if("available".equals(((FBUser)getItem(position)).getOnlinePresence())
					&& !"available".equals(((FBUser)getItem(position+1)).getOnlinePresence())) {
				needOfflineSeparator = true;
				view = inflater.inflate(viewId, null);
				return fillView(view, position);
			} else if (!"available".equals(((FBUser)getItem(position)).getOnlinePresence())
					&& "available".equals(((FBUser)getItem(position-1)).getOnlinePresence())) {
				if(noMoreSeparators) {
					view = inflater.inflate(viewId, null);
					return fillView(view, position);
				} else {
					view = inflater.inflate(R.layout.list_friend_category, null);
					TextView tv = (TextView) view.findViewById(R.id.separator);
					tv.setText("Offline " + offlineCount);
					return fillView(view, position);
				}
			} else {
				view = inflater.inflate(viewId, null);
				return fillView(view, position);
			}
		} else {
			view = inflater.inflate(viewId, null);
			return fillView(view, position);
		}
	}
	
	private View fillView(View view, int position) {
		ViewHolder holder = createHolder(view);
		view.setTag(holder);
		holder.data = getItem(position);
		bindHolder(holder);
		return view;
	}

	public void setSourceData(List<FBUser> users) {
		srcData = users;
	}

	public List<FBUser> getSourceData() {
		return this.srcData;
	}

	@Override
	public void notifyDataSetChanged() {		
		setData(filter(srcData));
		super.notifyDataSetChanged();
	}

	private List<FBUser> filter(List<FBUser> srcData) {

		List<FBUser> users = new ArrayList<FBUser>();
		List<FBUser> onlineUsers = new ArrayList<FBUser>();
		List<FBUser> offlineUsers = new ArrayList<FBUser>();
		onlineCount = 0;
		offlineCount = 0;
		allCount = srcData.size();

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.context);
		if(sharedPref.getBoolean("hide_offline", false)) {
			for (FBUser u : srcData) {
				if ("available".equals(u.getPresence())) {
					users.add(u);
					onlineCount++;
				}
			}
		} else {
			if("Name".equals(sharedPref.getString("buddies_sort", "Availability"))) {
				users = srcData;
				for (FBUser u : srcData) {
					if ("available".equals(u.getPresence())) {
						onlineCount++;
					} else {
						offlineCount++;
					}
				}
			} else {
				for (FBUser u : srcData) {
					if ("available".equals(u.getPresence())) {
						onlineUsers.add(u);
						onlineCount++;
					} else {
						offlineUsers.add(u);
						offlineCount++;
					}
				}
				users.addAll(onlineUsers);
				users.addAll(offlineUsers);
			}			
		}
		return users;
	}

}
