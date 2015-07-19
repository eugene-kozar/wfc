package fb.wallpaper.chat.service;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.facebook.Session;

import fb.wallpaper.chat.data.FBUser;
import fb.wallpaper.chat.xmpp.FacebookChatSample;

public class FBChatThread extends Thread {

	private static final Logger LOG = Logger.getLogger(FBChatThread.class);
	public static ConcurrentHashMap<String , String> usersPresence = new ConcurrentHashMap<String, String>();;

	private LocalBroadcastManager broadcaster;
	private XMPPConnection connection;
	private Context context;
	private Chat currentChat;
	private volatile boolean isActive = false;
	private SharedPreferences sharedPreferences;

	private BroadcastReceiver fbChatServiceReceiver;

	public FBChatThread(Context context, LocalBroadcastManager broadcaster) {
		super();
		this.context = context;
		this.broadcaster = broadcaster;
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

	}

	@Override
	public void run() {
		isActive = true;
		loginToFbChat();

		fbChatServiceReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Intent intent_result = new Intent("FBChatResult");
				try {
					if(sharedPreferences.getBoolean("connect_work", false) && 
							sharedPreferences.getBoolean("service_work", false)) {
						fb.wallpaper.chat.data.Message s = (fb.wallpaper.chat.data.Message) intent
								.getSerializableExtra("FBChatOut");
						String to = getJabblerIdString(s.getUserWith());
						if (currentChat == null	|| !currentChat.getParticipant().equals(to)) {
							currentChat = connection.getChatManager().createChat(to, null);
						}
						currentChat.sendMessage(s.getText());
						LOG.info("Message " + s.getText() + " sent successfully");	
						intent_result.putExtra("FBChatResult", s);
						broadcaster.sendBroadcast(intent_result);
					} else {
						throw new Exception();
					}
				} catch (Exception e) {
					fb.wallpaper.chat.data.Message s = null;
					LOG.error("Message was not sent successfully", e);					
					intent_result.putExtra("FBChatResult", s);
					broadcaster.sendBroadcast(intent_result);
				}
			}
		};

		LocalBroadcastManager.getInstance(context).registerReceiver(
				(fbChatServiceReceiver), new IntentFilter("FBChatOut"));

		connection.getRoster().addRosterListener(new RosterListener() {
			@Override
			public void presenceChanged(Presence presence) {				
				String jabberId = presence.getFrom();
				String userUid = jabberId.substring(1, jabberId.indexOf('@'));
				LOG.info("Presence changed: " + userUid);
				sendPresenceNotification(userUid, presence.getType().name());
			}

			@Override
			public void entriesUpdated(Collection<String> entries) {
			}

			@Override
			public void entriesDeleted(Collection<String> entries) {
			}

			@Override
			public void entriesAdded(Collection<String> entries) {
			}
		});

		ChatManager chatManager = connection.getChatManager();
		chatManager.addChatListener(new ChatManagerListener() {
			@Override
			public void chatCreated(Chat chat, boolean arg1) {
				LOG.info("New chat created");
				chat.addMessageListener(new MessageListener() {
					@Override
					public void processMessage(Chat chat, Message message) {
						LOG.info("Processing message");
						if (message != null && message.getBody() != null) {
							fb.wallpaper.chat.data.Message receivedMessage = new fb.wallpaper.chat.data.Message();
							receivedMessage.setText(message.getBody());
							receivedMessage
							.setType(fb.wallpaper.chat.data.Message.MESSAGE_IN);
							receivedMessage.setCreatedTime(new Date().getTime());

							String from = message.getFrom();
							FBUser userFrom = new FBUser();
							userFrom.setUid(from.substring(1, from.indexOf('@')));
							receivedMessage.setUserWith(userFrom);

							sendIncomingMessageNotification(receivedMessage);							
						}
					}
				});
			}
		});

		PacketFilter filter = new AndFilter(new PacketTypeFilter(Message.class));
		PacketCollector collector = connection.createPacketCollector(filter);

		while (isActive) {
			collector.nextResult();
			// Packet packet = collector.nextResult();

			// if (packet instanceof Message) {
			// Message msg = (Message) packet;
			// Utils.log("1Got message:" + msg.getBody());
			// }
		}

	}

	public void setActive(boolean active) {
		this.isActive = active;
	}

	public boolean isActive() {
		return this.isActive;
	}

	private void connect() throws Exception {
		LOG.info("Connecting to chat.facebook.com ...");

		String accessToken = Session.getActiveSession().getAccessToken();
		String consumerKey = Session.getActiveSession().getApplicationId();

		connection = FacebookChatSample.createXMPPConnection();
		connection.connect();
		LOG.info("chat.facebook.com: connected");
		connection.login(consumerKey, accessToken);
		LOG.info("Login complete. Connection established");
	}

	private void loginToFbChat() {
		boolean tryReconnect = true;
		while (tryReconnect) {
			try {
				connect();
				sharedPreferences.edit().putBoolean("connect_work", true).commit();
				sharedPreferences.edit().putBoolean("service_work", true).commit();
				tryReconnect = false;
			} catch (Exception e) {
				LOG.error(e);
				sharedPreferences.edit().putBoolean("connect_work", false).commit();
				try { Thread.sleep(5000); } catch (InterruptedException e1) {}
			}
		}
	}

	private void sendIncomingMessageNotification(
			fb.wallpaper.chat.data.Message message) {
		if (message != null) {
			// Toast.makeText(context, "Messge received from " +
			// message.getUserWith().getUid() + ": " + message.getText(),
			// Toast.LENGTH_SHORT).show();
			Intent intent = new Intent("FBChat");
			intent.putExtra("FBChat", message);
			broadcaster.sendBroadcast(intent);
		}

	}

	private void sendPresenceNotification(String userUid, String presence) {
		Intent intent = new Intent("FBChatPresenceUpdate");
		intent.putExtra("userUid", userUid);
		intent.putExtra("presence", presence);
		broadcaster.sendBroadcast(intent);
	}

	private static String getJabblerIdString(FBUser user) {
		return String.format(Locale.US, "-%d@chat.facebook.com",
				Long.valueOf(user.getUid()));
	}
}
