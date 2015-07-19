package fb.wallpaper.chat.data.provider;

import java.util.ArrayList;
import java.util.List;

import fb.wallpaper.chat.data.FBUser;
import fb.wallpaper.chat.data.provider.listener.FriendListFetchListener;
import fb.wallpaper.chat.data.provider.listener.MessageHistoryFetchListener;
import fb.wallpaper.chat.data.provider.listener.PersonalInfoFetchListener;
import fb.wallpaper.chat.data.provider.listener.RecentThreadsFetchListener;
import fb.wallpaper.chat.data.provider.listener.UpdateStatusListener;
import fb.wallpaper.chat.data.provider.listener.UserInfoFetchListener;

public class DummyProfileInfoProvider implements ProfileInfoProvider {

	@Override
	public void fetchFriendList(FriendListFetchListener listener) {
		List<FBUser> users  = new ArrayList<FBUser>();
		for (int i = 0; i < 25; i++) {
			FBUser user = new FBUser();
			user.setId(i);
			user.setName("TestUser" + i);
			users.add(user);
		}
		listener.onFriendListFetched(users, null);
		
	}

	@Override
	public void fetchPersonalInfo(PersonalInfoFetchListener listener) {
		FBUser myInfo = new FBUser();
		myInfo.setId(-1);
		myInfo.setName("MyTestName");
		listener.onPersonalInfoFetched(myInfo, null);
		
	}

	@Override
	public void updateStatus(String status, UpdateStatusListener listener) {
		listener.onStatusUpdated(status, null);		
	}

	@Override
	public void fetchMessageHistory(FBUser withUser,
			MessageHistoryFetchListener listener) {
		
	}

	@Override
	public void fetchRecentThreads(RecentThreadsFetchListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fetchUserInfo(String uid, UserInfoFetchListener listener) {
		// TODO Auto-generated method stub
		
	}

}
