package fb.wallpaper.chat;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

public class AboutActivity extends SherlockActivity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
        getSupportActionBar().hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
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
