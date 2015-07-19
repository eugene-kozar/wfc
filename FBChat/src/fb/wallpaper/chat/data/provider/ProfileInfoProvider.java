package fb.wallpaper.chat.data.provider;

import fb.wallpaper.chat.data.FBUser;
import fb.wallpaper.chat.data.provider.listener.FriendListFetchListener;
import fb.wallpaper.chat.data.provider.listener.MessageHistoryFetchListener;
import fb.wallpaper.chat.data.provider.listener.PersonalInfoFetchListener;
import fb.wallpaper.chat.data.provider.listener.RecentThreadsFetchListener;
import fb.wallpaper.chat.data.provider.listener.UpdateStatusListener;
import fb.wallpaper.chat.data.provider.listener.UserInfoFetchListener;

public interface ProfileInfoProvider {
	void fetchFriendList(FriendListFetchListener listener);

	void fetchPersonalInfo(PersonalInfoFetchListener listener);

	void updateStatus(String status, UpdateStatusListener listener);
	
	void fetchMessageHistory(FBUser withUser, MessageHistoryFetchListener listener);

	void fetchRecentThreads(RecentThreadsFetchListener listener);
	
	void fetchUserInfo(String uid, UserInfoFetchListener listener);
}
