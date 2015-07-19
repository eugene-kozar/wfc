package fb.wallpaper.chat.data.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphObject;

import fb.wallpaper.chat.data.FBUser;
import fb.wallpaper.chat.data.Message;
import fb.wallpaper.chat.data.MessageThread;
import fb.wallpaper.chat.data.provider.listener.FriendListFetchListener;
import fb.wallpaper.chat.data.provider.listener.MessageHistoryFetchListener;
import fb.wallpaper.chat.data.provider.listener.PersonalInfoFetchListener;
import fb.wallpaper.chat.data.provider.listener.RecentThreadsFetchListener;
import fb.wallpaper.chat.data.provider.listener.UpdateStatusListener;
import fb.wallpaper.chat.data.provider.listener.UserInfoFetchListener;

public class FBProfileInfoProvider implements ProfileInfoProvider {
	private static final Logger LOG = Logger.getLogger(FBProfileInfoProvider.class);
	
	@Override
	public void fetchFriendList(final FriendListFetchListener listener) {
		LOG.info("Fetching friends list");
		String fqlQuery = "SELECT uid, name, pic_square, online_presence FROM user WHERE uid IN (SELECT uid2 FROM friend WHERE uid1 = me()) ORDER BY name";
		Bundle params = new Bundle();
		params.putString("q", fqlQuery);

		Request request = new Request(Session.getActiveSession(), "/fql",
				params, HttpMethod.GET, new Request.Callback() {
					public void onCompleted(Response response) {
						List<FBUser> users = new ArrayList<FBUser>();
						Exception error = null;
						try {
							GraphObject graphObject = response.getGraphObject();
							JSONObject jsonObject = graphObject.getInnerJSONObject();
							
							//Log.d("data", jsonObject.toString(0));

							JSONArray array = jsonObject.getJSONArray("data");
							
							
							for (int i = 0; i < array.length(); i++) {
								JSONObject friend = array.getJSONObject(i);

								FBUser currentUser = new FBUser();
								currentUser.setUid(friend.getString("uid"));
								currentUser.setName(friend.getString("name"));
								currentUser.setProfilePictureSquare(friend.getString("pic_square"));
								currentUser.setOnlinePresence(friend.getString("online_presence"));
								
								users.add(currentUser);
							}
						} catch (Exception e) {
							error = e;
						}
						
						listener.onFriendListFetched(users, error);
					}
				});
		Request.executeBatchAsync(request);
	}

	@Override
	public void fetchPersonalInfo(final PersonalInfoFetchListener listener) {
		LOG.info("Fetching personal info");
		String fqlQuery = "SELECT uid, name, pic_square, status.message FROM user WHERE uid = me()";
		Bundle params = new Bundle();
		params.putString("q", fqlQuery);

		Request request = new Request(Session.getActiveSession(), "/fql",
				params, HttpMethod.GET, new Request.Callback() {
					public void onCompleted(Response response) {
						Exception error = null;
						FBUser currentUser = null;
						
						try {
							GraphObject graphObject = response.getGraphObject();
							JSONObject jsonObject = graphObject.getInnerJSONObject();
							
							//Utils.log("data " + jsonObject.toString(0));

							JSONArray array = jsonObject.getJSONArray("data");
							JSONObject userInfo = array.getJSONObject(0);

							currentUser = new FBUser();
							currentUser.setUid(userInfo.getString("uid"));
							currentUser.setName(userInfo.getString("name"));
							currentUser.setProfilePictureSquare(userInfo.getString("pic_square"));
							Object statusObj = userInfo.get("status");
							if (statusObj != null && statusObj instanceof JSONObject) {
								JSONObject statusJson = (JSONObject) statusObj;
								currentUser.setStatus(statusJson.getString("message"));
							}
						} catch (Exception e) {
							error = e;
						}
						
						listener.onPersonalInfoFetched(currentUser, error);
					}
				});
		Request.executeBatchAsync(request);
	}

	@Override
	public void updateStatus(final String status, final UpdateStatusListener listener) {
		LOG.info("Updating status");
		Request request = Request.newStatusUpdateRequest(Session.getActiveSession(), status, new Request.Callback() {
                     @Override
                     public void onCompleted(Response response) {
                    	 Exception e = null;
                    	 if (response != null && response.getError() != null) {
                    		 e = response.getError().getException();
                    	 }
                    	 listener.onStatusUpdated(status, e);
                     }
                 });
         request.executeAsync();
	}
	
