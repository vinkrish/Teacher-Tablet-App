package in.teacher.adapter;

import in.teacher.activity.R;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class HwAdapter extends BaseAdapter {
	private ArrayList<String> data = new ArrayList<String>();
	private LayoutInflater inflater = null;

	public HwAdapter(Context context, ArrayList<String> data) {
		this.data = data;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		RecordHolder holder = null;

		if (row == null) {
			row = inflater.inflate(R.layout.hw_list, parent, false);
			holder = new RecordHolder();
			holder.txt = (TextView) row.findViewById(R.id.txt);
			row.setTag(holder);
		}else
			holder = (RecordHolder) row.getTag();

		String gridItem = data.get(position);
		holder.txt.setText(gridItem);

		return row;
	}

	public static class RecordHolder {
		public TextView txt;
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

}
