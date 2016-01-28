package in.teacher.sectionincharge;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import in.teacher.activity.R;
import in.teacher.dao.ClasDao;
import in.teacher.dao.StudentsDao;
import in.teacher.dao.SubjectGroupDao;
import in.teacher.dao.SubjectsDao;
import in.teacher.dao.TempDao;
import in.teacher.fragment.Dashbord;
import in.teacher.sqlite.Students;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ReplaceFragment;

/**
 * Created by vinkrish on 30/09/15.
 * Don't expect comments explaining every piece of code, class and function names are self explanatory.
 */
public class SubjectMapStudentCreate extends Fragment {
    private SQLiteDatabase sqliteDatabase;
    private int classId, sectionId;
    private List<Integer> subjectGroupIdList = new ArrayList<>();
    private List<String> subjectGroupNameList = new ArrayList<>();
    //private HashMap<Integer, List<Integer>> subjectGroupMap = new HashMap<>();
    private List<Integer> subjectIdList = new ArrayList<>();
    private List<String> subjectNameList = new ArrayList<>();
    private List<Integer> selectedSubjectId = new ArrayList<>();
    private Button editUpdateBtn, mapSubjectBtn;
    private ArrayList<Integer> studIdList = new ArrayList<>();
    private ArrayList<String> studNameList = new ArrayList<>();
    protected boolean[] studentSelections;
    private List<Integer> selectedStudentsId = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.subject_map_student_create, container, false);
        editUpdateBtn = (Button) view.findViewById(R.id.edit_update);
        mapSubjectBtn = (Button) view.findViewById(R.id.map_subject);

        sqliteDatabase = AppGlobal.getSqliteDatabase();
        init();

        RelativeLayout table = (RelativeLayout) view.findViewById(R.id.table);
        table.addView(new TableMainLayout(this.getActivity()));

        return view;
    }

    private void init() {

        Temp t = TempDao.selectTemp(sqliteDatabase);
        classId = t.getClassId();
        sectionId = t.getSectionId();
        CommonDialogUtils.hideKeyboard(getActivity());

        subjectGroupIdList = ClasDao.getSubjectGroupIds(sqliteDatabase, classId);

        if (subjectGroupIdList.size() == 0) {
            CommonDialogUtils.displayAlertWhiteDialog(getActivity(), "Class has no subjects assigned, please contact the admin");
            ReplaceFragment.replace(new Dashbord(), getFragmentManager());
        } else {
            StringBuilder sb = new StringBuilder();
            for (Integer ids : subjectGroupIdList) {
                sb.append(ids + ",");
            }
            subjectGroupNameList = SubjectGroupDao.getSubjectGroupNameList(sqliteDatabase, sb.substring(0, sb.length() - 1));

            mapSubjectBtn.setActivated(false);
            mapSubjectBtn.setOnClickListener(mapSubjectListener);

            editUpdateBtn.setOnClickListener(editListener);

            if (StudentsDao.isFewStudentMapped(sqliteDatabase, sectionId)) {
                editUpdateBtn.setVisibility(View.VISIBLE);
            }
        }

    }

    View.OnClickListener editListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ReplaceFragment.replace(new SubjectMapStudentEdit(), getFragmentManager());
        }
    };

    View.OnClickListener mapSubjectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mapSubjectBtn.isActivated()) {
                studIdList.clear();
                studNameList.clear();
                List<Students> studentList = StudentsDao.selectStudentsUnmapped(sectionId, sqliteDatabase);
                for (Students s : studentList) {
                    studIdList.add(s.getStudentId());
                    studNameList.add(s.getName());
                }
                studentSelections = new boolean[studIdList.size()];
                showStudentDialog();
            }
        }
    };

    public void showStudentDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Students")
                .setCancelable(false)
                .setMultiChoiceItems(studNameList.toArray(new CharSequence[studIdList.size()]), studentSelections, new StudentSelectionClickHandler())
                .setPositiveButton("OK", new StudentButtonClickHandler())
                .show();
    }

    public class StudentSelectionClickHandler implements DialogInterface.OnMultiChoiceClickListener {
        public void onClick(DialogInterface dialog, int clicked, boolean selected) {
            if (selected) studentSelections[clicked] = true;
            else studentSelections[clicked] = false;
        }
    }

    public class StudentButtonClickHandler implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface dialog, int clicked) {
            switch (clicked) {
                case DialogInterface.BUTTON_POSITIVE:
                    selectedStudent();
                    break;
            }
        }
    }

    protected void selectedStudent() {
        for (int i = 0; i < studIdList.size(); i++) {
            if (studentSelections[i]) {
                selectedStudentsId.add(studIdList.get(i));
            }
        }
        new CalledSubmit().execute();
    }

    class CalledSubmit extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog = new ProgressDialog(getActivity());

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage("Submitting marks...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            StringBuilder sb = new StringBuilder();
            for (Integer sbi : selectedSubjectId) {
                sb.append(sbi).append("#");
            }
            for (Integer ssi : selectedStudentsId) {
                String sql = "update students set SubjectIds = '" + sb.substring(0, sb.length() - 1) + "' where StudentId = " + ssi;
                try {
                    sqliteDatabase.execSQL(sql);
                    ContentValues cv = new ContentValues();
                    cv.put("Query", sql);
                    sqliteDatabase.insert("uploadsql", null, cv);
                } catch (SQLException e) {
                }
            }
            return null;
        }

        protected void onPostExecute(Void v) {
            super.onPostExecute(v);
            pDialog.dismiss();
            if (StudentsDao.isStudentMapped(sqliteDatabase, sectionId)) {
                Toast.makeText(getActivity(), "All students are mapped to respective subjects.", Toast.LENGTH_LONG).show();
                ReplaceFragment.replace(new Dashbord(), getFragmentManager());
            } else {
                Toast.makeText(getActivity(), "Selected students are mapped to respective subjects.", Toast.LENGTH_LONG).show();
                ReplaceFragment.replace(new SubjectMapStudentCreate(), getFragmentManager());
            }
        }
    }

    public class TableMainLayout extends RelativeLayout {
        public final String TAG = "TableMainLayout.java";

        String headers[] = {
                "Subject Group",
                "Subject"
        };

        TableLayout tableA;
        TableLayout tableB;
        TableLayout tableC;
        TableLayout tableD;

        ScrollView scrollViewC;
        ScrollView scrollViewD;

        Context context;

        int headerCellsWidth[] = new int[headers.length];

        public TableMainLayout(Context context) {

            super(context);

            this.context = context;

            this.initComponents();
            this.setComponentsId();
            this.setScrollViewAndHorizontalScrollViewTag();

            this.scrollViewC.addView(this.tableC);

            this.scrollViewD.addView(this.tableD);

            this.addComponentToMainLayout();
            this.setBackgroundColor(Color.RED);

            this.addTableRowToTableA();
            this.addTableRowToTableB();

            this.resizeHeaderHeight();

            this.getTableRowHeaderCellWidth();

            this.generateTableC_AndTable_D();

            this.resizeBodyTableRowHeight();
        }

        private void initComponents() {

            this.tableA = new TableLayout(this.context);
            this.tableB = new TableLayout(this.context);
            this.tableC = new TableLayout(this.context);
            this.tableD = new TableLayout(this.context);

            this.scrollViewC = new MyScrollView(this.context);
            this.scrollViewD = new MyScrollView(this.context);

            this.tableA.setBackgroundColor(Color.LTGRAY);
            this.tableB.setBackgroundColor(Color.LTGRAY);

        }

        private void setComponentsId() {
            this.tableA.setId(View.generateViewId());
            this.tableB.setId(View.generateViewId());
            this.scrollViewC.setId(View.generateViewId());
            this.scrollViewD.setId(View.generateViewId());
        }

        private void setScrollViewAndHorizontalScrollViewTag() {
            this.scrollViewC.setTag("scroll view c");
            this.scrollViewD.setTag("scroll view d");
        }

        private void addComponentToMainLayout() {

            RelativeLayout.LayoutParams componentB_Params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            componentB_Params.addRule(RelativeLayout.RIGHT_OF, this.tableA.getId());

            RelativeLayout.LayoutParams componentC_Params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            componentC_Params.addRule(RelativeLayout.BELOW, this.tableA.getId());

            RelativeLayout.LayoutParams componentD_Params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            componentD_Params.addRule(RelativeLayout.RIGHT_OF, this.scrollViewC.getId());
            componentD_Params.addRule(RelativeLayout.BELOW, this.tableB.getId());

            this.addView(this.tableA);
            this.addView(this.tableB, componentB_Params);
            this.addView(this.scrollViewC, componentC_Params);
            this.addView(this.scrollViewD, componentD_Params);

        }

        private void addTableRowToTableA() {
            this.tableA.addView(this.componentATableRow());
        }

        private void addTableRowToTableB() {
            this.tableB.addView(this.componentBTableRow());
        }

        TableRow componentATableRow() {

            TableRow.LayoutParams params = new TableRow.LayoutParams(getCellWidth(), LayoutParams.MATCH_PARENT);
            params.setMargins(0, 5, 0, 5);

            TableRow componentATableRow = new TableRow(this.context);
            TextView textView = this.headerTextView(this.headers[0]);
            textView.setLayoutParams(params);
            textView.setTextSize(18);
            textView.setTypeface(null, Typeface.BOLD);
            // textView.setPadding(20, 5, 5, 5);
            componentATableRow.addView(textView);

            return componentATableRow;
        }

        TableRow componentBTableRow() {

            TableRow componentBTableRow = new TableRow(this.context);
            int headerFieldCount = this.headers.length;

            TableRow.LayoutParams params = new TableRow.LayoutParams(getCellWidth(), LayoutParams.MATCH_PARENT);
            params.setMargins(0, 5, 0, 5);

            for (int x = 1; x < headerFieldCount; x++) {
                TextView textView = this.headerTextView(this.headers[x]);
                textView.setLayoutParams(params);
                textView.setTextSize(18);
                textView.setTypeface(null, Typeface.BOLD);
                // textView.setPadding(20, 5, 5, 5);
                componentBTableRow.addView(textView);
            }

            return componentBTableRow;
        }

        private void generateTableC_AndTable_D() {

            for (int x = 0; x < this.headerCellsWidth.length; x++) {
                Log.v("TableMainLayout.java", this.headerCellsWidth[x] + "");
            }

            for (int i = 0; i < subjectGroupNameList.size(); i++) {

                TableRow tableRowForTableC = this.tableRowForTableC(subjectGroupNameList.get(i));
                TableRow taleRowForTableD = this.taleRowForTableD(subjectGroupIdList.get(i));

                tableRowForTableC.setBackgroundColor(Color.LTGRAY);
                taleRowForTableD.setBackgroundColor(Color.LTGRAY);

                this.tableC.addView(tableRowForTableC);
                this.tableD.addView(taleRowForTableD);
            }
        }

        TableRow tableRowForTableC(String s) {

            TableRow.LayoutParams params = new TableRow.LayoutParams(getCellWidth(), LayoutParams.MATCH_PARENT);
            params.setMargins(0, 2, 0, 0);

            TableRow tableRowForTableC = new TableRow(this.context);
            TextView textView = this.bodyTextView(s);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(16);
            textView.setPadding(20, 10, 5, 10);
            tableRowForTableC.addView(textView, params);

            return tableRowForTableC;
        }

        TableRow taleRowForTableD(int groupId) {

            TableRow taleRowForTableD = new TableRow(this.context);
            TableRow.LayoutParams params = new TableRow.LayoutParams(getCellWidth(), LayoutParams.MATCH_PARENT);
            params.setMargins(2, 2, 0, 0);

            subjectIdList.clear();
            subjectIdList = SubjectGroupDao.getSubjectIdsInGroup(sqliteDatabase, groupId);
            StringBuilder sb = new StringBuilder();
            for (Integer ids : subjectIdList) {
                sb.append(ids + ",");
            }
            subjectNameList.clear();
            subjectNameList = SubjectsDao.getSubjectNameList(sqliteDatabase, sb.substring(0, sb.length() - 1));

            //subjectGroupMap.put(groupId, subjectIdList);

            //printMap(subjectGroupMap);

            final RadioButton[] rb = new RadioButton[subjectIdList.size()];
            RadioGroup rg = new RadioGroup(this.getContext());
            rg.setGravity(Gravity.CENTER_VERTICAL);
            rg.setBackgroundColor(Color.WHITE);
            rg.setPadding(20, 5, 0, 5);
            rg.setTag(groupId);
            // rg.setBackgroundResource(R.drawable.radio_border);
            rg.setOrientation(RadioGroup.VERTICAL);
            for (int j = 0; j < subjectNameList.size(); j++) {
                rb[j] = new RadioButton(this.getContext());
                rb[j].setGravity(Gravity.CENTER_VERTICAL);
                rb[j].setPadding(5, 10, 0, 10);
                rb[j].setTag(subjectIdList.get(j));
                rb[j].setText(subjectNameList.get(j));
                //rb[l].setId(l + 100);
                rb[j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int id = (Integer) v.getTag();
                        if (!selectedSubjectId.contains(id)) {
                            if (((RadioGroup) v.getParent()).getChildCount() == 1) {
                                selectedSubjectId.add(id);
                            } else {
                                //List<Integer> idList  = subjectGroupMap.get((Integer)((RadioGroup) v.getParent()).getTag());
                                // printMap(subjectGroupMap);
                                List<Integer> idList = SubjectGroupDao.getSubjectIdsInGroup(sqliteDatabase, (Integer) ((RadioGroup) v.getParent()).getTag());
                                for (Integer ids : idList) {
                                    selectedSubjectId.remove(ids);
                                }
                                selectedSubjectId.add(id);
                            }
                        }
                        if (selectedSubjectId.size() == subjectGroupIdList.size()) {
                            mapSubjectBtn.setActivated(true);
                        }
                    }
                });
                rg.addView(rb[j]);
            }
            taleRowForTableD.addView(rg, params);

            return taleRowForTableD;

        }

        int getCellWidth() {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int width = (displayMetrics.widthPixels) / 2;
            return width;
        }

        TextView bodyTextView(String label) {
            TextView bodyTextView = new TextView(this.context);
            bodyTextView.setBackgroundColor(Color.WHITE);
            bodyTextView.setText(label);
            bodyTextView.setGravity(Gravity.CENTER);
            return bodyTextView;
        }

        TextView headerTextView(String label) {
            TextView headerTextView = new TextView(this.context);
            //   headerTextView.setBackgroundColor(Color.WHITE);
            headerTextView.setText(label);
            headerTextView.setGravity(Gravity.CENTER);
            return headerTextView;
        }

        // resizing TableRow height starts here
        void resizeHeaderHeight() {

            TableRow productNameHeaderTableRow = (TableRow) this.tableA.getChildAt(0);
            TableRow productInfoTableRow = (TableRow) this.tableB.getChildAt(0);

            int rowAHeight = this.viewHeight(productNameHeaderTableRow);
            int rowBHeight = this.viewHeight(productInfoTableRow);

            TableRow tableRow = rowAHeight < rowBHeight ? productNameHeaderTableRow : productInfoTableRow;
            int finalHeight = rowAHeight > rowBHeight ? rowAHeight : rowBHeight;

            this.matchLayoutHeight(tableRow, finalHeight);
        }

        void getTableRowHeaderCellWidth() {

            int tableAChildCount = ((TableRow) this.tableA.getChildAt(0)).getChildCount();
            int tableBChildCount = ((TableRow) this.tableB.getChildAt(0)).getChildCount();

            for (int x = 0; x < (tableAChildCount + tableBChildCount); x++) {

                if (x == 0) {
                    this.headerCellsWidth[x] = this.viewWidth(((TableRow) this.tableA.getChildAt(0)).getChildAt(x));
                } else {
                    this.headerCellsWidth[x] = this.viewWidth(((TableRow) this.tableB.getChildAt(0)).getChildAt(x - 1));
                }

            }
        }

        void resizeBodyTableRowHeight() {

            int tableC_ChildCount = this.tableC.getChildCount();

            for (int x = 0; x < tableC_ChildCount; x++) {

                TableRow productNameHeaderTableRow = (TableRow) this.tableC.getChildAt(x);
                TableRow productInfoTableRow = (TableRow) this.tableD.getChildAt(x);

                int rowAHeight = this.viewHeight(productNameHeaderTableRow);
                int rowBHeight = this.viewHeight(productInfoTableRow);

                TableRow tableRow = rowAHeight < rowBHeight ? productNameHeaderTableRow : productInfoTableRow;
                int finalHeight = rowAHeight > rowBHeight ? rowAHeight : rowBHeight;

                this.matchLayoutHeight(tableRow, finalHeight);
            }

        }

        // match all height in a table row
        // to make a standard TableRow height
        private void matchLayoutHeight(TableRow tableRow, int height) {

            int tableRowChildCount = tableRow.getChildCount();

            // if a TableRow has only 1 child
            if (tableRow.getChildCount() == 1) {

                View view = tableRow.getChildAt(0);
                TableRow.LayoutParams params = (TableRow.LayoutParams) view.getLayoutParams();
                params.height = height - (params.bottomMargin + params.topMargin);

                return;
            }

            // if a TableRow has more than 1 child
            for (int x = 0; x < tableRowChildCount; x++) {

                View view = tableRow.getChildAt(x);

                TableRow.LayoutParams params = (TableRow.LayoutParams) view.getLayoutParams();

                if (!isTheHeighestLayout(tableRow, x)) {
                    params.height = height - (params.bottomMargin + params.topMargin);
                    return;
                }
            }

        }

        // check if the view has the highest height in a TableRow
        private boolean isTheHeighestLayout(TableRow tableRow, int layoutPosition) {

            int tableRowChildCount = tableRow.getChildCount();
            int heighestViewPosition = -1;
            int viewHeight = 0;

            for (int x = 0; x < tableRowChildCount; x++) {
                View view = tableRow.getChildAt(x);
                int height = this.viewHeight(view);

                if (viewHeight < height) {
                    heighestViewPosition = x;
                    viewHeight = height;
                }
            }

            return heighestViewPosition == layoutPosition;
        }

        // read a view's height
        private int viewHeight(View view) {
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            return view.getMeasuredHeight();
        }

        // read a view's width
        private int viewWidth(View view) {
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            return view.getMeasuredWidth();
        }

        class MyScrollView extends ScrollView {
            public MyScrollView(Context context) {
                super(context);
            }

            @Override
            protected void onScrollChanged(int l, int t, int oldl, int oldt) {

                String tag = (String) this.getTag();

                if (tag.equalsIgnoreCase("scroll view c")) {
                    scrollViewD.scrollTo(0, t);
                } else {
                    scrollViewC.scrollTo(0, t);
                }
            }
        }

    }

}
