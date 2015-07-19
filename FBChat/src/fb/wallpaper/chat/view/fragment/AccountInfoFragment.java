package fb.wallpaper.chat.view.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import fb.wallpaper.chat.LoginActivity;
import fb.wallpaper.chat.R;
import fb.wallpaper.chat.data.FBUser;
import fb.wallpaper.chat.data.provider.ProfileDataProviderFactory;
import fb.wallpaper.chat.data.provider.ProfileInfoProvider;
import fb.wallpaper.chat.data.provider.listener.PersonalInfoFetchListener;
import fb.wallpaper.chat.data.provider.listener.UpdateStatusListener;
import fb.wallpaper.chat.database.AccountDAO;
import fb.wallpaper.chat.database.async.SaveAccountInfoTask;
import fb.wallpaper.chat.utils.Utils;

public class AccountInfoFragment extends TrackedFragment {

	private ProfileInfoProvider dataProvider;
	private LocalBroadcastManager broadcaster;
	private ImageLoader imageLoader = ImageLoader.getInstance();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		final View view = inflater.inflate(R.layout.fragment_account, container, false);
		
		dataProvider = ProfileDataProviderFactory.getProfileProvider();
		dataProvider.fetchPersonalInfo(new PersonalInfoFetchListener() {
			@Override
			public void onPersonalInfoFetched(FBUser personalInfo, Exception e) {
				FBUser me = new FBUser();
				Exception error = null;
				if (e == null) {
					try {
						SaveAccountInfoTask saveAccount = new SaveAccountInfoTask(getSherlockActivity());
						saveAccount.execute(personalInfo);					
						me = personalInfo;
					} catch(Exception ex) {
						error = ex;
					}	
										
				} else {					
					AccountDAO accountDao = new AccountDAO(getSherlockActivity());
					try {
						me = accountDao.getAccountInfo();
						accountDao.close();
					} catch(Exception daoError) {
						error = daoError;
					}					
					
				}
				
				if (error == null) {
					try {
						ImageView profilePict = (ImageView) view.findViewById(R.id.avatar);					
						final ProgressBar spinner = (ProgressBar) view.findViewById(R.id.loading);
												
						TextView username = (TextView)view.findViewById(R.id.username);
						username.setText(me.getName());
						
						TextView status = (TextView)view.findViewById(R.id.status);
						status.setText(me.getStatus());
						
						imageLoader.displayImage(me.getProfilePictureSquare(100), profilePict, Utils.DEFAULT_IMAGE_OPTIONS,
								new SimpleImageLoadingListener() {
									@Override
									public void onLoadingStarted(String imageUri, View view) {
										spinner.setVisibility(View.VISIBLE);
									}

									@Override
									public void onLoadingFailed(String imageUri, View view, FailReason failReason) {									
										spinner.setVisibility(View.GONE);
									}

									@Override
									public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
										spinner.setVisibility(View.GONE);
									}
								});
					} catch (Exception e2) {
						Toast.makeText(AccountInfoFragment.this.getSherlockActivity(), "No Internet connection", Toast.LENGTH_SHORT).show();
					}
					
				} else {
					Toast.makeText(AccountInfoFragment.this.getSherlockActivity(), error.getClass().toString(), Toast.LENGTH_SHORT).show();
				}				
			}
		});
		
		
		final EditText editStatus = (EditText) view.findViewById(R.id.edit_status);
		
		
		Button btnUpdateStatus = (Button) view.findViewById(R.id.update);
		ConnectivityManager conMgr = (ConnectivityManager) getActivity().getSystemService (Context.CONNECTIVITY_SERVICE);
		if (!(conMgr.getActiveNetworkInfo() != null
				&& conMgr.getActiveNetworkInfo().isAvailable()
				&& conMgr.getActiveNetworkInfo().isConnected())) {
				btnUpdateStatus.setEnabled(false);
		}
		btnUpdateStatus.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				dataProvider.updateStatus(editStatus.getText().toString(),
						new UpdateStatusListener() {
							@Override
							public void onStatusUpdated(String response,
									Exception e) {
								if (e == null) {
									TextView status = (TextView)view.findViewById(R.id.status);
									status.setText(response);
								}
							}
						});

			}
		});
		
		Button btnLogout = (Button) view.findViewById(R.id.logout);
		btnLogout.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
		        ((LoginActivity)getActivity()).logOut();
			}
		});

		return view;
	}


}
