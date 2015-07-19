package fb.wallpaper.chat.view.list.holder;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import fb.wallpaper.chat.view.list.adapter.AbstractListAdapter.ViewHolder;

public class RecentMessageThreadHolder extends ViewHolder {
	private TextView textView;
	private ImageView imageView;
	private ProgressBar progressBar;
	private TextView snippet;
	private TextView lastTime;
	private ImageView userPresence;

	public RecentMessageThreadHolder(TextView textView, ImageView imageView,
			ProgressBar progressBar, TextView snippet, ImageView userPresence, TextView lastTime) {
		super();
		this.snippet = snippet;
		this.lastTime = lastTime;
		this.textView = textView;
		this.imageView = imageView;
		this.progressBar = progressBar;
		this.userPresence = userPresence;
	}

	public TextView getTextView() {
		return textView;
	}

	public void setTextView(TextView textView) {
		this.textView = textView;
	}
	
	public TextView getLastTime() {
		return lastTime;
	}

	public void setLastTime(TextView lastTime) {
		this.lastTime = lastTime;
	}

	public ImageView getImageView() {
		return imageView;
	}

	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public void setProgressBar(ProgressBar progressBar) {
		this.progressBar = progressBar;
	}

	public TextView getSnippet() {
		return snippet;
	}

	public void setSnippet(TextView snippet) {
		this.snippet = snippet;
	}

	public ImageView getUserPresence() {
		return userPresence;
	}

	public void setUserPresence(ImageView userPresence) {
		this.userPresence = userPresence;
	}

}
