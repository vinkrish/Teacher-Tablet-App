package in.teacher.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vinkrish on 01/10/15.
 */
public class SubjectGroupDao {

    public static List<String> getSubjectGroupNameList(SQLiteDatabase sqliteDatabase, String ids){
        Cursor c = sqliteDatabase.rawQuery("SELECT SubjectGroup FROM subjects_groups where id in (" + ids + ")", null);
        List<String> list = new ArrayList<>();
        c.moveToFirst();
        while(!c.isAfterLast()){
            list.add(c.getString(c.getColumnIndex("SubjectGroup")));
            c.moveToNext();
        }
        c.close();
        return list;
    }

    public static List<Integer> getSubjectIdsInGroup(SQLiteDatabase sqliteDatabase, int groupId) {
        Cursor c = sqliteDatabase.rawQuery("select subject_ids from subjects_groups where id = " + groupId, null);
        List<Integer> idList = new ArrayList<>();
        String ids = null;
        c.moveToFirst();
        while (!c.isAfterLast()) {
            ids = c.getString(c.getColumnIndex("subject_ids"));
            c.moveToNext();
        }
        c.close();
        String[] idArray = ids.split("#");
        for(String id: idArray){
            idList.add(Integer.parseInt(id));
        }
        return idList;
    }


}
