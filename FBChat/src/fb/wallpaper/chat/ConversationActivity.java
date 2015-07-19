package fb.wallpaper.chat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import fb.wallpaper.chat.data.FBUser;
import fb.wallpaper.chat.data.Message;
import fb.wallpaper.chat.data.MessageThread;
import fb.wallpaper.chat.data.Smile;
import fb.wallpaper.chat.data.provider.ProfileDataProviderFactory;
import fb.wallpaper.chat.data.provider.ProfileInfoProvider;
import fb.wallpaper.chat.data.provider.listener.MessageHistoryFetchListener;
import fb.wallpaper.chat.data.provider.listener.RecentThreadsFetchListener;
import fb.wallpaper.chat.database.MessageDAO;
import fb.wallpaper.chat.database.async.SaveMessagesTask;
import fb.wallpaper.chat.utils.Constants;
import fb.wallpaper.chat.view.FBChatView;
import fb.wallpaper.chat.view.adapters.ImageGalleryAdapter;
import fb.wallpaper.chat.view.list.adapter.ChatsAdapter;
import fb.wallpaper.chat.view.list.adapter.ImagePagerAdapter;
import fb.wallpaper.chat.view.list.adapter.SmileysAdapter;

public class ConversationActivity extends SherlockActivity {

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private FBChatView fbcv;
	private EditText ect;
	private TextView fr_name;
	private Button btn;
	private LocalBroadcastManager broadcaster;
	private ImageView smilesPopupBtn;
	private FrameLayout chatsPopupBtn;
	private ChatsAdapter chatsAdapter;
	private ProfileInfoProvider dataProvider;
	private long tmpTime = 0;
	private String friendname = "";
	private String[] images;
	private FrameLayout bottomPanel;
	private String fromWhere;

	private BroadcastReceiver fbChatServiceReceiver;
	private BroadcastReceiver fbChatThreadReceiver;

