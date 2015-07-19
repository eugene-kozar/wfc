package fb.wallpaper.chat.database;

import fb.wallpaper.chat.data.FBUser;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

public class AccountDAO extends AbstractDAO {

	private String[] allColumnsAccount = { DBHelper.COLUMN_USER_ID,
			DBHelper.COLUMN_NAME, DBHelper.COLUMN_STATUS };

	public AccountDAO(Context context) throws SQLException {
		super(context);
	}

	public long saveAccountInfo(FBUser user) {
		ContentValues values = new ContentValues();
		values.put(DBHelper.COLUMN_USER_ID, user.getUid());
		values.put(DBHelper.COLUMN_NAME, user.getName());
		values.put(DBHelper.COLUMN_STATUS, user.getStatus());
		return database.insert(DBHelper.TABLE_ACCOUNT, null, values);
	}

	public FBUser getAccountInfo() {
		Cursor cursor = database.query(DBHelper.TABLE_ACCOUNT,
				allColumnsAccount, null, null, null, null, null);

		cursor.moveToFirst();
		FBUser user = cursorToUserAccount(cursor);
		cursor.close();
		return user; 
	}

	public int deleteAccountInfo() {
		return database.delete(DBHelper.TABLE_ACCOUNT, null, null);
	}

	private FBUser cursorToUserAccount(Cursor cursor) {
		if (cursor.getCount() != 0) {
			FBUser user = new FBUser();
			user.setUid(cursor.getString(0));
			user.setName(cursor.getString(1));
			user.setStatus(cursor.getString(2));
			return user;
		}
		return null;
	}

}
