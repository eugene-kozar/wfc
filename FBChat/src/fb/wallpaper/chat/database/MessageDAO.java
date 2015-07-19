package fb.wallpaper.chat.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import fb.wallpaper.chat.data.Message;

public class MessageDAO extends AbstractDAO {

	public MessageDAO(Context context) {
		super(context);
	}

	public long saveMessage(Message message) {
		ContentValues values = new ContentValues();
		values.put(DBHelper.COLUMN_USER_ID, message.getUid());
		values.put(DBHelper.COLUMN_TEXT, message.getText());
		values.put(DBHelper.COLUMN_DATE, message.getCreatedTime());
		values.put(DBHelper.COLUMN_IN_OUT, message.getType());
		return database.insert(DBHelper.TABLE_MESSAGE, null, values);
	}

	public void saveMessages(List<Message> messageList) {
		for (Message message : messageList) {
			saveMessage(message);
		}
	}

	public Message getMessage(int messageId) {
		Cursor cursor = database.rawQuery("select * from "
				+ DBHelper.TABLE_MESSAGE + " where " + DBHelper.COLUMN_ID
				+ " = '" + messageId + "'", null);
		cursor.moveToFirst();
		Message message = cursorToMessage(cursor);
		cursor.close();
		return message;
	}

	public List<Message> getMessagesForUser(String uid) {
		List<Message> messageList = new ArrayList<Message>();
		Cursor cursor = database.rawQuery("select * from "
				+ DBHelper.TABLE_MESSAGE + " where " + DBHelper.COLUMN_USER_ID
				+ " = '" + uid + "' order by date ASC", null);
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			messageList.add(cursorToMessage(cursor));
			cursor.moveToNext();
		}
		cursor.close();
		return messageList;
	}

	public int deleteMessagesForUser(String uid) {
		return database.delete(DBHelper.TABLE_MESSAGE, DBHelper.COLUMN_USER_ID
				+ "=" + uid, null);
	}

	public int deleteAllMessages() {
		return database.delete(DBHelper.TABLE_MESSAGE, null, null);
	}

	public List<Message> getRecentMessages() {
		List<Message> messageList = new ArrayList<Message>();
		Cursor cursor = database.rawQuery("select id,user_id, text, max("
				+ DBHelper.COLUMN_DATE + "),in_out from message group by "
				+ DBHelper.COLUMN_USER_ID + " order by date DESC", null);
		cursor.moveToFirst();
		int count = 0;
		while (!cursor.isAfterLast() && count < 30) {
			messageList.add(cursorToMessage(cursor));
			cursor.moveToNext();
			count++;
		}
		cursor.close();
		return messageList;
	}

	private Message cursorToMessage(Cursor cursor) {
		if (cursor.getCount() != 0) {
			Message message = new Message();
			message.setId(cursor.getInt(0));
			message.setUid(cursor.getString(1));
			message.setText(cursor.getString(2));
			message.setCreatedTime(cursor.getLong(3));
			message.setType(cursor.getInt(4));
			message.setUserWith(getUser(cursor.getString(1)));
			return message;
		}
		return null;
	}

}
