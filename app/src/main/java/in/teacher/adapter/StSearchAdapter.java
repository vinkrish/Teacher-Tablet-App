package in.teacher.adapter;

import in.teacher.activity.R;
import in.teacher.sqlite.Amr;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by vinkrish.
 */

public class StSearchAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Amr> data = new ArrayList<>();
    private LayoutInflater inflater = null;

    public StSearchAdapter(Context context, ArrayList<Amr> listArray) {
        this.context = context;
        this.data = listArray;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder;

        if (row == null) {
            row = inflater.inflate(R.layout.st_search_list, parent, false);
            holder = new RecordHolder();
            holder.text1 = (TextView) row.findViewById(R.id.sub);
            holder.text2 = (TextView) row.findViewById(R.id.name);
            holder.int1 = (TextView) row.findViewById(R.id.tot);
            holder.int2 = (TextView) row.findViewById(R.id.abs);
            holder.pb = (ProgressBar) row.findViewById(R.id.avgProgress);
            holder.percentage = (TextView) row.findViewById(R.id.percent);
            row.setTag(holder);
        } else holder = (RecordHolder) row.getTag();

        if (position % 2 == 0)
            row.setBackgroundColor(Color.rgb(255, 255, 255));
        else
            row.setBackgroundColor(Color.rgb(237, 239, 242));

        Amr listItem = data.get(position);
        holder.text1.setText(listItem.getText1());
        holder.text2.setText(listItem.getText2());
        holder.int1.setText(listItem.getInt1() + "");
        holder.int2.setText(listItem.getInt2() + "");

        if (listItem.getInt3() >= 75) {
            holder.pb.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_green));
        } else if (listItem.getInt3() >= 50) {
            holder.pb.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_orange));
        } else {
            holder.pb.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_red));
        }
        holder.pb.setProgress(listItem.getInt3());
        holder.percentage.setText(String.valueOf(listItem.getInt3() + "%"));

        return row;
    }

    public static class RecordHolder {
        public TextView text1;
        public TextView text2;
        public TextView int1;
        public TextView int2;
        public ProgressBar pb;
        public TextView percentage;
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
