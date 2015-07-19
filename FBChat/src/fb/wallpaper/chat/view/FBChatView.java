package fb.wallpaper.chat.view;

import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;
import fb.wallpaper.chat.R;
import fb.wallpaper.chat.data.Message;
import fb.wallpaper.chat.view.adapters.DiscussArrayAdapter;

public class FBChatView extends ListView {
	private DiscussArrayAdapter adapter;
	
	public FBChatView(Context context, AttributeSet attrs) {
		super(context, attrs);
		adapter = new DiscussArrayAdapter(getContext(), R.layout.one_chat_message);
		this.setAdapter(adapter);
	}
	
	public void addMessage(Message m) {		
		adapter.add(m);
		this.setSelection(this.getCount());
	}

	public void addMessages(List<Message> mList) {
		for (Iterator<Message> iterator = mList.iterator(); iterator.hasNext();) {
			adapter.add(iterator.next());
		}
		this.setSelection(this.getCount());
	}	
}
