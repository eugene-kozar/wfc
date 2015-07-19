package fb.wallpaper.chat.view.adapters;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import fb.wallpaper.chat.R;
import fb.wallpaper.chat.data.Category;
import fb.wallpaper.chat.data.FBUser;
import fb.wallpaper.chat.data.Message;
import fb.wallpaper.chat.data.MessageThread;
import fb.wallpaper.chat.data.Smile;
import fb.wallpaper.chat.data.provider.ProfileDataProviderFactory;
import fb.wallpaper.chat.data.provider.ProfileInfoProvider;
import fb.wallpaper.chat.data.provider.listener.PersonalInfoFetchListener;
import fb.wallpaper.chat.data.provider.listener.UserInfoFetchListener;
import fb.wallpaper.chat.database.AccountDAO;
import fb.wallpaper.chat.database.async.SaveAccountInfoTask;
import fb.wallpaper.chat.utils.Utils;
import fb.wallpaper.chat.view.fragment.AccountInfoFragment;
import fb.wallpaper.chat.view.list.adapter.SmileysAdapter;

public class DiscussArrayAdapter extends ArrayAdapter<Message> {

	private TextView messageText;
	private List<Message> messages = new ArrayList<Message>();
	private List<Category> categories;
	private LinearLayout wrapper;
	private TextView tv;
	private LayoutInflater inflater;

	@Override
	public void add(Message object) {
		if(messages.size() != 0) {
			if((new Date(messages.get(messages.size()-1).getCreatedTime()).getYear() != 
					new Date(object.getCreatedTime()).getYear()) || 
					(new Date(messages.get(messages.size()-1).getCreatedTime()).getMonth() != 
					new Date(object.getCreatedTime()).getMonth()) ||
					(new Date(messages.get(messages.size()-1).getCreatedTime()).getDate() != 
					new Date(object.getCreatedTime()).getDate())) {
				messages.add(object);
				addCategory(new Date(object.getCreatedTime()).toLocaleString(), messages.size()-1);
			} else {
				messages.add(object);
			}
		} else {
			messages.add(object);
			addCategory(new Date(object.getCreatedTime()).toLocaleString(), messages.size()-1);
		}
		super.add(object);
	}

	public DiscussArrayAdapter(Context context, int textViewResourceId) {
		super(context, textViewResourceId);
		categories = new ArrayList<Category>();
		inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	}

	public int getCount() {
		return this.messages.size();
	}

	public Message getItem(int index) {
		return this.messages.get(index);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		Category category = new Category();
		/*if(categories.size() == 0) {
			getCategories(messages);
		}*/
		category = getCategory(position);

		if(category!=null) {
			view = inflater.inflate(R.layout.message_category, parent, false);
			TextView tv = (TextView) view.findViewById(R.id.message_separator);
			tv.setText(category.getText());
			return fillMessageRow(view, position);
		} else {
			view = inflater.inflate(R.layout.one_chat_message, null);
			return fillMessageRow(view, position);
		}		
	}

