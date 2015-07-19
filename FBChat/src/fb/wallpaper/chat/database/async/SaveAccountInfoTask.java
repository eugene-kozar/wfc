package fb.wallpaper.chat.database.async;

import org.apache.log4j.Logger;

import android.content.Context;
import android.os.AsyncTask;
import fb.wallpaper.chat.data.FBUser;
import fb.wallpaper.chat.database.AccountDAO;

public class SaveAccountInfoTask extends AsyncTask<FBUser, Integer, Integer> {

	private static final Logger LOG = Logger.getLogger(SaveAccountInfoTask.class);
	
	private Context context;

	public SaveAccountInfoTask(Context context) {
		super();
		this.context = context;
	}

	@Override
	protected Integer doInBackground(FBUser... params) {
		try {
			AccountDAO accountDAO = new AccountDAO(context);
			accountDAO.deleteAccountInfo();
			accountDAO.saveAccountInfo(params[0]);
			accountDAO.close();
			LOG.info("Account info was succesfully saved to the database");
		} catch (Exception e) {
			LOG.info("Eror was occured while saving account info to the database");
			LOG.error(e.getMessage(), e);
			return 0;
		}
		return 1;
	}
}
