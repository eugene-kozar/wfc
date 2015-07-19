package fb.wallpaper.chat.data.provider.listener;

import fb.wallpaper.chat.data.FBUser;

public interface PersonalInfoFetchListener {
	void onPersonalInfoFetched(FBUser personalInfo, Exception e);
}
