package fb.wallpaper.chat.data;

import java.io.Serializable;
import java.util.Locale;

public class FBUser extends BaseEntity implements Serializable {
	private static final long serialVersionUID = -9070939492041781364L;

	private String name;
	private String uid;
	private String profilePictureSquare;
	private String onlinePresence;
	private String status;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOnlinePresence() {
		return onlinePresence;
	}

	public void setOnlinePresence(String onlinePresence) {
		this.onlinePresence = onlinePresence;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getProfilePictureSquare() {
		return profilePictureSquare;
	}

	public void setProfilePictureSquare(String profilePicture) {
		this.profilePictureSquare = profilePicture;
	}

	public String getProfilePictureSquare(int size) {
		return String.format(Locale.US,
				"https://graph.facebook.com/%s/picture?width=%d&height=%d",
				uid, size, size);
	}

	public String getPresence() {
		return onlinePresence;
	}

	public void setPresence(String status) {
		this.onlinePresence = status;
	}

	public String getStatus() {
		if (status == null) {
			return "";
		}
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof FBUser)) {
			return false;
		}

		FBUser oUser = (FBUser) o;
		return oUser.getUid().equals(this.uid);
	}

}
