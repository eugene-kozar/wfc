package fb.wallpaper.chat.view.list.holder;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import fb.wallpaper.chat.view.list.adapter.AbstractListAdapter.ViewHolder;

public class SimpleListItemHolder extends ViewHolder {
	private TextView textView;
	private TextView status;
	private ImageView imageView;
	private ProgressBar progressBar;
	private ImageView userPresence;

	public SimpleListItemHolder(TextView textView, ImageView imageView,
			ProgressBar progressBar, ImageView userPresence, TextView status) {
		super();
		this.status = status;
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
	
	public TextView getStatus() {
		return status;
	}

	public void setStatus(TextView status) {
		this.status = status;
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

	public ImageView getUserPresence() {
		return userPresence;
	}

	public void setUserPresence(ImageView userPresence) {
		this.userPresence = userPresence;
	}

}
