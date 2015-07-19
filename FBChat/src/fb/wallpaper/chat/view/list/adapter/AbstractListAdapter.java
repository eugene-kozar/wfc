package fb.wallpaper.chat.view.list.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class AbstractListAdapter<E> extends BaseAdapter {
	private LayoutInflater inflater;
	private List<E> dataObjects;
	private int viewId;
	protected Context context;

	public static class ViewHolder {
		public Object data;
	}

	public static abstract class OnItemClickListener implements
			View.OnClickListener {
		private ViewHolder mViewHolder;

		public OnItemClickListener(ViewHolder holder) {
			mViewHolder = holder;
		}

		public void onClick(View v) {
			onClick(v, mViewHolder);
		}

		public abstract void onClick(View v, ViewHolder viewHolder);

	};

	public AbstractListAdapter(Context context, int viewid, List<E> objects) {

		inflater = LayoutInflater.from(context);
		dataObjects = objects;
		viewId = viewid;
		this.context = context;

		if (objects == null) {
			dataObjects = new ArrayList<E>();
		}
	}
	
	public void setData(List<E> data) {
		this.dataObjects = data;
	}
	
	public List<E> getData() {
		return dataObjects;
	}

	public int getCount() {
		return dataObjects.size();
	}

	public Object getItem(int position) {
		return dataObjects.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View view, ViewGroup parent) {
		ViewHolder holder;

		if (view == null) {
			view = inflater.inflate(viewId, null);
			holder = createHolder(view);
			view.setTag(holder);

		} else {
			holder = (ViewHolder) view.getTag();
		}

		holder.data = getItem(position);
		bindHolder(holder);

		return view;
	}

	protected abstract ViewHolder createHolder(View v);

	protected abstract void bindHolder(ViewHolder h);
}