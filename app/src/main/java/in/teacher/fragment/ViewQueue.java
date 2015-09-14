package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.adapter.Capitalize;
import in.teacher.adapter.QueueAdapter;
import in.teacher.dao.TeacherDao;
import in.teacher.dao.TempDao;
import in.teacher.dao.UploadSqlDao;
import in.teacher.sqlite.Temp;
import in.teacher.sqlite.UploadSql;
import in.teacher.util.AppGlobal;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by vinkrish.
 */

public class ViewQueue extends Fragment {
    private Context context;
    private SQLiteDatabase sqliteDatabase;
    private ListView lv;
    private ArrayList<UploadSql> queueList = new ArrayList<>();
    private List<UploadSql> uploadSqlList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.queue_view, container, false);
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        TextView queryCount = (TextView) view.findViewById(R.id.queue);

        lv = (ListView) view.findViewById(R.id.list);
        uploadSqlList = UploadSqlDao.selectUploadSql(sqliteDatabase);
        queryCount.setText("Query [" + uploadSqlList.size() + "]");

        populateListArray();

        return view;

    }

    private void populateListArray() {
        for (UploadSql upSqlList : uploadSqlList) {
            queueList.add(new UploadSql(upSqlList.getTableName(), upSqlList.getAction(), upSqlList.getQuery()));
        }
        QueueAdapter queueAdapter = new QueueAdapter(context, queueList);
        lv.setAdapter(queueAdapter);
    }

}
