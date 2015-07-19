package fb.wallpaper.chat.view.list.holder;

import fb.wallpaper.chat.view.list.adapter.AbstractListAdapter.ViewHolder;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ChatsPopupHolder extends ViewHolder{

	private ImageView imageView;
	private ProgressBar progressBar;
	private TextView textView;

	public ChatsPopupHolder(ImageView imageView, ProgressBar progressBar, TextView textView) {
		this.imageView = imageView;
		this.progressBar = progressBar;
		this.textView = textView;
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
	
	public TextView getTextView() {
		return textView;
	}

	public void setTextView(TextView textView) {
		this.textView = textView;
	}
	
}
