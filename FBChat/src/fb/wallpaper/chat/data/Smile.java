package fb.wallpaper.chat.data;

public class Smile {
	private String text;
	private int resourceId;

	public Smile(String text, int resourceId) {
		super();
		this.text = text;
		this.resourceId = resourceId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getResourceId() {
		return resourceId;
	}

	public void setResourceId(int resourceId) {
		this.resourceId = resourceId;
	}

}
