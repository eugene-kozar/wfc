package fb.wallpaper.chat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "fbchat.db";
	public static final int DATABASE_VERSION = 1;

	public static final String TABLE_USER = "user";
	public static final String TABLE_MESSAGE = "message";
	public static final String TABLE_ACCOUNT = "account";

	public static final String COLUMN_ID = "id";
	public static final String COLUMN_USER_ID = "user_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_STATUS = "status";
	public static final String COLUMN_TEXT = "text";
	public static final String COLUMN_DATE = "date";
	public static final String COLUMN_IN_OUT = "in_out";

	private static final String TABLE_USER_CREATE = "create table "
			+ TABLE_USER + "(" + COLUMN_USER_ID + " text primary key , "
			+ COLUMN_NAME + " text not null);";

	private static final String TABLE_MESSAGE_CREATE = "create table "
			+ TABLE_MESSAGE + "(" + COLUMN_ID
			+ " integer primary key autoincrement, " + COLUMN_USER_ID
			+ " text not null, " + COLUMN_TEXT + " text, " + COLUMN_DATE
			+ " integer, " + COLUMN_IN_OUT + " integer," + "FOREIGN KEY("
			+ COLUMN_USER_ID + ") REFERENCES " + TABLE_USER + "("
			+ COLUMN_USER_ID + ")" + ");";

	private static final String TABLE_ACCOUNT_CREATE = "create table "
			+ TABLE_ACCOUNT + "(" + COLUMN_USER_ID + " text primary key , "
			+ COLUMN_NAME + " text not null, " + COLUMN_STATUS
			+ " text not null);";

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.d("creating tables", "creating tables for the first time");
		database.execSQL(TABLE_USER_CREATE);
		database.execSQL(TABLE_MESSAGE_CREATE);
		database.execSQL(TABLE_ACCOUNT_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {

	}

}
