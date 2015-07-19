package fb.wallpaper.chat.view.fragment;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragment;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public abstract class TrackedFragment extends SherlockFragment {
    private Tracker tracker;
    private String activitysName;
    private String fragmentsName;
    @Override
    public void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        tracker = EasyTracker.getInstance(this.getActivity());
        fragmentsName = getClass().getSimpleName();
        activitysName = getActivity().getClass().getSimpleName();
    }

    @Override
    public void onResume() {

        super.onResume();
        tracker.set(Fields.SCREEN_NAME,activitysName+"/"+fragmentsName);
        tracker.send( MapBuilder.createAppView().build() );
    }
}
