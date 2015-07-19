package fb.wallpaper.chat;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.facebook.Session;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import fb.wallpaper.chat.data.FBUser;
import fb.wallpaper.chat.database.AccountDAO;
import fb.wallpaper.chat.service.FBChatService;
import fb.wallpaper.chat.view.fragment.AccountInfoFragment;
import fb.wallpaper.chat.view.fragment.BackgroundFragment;
import fb.wallpaper.chat.view.fragment.ChatsFragment;
import fb.wallpaper.chat.view.fragment.FriendsListFragment;
import fb.wallpaper.chat.view.fragment.LoginFragment;

public class LoginActivity extends SherlockFragmentActivity /*implements ActionBar.TabListener*/ {

	private static final Logger LOG = Logger.getLogger(LoginActivity.class);

	private SherlockFragment currentFragment;
	private Map<String, SherlockFragment> fragments;

	// TODO move this to string.xml
	public static final String FRIEDS_TAB = "Friends";
	public static final String ACCOUTN_TAB = "Account";
	public static final String CHATS_TAB = "Chats";
	public static final String BACKGROUND_TAB = "Background";
	public static String CurrentFragment = "";

	private BroadcastReceiver backgroundChangedReceiver;
	private BroadcastReceiver networkChangedReceiver;
	//private BroadcastReceiver landscapeChangedReceiver;
	//private String fromWhere;
	//private boolean first = true;

	ImageView userPresence;
	TextView userName;
	ImageView friends;
	ImageView chats;
	ImageView background;
	ImageView settings;
	
	public static boolean USER_EXIST = false;
	
	public void refreshUserName() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String userNameStr = sharedPref.getString("USER_NAME", "");		
		userName.setText(userNameStr.substring(0, userNameStr.indexOf(" ")));				
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		LOG.info("LoginActivity onCreated(). Starting application...");
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		fragments = new HashMap<String, SherlockFragment>();
		fragments.put(FRIEDS_TAB, new FriendsListFragment());
		fragments.put(ACCOUTN_TAB, new AccountInfoFragment());
		fragments.put(CHATS_TAB, new ChatsFragment());
		fragments.put(BACKGROUND_TAB, new BackgroundFragment());
		/////////////////////////////////////////////////////////
		getSupportActionBar().hide();
		getSupportActionBar().setCustomView(R.layout.action_bar);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		/////////////////////////////////////////////////////////
		userPresence = (ImageView) findViewById(R.id.userPresence);
		if(isNetworkAvailable()) {
			userPresence.setBackgroundResource(R.drawable.circle_green);
		} else {
			userPresence.setBackgroundResource(R.drawable.circle_red);
		}
		/////////////////////////////////////////////////////////
		userName = (TextView) findViewById(R.id.userName);
		friends = (ImageView) findViewById(R.id.tab_friends);
		chats = (ImageView) findViewById(R.id.tab_chats);
		background = (ImageView) findViewById(R.id.tab_background);
		settings = (ImageView) findViewById(R.id.settings_button);
		FBUser me = null;
		try {
			AccountDAO accountDao = new AccountDAO(this);
			me = accountDao.getAccountInfo();
			accountDao.close();
		} catch (Exception e) {
			Toast.makeText(getApplicationContext(), e.getClass().toString(), Toast.LENGTH_SHORT).show();
		}
		
