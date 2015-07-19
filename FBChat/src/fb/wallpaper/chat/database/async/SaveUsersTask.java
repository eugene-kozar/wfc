package fb.wallpaper.chat.database.async;

import java.util.List;

import org.apache.log4j.Logger;

import android.content.Context;
import android.os.AsyncTask;
import fb.wallpaper.chat.data.FBUser;
import fb.wallpaper.chat.database.UserDAO;
import fb.wallpaper.chat.utils.Utils;

public class SaveUsersTask extends AsyncTask<List<FBUser>, Integer, Integer> {

	private static final Logger LOG = Logger.getLogger(SaveUsersTask.class);
	private Context context;

	public SaveUsersTask(Context context) {
		super();
		this.context = context;
	}

	@Override
	protected Integer doInBackground(List<FBUser>... params) {
		try {
			if (!params[0].isEmpty()) {
				UserDAO userDAO = new UserDAO(context);
				userDAO.deleteAllUsers();
				userDAO.saveUsers(params[0]);
				userDAO.close();
				LOG.info("Users were succesfully saved to the database");
			}
		} catch (Exception e) {
			LOG.info("Eror was occured while saving users to the database");
			LOG.error(e);
			return 0;
		}
		return 1;
	}
}
