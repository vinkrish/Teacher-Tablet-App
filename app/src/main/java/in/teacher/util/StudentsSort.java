package in.teacher.util;

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

}
