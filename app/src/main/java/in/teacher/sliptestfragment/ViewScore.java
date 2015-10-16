package in.teacher.sliptestfragment;

import in.teacher.activity.R;
import in.teacher.adapter.SlipTestAdapter;
import in.teacher.dao.ClasDao;
import in.teacher.dao.SectionDao;
import in.teacher.dao.SlipTestMarkDao;
import in.teacher.dao.SlipTesttDao;
import in.teacher.dao.StAvgDao;
import in.teacher.dao.TempDao;
import in.teacher.sqlite.SlipTestt;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.PercentageSlipTest;
import in.teacher.util.ReplaceFragment;
import in.teacher.util.SwipeDismissListViewTouchListener;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by vinkrish.
 */
public class ViewScore extends Fragment {
    private Context context;
    private Activity act;
    private SQLiteDatabase sqliteDatabase;
    private ListView lv;
    private String className, sectionName;
    private int schoolId, classId, sectionId, subjectId;
    private Long selectedSlipTestId, slipTestId;
    private List<SlipTestt> slipTestList;
    private List<Long> stIdList = new ArrayList<>();
    private List<String> portionNameList = new ArrayList<>();
    private List<String> dateList = new ArrayList<>();
    private ArrayList<SlipTestt> viewScoreList = new ArrayList<>();
    private SlipTestAdapter stAdapter;
    private List<Double> avgMarkList = new ArrayList<>();
    private List<Integer> maxMarkList = new ArrayList<>();
    private List<Integer> progressList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.view_score, container, false);
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();
        act = AppGlobal.getActivity();

        /*List<Clas> cList = ClasDao.selectClas(sqliteDatabase);
        List<Section> sList = SectionDao.selectSection(sqliteDatabase);*/

        Temp t = TempDao.selectTemp(sqliteDatabase);
        classId = t.getCurrentClass();
        schoolId = t.getSchoolId();
        sectionId = t.getCurrentSection();
        subjectId = t.getCurrentSubject();
        slipTestId = t.getSlipTestId();

        className = ClasDao.getClassName(classId, sqliteDatabase);
        sectionName = SectionDao.getSectionName(sectionId, sqliteDatabase);

        /*for (Clas c : cList) {
            if (classId == c.getClassId()) {
                className = c.getClassName();
                break;
            }
        }

        for (Section s : sList) {
            if (sectionId == s.getSectionId()) {
                sectionName = s.getSectionName();
                break;
            }
        }*/

        TextView viewTop = (TextView) view.findViewById(R.id.viewTop);
        StringBuilder vT = new StringBuilder();
        vT.append(className).append("-" + sectionName + "  ").append("Tap on the sliptest to view detailed info");
        viewTop.setText(vT);

        initialize();

        lv = (ListView) view.findViewById(R.id.list);
        populateListArray();
        stAdapter = new SlipTestAdapter(context, R.layout.st_list, viewScoreList);
        lv.setAdapter(stAdapter);

        lv.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                selectedSlipTestId = stIdList.get(pos);
                TempDao.updateSlipTestId(selectedSlipTestId, sqliteDatabase);
                int count = SlipTestMarkDao.findSTMarkEntered(selectedSlipTestId, schoolId, sqliteDatabase);
                if (count > 0) {
                    ReplaceFragment.replace(new UpdateSlipTestMark(), getFragmentManager());
                } else {
                    CommonDialogUtils.displayAlertWhiteDialog(act, "Data is not synced yet");
                }
            }
        });

        SwipeDismissListViewTouchListener touchListener =
                new SwipeDismissListViewTouchListener(lv,
                        new SwipeDismissListViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return true;
                            }

                            @Override
                            public void leftSwipe(int position) {
                                if (SwipeDismissListViewTouchListener.leftSwipeFlag) {
                                    Toast.makeText(context, "position" + position, Toast.LENGTH_SHORT).show();
                                    slipTestId = stIdList.get(position);
                                    TempDao.updateSlipTestId(slipTestId, sqliteDatabase);
                                    ReplaceFragment.replace(new EditSlipTest(), getFragmentManager());
                                }
                            }

                            @Override
                            public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                                for (int position : reverseSortedPositions) {
                                    stAdapter.remove(stAdapter.getItem(position));
                                    resetAdapter(position);
                                }
                                //   stAdapter.notifyDataSetChanged();
                            }
                        });
        lv.setOnTouchListener(touchListener);
        lv.setOnScrollListener(touchListener.makeScrollListener());

        return view;

    }

    public void resetAdapter(final int position) {
        AlertDialog.Builder submitBuilder = new AlertDialog.Builder(act);
        submitBuilder.setCancelable(false);
        submitBuilder.setTitle("Confirm your action");
        submitBuilder.setMessage("Do you want to delete slip test : " + portionNameList.get(position) + " created at " + dateList.get(position));
        submitBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
                initialize();
                populateListArray();
                lv.setAdapter(stAdapter);
            }
        });
        submitBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                selectedSlipTestId = stIdList.get(position);
                SlipTesttDao.deleteSlipTest(selectedSlipTestId, schoolId, sqliteDatabase);
                initialize();
                double updatedSTAvg = PercentageSlipTest.findSlipTestPercentage(sectionId, subjectId, sqliteDatabase);
                StAvgDao.updateSlipTestAvg(sectionId, subjectId, updatedSTAvg, sqliteDatabase);
                populateListArray();
                lv.setAdapter(stAdapter);
            }
        });
        submitBuilder.show();
    }

    private void initialize() {
        stIdList.clear();
        dateList.clear();
        portionNameList.clear();
        avgMarkList.clear();
        maxMarkList.clear();
        progressList.clear();

        slipTestList = SlipTesttDao.selectSlipTest(sectionId, subjectId, sqliteDatabase);
        for (SlipTestt st : slipTestList) {
            stIdList.add(st.getSlipTestId());
            dateList.add(st.getTestDate());
            portionNameList.add(st.getPortionName());
            avgMarkList.add(st.getAverageMark());
            maxMarkList.add(st.getMaximumMark());
        }

        for (int i = 0; i < avgMarkList.size(); i++) {
            double d = (avgMarkList.get(i) / Double.parseDouble(maxMarkList.get(i) + "")) * 100;
            progressList.add((int) d);
        }

    }

    private void populateListArray() {
        viewScoreList.clear();
        for (int i = 0; i < stIdList.size(); i++) {
            if (portionNameList.get(i).length() > 27) {
                String s = portionNameList.get(i).substring(0, 25) + "...";
                viewScoreList.add(new SlipTestt(Integer.toString(i + 1), dateList.get(i), s, progressList.get(i)));
            } else {
                viewScoreList.add(new SlipTestt(Integer.toString(i + 1), dateList.get(i), portionNameList.get(i), progressList.get(i)));
            }

        }

    }

}
