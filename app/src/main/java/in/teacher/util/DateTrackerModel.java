package in.teacher.util;

import in.teacher.sqlite.DateTracker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Created by vinkrish.
 */

public class DateTrackerModel {

    public static DateTracker getDateTracker(int month, int year) {
        DateTracker dt = new DateTracker();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        int noOfDays = 0;
        Calendar cal = new GregorianCalendar(year, month, 1);
        dt.setSelectedMonth(month);
        dt.setFirstDate(dateFormat.format(cal.getTime()));
        while (cal.get(Calendar.MONTH) == month) {
            int day = cal.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SUNDAY) {
            } else {
                noOfDays += 1;
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        dt.setNoOfDays(noOfDays);
        cal.add(Calendar.DAY_OF_YEAR, -1);
        dt.setLastDate(dateFormat.format(cal.getTime()));
        return dt;
    }

    public static DateTracker getDateTracker1(int day1, int day2, int month, int year) {
        DateTracker dt = new DateTracker();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        int noOfDays = 0;
        Calendar cal = new GregorianCalendar(year, month, day1);
        dt.setSelectedMonth(month);
        dt.setFirstDate(dateFormat.format(cal.getTime()));
        while (cal.get(Calendar.MONTH) == month && cal.get(Calendar.DAY_OF_MONTH) <= day2) {
            int day = cal.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SUNDAY) {
            } else {
                noOfDays += 1;
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        dt.setNoOfDays(noOfDays);
        cal = new GregorianCalendar(year, month, day2);
        dt.setLastDate(dateFormat.format(cal.getTime()));
        return dt;
    }

    public static DateTracker getDateTracker2(int day2, int month, int year) {
        DateTracker dt = new DateTracker();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        int noOfDays = 0;
        Calendar cal = new GregorianCalendar(year, month, day2);
        dt.setSelectedMonth(month);
        dt.setFirstDate(dateFormat.format(cal.getTime()));
        while (cal.get(Calendar.MONTH) == month) {
            int day = cal.get(Calendar.DAY_OF_WEEK);
            if (day == Calendar.SUNDAY) {
            } else {
                noOfDays += 1;
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        dt.setNoOfDays(noOfDays);
        cal.add(Calendar.DAY_OF_YEAR, -1);
        dt.setLastDate(dateFormat.format(cal.getTime()));
        return dt;
    }

}
