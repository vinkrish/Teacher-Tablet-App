package in.teacher.adapter;

import in.teacher.activity.R;
import in.teacher.sqlite.Students;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by vinkrish.
 */

public class MarksAdapter extends BaseAdapter {
    private ArrayList<Students> data = new ArrayList<>();
    private LayoutInflater inflater;

    public MarksAdapter(Context context, ArrayList<Students> gridArray) {
        this.data = gridArray;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder;

        if (row == null) {
            row = inflater.inflate(R.layout.marks_list, parent, false);
            holder = new RecordHolder();
            holder.txtRollNo = (TextView) row.findViewById(R.id.rollNo);
            holder.txtName = (TextView) row.findViewById(R.id.name);
            holder.txtMarks = (TextView) row.findViewById(R.id.score);
            holder.iV = (ImageView) row.findViewById(R.id.indicator);
            row.setTag(holder);
        } else holder = (RecordHolder) row.getTag();

        Students gridItem = data.get(position);
        holder.txtRollNo.setText(String.valueOf(gridItem.getRollNoInClass()));
        holder.txtName.setText(gridItem.getName());
        holder.txtMarks.setText(gridItem.getScore());
        holder.iV.setImageBitmap(gridItem.getAttMarker());
        return row;
    }

    public static class RecordHolder {
        public TextView txtRollNo;
        public TextView txtName;
        public TextView txtMarks;
        public ImageView iV;
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
