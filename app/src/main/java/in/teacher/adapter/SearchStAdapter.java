package in.teacher.adapter;

import in.teacher.activity.R;
import in.teacher.sqlite.CommonObject;

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
public class SearchStAdapter extends BaseAdapter {
    private ArrayList<CommonObject> data = new ArrayList<>();
    private LayoutInflater inflater;

    public SearchStAdapter(Context context, ArrayList<CommonObject> listArray) {
        this.data = listArray;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder;

        if (row == null) {
            row = inflater.inflate(R.layout.search_st_list, parent, false);
            holder = new RecordHolder();
            holder.int1 = (TextView) row.findViewById(R.id.li_txt1);
            holder.txt1 = (TextView) row.findViewById(R.id.li_txt2);
            holder.txt2 = (TextView) row.findViewById(R.id.li_txt3);
            holder.int2 = (TextView) row.findViewById(R.id.li_txt4);
            holder.int3 = (TextView) row.findViewById(R.id.li_txt5);
            row.setTag(holder);
        } else holder = (RecordHolder) row.getTag();

        if (position % 2 == 0)
            row.setBackgroundColor(Color.rgb(255, 255, 255));
        else
            row.setBackgroundColor(Color.rgb(237, 239, 242));

        CommonObject listItem = data.get(position);
        holder.int1.setText(listItem.getInt1() + "");
        holder.int2.setText(listItem.getInt2() + "");
        holder.int3.setText(listItem.getInt3() + "");
        holder.txt1.setText(listItem.getText1());
        holder.txt2.setText(listItem.getText2());

        return row;
    }

    public static class RecordHolder {
        public TextView int1;
        public TextView int2;
        public TextView int3;
        public TextView txt1;
        public TextView txt2;
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