	@Override 
	public void fetchRecentThreads(final RecentThreadsFetchListener listener) {
		LOG.info("Fetching recent threads");
		String fqlQuery = "{\"threads\":\"SELECT snippet, thread_id, updated_time, unread, recipients, message_count FROM thread WHERE viewer_id = me() AND folder_id = 0 ORDER BY updated_time DESC LIMIT 30\",\"user_info\":\"SELECT uid, name FROM user WHERE uid IN (SELECT recipients FROM #threads) AND uid <> me()\"}";
		Bundle params = new Bundle();
		params.putString("q", fqlQuery);

		Request request = new Request(Session.getActiveSession(), "/fql", params, HttpMethod.GET, new Request.Callback() {
					public void onCompleted(Response response) {
						try {
							GraphObject graphObject = response.getGraphObject();
							JSONObject jsonObject = graphObject.getInnerJSONObject();

							JSONArray array = jsonObject.getJSONArray("data");
							if (array.length() != 2) {
								throw new Exception("Threads may be corrupted");
							}
							JSONObject threadsJSON = array.getJSONObject(0);
							JSONObject usersInfo = array.getJSONObject(1);

							JSONArray threadsJSONArray = threadsJSON.getJSONArray("fql_result_set");
							JSONArray usersJSONArray = usersInfo.getJSONArray("fql_result_set");
							
							Map<String, String> usersNames = new HashMap<String, String>();
							
							for(int i = 0; i < usersJSONArray.length(); i++) {
								JSONObject userJsonObj = usersJSONArray.getJSONObject(i);
								usersNames.put(userJsonObj.getString("uid"), userJsonObj.getString("name"));
							}
													
							List<MessageThread> messageThread = new ArrayList<MessageThread>();
							
							for (int i = 0; i < threadsJSONArray.length(); i++) {
								JSONObject threadJsonObj = threadsJSONArray.getJSONObject(i);
								JSONArray recipients = threadJsonObj.getJSONArray("recipients");
								if (recipients.length() > 2) {
									continue;
								}
								
								String uid = null;
								String name = null;
								for (int j = 0; j < recipients.length(); j++) {
									uid = recipients.getString(j);
									name = usersNames.get(uid);
									if (name != null) break;
								}
									
								if (uid == null || name == null) {
									continue;
									//throw new Exception("Unexpected error. User info is not valid");
								}
								
								FBUser userWith = new FBUser();
								userWith.setUid(uid);
								userWith.setName(name);
								
								MessageThread thread = new MessageThread();
								thread.setUserWith(userWith);
								thread.setSnippet(threadJsonObj.getString("snippet"));
								thread.setTime((long)Long.parseLong(threadJsonObj.getString("updated_time"))*1000);
								thread.setUnreadCount(threadJsonObj.getInt("unread"));
								thread.setMessageCount(threadJsonObj.getInt("message_count"));							
								
								messageThread.add(thread);
							}
							
							listener.onThreadsFetched(messageThread, null);							
						} catch (Exception e) {
							listener.onThreadsFetched(null, e);
						}						
					}
				});
		Request.executeBatchAsync(request);
	}

