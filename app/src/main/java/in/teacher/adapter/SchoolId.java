package in.teacher.adapter;

import in.teacher.dao.TempDao;
import in.teacher.sqlite.SqlDbHelper;
import in.teacher.sqlite.Temp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by vinkrish.
 */
public class SchoolId {

    public static int getSchoolId(Context context) {
        int schoolId = 0;
        SqlDbHelper sqlHandler = SqlDbHelper.getInstance(context);
        SQLiteDatabase sqliteDatabase = sqlHandler.getWritableDatabase();
        Temp t = TempDao.selectTemp(sqliteDatabase);
        schoolId = t.getSchoolId();
        return schoolId;
    }

}
