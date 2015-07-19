package fb.wallpaper.chat.view.fragment;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import fb.wallpaper.chat.R;
import fb.wallpaper.chat.data.FBUser;
import fb.wallpaper.chat.data.Message;
import fb.wallpaper.chat.data.MessageThread;
import fb.wallpaper.chat.data.provider.ProfileDataProviderFactory;
import fb.wallpaper.chat.data.provider.ProfileInfoProvider;
import fb.wallpaper.chat.data.provider.listener.RecentThreadsFetchListener;
import fb.wallpaper.chat.database.MessageDAO;
import fb.wallpaper.chat.service.FBChatThread;
import fb.wallpaper.chat.view.list.adapter.RecentMessageThreadsListAdapter;

public class ChatsFragment extends TrackedFragment{
	
	private static final Logger LOG = Logger.getLogger(ChatsFragment.class);
	
	private ListView threadsList;
	private RecentMessageThreadsListAdapter adapter;
	private ProfileInfoProvider dataProvider;
	private boolean dataFetched = false;
	
	private BroadcastReceiver presenceNotificationreceiver;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LOG.info("Creating chatsfragment");
		View view = inflater.inflate(R.layout.fragment_chats, container, false);		
		
		
		threadsList = (ListView) view.findViewById(R.id.recentThreads);
		adapter = new RecentMessageThreadsListAdapter(getSherlockActivity(), R.layout.list_item_recent_threads, new ArrayList<MessageThread>());
		threadsList.setAdapter(adapter);
		
		final ProgressBar loading = (ProgressBar) view.findViewById(R.id.loading);
		loading.setVisibility(View.VISIBLE);
		
		dataProvider = ProfileDataProviderFactory.getProfileProvider();
		dataProvider.fetchRecentThreads(new RecentThreadsFetchListener() {			
			@Override
			public void onThreadsFetched(List<MessageThread> threads, Exception e) {
				loading.setVisibility(View.GONE);
				if (e == null) {
					dataFetched = true;
					adapter.setData(threads);
					adapter.notifyDataSetChanged();
					updateStatuses();
				} else {
					
					try {
						MessageDAO messageDao = new MessageDAO(getSherlockActivity());
						List<Message> recentMessages = messageDao.getRecentMessages();
						List<MessageThread> threadsDb = new ArrayList<MessageThread>();
						for (Message m : recentMessages) {
							MessageThread t = new MessageThread();
							t.setSnippet(m.getText());
							t.setTime(m.getCreatedTime());
							t.setUserWith(m.getUserWith());
							threadsDb.add(t);
						}
						adapter.setData(threadsDb);
						adapter.notifyDataSetChanged();						
					} catch(Exception ex) {
						//Toast.makeText(getSherlockActivity(), ex.getClass().toString(), Toast.LENGTH_SHORT).show();
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
		
		return view;
	}
	
	private void updateStatuses() {
		if (FBChatThread.usersPresence.size() > 0 && dataFetched) {			
			for (int i = 0; i < adapter.getCount(); i++) {				
				MessageThread messageThread = (MessageThread) adapter.getItem(i);
				FBUser user = messageThread.getUserWith();
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
	}

	
	@Override
	public void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this.getSherlockActivity()).unregisterReceiver(presenceNotificationreceiver);
	}
}
