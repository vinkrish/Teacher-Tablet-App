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
import android.widget.TextView;

/**
 * Created by vinkrish.
 */

public class AttGraph extends BaseAdapter {
    private ArrayList<Amr> data = new ArrayList<>();
    private LayoutInflater inflater = null;

    public AttGraph(Context context, ArrayList<Amr> listArray) {
        this.data = listArray;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder;

        if (row == null) {
            row = inflater.inflate(R.layout.att_graph_list, parent, false);
            holder = new RecordHolder();
            holder.txt1 = (TextView) row.findViewById(R.id.txt1);
            holder.txt2 = (TextView) row.findViewById(R.id.txt2);
            holder.txt3 = (TextView) row.findViewById(R.id.txt3);
            row.setTag(holder);
        } else holder = (RecordHolder) row.getTag();

        if (position % 2 == 0)
            row.setBackgroundColor(Color.rgb(255, 255, 255));
        else
            row.setBackgroundColor(Color.rgb(237, 239, 242));

        Amr listItem = data.get(position);
        holder.txt1.setText(listItem.getText1());
        holder.txt2.setText(listItem.getText2());
        holder.txt3.setText(listItem.getText3());

        return row;
    }

    public static class RecordHolder {
        public TextView txt1;
        public TextView txt2;
        public TextView txt3;
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
