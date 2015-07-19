package fb.wallpaper.chat.data;

public class Category {
	private String text;
	private int index;
	private int itemCount;
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	public int getItemCount() {
		return itemCount;
	}
	
	public void setItemCount(int itemCount) {
		this.itemCount = itemCount;
	}
	
	@Override
	public String toString() {
		return index + " " + text + " " + itemCount + "\n";
	}
	
}
