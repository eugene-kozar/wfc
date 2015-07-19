package fb.wallpaper.chat.data.provider.listener;

import java.util.List;

import fb.wallpaper.chat.data.MessageThread;

public interface RecentThreadsFetchListener {
	void onThreadsFetched(List<MessageThread> threads, Exception e); 
}
