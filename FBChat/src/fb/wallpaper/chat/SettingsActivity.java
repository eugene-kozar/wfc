package fb.wallpaper.chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

public class SettingsActivity extends SherlockPreferenceActivity implements OnSharedPreferenceChangeListener{
	private SharedPreferences prefs;
	private LocalBroadcastManager broadcaster;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSupportActionBar().hide();
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        broadcaster = LocalBroadcastManager.getInstance(getApplicationContext());

        /*getActionBar().hide();
		getActionBar().setCustomView(R.layout.action_bar);
		getActionBar().setDisplayShowCustomEnabled(true);
		getActionBar().setDisplayUseLogoEnabled(false);
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayShowHomeEnabled(false);*/
        
        String action = getIntent().getAction();
        if ("fb.wallpaper.chat.AboutAction".equals(action)) {
        	Intent intent = new Intent(SettingsActivity.this, AboutActivity.class);
			startActivity(intent);
        }
    }
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    	if (key.equals("buddies_sort") || key.equals("hide_offline")) {
    		Intent intent = new Intent("FBChatFriendsChanged");
    		broadcaster.sendBroadcast(intent);
        }
    	if (key.equals("background")) {
    		Intent intent = new Intent("FBChatBackgroundChanged");
    		broadcaster.sendBroadcast(intent);
        }
    	if (key.equals("allow_landscape")) {
    		Intent intent = new Intent("FBChatLandscapeChanged");
    		broadcaster.sendBroadcast(intent);
			if(!prefs.getBoolean("allow_landscape", true)) {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			} else {
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
			}
        }
	}

	@Override
	protected void onResume() {
		super.onResume();
		prefs.registerOnSharedPreferenceChangeListener((OnSharedPreferenceChangeListener) this);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		prefs.unregisterOnSharedPreferenceChangeListener((OnSharedPreferenceChangeListener) this);
	}
	  @Override
	  public void onStart() {
	    super.onStart();
		EasyTracker t = EasyTracker.getInstance(this);
		t.activityStart(this);
	//	t.send(MapBuilder
	//		    .createAppView()
	//		    .set(Fields.SCREEN_NAME,getClass().getSimpleName())
	//		    .build()
	//		);
	  }

	  @Override
	  public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);
	  }
}
