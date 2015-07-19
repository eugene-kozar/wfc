package fb.wallpaper.chat;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import fb.wallpaper.chat.data.FBUser;
import fb.wallpaper.chat.data.Message;
import fb.wallpaper.chat.view.FBChatView;

public class ChatActivity extends Activity {
	private FBChatView fbcv;
	private EditText ect;
	private Button btn;
	private TextView fr_name;
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
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);
               
        fr_name = (TextView) findViewById(R.id.friend_name);
		fr_name.setText("Test");
		
        fbcv = (FBChatView) findViewById(R.id.messageList);
		ect = (EditText) findViewById(R.id.editChatText);
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if(!sharedPref.getBoolean("auto_correct", true)) 
			ect.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
		if(sharedPref.getBoolean("auto_capitalization", false))
			ect.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
		if(!sharedPref.getBoolean("allow_landscape", false))
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		ect.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// Perform action on key press
					Message m = new Message();
					m.setText(ect.getText().toString());
					m.setCreatedTime(new Date().getTime());
					m.setType(Message.MESSAGE_OUT);
					fbcv.addMessage(m);
					ect.setText("");
					return true;
				}
				return false;
			}
		});
		
		btn = (Button) findViewById(R.id.send_button);
		btn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				Message m = new Message();
				m.setText(ect.getText().toString());
				m.setCreatedTime(new Date().getTime());
				m.setType(Message.MESSAGE_OUT);
				fbcv.addMessage(m);
				ect.setText("");
			}
		});
		
		addItems();
		/*SharedPreferences sharedPref = PreferenceManager.
                getDefaultSharedPreferences(getBaseContext());*/
		
		if(sharedPref.getBoolean("message_app_open", true)) {
			try {
				
				String alarms = sharedPref.getString("ringtone", "default ringtone");
				Uri alert = Uri.parse(alarms);
				
	        	MediaPlayer mMediaPlayer = new MediaPlayer();
	        	mMediaPlayer.setDataSource(this, alert);
	        	final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	        	if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
		        	mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
		        	mMediaPlayer.prepare();
		        	mMediaPlayer.start();
	        	}
	        } catch(Exception e) {}
		}
	}
	
	public void addItems() {
		List<Message> mList = new LinkedList<Message>();
		FBUser fbuser = new FBUser();
		
		Message m1 = new Message(new Date().getTime(), "Hello bubbles!", fbuser, Message.MESSAGE_IN, fbuser.getUid());
		mList.add(m1);
		
		Message m2 = new Message(new Date().getTime(), "Hi!", fbuser, Message.MESSAGE_OUT, fbuser.getUid());
		mList.add(m2);
		
		Message m3 = new Message(new Date().getTime(), "Good!", fbuser, Message.MESSAGE_IN, fbuser.getUid());
		mList.add(m3);

		fbcv.addMessages(mList);
	}
}
