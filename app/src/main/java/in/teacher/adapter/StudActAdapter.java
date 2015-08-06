package in.teacher.adapter;

import in.teacher.activity.R;
import in.teacher.sqlite.Amr;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class StudActAdapter extends BaseAdapter {
	private Context context;
	private List<Amr> data = new ArrayList<>();
	private LayoutInflater inflater = null;

	public StudActAdapter(Context context, List<Amr> listArray) {
		this.context = context;
		this.data = listArray;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		RecordHolder holder;

		if (row == null) {
			row = inflater.inflate( R.layout.search_act_list, parent, false);
			holder = new RecordHolder();
			holder.txt1 = (TextView) row.findViewById(R.id.txt1);
            holder.txt2 = (TextView) row.findViewById(R.id.txt2);
			holder.pb1 = (ProgressBar) row.findViewById(R.id.avgProgress1);
			holder.percentage1 = (TextView) row.findViewById(R.id.percent1);
			holder.pb2 = (ProgressBar) row.findViewById(R.id.avgProgress2);
			holder.percentage2 = (TextView) row.findViewById(R.id.percent2);
			row.setTag(holder);
		}else{
			holder = (RecordHolder) row.getTag();
		}

		if(position % 2 == 0)
			row.setBackgroundResource(R.drawable.list_selector1);
		else
			row.setBackgroundResource(R.drawable.list_selector2);

		Amr listItem = data.get(position);
		holder.txt1.setText(listItem.getText1());
        holder.txt2.setText(listItem.getText2());

		if(listItem.getInt1()>=75){
			holder.pb1.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_green));
		}else if(listItem.getInt1()>=50){
			holder.pb1.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_orange));
		}else{
			holder.pb1.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_red));
		}
		holder.pb1.setProgress(listItem.getInt1());
		holder.percentage1.setText(String.valueOf(listItem.getInt1()+"%"));
		
		if(listItem.getInt2()>=75){
			holder.pb2.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_green));
		}else if(listItem.getInt2()>=50){
			holder.pb2.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_orange));
		}else{
			holder.pb2.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_red));
		}
		holder.pb2.setProgress(listItem.getInt2());
		holder.percentage2.setText(String.valueOf(listItem.getInt2()+"%"));

		return row;
	}

	public static class RecordHolder{
		public TextView txt1;
		public TextView txt2;
		public ProgressBar pb1;
		public TextView percentage1;
		public ProgressBar pb2;
		public TextView percentage2;
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
