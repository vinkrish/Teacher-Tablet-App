package in.teacher.util;

import java.util.Comparator;

import in.teacher.sqlite.GradesClassWise;

/**
 * Created by vinkrish on 21/01/16.
 */
public class GradeClassWiseSort implements Comparator<GradesClassWise>{
    @Override
    public int compare(GradesClassWise lhs, GradesClassWise rhs) {
        return lhs.getGradePoint()-rhs.getGradePoint();
    }
}
