package in.teacher.adapter;

import in.teacher.activity.R;
import in.teacher.sqlite.Students;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.LayoutInflater;

public class AttendanceAdapter extends BaseAdapter {
	private ArrayList<Students>	data = new ArrayList<Students>();
	private LayoutInflater inflater = null;

	public AttendanceAdapter(Context context, ArrayList<Students> gridArray) {
		this.data = gridArray;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		RecordHolder holder = null;

		if (row == null) {
			row = inflater.inflate(R.layout.attendance_grid, parent, false);
			holder = new RecordHolder();
			holder.txtRollNo = (TextView) row.findViewById(R.id.rollNo);
			holder.txtName = (TextView) row.findViewById(R.id.name);
			holder.imageAttend = (ImageView) row.findViewById(R.id.attendPic);
			row.setTag(holder);
		}else
			holder = (RecordHolder) row.getTag();

		Students gridItem = data.get(position);
		holder.txtRollNo.setText(String.valueOf(gridItem.getRollNoInClass()));
		holder.txtName.setText(gridItem.getName());
		holder.imageAttend.setImageBitmap(gridItem.getAttMarker());

		return row;
	}

	public static class RecordHolder {
		public TextView txtRollNo;
		public TextView txtName;
		public ImageView imageAttend;
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
