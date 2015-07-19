package fb.wallpaper.chat.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;
import fb.wallpaper.chat.ConversationActivity;
import fb.wallpaper.chat.R;
import fb.wallpaper.chat.data.FBUser;
import fb.wallpaper.chat.data.Message;
import fb.wallpaper.chat.data.MessageThread;
import fb.wallpaper.chat.data.provider.ProfileDataProviderFactory;
import fb.wallpaper.chat.data.provider.ProfileInfoProvider;
import fb.wallpaper.chat.data.provider.listener.RecentThreadsFetchListener;
import fb.wallpaper.chat.data.provider.listener.UserInfoFetchListener;
import fb.wallpaper.chat.database.UserDAO;

public class FBChatService extends Service {
	
	private static final Logger LOG = Logger.getLogger(FBChatService.class);
	private ProfileInfoProvider dataProvider;
	private LocalBroadcastManager broadcaster;
	private FBChatThread chatServiceThread;
	private BroadcastReceiver fbChatServiceReceiver;
	private SharedPreferences sharedPref; 
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		broadcaster = LocalBroadcastManager.getInstance(this);
		chatServiceThread = new FBChatThread(this, broadcaster);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		sharedPref.edit().putString("ringtone", alert.toString()).commit();

		fbChatServiceReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Message s = (Message) intent.getSerializableExtra("FBChat");
				if(sharedPref.getBoolean("push_alerts", true)) {
					showNotification(s);
				}
				if(isApplicationSentToBackground(getBaseContext())) {
					if(sharedPref.getBoolean("message_app_background", true)) {
						playRingtone();
					}
				} else {
					if(sharedPref.getBoolean("message_app_open", true)) {
						playRingtone();
					}
				}
			}
		};
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {		
		LocalBroadcastManager.getInstance(this).registerReceiver((fbChatServiceReceiver), new IntentFilter("FBChat"));
		if (!chatServiceThread.isActive()) {
			chatServiceThread.start();		
		}
		return super.onStartCommand(intent, flags, startId);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(fbChatServiceReceiver);
		chatServiceThread.setActive(false);
		chatServiceThread.interrupt();
	}

	private void showNotification(Message message) {
		FBUser userWith = new FBUser();
		String contentText = "";
		
		if(sharedPref.getBoolean("show_alert_preview", true)) {
			userWith = getUserInfo(message.getUserWith().getUid());
			if(userWith != null)
				contentText = userWith.getName() + ": " + message.getText();
			else
				contentText = message.getUserWith().getUid() + ": " + message.getText();
		} 
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.logo)
			.setContentTitle("FBChat. Message Received")
			.setContentText(contentText);
		Intent resultIntent = new Intent(this, ConversationActivity.class);
		if(userWith != null)
			resultIntent.putExtra("userWith", userWith);

		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		stackBuilder.addParentStack(ConversationActivity.class);
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification n = mBuilder.build();
		n.flags |= Notification.FLAG_AUTO_CANCEL;

		mNotificationManager.notify(0, mBuilder.build());

	}

	public static boolean isApplicationSentToBackground(final Context context) {
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> tasks = am.getRunningTasks(1);
		if (!tasks.isEmpty()) {
			ComponentName topActivity = tasks.get(0).topActivity;
			if (!topActivity.getPackageName().equals(context.getPackageName())) {
				return true;
			}
		}
		return false;
	}

	public void playRingtone() {
		try {
			String alarms = sharedPref.getString("ringtone", null);
			Uri alert;
			alert = Uri.parse(alarms);

			MediaPlayer mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setDataSource(this, alert);
			final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mMediaPlayer.prepare();
				mMediaPlayer.start();				
				Thread.sleep(mMediaPlayer.getDuration());
				mMediaPlayer.stop();
			}
		} catch(Exception e) {
			LOG.error(e);
		}
	}

	public FBUser getUserInfo(String uid) {
		FBUser userInfo = new FBUser();
		UserDAO userDao = new UserDAO(getApplicationContext());
		userInfo = userDao.getUser(uid);
		userDao.close();
		return userInfo;		
		
		//Try to get user from Facebook by uid
		
		/*FBUser userInfo = new FBUser();
		try {
			UserDAO userDao = new UserDAO(getApplicationContext());
			userInfo = userDao.getUser(uid);
			if(userInfo == null)
				throw new Exception("No user in db");
			userDao.close();
		} catch (Exception e) {
			dataProvider = ProfileDataProviderFactory.getProfileProvider();
			dataProvider.fetchUserInfo(uid, new UserInfoFetchListener() {

				@Override
				public void onUserInfoFetched(FBUser user, Exception e) {
					if(e == null) {
						UserHolder.fbUser = user;
					} else {
						UserHolder.fbUser = new FBUser();
						UserHolder.fbUser.setName("Somebody");
						UserHolder.fbUser.setId(user.getId());
						UserHolder.fbUser.setOnlinePresence("available");
						UserHolder.fbUser.setPresence("No status");
						UserHolder.fbUser.setStatus("No status");
						UserHolder.fbUser.setUid(user.getUid());
						UserHolder.fbUser.setProfilePictureSquare("http://cs7003.vk.me/c408816/v408816954/56d4/7NfD2crsmNk.jpg");
					}
				}
			});	
			userInfo = UserHolder.fbUser;
		}			
		return userInfo;*/
	}
	
	/*private static class UserHolder {
		public static FBUser fbUser;
	}*/

}
