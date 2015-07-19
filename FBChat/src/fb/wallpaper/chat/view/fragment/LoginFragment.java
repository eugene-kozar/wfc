package fb.wallpaper.chat.view.fragment;

import java.util.Arrays;

import org.apache.log4j.Logger;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Gallery;
import android.widget.Spinner;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

import fb.wallpaper.chat.R;
import fb.wallpaper.chat.service.FBChatService;
import fb.wallpaper.chat.utils.Constants;
import fb.wallpaper.chat.view.adapters.ImageGalleryAdapter;
import fb.wallpaper.chat.view.list.adapter.ImagePagerAdapter;

public class LoginFragment extends TrackedFragment {
	private static final Logger LOG = Logger.getLogger(FriendsListFragment.class);
	private SharedPreferences sharedPref;

	private UiLifecycleHelper uiHelper;
	
	private Session.StatusCallback callback = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    uiHelper = new UiLifecycleHelper(getActivity(), callback);
	    uiHelper.onCreate(savedInstanceState);
	    getSherlockActivity().getSupportActionBar().hide();
	    LOG.info("Login Fragment onCreate");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    LOG.info("Login Fragment onCreateView");
	    View view = inflater.inflate(R.layout.fragment_login, container, false);
		sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

	    LoginButton authButton = (LoginButton) view.findViewById(R.id.login_button);
		authButton.setFragment(this);	
		authButton.setReadPermissions(Arrays.asList("friends_online_presence", "user_online_presence", "xmpp_login", "user_status", "read_mailbox"));

		final Gallery gallery = (Gallery) view.findViewById(R.id.gallery);
		//final Spinner m_myDynamicSpinner = (Spinner) view.findViewById(R.id.picture_category);
		final ViewPager pager = (ViewPager) view.findViewById(R.id.pager);
		
		final CheckBox checkBox = (CheckBox) view.findViewById(R.id.home_screen);
		if(sharedPref.getBoolean("home_screen", true)) {
			checkBox.setChecked(true);
      		sharedPref.edit().putBoolean("home_screen", true).commit();
		} else {
			checkBox.setChecked(false);
		}
		checkBox.setOnClickListener(new View.OnClickListener() {
	          @Override
	          public void onClick(View v) { 
	              if(checkBox.isChecked()) {
	          		sharedPref.edit().putBoolean("home_screen", true).commit();
	              } else {
	          		sharedPref.edit().putBoolean("home_screen", false).commit();
	              }
	          }
	    });
		
		gallery.setAdapter(new ImageGalleryAdapter(getSherlockActivity(), Constants.Thor2_80x80));
		gallery.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				pager.setCurrentItem(position);
			}
		});
		String array[] = { "Thor 2" };
		ArrayAdapter<String> sp_adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_text, array);
		sp_adapter.setDropDownViewResource(R.layout.spinner_item);
		pager.setAdapter(new ImagePagerAdapter(getActivity(), Constants.Thor2));
		pager.setCurrentItem(2);
		gallery.setSelection(2);
		/*m_myDynamicSpinner.setAdapter(sp_adapter);
		m_myDynamicSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	String picture_category = m_myDynamicSpinner.getSelectedItem().toString();
            	if("Thor 2".equals(picture_category)) {	
					pager.setAdapter(new ImagePagerAdapter(getActivity(), Constants.Thor2));
				}
            	pager.setCurrentItem(2);
        		gallery.setSelection(2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) { }
        });		*/
		
		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
		String background = sharedPref.getString("background", null);
		if(background==null) {
			background = Constants.Thor2[2];
			sharedPref.edit().putString("background", background).commit();
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
			getActivity().getWindow().setBackgroundDrawable(drawable);
		}
		
		return view;
	}
	
	
	@Override
	public void onResume() {
		super.onResume();

	    Session session = Session.getActiveSession();
	    if (session != null && (session.isOpened() || session.isClosed()) ) {
	        onSessionStateChange(session, session.getState(), null);
	    }

		uiHelper.onResume();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    uiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    LOG.info("Session state changed");
	    if (state.isOpened()) {
	    	 LOG.info("State is opened");
	    	//TODO
	    	// Added as temp workaround. Move result handling to LoginActivity
	    	try {
	    		if (!hasPublishPermission()) {
	    			session.requestNewPublishPermissions(new Session.NewPermissionsRequest(LoginFragment.this, Arrays.asList("publish_actions")));
	    		}
            	    	
	    		if (!isFBChatRunning()) {
	    			String accessToken = null;
	    			String consumerKey = null;		
	    			if (session != null) {
	    				accessToken = session.getAccessToken();
	    				consumerKey = session.getApplicationId();
	    			}		
	    			
	    			if (accessToken != null && consumerKey != null) {		
	    				LOG.info("Starting service from LoginFragment...");
	    				Intent service = new Intent(getSherlockActivity(), FBChatService.class);
	    				getSherlockActivity().startService(service);
	    			} else {
	    				LOG.info("No active session... Service is not started");
	    			}		
	    		} else {
	    			LOG.info("Service is running");
	    		}
	    		
	    		
	    		
	    		Toast.makeText(getSherlockActivity(), "Logged in...", Toast.LENGTH_SHORT).show();
	    		getSherlockActivity().getSupportActionBar().show();  		
	    		
	    		SherlockFragment currentFragment = new FriendsListFragment();
	    		FragmentManager fm = getSherlockActivity().getSupportFragmentManager();
	    		fm.beginTransaction().replace(android.R.id.content, currentFragment).commit();
	    		fm.executePendingTransactions();
	    	} catch (Exception e) { }
	    	
	    } else if (state.isClosed()) {
	    	try {
	    		getSherlockActivity().getSupportActionBar().hide();
	    		Toast.makeText(getSherlockActivity(), "Logged out...", Toast.LENGTH_SHORT).show();
	    	} catch(Exception e) { }
	    }
	}
	
	private boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null && session.getPermissions().contains("publish_actions");
	}
	
	private boolean isFBChatRunning() {
	    ActivityManager manager = (ActivityManager) getSherlockActivity().getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (FBChatService.class.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
	}
}
