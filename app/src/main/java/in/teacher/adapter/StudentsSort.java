package in.teacher.adapter;

import in.teacher.sqlite.Students;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by vinkrish.
 */

public class StudentsSort implements Comparator<Students> {

    @Override
    public int compare(Students arg0, Students arg1) {
        return arg0.getRollNoInClass() - arg1.getRollNoInClass();
    }

    public static List<Students> sortOut(List<Students> sList) {
        List<Students> sortedList = new ArrayList<Students>();
        for (int i = 0, j = sList.size() - 1; i < j; i++) {
            Students s = null;
            for (int k = sList.size() - i - 1; k > 0; k--) {
                int idx = i + 1;
                Students s1 = sList.get(i);
                Students s2 = sList.get(idx);
                if (s2.getRollNoInClass() < s1.getRollNoInClass()) {
                    Students temp = s2;
                    s = s2;
                    //	sortedList.add(s2);
                    sList.set(i, s2);
                    sList.set(idx, temp);
                } else {
                    s = s1;
                    //	sortedList.add(s1);
                }
                idx += 1;
            }
            sortedList.add(s);
        }
        return sortedList;
    }

}
