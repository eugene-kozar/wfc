package fb.wallpaper.chat.data;

public class MessageThread extends BaseEntity {
	private String snippet;
	private FBUser userWith;
	private String uid;
	private int messageCount;
	private int unreadCount;
	private long time;
	
	public int getUnreadCount() {
		return unreadCount;
	}

	public void setUnreadCount(int unreadCount) {
		this.unreadCount = unreadCount;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}
	
	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public FBUser getUserWith() {
		return userWith;
	}

	public void setUserWith(FBUser userWith) {
		this.userWith = userWith;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public int getMessageCount() {
		return messageCount;
	}

	public void setMessageCount(int messageCount) {
		this.messageCount = messageCount;
	}

}
