package fb.wallpaper.chat.database.async;

import java.util.List;

import org.apache.log4j.Logger;

import android.content.Context;
import android.os.AsyncTask;
import fb.wallpaper.chat.data.Message;
import fb.wallpaper.chat.database.MessageDAO;

public class SaveMessagesTask extends AsyncTask<List<Message>, Integer, Integer> {

	private static final Logger LOG = Logger.getLogger(SaveMessagesTask.class);
	
	private Context context;

	public SaveMessagesTask(Context context) {
		super();
		this.context = context;
	}

	@Override
	protected Integer doInBackground(List<Message>... params) {
		try {
			if (!params[0].isEmpty()) {
				MessageDAO messageDAO = new MessageDAO(context);
				messageDAO.deleteMessagesForUser(params[0].get(0).getUserWith()
						.getUid());
				messageDAO.saveMessages(params[0]);
				messageDAO.close();
				LOG.info("Messages were succesfully saved to the database");
			}
		} catch (Exception e) {
			LOG.info("Eror was occured while saving messages to the database");
			LOG.error(e.getMessage(), e);
			return 0;
		}
		return 1;
	}

}