	@Override
	public void fetchMessageHistory(final FBUser withUser, final MessageHistoryFetchListener listener) {
		LOG.info("Fetching message history");
		// TODO Optimize this. Check if one query is possible
		//String fqlQuery = " {'msg_threads':'SELECT thread_id, recipients FROM thread WHERE viewer_id = me() AND folder_id = 0','msg_messages':'SELECT body,viewer_id, thread_id, author_id FROM message WHERE thread_id IN (SELECT thread_id FROM #msg_threads) AND viewer_id = me() LIMIT 200'}";
		//String fqlQuery = " {'msg_threads':'SELECT thread_id, author_id, created_time FROM message WHERE thread_id IN (SELECT thread_id FROM thread WHERE folder_id = 0) AND author_id = '100003231787457' ORDER BY created_time ASC LIMIT 1','msg_messages':'SELECT thread_id, body, author_id, created_time FROM message WHERE thread_id IN (SELECT thread_id FROM #msg_threads) ORDER BY created_time ASC'}";
		//String fqlQuery = "SELECT message_count,originator FROM thread WHERE (viewer_id = me() OR originator = me())AND folder_id = 0";
		String fqlQuery = "SELECT thread_id, recipients, message_count FROM thread WHERE viewer_id = me() AND folder_id = 0";
		Bundle params = new Bundle();
		params.putString("q", fqlQuery);

		Request request = new Request(Session.getActiveSession(), "/fql",
				params, HttpMethod.GET, new Request.Callback() {
					public void onCompleted(Response response) {
						try {
							LOG.info(response.toString());
							String threadId = null;
							GraphObject graphObject = response.getGraphObject();
							JSONObject jsonObject = graphObject.getInnerJSONObject();
							JSONArray array = jsonObject.getJSONArray("data");

							boolean threadExist = false;
							
							for (int i = 0; i < array.length(); i++) {
								JSONObject o = array.getJSONObject(i);
								JSONArray recipients = o.getJSONArray("recipients");				
								if (arrayContains(recipients, withUser.getUid())) {
									threadExist = true;
									threadId = o.getString("thread_id");
									String fqlQuery = "SELECT message_id, body, viewer_id, author_id, thread_id, created_time FROM message WHERE thread_id = " + threadId + " ORDER BY created_time DESC LIMIT 40";
									Bundle params = new Bundle();
									params.putString("q", fqlQuery);
									
									Request request = new Request(Session.getActiveSession(), "/fql", params, HttpMethod.GET, new Request.Callback() {
												public void onCompleted(Response response) {
													try {
														LOG.info(response.toString());
														List<Message> messages = new ArrayList<Message>();
														GraphObject graphObject = response.getGraphObject();
														JSONObject jsonObject = graphObject.getInnerJSONObject();
														JSONArray array = jsonObject.getJSONArray("data");
														for (int i = 0; i < array.length(); i++) {
															Message m = new Message();
															JSONObject messageJson = array.getJSONObject(i);
															m.setUid(withUser.getUid());
															m.setText(messageJson.getString("body"));
															m.setCreatedTime((long)messageJson.getLong("created_time")*1000);
															m.setUserWith(withUser);
															if (messageJson.getString("author_id").equals(withUser.getUid())) {
																m.setType(Message.MESSAGE_IN);
															} else {
																m.setType(Message.MESSAGE_OUT);
															}
															messages.add(m);
														}
														listener.onMessageHistoryFetched(messages, null);
													} catch(Exception e) {
														listener.onMessageHistoryFetched(null, e);
													}
												}
											});
									Request.executeBatchAsync(request);									
									break;
								}								
							}
							if (!threadExist) {
								listener.onMessageHistoryFetched(new ArrayList<Message>(), null);
							}
						} catch (Exception e) {
							listener.onMessageHistoryFetched(null, e);
						}
					}
				});
		Request.executeBatchAsync(request);
	}	
	
	@Override
	public void fetchUserInfo(String uid, final UserInfoFetchListener listener) {
		LOG.info("Fetching user info");
		String fqlQuery = "SELECT uid, name, pic_square, online_presence FROM user WHERE uid = " + uid;
		Bundle params = new Bundle();
		params.putString("q", fqlQuery);

		Request request = new Request(Session.getActiveSession(), "/fql",
				params, HttpMethod.GET, new Request.Callback() {
					public void onCompleted(Response response) {
						Exception error = null;
						FBUser currentUser = null;
						
						try {
							GraphObject graphObject = response.getGraphObject();
							JSONObject jsonObject = graphObject.getInnerJSONObject();
							
							//Utils.log("data " + jsonObject.toString(0));

							JSONArray array = jsonObject.getJSONArray("data");
							JSONObject JSONUser = array.getJSONObject(0);

							currentUser = new FBUser();
							currentUser.setUid(JSONUser.getString("uid"));
							currentUser.setName(JSONUser.getString("name"));
							currentUser.setProfilePictureSquare(JSONUser.getString("pic_square"));
							currentUser.setOnlinePresence(JSONUser.getString("online_presence"));
							Object statusObj = JSONUser.get("status");
							if (statusObj != null && statusObj instanceof JSONObject) {
								JSONObject statusJson = (JSONObject) statusObj;
								currentUser.setStatus(statusJson.getString("message"));
							}
						} catch (Exception e) {
							error = e;
						}
						
						listener.onUserInfoFetched(currentUser, error);
					}
				});
		Request.executeBatchAsync(request);
	}
	
	private boolean arrayContains(JSONArray array, String value) {
		boolean result = false;
		if (array.length() == 2) {
			for (int i = 0; i < array.length(); i++) {
				try {
					if (value.equals(array.getString(i))) {
						result = true;
						break;
					}
				} catch (JSONException e) { }
			}
		}
		return result;
	}
}
