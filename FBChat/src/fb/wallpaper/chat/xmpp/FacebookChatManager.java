package fb.wallpaper.chat.xmpp;

import java.util.Collection;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;

import com.facebook.Session;

import fb.wallpaper.chat.utils.Utils;

public class FacebookChatManager {

	private static FacebookChatManager chatManager;
	private XMPPConnection connection;
	private final String SERVER = "chat.facebook.com";
	private final int PORT = 5222;
	private final String FACEBOOK_MECHANISM = "X-FACEBOOK-PLATFORM";
	private RosterListener rosterListner;

	private FacebookChatManager(RosterListener rosterListner) {
		this.rosterListner = rosterListner;
		ConnectionConfiguration connFig = new ConnectionConfiguration(SERVER,
				PORT);
		connFig.setSASLAuthenticationEnabled(true);
		connection = new XMPPConnection(connFig);
		// setup facebook authentication mechanism
		SASLAuthentication.registerSASLMechanism(FACEBOOK_MECHANISM,
				SASLXFacebookPlatformMechanism.class);
		SASLAuthentication.supportSASLMechanism(FACEBOOK_MECHANISM, 0);
	}

	public static FacebookChatManager getInstance(RosterListener rosterListner) {
		if (chatManager == null) {
			chatManager = new FacebookChatManager(rosterListner);
		}
		return chatManager;
	}

	public boolean connect() {
		try {
			connection.connect();
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
			connection.disconnect();
		}
		return false;
	}

	public void disConnect() {
		connection.disconnect();
	}

	public boolean logIn(String apiKey, String accessToken) {
		try {
			connection.login(apiKey, accessToken);
			setPresenceState(Presence.Type.available, "");
			connection.getRoster().addRosterListener(rosterListner);
			return true;
		} catch (XMPPException e) {
			connection.disconnect();
			e.printStackTrace();
		}
		return false;
	}

	public Roster getRoster() {
		return connection.getRoster();
	}

	public Chat createNewChat(String user, MessageListener messageListner) {
		return connection.getChatManager().createChat(user, messageListner);
	}

	public void registerNewIncomingChatListner(
			ChatManagerListener chatManagerListner) {
		connection.getChatManager().addChatListener(chatManagerListner);
	}

	public void setPresenceState(Type precenseType, String status) {
		Presence presence = new Presence(precenseType);
		presence.setStatus(status);
		connection.sendPacket(presence);
	}

	public Presence getUserPresence(String userId) {
		return connection.getRoster().getPresence(userId);
	}
	
//	public void test() {
//
//		String appId = "480244048734929";
//
//		FacebookChatManager facebookChatManager = FacebookChatManager.getInstance(new RosterListener() {
//			
//			@Override
//			public void presenceChanged(Presence arg0) {
//				Utils.log("presence changed");				
//			}
//			
//			@Override
//			public void entriesUpdated(Collection<String> arg0) {
//				Utils.log("entries changed");				
//			}
//			
//			@Override
//			public void entriesDeleted(Collection<String> arg0) {
//				Utils.log("entries deselected");				
//			}
//			
//			@Override
//			public void entriesAdded(Collection<String> arg0) {
//				Utils.log("entries added");				
//			}
//		});
//
//		if (facebookChatManager.connect()) {
//			Utils.log("Connected");
//			if (facebookChatManager.logIn(appId, Session.getActiveSession().getAccessToken())) {
//				//Roster rooster =  facebookChatManager.getRoster();				
//				Utils.log("complete");
//				
//				Utils.log(facebookChatManager.getRoster().getGroupCount() + "");
//		    } else {
//		    	Utils.log("Not authenticated");
//		    }
//		} else {
//			Utils.log("Not connected");
//		}
//	}
}
