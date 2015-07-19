package fb.wallpaper.chat.data;

import java.util.List;

public class Chat extends BaseEntity{
	
	private List<FBUser> users;

	public List<FBUser> getUsers() {
		return users;
	}

	public void setUsers(List<FBUser> users) {
		this.users = users;
	}

}
