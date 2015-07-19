package fb.wallpaper.chat.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import fb.wallpaper.chat.data.FBUser;

public class AbstractDAO {
	protected SQLiteDatabase database;
	protected DBHelper dbHelper;

	public AbstractDAO(Context context) throws SQLException {
		dbHelper = new DBHelper(context);
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public FBUser getUser(String uid) {
		Cursor cursor = database.rawQuery("select * from "
				+ DBHelper.TABLE_USER + " where " + DBHelper.COLUMN_USER_ID
				+ " = '" + uid + "'", null);

		cursor.moveToFirst();
		FBUser user = cursorToUser(cursor);
		cursor.close();
		return user;
	}

	protected FBUser cursorToUser(Cursor cursor) {
		if (cursor.getCount() != 0) {
			FBUser user = new FBUser();
			user.setUid(cursor.getString(0));
			user.setName(cursor.getString(1));
			return user;
		}
		return null;
	}
}
