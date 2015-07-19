package fb.wallpaper.chat.view.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import fb.wallpaper.chat.view.list.adapter.SuggestionsAdapter;
//import com.actionbarsherlock.widget.SuggestionsAdapter;
import com.actionbarsherlock.widget.SearchView;

import fb.wallpaper.chat.ConversationActivity;
import fb.wallpaper.chat.LoginActivity;
import fb.wallpaper.chat.R;
import fb.wallpaper.chat.data.FBUser;
import fb.wallpaper.chat.data.provider.ProfileDataProviderFactory;
import fb.wallpaper.chat.data.provider.ProfileInfoProvider;
import fb.wallpaper.chat.data.provider.listener.FriendListFetchListener;
import fb.wallpaper.chat.data.provider.listener.PersonalInfoFetchListener;
import fb.wallpaper.chat.database.UserDAO;
import fb.wallpaper.chat.database.async.SaveUsersTask;
import fb.wallpaper.chat.service.FBChatThread;
import fb.wallpaper.chat.view.list.adapter.FBUsersListAdapter;

public class FriendsListFragment extends TrackedFragment implements SearchView.OnQueryTextListener,
SearchView.OnSuggestionListener	{
	private static final Logger LOG = Logger.getLogger(FriendsListFragment.class);

	private ProfileInfoProvider dataProvider;

	private BroadcastReceiver presenceNotificationreceiver;
	private BroadcastReceiver friendsChangedReceiver;

	private FBUsersListAdapter adapter;
	private ListView friendsList;
	private boolean dataFetched = false;

	List<FBUser> friendList = new ArrayList<FBUser>();
	List<FBUser> searchFriendList;
	SearchView searchView;
	LinearLayout searchLayout;

	private SuggestionsAdapter mSuggestionsAdapter;
	private static final String[] COLUMNS = {
		BaseColumns._ID,
		SearchManager.SUGGEST_COLUMN_TEXT_1,
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_friend_list, container, false);		
		/*SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
		if(!sharedPref.getBoolean("allow_landscape", true)) {
			getSherlockActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		} else {
			getSherlockActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		}*/

		friendsList = (ListView) view.findViewById(R.id.friendsList);
		adapter = new FBUsersListAdapter(getSherlockActivity(), R.layout.list_item_friend, new ArrayList<FBUser>());
		friendsList.setAdapter(adapter);
		
		final LinearLayout searchlayout = (LinearLayout) view.findViewById(R.id.searchLayout);
		final ProgressBar loading = (ProgressBar) view.findViewById(R.id.loading);
		loading.setVisibility(View.VISIBLE);

	
		
		
		dataProvider = ProfileDataProviderFactory.getProfileProvider();
		
		if (!LoginActivity.USER_EXIST) {
			dataProvider.fetchPersonalInfo(new PersonalInfoFetchListener() {
				@Override
				public void onPersonalInfoFetched(FBUser personalInfo, Exception e) {
					try {				
					SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getSherlockActivity());
					Editor editor = sharedPref.edit();
					if(personalInfo!=null) {
						if(!"".equals(personalInfo.getName())) {
							editor.putString("USER_NAME", personalInfo.getName());
							editor.commit();
						} 
					}				
					
					LoginActivity loginAct = (LoginActivity) getSherlockActivity();
					loginAct.refreshUserName();
					} catch(Exception ex) {}
				}
			});
		}
		
		dataProvider.fetchFriendList(new FriendListFetchListener() {
			@Override
			public void onFriendListFetched(List<FBUser> userList, Exception e) {
				loading.setVisibility(View.GONE);
				searchlayout.setVisibility(View.VISIBLE);
				if (e == null) {
					try {
						SaveUsersTask cacheUserTask = new SaveUsersTask(getSherlockActivity());
						cacheUserTask.execute(userList);
						adapter.setSourceData(userList);
						adapter.notifyDataSetChanged();
						friendList = userList;
						dataFetched = true;
						updateStatuses();
					} catch(Exception ex) {
						//Toast.makeText(FriendsListFragment.this.getSherlockActivity(), ex.getClass().toString(), Toast.LENGTH_SHORT).show();
					}
				} else {
					try {
						UserDAO userDao = new UserDAO(getSherlockActivity());
						friendList = userDao.getAllUsers();
						adapter.setSourceData(friendList);
						adapter.notifyDataSetChanged();						
					} catch(Exception ex) {
						//Toast.makeText(FriendsListFragment.this.getSherlockActivity(), ex.getClass().toString(), Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

		presenceNotificationreceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {				
				String userUid = intent.getExtras().getString("userUid");
				String presence = intent.getExtras().getString("presence");
				FBChatThread.usersPresence.put(userUid, presence);	
				LOG.info("OnReceive: " + userUid + "|" + presence);
				updateStatuses();
			}	
		};

		friendsChangedReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				LOG.info("OnReceive: friendsChangedReceiver");
				updateStatuses();				
			}
		};

		//Create the search view
		searchView = (SearchView) view.findViewById(R.id.search);
		searchView.setQueryHint("Search for friends...");
		searchView.setOnQueryTextListener(this);
		searchView.setOnSuggestionListener(this);
		
		searchLayout = (LinearLayout) view.findViewById(R.id.searchLayout);
		searchLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				searchView.setIconified(false);
			}
		});		
		
		return view;
	}

	private void updateStatuses() {
		if (FBChatThread.usersPresence.size() > 0 && dataFetched) {			
			List<FBUser> sourceUsers = ((FBUsersListAdapter)friendsList.getAdapter()).getSourceData();
			for (int i = 0; i < sourceUsers.size(); i++) {				
				FBUser user = sourceUsers.get(i);
				if (user != null && FBChatThread.usersPresence.containsKey(user.getUid())) {
					String presence = FBChatThread.usersPresence.get(user.getUid());
					user.setPresence(presence);
				}
			}
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		LocalBroadcastManager.getInstance(this.getSherlockActivity()).registerReceiver((presenceNotificationreceiver), new IntentFilter("FBChatPresenceUpdate"));
		LocalBroadcastManager.getInstance(this.getSherlockActivity()).registerReceiver((friendsChangedReceiver), new IntentFilter("FBChatFriendsChanged"));
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this.getSherlockActivity()).unregisterReceiver(presenceNotificationreceiver);
		LocalBroadcastManager.getInstance(this.getSherlockActivity()).unregisterReceiver(friendsChangedReceiver);
	}

	@Override
	public boolean onSuggestionSelect(int position) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onSuggestionClick(int position) {
		Cursor c = (Cursor) mSuggestionsAdapter.getItem(position);
		String query = c.getString(c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
		FBUser user = new FBUser();
		for (int i = 0; i < friendList.size(); i++) {
			if(friendList.get(i).getName().equals(query)) {
				user = friendList.get(i);
				Intent intent = new Intent();
				intent.putExtra("userWith", user);
				intent.putExtra("fromFriends", true);
				intent.setClass(getSherlockActivity(), ConversationActivity.class);
				getSherlockActivity().startActivity(intent);
			}
		}        	
		return true; 
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		adapter.setSourceData(searchFriendList);
		adapter.notifyDataSetChanged();
		friendsList.setAdapter(adapter);
		friendsList.requestFocus();
		InputMethodManager inputManager = (InputMethodManager)            
				getActivity().getSystemService(Context.INPUT_METHOD_SERVICE); 
		inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),      
				InputMethodManager.HIDE_NOT_ALWAYS);
		return true;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		String userName = "";
		searchFriendList = new ArrayList<FBUser>();
		MatrixCursor cursor = new MatrixCursor(COLUMNS);
		mSuggestionsAdapter = new SuggestionsAdapter(getSherlockActivity(), cursor);
		searchView.setSuggestionsAdapter(mSuggestionsAdapter);
		for (int i = 0; i < friendList.size(); i++) {
			userName = friendList.get(i).getName();
			if(userName.toLowerCase().startsWith(newText.toLowerCase())) {
				searchFriendList.add(friendList.get(i));
				cursor.addRow(new String[]{i+"", userName});
			}
		}
		mSuggestionsAdapter = new SuggestionsAdapter(getSherlockActivity(), cursor);
		searchView.setSuggestionsAdapter(mSuggestionsAdapter);
		return true;
	}
}
