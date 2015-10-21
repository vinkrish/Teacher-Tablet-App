package in.teacher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import in.teacher.activity.R;
import in.teacher.sqlite.CommonObject;

/**
 * Created by vinkrish on 21/10/15.
 */
public class StudentProfileAdapter extends ArrayAdapter<CommonObject> {
    private int resource;
    private ArrayList<CommonObject> data = new ArrayList<>();
    private LayoutInflater inflater;

    public StudentProfileAdapter(Context context, int resource, ArrayList<CommonObject> objects) {
        super(context, resource, objects);
        this.resource = resource;
        this.data = objects;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder;

        if (row == null) {
            row = inflater.inflate(resource, parent, false);
            holder = new RecordHolder();
            holder.text1 = (TextView) row.findViewById(R.id.txt1);
            holder.text2 = (TextView) row.findViewById(R.id.txt2);
            holder.text3 = (TextView) row.findViewById(R.id.txt3);
            row.setTag(holder);
        } else holder = (RecordHolder) row.getTag();

        if (position % 2 == 0)
            row.setBackgroundResource(R.drawable.list_selector1);
        else
            row.setBackgroundResource(R.drawable.list_selector2);

        CommonObject listItem = data.get(position);
        holder.text1.setText(listItem.getText1());
        holder.text2.setText(listItem.getText2());
        holder.text3.setText(listItem.getText3());

        return row;
    }

    public static class RecordHolder {
        public TextView text1;
        public TextView text2;
        public TextView text3;
    }

}
