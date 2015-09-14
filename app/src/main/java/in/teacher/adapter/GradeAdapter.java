package in.teacher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;

/**
 * Created by vinkrish.
 */

public class GradeAdapter extends BaseAdapter {

    private List<String> data = new ArrayList<>();
    private LayoutInflater inflater = null;

    public GradeAdapter(Context context, List<String> gridArray) {
        this.data = gridArray;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder;

        if (row == null) {
            row = inflater.inflate(R.layout.grade_grid, parent, false);
            holder = new RecordHolder();
            holder.gradeBut = (TextView) row.findViewById(R.id.grade_button);
            row.setTag(holder);
        } else holder = (RecordHolder) row.getTag();

        String gridItem = data.get(position);
        holder.gradeBut.setText(gridItem);

        return row;
    }

    public static class RecordHolder {
        public TextView gradeBut;
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
