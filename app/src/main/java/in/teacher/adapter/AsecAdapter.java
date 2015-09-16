package in.teacher.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import in.teacher.activity.R;
import in.teacher.sqlite.Amr;

/**
 * Created by vinkrish.
 */
public class AsecAdapter extends ArrayAdapter<Amr> {
    private int resource;
    private ArrayList<Amr> data = new ArrayList<>();
    private LayoutInflater inflater = null;

    public AsecAdapter(Context context, int resource, ArrayList<Amr> listArray) {
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
            holder.idx = (TextView) row.findViewById(R.id.txt1);
            holder.txtAbsentee = (TextView) row.findViewById(R.id.txt2);
            row.setTag(holder);
        } else holder = (RecordHolder) row.getTag();

        if (position % 2 == 0)
            row.setBackgroundResource(R.drawable.list_selector1);
        else
            row.setBackgroundResource(R.drawable.list_selector2);

        Amr listItem = data.get(position);
        holder.idx.setText(listItem.getText1());
        holder.txtAbsentee.setText(listItem.getText3());

        return row;
    }

    public static class RecordHolder {
        public TextView idx;
        public TextView txtAbsentee;
    }

}
