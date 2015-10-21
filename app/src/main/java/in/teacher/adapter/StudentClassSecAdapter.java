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
 * Created by vinkrish.
 */
public class StudentClassSecAdapter extends ArrayAdapter<CommonObject> {
    private int resource;
    private ArrayList<CommonObject> data = new ArrayList<>();
    private LayoutInflater inflater;

    public StudentClassSecAdapter(Context context, int resource, ArrayList<CommonObject> listArray) {
        super(context, resource, listArray);
        this.resource = resource;
        this.data = listArray;
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
            row.setTag(holder);
        } else holder = (RecordHolder) row.getTag();

        if (position % 2 == 0)
            row.setBackgroundResource(R.drawable.list_selector1);
        else
            row.setBackgroundResource(R.drawable.list_selector2);

        CommonObject listItem = data.get(position);
        holder.text1.setText(listItem.getText1());
        holder.text2.setText(listItem.getText2());

        return row;
    }

    public static class RecordHolder {
        public TextView text1;
        public TextView text2;
    }

}
