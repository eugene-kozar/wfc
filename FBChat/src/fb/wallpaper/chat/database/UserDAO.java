package fb.wallpaper.chat.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import fb.wallpaper.chat.data.FBUser;

public class UserDAO extends AbstractDAO {

	public UserDAO(Context context) {
		super(context);
	}

	private String[] allColumnsUser = { DBHelper.COLUMN_USER_ID,
			DBHelper.COLUMN_NAME };

	public long saveUser(FBUser user) {
		ContentValues values = new ContentValues();
		values.put(DBHelper.COLUMN_USER_ID, user.getUid());
		values.put(DBHelper.COLUMN_NAME, user.getName());
		return database.insert(DBHelper.TABLE_USER, null, values);
	}

	public void saveUsers(List<FBUser> users) {
		for (FBUser user : users) {
			saveUser(user);
		}
	}

	
	public List<FBUser> getAllUsers() {
		List<FBUser> userList = new ArrayList<FBUser>();
		Cursor cursor = database.query(DBHelper.TABLE_USER, allColumnsUser,
				null, null, null, null, null);

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			userList.add(cursorToUser(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		return userList;
	}

	public int deleteUser(String uid) {
		return database.delete(DBHelper.TABLE_USER, DBHelper.COLUMN_USER_ID
				+ "=" + uid, null);
	}

	public int deleteAllUsers() {
		return database.delete(DBHelper.TABLE_USER, null, null);
	}

}
