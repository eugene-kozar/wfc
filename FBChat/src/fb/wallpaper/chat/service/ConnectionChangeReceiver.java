package fb.wallpaper.chat.service;

import org.apache.log4j.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.facebook.Session;

public class ConnectionChangeReceiver extends BroadcastReceiver {

	private static final Logger LOG = Logger.getLogger(ConnectionChangeReceiver.class);
	private Context context;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.context = context;
		LOG.info("Network connectivity change");
		Intent networkIntent = new Intent("NetworkChangedReceiver");
		LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(context);
		broadcaster.sendBroadcast(networkIntent);
		if (isNetworkAvailable()) {
			LOG.info("Network state change event: network available");
			
			Session session = Session.getActiveSession();
			String accessToken = null;
			String consumerKey = null;		
			if (session != null) {
				accessToken = session.getAccessToken();
				consumerKey = session.getApplicationId();
			}		
			
			if (accessToken != null && consumerKey != null) {		
				LOG.info("Starting service from ConnectionStateSchangeReceiver...");
				Intent service = new Intent(context, FBChatService.class);
				context.startService(service);
				sharedPreferences.edit().putBoolean("service_work", true).commit();
			} else {
				LOG.info("No active session... Service is not started");
				sharedPreferences.edit().putBoolean("service_work", false).commit();
			}			
		} else {
			LOG.info("Stopping service from ConnectionStateSchangeReceiver...");
			context.stopService(new Intent(context, FBChatService.class));
			sharedPreferences.edit().putBoolean("service_work", false).commit();
		}
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}