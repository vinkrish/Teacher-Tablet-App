package in.teacher.adapter;

import in.teacher.activity.R;
import in.teacher.model.HW;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by vinkrish.
 */

public class HomeworkViewAdapter extends BaseAdapter {
    private LayoutInflater inflater = null;
    private ArrayList<HW> data = new ArrayList<>();

    public HomeworkViewAdapter(Context context, ArrayList<HW> data) {
        this.data = data;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder;
        if (row == null) {
            row = inflater.inflate(R.layout.homework_view_item, parent, false);
            holder = new RecordHolder();
            holder.tvSubject = (TextView) row.findViewById(R.id.subjectName);
            holder.tvHomework = (TextView) row.findViewById(R.id.homeworkDetails);
            row.setTag(holder);
        } else holder = (RecordHolder) row.getTag();

        HW e = data.get(position);
        holder.tvSubject.setText(e.getSubject());
        holder.tvHomework.setText(e.getHomework());

        return row;
    }

    public class RecordHolder {
        public TextView tvSubject;
        public TextView tvHomework;
    }

}
