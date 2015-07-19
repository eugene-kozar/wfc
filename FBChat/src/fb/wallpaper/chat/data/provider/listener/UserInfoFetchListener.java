package fb.wallpaper.chat.data.provider.listener;

import fb.wallpaper.chat.data.FBUser;

public interface UserInfoFetchListener {
	void onUserInfoFetched(FBUser user, Exception e);
}
