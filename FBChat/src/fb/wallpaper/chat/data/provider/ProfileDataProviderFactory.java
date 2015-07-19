package fb.wallpaper.chat.data.provider;

public class ProfileDataProviderFactory {
	public static ProfileInfoProvider getProfileProvider() {
		return new FBProfileInfoProvider();
		//return new DummyProfileInfoProvider();
	}
}