	private GridView smilesView;
	private View backgroundsView;
	private LinearLayout chatsView;
	private List<Message> messages;
	private FBUser user;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_conversation);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getSupportActionBar().hide();

		broadcaster = LocalBroadcastManager.getInstance(this);
		if((boolean) getIntent().getBooleanExtra("fromFriends", false)) {
			fromWhere = "Friends";
		} else {
			fromWhere = "Chats";
		}
		//Original code...
		/*final FBUser user = (FBUser) getIntent().getSerializableExtra("userWith");
		String friendname = user.getName();*/

		//Now there is app is not crashes during NullPointerException, but this code is not good...

		FBUser user1 = new FBUser();
		boolean b = false;
		try {
			user1 = (FBUser) getIntent().getSerializableExtra("userWith");
			friendname = user1.getName();
			b = true;
		} catch (Exception e) {
			b = false;
			Toast.makeText(getApplicationContext(), "No name", Toast.LENGTH_SHORT).show();
		}
		if(b)
			user = user1;
		else {
			user = new FBUser();
			user.setName("Unknown user");
		}
		///////////////////////////////////////////////////////////////////////////
		//final Gallery gallery = (Gallery) findViewById(R.id.gallery);
		//gallery.setAdapter(new ImageGalleryAdapter(this, Constants.IMAGES));
		////////////////////////////////////////////////////////////////////////////
		fr_name = (TextView) findViewById(R.id.friend_name);
		fr_name.setText(friendname);
		fr_name.setFocusableInTouchMode(true);
		fr_name.requestFocus();

		fbcv = (FBChatView) findViewById(R.id.messageList);
		fbcv.setDivider(null);
		fbcv.setDividerHeight(0);
		ect = (EditText) findViewById(R.id.editChatText);

		changeBackground();

		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if(!sharedPref.getBoolean("auto_correct", true)) 
			ect.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
		if(sharedPref.getBoolean("auto_capitalization", false))
			ect.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		if(!sharedPref.getBoolean("allow_landscape", true))
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		//Counting of users, which have unread messages
		dataProvider = ProfileDataProviderFactory.getProfileProvider();
		dataProvider.fetchRecentThreads(new RecentThreadsFetchListener() {			
			@Override
			public void onThreadsFetched(List<MessageThread> threads, Exception e) {
				int unreadUsersCount = 0;
				if(threads!=null) {
					for (MessageThread messageThread : threads) {
						if(messageThread.getUnreadCount() != 0) {
							unreadUsersCount++;
						}
					}
				}
				if(unreadUsersCount != 0) {
					TextView textView = (TextView) findViewById(R.id.chats_bubble_text);
					textView.setText(unreadUsersCount + "");
					textView.setBackgroundResource(R.drawable.unread_bubble);
				}
			}
		});	

		fbChatThreadReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Message m = (Message) intent.getSerializableExtra("FBChatResult");
				if(m == null) {
					Toast.makeText(getApplicationContext(), 
							"Message was not sent successfully", 
							Toast.LENGTH_SHORT).show();
				} else {
					if(tmpTime == m.getCreatedTime()) {
						fbcv.addMessage(m);
						ect.setText("");
						tmpTime = 0;
					}
				}				
			}
		};

		smilesPopupBtn = (ImageView) findViewById(R.id.plus_button);
		smilesPopupBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (bottomPanel != null) {
					toogleBottomPanel();
				} else {
					initiateBottomPanel();
				}
			}
		});

		chatsPopupBtn = (FrameLayout) findViewById(R.id.chats_button);
		chatsPopupBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (chatsView == null) {
					initiateChatsPopupWindow();
				}
				toggleChatsView();
			}
		});

		ect.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					if(!"".equals(ect.getText().toString())) {
						Message m = new Message();
						m.setUserWith(user);
						m.setText(ect.getText().toString());
						m.setCreatedTime(new Date().getTime());
						m.setType(Message.MESSAGE_OUT);
						sendMessage(m);						
						return true;
					}
				}
				return false;
			}
		});

		btn = (Button) findViewById(R.id.send_button);
		if (!isNetworkAvailable()) {
			btn.setEnabled(false);
		}
		btn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				//fbcv.addMessage(new OneMessage(false, ect.getText().toString(), new Date()));
				if(!"".equals(ect.getText().toString())) {
					Message m = new Message();
					m.setUserWith(user);
					m.setText(ect.getText().toString());
					m.setCreatedTime(new Date().getTime());
					m.setType(Message.MESSAGE_OUT);
					sendMessage(m);						
				}	
			}
		});

		btn = (Button) findViewById(R.id.back_button);
		btn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		fbChatServiceReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Message s = (Message) intent.getSerializableExtra("FBChat");
				if (user.equals(s.getUserWith())) {
					addMessage(s);
				} else {
					//Counting of users, which have unread messages
					dataProvider = ProfileDataProviderFactory.getProfileProvider();
					dataProvider.fetchRecentThreads(new RecentThreadsFetchListener() {			
						@Override
						public void onThreadsFetched(List<MessageThread> threads, Exception e) {
							int unreadUsersCount = 0;
							for (MessageThread messageThread : threads) {
								if(messageThread.getUnreadCount() != 0) {
									unreadUsersCount++;
								}
							}
							if(unreadUsersCount != 0) {
								TextView textView = (TextView) findViewById(R.id.chats_bubble_text);
								textView.setText(unreadUsersCount + "");
								textView.setBackgroundResource(R.drawable.unread_bubble);
							}
						}
					});	
					Toast.makeText(ConversationActivity.this, "Message received from another user", Toast.LENGTH_SHORT).show();
				}
			}
		};
		final ProgressBar loading = (ProgressBar) findViewById(R.id.loading);
		loading.setVisibility(View.VISIBLE);

		ProfileInfoProvider provider = ProfileDataProviderFactory.getProfileProvider();
		provider.fetchMessageHistory(user, new MessageHistoryFetchListener() {

			@Override
			public void onMessageHistoryFetched(List<Message> messages, Exception e) {
				loading.setVisibility(View.GONE);
				if (e == null) {
					ConversationActivity.this.messages = messages; 
					SaveMessagesTask sMessageTask = new SaveMessagesTask(ConversationActivity.this);
					sMessageTask.execute(messages);
					for (int i = messages.size(); i > 0; i--) {
						addMessage(messages.get(i-1));
					}
				} else {
					try {
						MessageDAO mDao = new MessageDAO(ConversationActivity.this);
						List<Message> messagesFromDb = mDao.getMessagesForUser(user.getUid());
						ConversationActivity.this.messages = messagesFromDb;
						for (int i = 0; i < messagesFromDb.size(); i++) {
							addMessage(messagesFromDb.get(i));
						}												
					} catch(Exception ex) {
						Toast.makeText(ConversationActivity.this, ex.getClass().toString(), Toast.LENGTH_SHORT).show();
					}
				}
			}
		});				
	}

	private void toggleChatsView() {
		if (chatsView != null) {
			changeChatsVisibility(!(chatsView.getVisibility() == View.VISIBLE));
		}
	}

	private void changeChatsVisibility(boolean visible) {
		if (chatsView != null) {
			if (visible) {
				chatsView.setVisibility(View.VISIBLE);
			} else {
				chatsView.setVisibility(View.GONE);
			}
		}
	}

	private void toogleBottomPanel() {
		if (bottomPanel != null) {
			changeBotPanelVisibility(!(bottomPanel.getVisibility() == View.VISIBLE));
		}
	}

	private void changeBotPanelVisibility(boolean visible) {
		if (bottomPanel != null) {
			if (visible) {
				smilesPopupBtn.setImageResource(R.drawable.close);
				bottomPanel.setVisibility(View.VISIBLE);
			} else {
				smilesPopupBtn.setImageResource(R.drawable.plus);
				bottomPanel.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void onBackPressed() {
		if (bottomPanel != null && bottomPanel.getVisibility() == View.VISIBLE) {
			changeBotPanelVisibility(false);
		} else {
			super.onBackPressed();
			/*Intent refresh = new Intent(this, LoginActivity.class);
			refresh.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			refresh.putExtra("fromWhere", fromWhere);
			startActivity(refresh);
			this.finish();*/
		}
	}

	private void initiateBottomPanel() {
		try {
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.popup_smiles, (ViewGroup) findViewById(R.id.smileys_popup_content));

			bottomPanel = (FrameLayout) findViewById(R.id.slidePanel);
			bottomPanel.addView(layout);
			changeBotPanelVisibility(true);			

			final FrameLayout bottomContent = (FrameLayout) layout.findViewById(R.id.bottomContent);

			smilesView = getSmilesView();
			bottomContent.removeAllViews();
			bottomContent.addView(smilesView);

			ImageView showSmileysButton = (ImageView) layout.findViewById(R.id.showSmiles);
			showSmileysButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (smilesView == null) {
						smilesView = getSmilesView();
					}
					bottomContent.removeAllViews();
					bottomContent.addView(smilesView);
				}
			});

			ImageView showBackgroundsButton = (ImageView) layout.findViewById(R.id.showBackgrounds);
			Display display = getWindowManager().getDefaultDisplay(); 
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,      
					RelativeLayout.LayoutParams.WRAP_CONTENT
					);
			params.setMargins((display.getWidth() - 116)/2, 0, 0, 0);
			showBackgroundsButton.setLayoutParams(params);		
			showBackgroundsButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (backgroundsView == null) {
						backgroundsView = getBackgroundView();
					}
					bottomContent.removeAllViews();
					bottomContent.addView(backgroundsView);
				}
			});


			ImageView sendEmail = (ImageView) layout.findViewById(R.id.sendEmail);		
			sendEmail.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
					emailIntent.setType("plain/text");
					emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, converChatToString(messages));
					ConversationActivity.this.startActivity(Intent.createChooser(emailIntent, "Send email..."));			 
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private GridView getSmilesView() {
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.panel_part_smiles, null);

		smilesView = (GridView) layout.findViewById(R.id.smiles_grid);
		smilesView.setAdapter(new SmileysAdapter(this));			
		smilesView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				Smile smile = SmileysAdapter.IMAGES[position];					
				ect.append(smile.getText());					
			}
		});

		return smilesView;
	}

	private View getBackgroundView() {
		try {
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final View layout = inflater.inflate(R.layout.popup_background, null);

			final Gallery gallery = (Gallery) layout.findViewById(R.id.gallery);
			//final Spinner m_myDynamicSpinner = (Spinner) layout.findViewById(R.id.picture_category);

			gallery.setAdapter(new ImageGalleryAdapter(this, Constants.Thor2_80x80));
			String array[] = { "Thor 2"};
			ArrayAdapter<String> sp_adapter = new ArrayAdapter<String>(this, R.layout.spinner_text, array);
			sp_adapter.setDropDownViewResource(R.layout.spinner_item);
			gallery.setSelection(2);
			/*m_myDynamicSpinner.setAdapter(sp_adapter);
			m_myDynamicSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
					String picture_category = m_myDynamicSpinner.getSelectedItem().toString();
					if("Thor 2".equals(picture_category)) {		
						images = Constants.Thor2_80x80;
						gallery.setAdapter(new ImageGalleryAdapter(ConversationActivity.this, images));
					}
					//else if("Cars".equals(picture_category)) {}
					gallery.setSelection(2);
				}
				@Override
				public void onNothingSelected(AdapterView<?> parentView) {}
			});	*/

			Button setBtn = (Button) layout.findViewById(R.id.set_background_btn);
			setBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
					images = Constants.Thor2;
					sharedPref.edit().putString("background"+friendname, images[gallery.getSelectedItemPosition()]).commit();
					changeBackground();
				}
			});
			return layout;
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(this, "Exception\n" + e.getClass().toString(), Toast.LENGTH_LONG).show();
		}
		return null;
	}

	private void initiateChatsPopupWindow() {
		try {
			LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.popup_chats, null);

			GridView  gridView = (GridView) view.findViewById(R.id.chats_grid);
			chatsAdapter = new ChatsAdapter(this, R.id.profile_picture, new ArrayList<MessageThread>());
			gridView.setAdapter(chatsAdapter);	

			dataProvider = ProfileDataProviderFactory.getProfileProvider();
			dataProvider.fetchRecentThreads(new RecentThreadsFetchListener() {			
				@Override
				public void onThreadsFetched(List<MessageThread> threads, Exception e) {
					if (e == null) {
						if(threads!=null) {
							chatsAdapter.setData(threads.subList(0, 5));
							chatsAdapter.notifyDataSetChanged();
						}
					} else {
						Toast.makeText(getApplicationContext(), "dataProvider don't work\n" + e.getClass().toString(), Toast.LENGTH_SHORT).show();					
					}	
				}
			});

			chatsView = (LinearLayout) findViewById(R.id.topPanel);
			chatsView.addView(view);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String converChatToString(List<Message> messages) {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String userName = sharedPref.getString("USER_NAME", "Me");

		StringBuilder result = new StringBuilder();
		for(Message m : messages) {
			if(m.getType() == Message.MESSAGE_IN) {
				result.append(m.getUserWith().getName());
			} else {
				result.append(userName);
			}
			result.append("(");
			result.append(sdf.format(new Date(m.getCreatedTime())));
			result.append("):");
			result.append(m.getText());
			result.append("\n");
		}		
		return result.toString();
	}

	public void addMessage(Message m) {
		fbcv.addMessage(m);
	}

	@Override
	protected void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(this).registerReceiver((fbChatServiceReceiver), new IntentFilter("FBChat"));
		LocalBroadcastManager.getInstance(this).registerReceiver((fbChatThreadReceiver), new IntentFilter("FBChatResult"));
		EasyTracker t = EasyTracker.getInstance(this);
		t.activityStart(this);
	//	t.send(MapBuilder
	//		    .createAppView()
	//		    .set(Fields.SCREEN_NAME,getClass().getSimpleName())
	//		    .build()
	//		);
	}

	@Override
	protected void onStop() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(fbChatServiceReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(fbChatThreadReceiver);
		EasyTracker.getInstance(this).activityStop(this);
		super.onStop();
	}

	public void sendMessage(Message message) {
		if(isNetworkAvailable()) {
			Intent intent = new Intent("FBChatOut");
			if(message != null) {
				intent.putExtra("FBChatOut", message);
				tmpTime = message.getCreatedTime();
			}
			broadcaster.sendBroadcast(intent);
		} else {
			Toast.makeText(getApplicationContext(), "No Internet connection", Toast.LENGTH_SHORT).show();
		}
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.isAvailable();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		changeBackground();		
	}

	private void changeBackground() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String background = sharedPref.getString("background"+friendname, null);
		if(background==null) {
			background = sharedPref.getString("background", null);
		} 
		if(background!=null) {
			int imageResourse = Integer.parseInt(background.substring(background.lastIndexOf("/")+1));
			Bitmap icon = BitmapFactory.decodeResource(getResources(), imageResourse);
			int width = icon.getWidth();
			int height = icon.getHeight();
			Bitmap fond;
			if(Configuration.ORIENTATION_PORTRAIT == getResources().getConfiguration().orientation) {
				if(height > width)
					fond = Bitmap.createBitmap(icon, 0, 0, width, height);
				else
					fond = Bitmap.createBitmap(icon, 0, 0, height*height/width, height);
			} else {
				if(height > width)
					fond = Bitmap.createBitmap(icon, 0, 0, width, width*width/height);
				else
					fond = Bitmap.createBitmap(icon, 0, 0, width, height);
			}			
			Drawable drawable = new BitmapDrawable(getResources(), fond);
			getWindow().setBackgroundDrawable(drawable);
		}
	}

}
