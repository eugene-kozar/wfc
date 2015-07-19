package fb.wallpaper.chat.data.provider.listener;

import java.util.List;

import fb.wallpaper.chat.data.FBUser;

public interface FriendListFetchListener {
	void onFriendListFetched(List<FBUser> userList, Exception e);
}
