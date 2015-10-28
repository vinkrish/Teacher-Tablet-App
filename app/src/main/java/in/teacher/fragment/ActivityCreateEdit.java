package in.teacher.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import in.teacher.activity.R;

/**
 * Created by vinkrish on 28/10/15.
 */
public class ActivityCreateEdit extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.act_create_edit, container, false);



        return view;
    }
}
