package in.teacher.adapter;

import in.teacher.activity.R;
import in.teacher.sqlite.UploadSql;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by vinkrish.
 */

public class QueueAdapter extends BaseAdapter {
    private ArrayList<UploadSql> data = new ArrayList<>();
    private LayoutInflater inflater;

    public QueueAdapter(Context context, ArrayList<UploadSql> listArray) {
        this.data = listArray;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        RecordHolder holder;

        if (row == null) {
            row = inflater.inflate(R.layout.queue_list, parent, false);
            holder = new RecordHolder();
            holder.txtQuery = (TextView) row.findViewById(R.id.tableQuery);
            row.setTag(holder);
        } else holder = (RecordHolder) row.getTag();

        if (position % 2 == 0)
            row.setBackgroundColor(Color.rgb(255, 255, 255));
        else
            row.setBackgroundColor(Color.rgb(237, 239, 242));

        UploadSql listItem = data.get(position);
        holder.txtQuery.setText(listItem.getQuery());

        Animation animationY = new TranslateAnimation(0, 0, holder.txtQuery.getHeight() / 4, 0);
        animationY.setDuration(500);
        row.startAnimation(animationY);
        animationY = null;

        return row;
    }

    public static class RecordHolder {
        public TextView txtQuery;
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
