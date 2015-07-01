package in.teacher.util;

import in.teacher.dao.SlipTesttDao;
import in.teacher.sqlite.SlipTestt;
import in.teacher.sqlite.SqlDbHelper;

import java.util.List;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class PercentageSlipTest {

	public static int findSlipTestPercentage(Context context, int sectionId, int subjectId, int schoolId){
		double mavg = 0;
		SqlDbHelper sqlHandler = SqlDbHelper.getInstance(context);
		SQLiteDatabase sqliteDatabase =  sqlHandler.getWritableDatabase();
		List<SlipTestt> slipTestList = SlipTesttDao.selectSlipTest(sectionId, subjectId, sqliteDatabase);
		int mlen = slipTestList.size();
		for(SlipTestt st: slipTestList){
			double d = (st.getAverageMark()/(double)st.getMaximumMark());
			mavg += d;
		}
		if(mlen==0){
			return 0;
		}else{
			return (int)(mavg/Double.parseDouble(mlen+"")*360);
		}
	}
}
