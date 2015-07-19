package fb.wallpaper.chat.data.provider.listener;

import java.util.List;

import fb.wallpaper.chat.data.Message;

public interface MessageHistoryFetchListener {
	void onMessageHistoryFetched(List<Message> messages, Exception e);
}