	private View fillMessageRow(View row, int position) {
		String time;
		int bubbleCase = 0;

		wrapper = (LinearLayout) row.findViewById(R.id.wrapper);
		Message coment = getItem(position);
		messageText = (TextView) row.findViewById(R.id.comment);
		messageText.setText(coment.getText());
		addSmiles(getContext(), (Spannable) messageText.getText());

		if(position==0) {
			if(coment.getType() == Message.MESSAGE_IN) {
				bubbleCase = 1;
			} else {
				bubbleCase = 3;
			}
		} else {
			if(coment.getType() != getItem(position-1).getType()) {
				if(coment.getType() == Message.MESSAGE_IN) {
					bubbleCase = 1;
				} else {
					bubbleCase = 3;
				}
			} else {
				if(coment.getType() == Message.MESSAGE_IN) {
					bubbleCase = 2;
				} else {
					bubbleCase = 4;
				}
			}
		}

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
		if(bubbleCase == 1) {
			String bubble_color_in = sharedPref.getString("incoming_bubble_color", "White");
			if("Red".equals(bubble_color_in)){
				wrapper.setBackgroundResource(R.drawable.bubble_in_red);
			} else if("Green".equals(bubble_color_in)){
				wrapper.setBackgroundResource(R.drawable.bubble_in_green);
			} else if("Blue".equals(bubble_color_in)){
				wrapper.setBackgroundResource(R.drawable.bubble_in_blue);
			} else if("Yellow".equals(bubble_color_in)){
				wrapper.setBackgroundResource(R.drawable.bubble_in_yellow);
			} else if("Grey".equals(bubble_color_in)){
				wrapper.setBackgroundResource(R.drawable.bubble_in_grey);
			} else if("White".equals(bubble_color_in)){
				wrapper.setBackgroundResource(R.drawable.bubble_in_white);
			}
			FrameLayout avatar = (FrameLayout) row.findViewById(R.id.userAva);
			avatar.setVisibility(ImageView.VISIBLE);
			RelativeLayout rLayout = (RelativeLayout) row.findViewById(R.id.relativeMessage);
			rLayout.setPadding(0, 10, 0, 0);
		} else if (bubbleCase == 2) {
			String bubble_color_in = sharedPref.getString("incoming_bubble_color", "White");
			if("Red".equals(bubble_color_in)){
				wrapper.setBackgroundResource(R.drawable.bubble_in_red_simple);
			} else if("Green".equals(bubble_color_in)){
				wrapper.setBackgroundResource(R.drawable.bubble_in_green_simple);
			} else if("Blue".equals(bubble_color_in)){
				wrapper.setBackgroundResource(R.drawable.bubble_in_blue_simple);
			} else if("Yellow".equals(bubble_color_in)){
				wrapper.setBackgroundResource(R.drawable.bubble_in_yellow_simple);
			} else if("Grey".equals(bubble_color_in)){
				wrapper.setBackgroundResource(R.drawable.bubble_in_grey_simple);
			} else if("White".equals(bubble_color_in)){
				wrapper.setBackgroundResource(R.drawable.bubble_in_white_simple);
			}
		} else if (bubbleCase == 3) {
			String bubble_color_out = sharedPref.getString("outgoing_bubble_color", "Blue");
			if("Red".equals(bubble_color_out)){
				wrapper.setBackgroundResource(R.drawable.bubble_out_red);
			} else if("Green".equals(bubble_color_out)){
				wrapper.setBackgroundResource(R.drawable.bubble_out_green);
			} else if("Blue".equals(bubble_color_out)){
				wrapper.setBackgroundResource(R.drawable.bubble_out_blue);
			} else if("Yellow".equals(bubble_color_out)){
				wrapper.setBackgroundResource(R.drawable.bubble_out_yellow);
			} else if("Grey".equals(bubble_color_out)){
				wrapper.setBackgroundResource(R.drawable.bubble_out_grey);
			} else if("White".equals(bubble_color_out)){
				wrapper.setBackgroundResource(R.drawable.bubble_out_white);
			}
			FrameLayout avatar = (FrameLayout) row.findViewById(R.id.myAva);
			avatar.setVisibility(ImageView.VISIBLE);
			RelativeLayout rLayout = (RelativeLayout) row.findViewById(R.id.relativeMessage);
			rLayout.setPadding(0, 10, 0, 0);
		} else {
			String bubble_color_out = sharedPref.getString("outgoing_bubble_color", "Blue");
			if("Red".equals(bubble_color_out)){
				wrapper.setBackgroundResource(R.drawable.bubble_out_red_simple);
			} else if("Green".equals(bubble_color_out)){
				wrapper.setBackgroundResource(R.drawable.bubble_out_green_simple);
			} else if("Blue".equals(bubble_color_out)){
				wrapper.setBackgroundResource(R.drawable.bubble_out_blue_simple);
			} else if("Yellow".equals(bubble_color_out)){
				wrapper.setBackgroundResource(R.drawable.bubble_out_yellow_simple);
			} else if("Grey".equals(bubble_color_out)){
				wrapper.setBackgroundResource(R.drawable.bubble_out_grey_simple);
			} else if("White".equals(bubble_color_out)){
				wrapper.setBackgroundResource(R.drawable.bubble_out_white_simple);
			}
		}

		LinearLayout messageRow = (LinearLayout) row.findViewById(R.id.message);
		messageRow.setGravity((coment.getType() == Message.MESSAGE_IN) ? Gravity.LEFT : Gravity.RIGHT);
		Date date = new Date (coment.getCreatedTime());
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		time = sdf.format(date);
		
		tv = (TextView) row.findViewById(R.id.message_time);
		tv.setText(time);

		if(bubbleCase == 3) {
			final View v = row;
			ProfileInfoProvider dataProvider = ProfileDataProviderFactory.getProfileProvider();
			final ImageLoader imageLoader = ImageLoader.getInstance();
			final ProgressBar spinner = (ProgressBar) v.findViewById(R.id.loading);
			dataProvider.fetchPersonalInfo(new PersonalInfoFetchListener() {
				@Override
				public void onPersonalInfoFetched(FBUser personalInfo, Exception e) {
					if(personalInfo != null){
						ImageView myAvatar = (ImageView) v.findViewById(R.id.myAvatar);
						//Uri uri = Uri.parse(personalInfo.getProfilePictureSquare(50));
						//myAvatar.setImageURI(uri);
						imageLoader.displayImage(personalInfo.getProfilePictureSquare(50), myAvatar, Utils.DEFAULT_IMAGE_OPTIONS,
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
				}
			});
		}

		if(bubbleCase == 1) {
			final View v = row;
			ProfileInfoProvider dataProvider = ProfileDataProviderFactory.getProfileProvider();
			final ImageLoader imageLoader1 = ImageLoader.getInstance();
			final ProgressBar spinner1 = (ProgressBar) v.findViewById(R.id.loading1);
			dataProvider.fetchUserInfo(coment.getUid(), new UserInfoFetchListener() {

				@Override
				public void onUserInfoFetched(FBUser user, Exception e) {
					if(user != null) {
						ImageView userAvatar = (ImageView) v.findViewById(R.id.userAvatar);
						imageLoader1.displayImage(user.getProfilePictureSquare(50), userAvatar, Utils.DEFAULT_IMAGE_OPTIONS,
								new SimpleImageLoadingListener() {
							@Override
							public void onLoadingStarted(String imageUri, View view) {
								spinner1.setVisibility(View.VISIBLE);
							}

							@Override
							public void onLoadingFailed(String imageUri, View view, FailReason failReason) {									
								spinner1.setVisibility(View.GONE);
							}

							@Override
							public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
								spinner1.setVisibility(View.GONE);
							}
						});
					}
				}

			});
		}
		return row;
	}

	public Bitmap decodeToBitmap(byte[] decodedByte) {
		return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
	}


	private static final Factory spannableFactory = Spannable.Factory.getInstance();
	private static final Map<Pattern, Integer> emoticons = new HashMap<Pattern, Integer>();

	static {
		for (Smile s : SmileysAdapter.IMAGES)  {
			addPattern(s.getText(), s.getResourceId());		    
		}	    
	}

	private static void addPattern(String smile, int resource) {
		emoticons.put(Pattern.compile(Pattern.quote(smile)), resource);
	}

	public static boolean addSmiles(Context context, Spannable spannable) {
		boolean hasChanges = false;
		for (Entry<Pattern, Integer> entry : emoticons.entrySet()) {
			Matcher matcher = entry.getKey().matcher(spannable);
			while (matcher.find()) {
				boolean set = true;
				for (ImageSpan span : spannable.getSpans(matcher.start(), matcher.end(), ImageSpan.class))
					if (spannable.getSpanStart(span) >= matcher.start() && spannable.getSpanEnd(span) <= matcher.end())
						spannable.removeSpan(span);
					else {
						set = false;
						break;
					}
				if (set) {
					hasChanges = true;
					spannable.setSpan(new ImageSpan(context, entry.getValue()),
							matcher.start(), matcher.end(),
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
		}
		return hasChanges;
	}

	public static Spannable getSmiledText(Context context, CharSequence text) {
		Spannable spannable = spannableFactory.newSpannable(text);
		addSmiles(context, spannable);
		return spannable;
	}

	/*private void getCategories(List<Message> messages) {
		int itemCount = 0;
		for (int i = messages.size()-1; i >= 0; i--) {
			if(i == 0) {
				//Category
				addCategory(new Date(messages.get(i).getCreatedTime()).toLocaleString(), i, itemCount);
				itemCount = 0;
			} else {
				if(new Date(messages.get(i).getCreatedTime()).getDate() == 
						new Date(messages.get(i-1).getCreatedTime()).getDate()) {
					//Simple message
					itemCount++;
				} else {
					//Category
					addCategory(new Date(messages.get(i).getCreatedTime()).toLocaleString(), i, itemCount);
					itemCount = 0;
				}
			}
		}
	}*/

	private void addCategory(String text, int index) {
		Category category = new Category();
		category.setIndex(index);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		String str = sdf.format(new Date(messages.get(index).getCreatedTime()));
		category.setText(str);
		categories.add(category);
	}

	private Category getCategory(int position) {
		for (int i = 0; i < categories.size(); i++) {
			if(categories.get(i).getIndex() == position)
				return categories.get(i);
		}
		return null;
	}

}