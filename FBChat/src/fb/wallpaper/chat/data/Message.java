package fb.wallpaper.chat.data;

import java.io.Serializable;


public class Message extends BaseEntity implements Serializable {
	private static final long serialVersionUID = 3211669485592900082L;
	
	public static final int MESSAGE_IN = 0;
	public static final int MESSAGE_OUT = 1;

	private long createdTime;
	private String text;
	private FBUser userWith;
	private int type;
	private String uid;
	
	public Message() {
	}
	
	public Message(long createdTime, String text, FBUser userWith, int type, String uid) {
		this.createdTime = createdTime;
		this.text = text;
		this.userWith = userWith;
		this.type = type;
		this.uid = uid;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public FBUser getUserWith() {
		return userWith;
	}

	public void setUserWith(FBUser userWith) {
		this.userWith = userWith;
	}
}
