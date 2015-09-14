package in.teacher.adapter;

import in.teacher.activity.R;
import in.teacher.sqlite.SlipTestt;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by vinkrish.
 */

public class SlipTestAdapter extends ArrayAdapter<SlipTestt> {
    private ArrayList<SlipTestt> data = new ArrayList<SlipTestt>();
    private LayoutInflater inflater = null;
    private Context context;
    private int resource;

    public SlipTestAdapter(Context context, int resource, ArrayList<SlipTestt> listArray) {
        super(context, resource, listArray);
        this.data = listArray;
        this.context = context;
        this.resource = resource;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder;

        if (row == null) {
            row = inflater.inflate(resource, parent, false);
            holder = new RecordHolder();
            holder.index = (TextView) row.findViewById(R.id.idx);
            holder.txtDate = (TextView) row.findViewById(R.id.dateName);
            holder.txtPortion = (TextView) row.findViewById(R.id.portion);
            holder.pb = (ProgressBar) row.findViewById(R.id.classAvgProgress);
            holder.percentage = (TextView) row.findViewById(R.id.percentage);
            row.setTag(holder);
        } else holder = (RecordHolder) row.getTag();

        SlipTestt listItem = data.get(position);
        holder.index.setText(listItem.getIdx());
        holder.txtDate.setText(listItem.getTestDate());
        holder.txtPortion.setText(listItem.getPortionName());
        holder.percentage.setText(String.valueOf(listItem.getProgress() + "%"));

        if (listItem.getProgress() >= 75) {
            holder.pb.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_green));
        } else if (listItem.getProgress() >= 50) {
            holder.pb.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_orange));
        } else {
            holder.pb.setProgressDrawable(context.getResources().getDrawable(R.drawable.progress_red));
        }
        holder.pb.setProgress(listItem.getProgress());

        return row;
    }

    public static class RecordHolder {
        public TextView index;
        public TextView txtDate;
        public TextView txtPortion;
        public ProgressBar pb;
        public TextView percentage;
    }

}
