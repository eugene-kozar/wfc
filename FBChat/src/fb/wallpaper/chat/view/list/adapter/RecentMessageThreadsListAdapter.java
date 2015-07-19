package fb.wallpaper.chat.view.list.adapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import fb.wallpaper.chat.ConversationActivity;
import fb.wallpaper.chat.R;
import fb.wallpaper.chat.data.MessageThread;
import fb.wallpaper.chat.data.Category;
import fb.wallpaper.chat.utils.Utils;
import fb.wallpaper.chat.view.list.adapter.AbstractListAdapter.ViewHolder;
import fb.wallpaper.chat.view.list.holder.RecentMessageThreadHolder;

public class RecentMessageThreadsListAdapter extends AbstractListAdapter<MessageThread> {

	private LayoutInflater inflater;
	List<Category> categories;
	private ImageLoader imageLoader = ImageLoader.getInstance();

	public RecentMessageThreadsListAdapter(Context context, int viewid, List<MessageThread> objects) {
		super(context, viewid, objects);
		inflater = LayoutInflater.from(context);
		categories = new ArrayList<Category>();
		
	}

	@Override
	protected ViewHolder createHolder(View v) {
		ImageView icon = (ImageView) v.findViewById(R.id.userImage);
		TextView text = (TextView) v.findViewById(R.id.userName);
		ProgressBar spinner = (ProgressBar) v.findViewById(R.id.loading);		
		TextView snippet = (TextView) v.findViewById(R.id.threadSnippet);
		TextView lastTime = (TextView) v.findViewById(R.id.lastTime);
		ImageView userPresence = (ImageView) v.findViewById(R.id.userPresence);
		ViewHolder result = new RecentMessageThreadHolder(text, icon, spinner, snippet, userPresence, lastTime);

		LinearLayout itemWrapper = (LinearLayout) v.findViewById(R.id.item_wrapper);		
		itemWrapper.setOnClickListener(new AbstractListAdapter.OnItemClickListener(result) {			
			public void onClick(View v, ViewHolder viewHolder) {
				MessageThread thread = (MessageThread) viewHolder.data;
				Intent intent = new Intent();
				intent.putExtra("userWith", thread.getUserWith());
				intent.setClass(context, ConversationActivity.class);
				context.startActivity(intent);						
			}
		});	

		return result;
	}

	@Override
	protected void bindHolder(ViewHolder h) {
		RecentMessageThreadHolder viewHolder = (RecentMessageThreadHolder) h;
		MessageThread messageThread = (MessageThread) viewHolder.data;

		if(messageThread.getUnreadCount()==0) {
			viewHolder.getTextView().setText(messageThread.getUserWith().getName());
		} else {
			viewHolder.getTextView().setText(messageThread.getUserWith().getName() + 
					" (" + messageThread.getUnreadCount() + ")");	
		}			
		viewHolder.getSnippet().setText(messageThread.getSnippet());

		if ("available".equals(messageThread.getUserWith().getPresence())) {
			viewHolder.getUserPresence().setImageResource(R.drawable.circle_green);
		} else {
			viewHolder.getUserPresence().setImageResource(R.drawable.circle_red);
		}
		
		long messageThreadTime = messageThread.getTime();
		viewHolder.getLastTime().setText(findTime(messageThreadTime));

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

	}	

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		
		Category category = new Category();
		if(categories.size() == 0) {
			getCategories(getData());
		}
		category = getCategory(position);
		
		if(category!=null) {
			view = inflater.inflate(R.layout.list_item_recent_threads_category, parent, false);
			TextView tv = (TextView) view.findViewById(R.id.chat_separator);
			tv.setText(category.getText() + " (" + category.getItemCount() + ")");
			return fillView(view, position);
		} else {
			view = inflater.inflate(R.layout.list_item_recent_threads, null);
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

	private int getCategories(List<MessageThread> messageThreads) {
		final long week = (long)7*24*60*60*1000;
		final long month = (long)30*24*60*60*1000;
		int itemCount = 0;
		Category category = new Category();
		for (int i = 0; i < messageThreads.size(); i++) {
			if(new Date(messageThreads.get(i).getTime()).getYear() == new Date().getYear() && 
					new Date(messageThreads.get(i).getTime()).getMonth() == new Date().getMonth() && 
					new Date(messageThreads.get(i).getTime()).getDate() == new Date().getDate()) {
				if(category.getText() == null){
					category.setIndex(i);
					category.setText("Today");
				}				
				itemCount++;
			} else {
				if(itemCount!=0 && "Today".equals(category.getText())) {
					category.setItemCount(itemCount);
					categories.add(category);
					itemCount = 0;
					category = new Category();
				}
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DATE, -1);
				if((new Date(messageThreads.get(i).getTime()).getYear() == cal.getTime().getYear()) && 
						(new Date(messageThreads.get(i).getTime()).getMonth() == cal.getTime().getMonth()) && 
						(new Date(messageThreads.get(i).getTime()).getDate() == cal.getTime().getDate())) {
					if(category.getText() == null){
						category.setIndex(i);
						category.setText("Yesterday");
					}				
					itemCount++;
				} else {
					if(itemCount!=0 && "Yesterday".equals(category.getText())) {
						category.setItemCount(itemCount);
						categories.add(category);
						itemCount = 0;
						category = new Category();
					}
					if(new Date().getTime() - new Date(messageThreads.get(i).getTime()).getTime() < week) {
						if(category.getText() == null){
							category.setIndex(i);
							category.setText("Earlier this week");
						}				
						itemCount++;
					} else {
						if(itemCount!=0 && "Earlier this week".equals(category.getText())) {
							category.setItemCount(itemCount);
							categories.add(category);
							itemCount = 0;
							category = new Category();
						}
						if(new Date().getTime() - new Date(messageThreads.get(i).getTime()).getTime() < month) {
							if(category.getText() == null){
								category.setIndex(i);
								category.setText("Earlier this month");
							}				
							itemCount++;
						} else {
							if(itemCount!=0 && "Earlier this month".equals(category.getText())) {
								category.setItemCount(itemCount);
								categories.add(category);
								itemCount = 0;
								category = new Category();
							}
							if(new Date().getTime() - new Date(messageThreads.get(i).getTime()).getTime() > month) {
								if(category.getText() == null){
									category.setIndex(i);
									category.setText("More than month");
									category.setItemCount(itemCount + messageThreads.size()-i);
									categories.add(category);
									return 1;
								}				
								itemCount++;
							}
						}
					}
				}
			}
		}
		return 0;
	}
	
	private Category getCategory(int position) {
		for (int i = 0; i < categories.size(); i++) {
			if(categories.get(i).getIndex() == position)
				return categories.get(i);
		}
		return null;
	}
	
	private String findTime(long messageThreadTime) {
		long day = (long)24*60*60*1000;
		long week = (long)7*day;
		SimpleDateFormat dateFormat;
		String time;
		Date threadDate = new Date(messageThreadTime);
		Date nowDate = new Date();
		if(nowDate.getTime() - threadDate.getTime() < day) {
			dateFormat = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
		} else if(nowDate.getTime() - threadDate.getTime() < week) {
			dateFormat = new SimpleDateFormat("E", Locale.ENGLISH);
		} else {
			dateFormat = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
		}
			time = dateFormat.format(threadDate);		
		return time;
	}
}