		if (me != null) {
			userName.setText(me.getName().substring(0, me.getName().indexOf(" ")));
			USER_EXIST = true;
			LOG.info("User exist: " + me.getName());
		}
		userName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				friends.setBackgroundResource(R.drawable.tab_friends);
				chats.setBackgroundResource(R.drawable.tab_chats);
				background.setBackgroundResource(R.drawable.tab_background);
				loadFragment(ACCOUTN_TAB);
			}
		});
		settings.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
				startActivity(intent);
			}
		});

		friends.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				friends.setBackgroundResource(R.drawable.tab_friends_dark);
				chats.setBackgroundResource(R.drawable.tab_chats);
				background.setBackgroundResource(R.drawable.tab_background);
				loadFragment(FRIEDS_TAB);
			}
		});

		chats.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				friends.setBackgroundResource(R.drawable.tab_friends);
				chats.setBackgroundResource(R.drawable.tab_chats_dark);
				background.setBackgroundResource(R.drawable.tab_background);
				loadFragment(CHATS_TAB);
			}
		});

		background.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				friends.setBackgroundResource(R.drawable.tab_friends);
				chats.setBackgroundResource(R.drawable.tab_chats);
				background.setBackgroundResource(R.drawable.tab_background_dark);
				loadFragment(BACKGROUND_TAB);
			}
		});

		if (savedInstanceState == null) {
			currentFragment = new LoginFragment();
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, currentFragment).commit();
		} else {
			currentFragment = (SherlockFragment) getSupportFragmentManager().findFragmentById(android.R.id.content);
		}

		changeBackground();		
		backgroundChangedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				LOG.info("OnReceive: backgroundChangedReceiver");
				changeBackground();		
			}
		};
		networkChangedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				LOG.info("OnReceive: networkChangedReceiver");
				if(isNetworkAvailable()) {
					userPresence.setBackgroundResource(R.drawable.circle_green);
				} else {
					userPresence.setBackgroundResource(R.drawable.circle_red);
				}	
			}
		};
		/*landscapeChangedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				if(!sharedPref.getBoolean("allow_landscape", false)) {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				} else {
					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
				}
			}
		};*/
		/*fromWhere = (String) getIntent().getStringExtra("fromWhere");
		if(fromWhere!=null) {
			if(FRIEDS_TAB.equals(fromWhere))
				loadFragment(FRIEDS_TAB);
			else if (CHATS_TAB.equals(fromWhere))
				loadFragment(CHATS_TAB);
		} */
		
		LOG.info("Application successfuly started");
	}

	public SherlockFragment getFragment(String name) {
		return fragments.get(name);
	}

	/*@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add("Settings");  
		menu.add("Logout");
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if("Settings".equals(item.getTitle())) {
			Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
			startActivity(intent);
		} 
		else if("Logout".equals(item.getTitle())) {
			OnClickListener okButtonListener = new OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {
					stopService(new Intent(LoginActivity.this, FBChatService.class));
					Session session = Session.getActiveSession();
					session.closeAndClearTokenInformation();
					finish();
				}
			};
			OnClickListener cancelButtonListener = new OnClickListener() {
				public void onClick(DialogInterface arg0, int arg1) {

				}
			};		
			new AlertDialog.Builder(this)
			.setTitle("Log out")
			.setMessage("Log out and shut down Facebook Wallpaper Chat?")
			.setPositiveButton("Log out", okButtonListener)
			.setNegativeButton("Cancel", cancelButtonListener)
			.show();
		}
		return super.onMenuItemSelected(featureId, item);
	}*/

	@Override
	protected void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(this).registerReceiver((backgroundChangedReceiver), 
				new IntentFilter("FBChatBackgroundChanged"));
		LocalBroadcastManager.getInstance(this).registerReceiver((networkChangedReceiver), 
				new IntentFilter("NetworkChangedReceiver"));
		/*LocalBroadcastManager.getInstance(this).registerReceiver((landscapeChangedReceiver), 
				new IntentFilter("FBChatLandscapeChanged"));*/	
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
		super.onStop();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(backgroundChangedReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(networkChangedReceiver);
		//LocalBroadcastManager.getInstance(this).unregisterReceiver(landscapeChangedReceiver);
		EasyTracker.getInstance(this).activityStop(this);
		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		changeBackground();		
	}

	private void changeBackground() {
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		String background = sharedPref.getString("background", null);
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

	private void loadFragment(String fragment) {
		/*getSupportActionBar().setCustomView(R.layout.action_bar);
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayShowHomeEnabled(false);*/
		try {
			InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); 
			inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Exception e) {
			// TODO: handle exception
		}		
		if(!getSupportActionBar().isShowing())
			getSupportActionBar().show();
		/*SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if(BACKGROUND_TAB.equals(fragment) || !sharedPref.getBoolean("allow_landscape", true)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}*/
		SherlockFragment targetFragment = fragments.get(fragment);
		if (targetFragment != null) {
			FragmentManager fm = getSupportFragmentManager();
			fm.beginTransaction().replace(android.R.id.content, targetFragment).commit();
			fm.executePendingTransactions();
		}
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.isAvailable();
	}
	
	public void logOut() {
		OnClickListener okButtonListener = new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				stopService(new Intent(LoginActivity.this, FBChatService.class));
				Session session = Session.getActiveSession();
				session.closeAndClearTokenInformation();
				finish();
			}
		};
		OnClickListener cancelButtonListener = new OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {

			}
		};		
		new AlertDialog.Builder(this)
			.setTitle("Log out")
			.setMessage("Log out and shut down Facebook Wallpaper Chat?")
			.setPositiveButton("Log out", okButtonListener)
			.setNegativeButton("Cancel", cancelButtonListener)
			.show();
	}

}
