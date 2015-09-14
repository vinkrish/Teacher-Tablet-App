package in.teacher.util;

import in.teacher.dao.SlipTesttDao;
import in.teacher.sqlite.SlipTestt;
import in.teacher.sqlite.SqlDbHelper;

import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by vinkrish.
 */

public class PercentageSlipTest {

    public static double findSlipTestPercentage(int sectionId, int subjectId, SQLiteDatabase sqliteDatabase) {
        double mavg = 0;
        List<SlipTestt> slipTestList = SlipTesttDao.selectSlipTest(sectionId, subjectId, sqliteDatabase);
        int mlen = slipTestList.size();
        for (SlipTestt st : slipTestList) {
            double d = (st.getAverageMark() / (double) st.getMaximumMark());
            mavg += d;
        }
        if (mlen == 0) return 0;
        else return (mavg / Double.parseDouble(mlen + "") * 360);
    }
}
