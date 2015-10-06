package in.teacher.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.teacher.activity.R;

/**
 * Created by vinkrish on 30/09/15.
 */
public class SubjectMapStudentUpdate extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.subject_map_student_update, container, false);

        return view;
    }

}
