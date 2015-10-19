package in.teacher.fragment;

import in.teacher.activity.R;
import in.teacher.activity.R.animator;
import in.teacher.dao.CceCoScholasticGradeDao;
import in.teacher.dao.TempDao;
import in.teacher.model.ExpChild;
import in.teacher.model.ExpGroup;
import in.teacher.sqlite.Temp;
import in.teacher.util.AppGlobal;

import java.util.ArrayList;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by vinkrish.
 */
public class CoScholastic extends Fragment {
    private Context context;
    private ExpandListAdapter ExpAdapter;
    private ArrayList<ExpGroup> ExpListItems;
    private ExpandableListView ExpandList;
    private int childPos, termWise = 0, Term, SecHeadingId, TopicId, AspectId, CoScholasticId;
    private int sectionId, classId;
    private ArrayList<Integer> termList = new ArrayList<>();
    private ArrayList<Integer> secHeadingList = new ArrayList<>();
    private ArrayList<String> secNameList = new ArrayList<>();
    private ArrayList<Integer> topicList = new ArrayList<>();
    private ArrayList<String> topicNameList = new ArrayList<>();
    private ArrayList<Integer> aspectList = new ArrayList<>();
    private ArrayList<String> aspectNameList = new ArrayList<>();
    private SQLiteDatabase sqliteDatabase;
    private int[] child = new int[5];
    private int[] childPosArr = new int[]{-1, -1, -1, -1, -1};

    private SparseArray<ArrayList<Integer>> termMap = new SparseArray<>();
    private SparseArray<ArrayList<Integer>> secMap = new SparseArray<>();
    private SparseArray<ArrayList<Integer>> topicMap = new SparseArray<>();

    private SparseArray<ArrayList<String>> termMaps = new SparseArray<>();
    private SparseArray<ArrayList<String>> secMaps = new SparseArray<>();
    private SparseArray<ArrayList<String>> topicMaps = new SparseArray<>();

    private ArrayList<Integer> termComplete = new ArrayList<>();
    private ArrayList<Integer> secComplete = new ArrayList<>();
    private ArrayList<Integer> topicComplete = new ArrayList<>();

    private SparseIntArray termCompleteMap = new SparseIntArray();
    private SparseIntArray secCompleteMap = new SparseIntArray();
    private SparseIntArray topicCompleteMap = new SparseIntArray();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.coscholastic, container, false);
        context = AppGlobal.getContext();
        sqliteDatabase = AppGlobal.getSqliteDatabase();

        Temp t = TempDao.selectTemp(sqliteDatabase);
        sectionId = t.getSectionId();
        classId = t.getClassId();

        setCoScholasticId();
        Cursor c1 = sqliteDatabase.rawQuery("select distinct Term from exams where ClassId=" + classId, null);
        c1.moveToFirst();
        termList.clear();
        while (!c1.isAfterLast()) {
            termList.add(c1.getInt(c1.getColumnIndex("Term")));
            c1.moveToNext();
        }
        c1.close();
        //	new CalledBackLoad().execute();

        ExpandList = (ExpandableListView) view.findViewById(R.id.expandibles);
        ExpandList.setGroupIndicator(null);
        ExpListItems = SetStandardGroups();
        ExpAdapter = new ExpandListAdapter(context, ExpListItems);
        ExpandList.setAdapter(ExpAdapter);

        ExpandList.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                //	childPos = child[groupPosition-1];
                for (int i = groupPosition; i < 5; i++) {
                    childPosArr[i] = -1;
                }
                if (groupPosition == 1) {
                    if (childPosArr[0] != -1) {
                        groupFirst();
                    }
                } else if (groupPosition == 2) {
                    if (childPosArr[1] != -1) {
                        groupSecond();
                    }
                } else if (groupPosition == 3) {
                    if (childPosArr[2] != -1) {
                        groupThird();
                    }
                } else if (groupPosition == 4) {
                    if (childPosArr[3] != -1) {
                        groupFourth();
                    }
                }

                return false;
            }
        });

        ExpandList.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                //	childPos = childPosition;
                //	v.setBackgroundColor(Color.BLUE);
                //	Log.d("parent-child", ExpGroup.get(groupPosition) +
                //			ExpChild.get(ExpGroup.get(groupPosition)).get(childPosition));
                for (int i = groupPosition; i < 5; i++) {
                    childPosArr[i] = -1;
                }
                if (groupPosition == 0) {
                    childPosArr[0] = childPosition;
                    groupFirst();
                } else if (groupPosition == 1) {
                    childPosArr[1] = childPosition;
                    groupSecond();
                } else if (groupPosition == 2) {
                    childPosArr[2] = childPosition;
                    groupThird();
                } else if (groupPosition == 3) {
                    childPosArr[3] = childPosition;
                    groupFourth();
                } else {
                    childPosArr[4] = childPosition;
                    groupFifth();
                }
                return false;
            }
        });

        return view;
    }

    private void groupFirst() {
        child[0] = childPosArr[0];
        ExpListItems.clear();
        ExpListItems = new ArrayList<>();
        int[] img1 = {R.drawable.cross, R.drawable.tick};
        String[] header = {"Select Evaluation", "Select Term", "Select Section Heading", "Select Topic", "Select Aspect"};
        String[] h = {"Select Evaluation", "Term wise", "Exam wise"};
        ArrayList<ExpChild> ch_list;
        for (int i = 0; i < 5; i++) {
            ch_list = new ArrayList<>();
            if (i == 0) {
                ExpChild ec0 = new ExpChild();
                ec0.setText(h[0]);
                ch_list.add(ec0);
                ExpChild ec1 = new ExpChild();
                ec1.setText(h[1]);
                ch_list.add(ec1);
                ExpChild ec2 = new ExpChild();
                ec2.setText(h[2]);
                ch_list.add(ec2);
            } else if (i == 1) {
                //	if(childPos==1){
                if (childPosArr[0] == 1) {
                    int temp = 0;
                    for (Integer term : termList) {
                        ExpChild e = new ExpChild();
                        if (termCompleteMap.get(termList.get(temp)) == 1) {
                            e.setSelectedChild(1);
                        } else {
                            e.setSelectedChild(0);
                        }
                        e.setText("Term " + term);
                        ch_list.add(e);
                    }
                }
            }
            ExpGroup eg = new ExpGroup();
            if (i == 0) {
                eg.setImage1(img1[1]);
                eg.setText1(h[childPosArr[0]]);
            } else {
                eg.setImage1(img1[0]);
                eg.setText1(header[i]);
            }
            eg.setItems(ch_list);
            ExpListItems.add(eg);
        }
        ExpAdapter = new ExpandListAdapter(context, ExpListItems);
        ExpandList.setAdapter(ExpAdapter);
        //	ExpAdapter.notifyDataSetChanged();
    }

    private void groupSecond() {
        Term = termList.get(childPosArr[1]);
        //	new CalledBackLoad().execute();
        doInBackground();
        child[1] = childPosArr[1];
        ExpListItems.clear();
        ExpListItems = new ArrayList<>();
        int[] img1 = {R.drawable.cross, R.drawable.tick};
        String[] header = {"Select Evaluation", "Select Term", "Select Section Heading", "Select Topic", "Select Aspect"};
        String[] h = {"Select Evaluation", "Term wise", "Exam wise"};
        ArrayList<ExpChild> ch_list;
        for (int i = 0; i < 5; i++) {
            /*for(int a = 0, nsize = termMap.size(); a < nsize; a++) {
			    Log.d("iterate", termMap.valueAt(a)+"");
			}*/
            ch_list = new ArrayList<>();
            if (i == 0) {
                ExpChild ec0 = new ExpChild();
                ec0.setText(h[0]);
                ch_list.add(ec0);
                ExpChild ec1 = new ExpChild();
                ec1.setText(h[1]);
                ch_list.add(ec1);
                ExpChild ec2 = new ExpChild();
                ec2.setText(h[2]);
                ch_list.add(ec2);
            } else if (i == 1) {
                int temp = 0;
                for (Integer term : termList) {
                    ExpChild e = new ExpChild();
                    if (termCompleteMap.get(termList.get(temp)) == 1) {
                        e.setSelectedChild(1);
                    } else {
                        e.setSelectedChild(0);
                    }
                    e.setText("Term " + term);
                    ch_list.add(e);
                }
            } else if (i == 2) {
                secHeadingList = termMap.get(Term);
                secNameList = termMaps.get(Term);
				/*for(int intt = 0, nsize = termMap.size(); intt < nsize; intt++) {
					if(Term==termMap.keyAt(intt)){
						secHeadingList = termMap.valueAt(intt);
						secNameList = termMaps.valueAt(intt);
					}
				}*/
                int temp = 0;
                for (String secName : secNameList) {
                    ExpChild e = new ExpChild();
                    if (secCompleteMap.get(secHeadingList.get(temp)) == 1) {
                        e.setSelectedChild(1);
                    } else {
                        e.setSelectedChild(0);
                    }
                    e.setText(secName);
                    ch_list.add(e);
                    temp += 1;
                }
            }
            ExpGroup eg = new ExpGroup();
            if (i == 0) {
                eg.setImage1(img1[1]);
                eg.setText1(h[child[0]]);
            } else if (i == 1) {
                eg.setImage1(img1[1]);
                eg.setText1("Term " + termList.get(childPosArr[1]));
            } else {
                eg.setImage1(img1[0]);
                eg.setText1(header[i]);
            }
            eg.setItems(ch_list);
            ExpListItems.add(eg);
        }
        ExpAdapter = new ExpandListAdapter(context, ExpListItems);
        ExpandList.setAdapter(ExpAdapter);
        //	ExpAdapter.notifyDataSetChanged();

    }

    private void groupThird() {
        SecHeadingId = secHeadingList.get(childPosArr[2]);
        child[2] = childPosArr[2];
        ExpListItems.clear();
        ExpListItems = new ArrayList<>();
        int[] img1 = {R.drawable.cross, R.drawable.tick};
        String[] header = {"Select Evaluation", "Select Term", "Select Section Heading", "Select Topic", "Select Aspect"};
        String[] h = {"Select Evaluation", "Term wise", "Exam wise"};
        ArrayList<ExpChild> ch_list;
        for (int i = 0; i < 5; i++) {
            ch_list = new ArrayList<>();
            if (i == 0) {
                ExpChild ec0 = new ExpChild();
                ec0.setText(h[0]);
                ch_list.add(ec0);
                ExpChild ec1 = new ExpChild();
                ec1.setText(h[1]);
                ch_list.add(ec1);
                ExpChild ec2 = new ExpChild();
                ec2.setText(h[2]);
                ch_list.add(ec2);
            } else if (i == 1) {
                int temp = 0;
                for (Integer term : termList) {
                    ExpChild e = new ExpChild();
                    if (termCompleteMap.get(termList.get(temp)) == 1) {
                        e.setSelectedChild(1);
                    } else {
                        e.setSelectedChild(0);
                    }
                    e.setText("Term " + term);
                    ch_list.add(e);
                }
            } else if (i == 2) {
                secHeadingList = termMap.get(Term);
                secNameList = termMaps.get(Term);
                int temp = 0;
                for (String secName : secNameList) {
                    ExpChild e = new ExpChild();
                    if (secCompleteMap.get(secHeadingList.get(temp)) == 1) {
                        e.setSelectedChild(1);
                    } else {
                        e.setSelectedChild(0);
                    }
                    e.setText(secName);
                    ch_list.add(e);
                    temp += 1;
                }
            } else if (i == 3) {
                topicList = secMap.get(SecHeadingId);
                topicNameList = secMaps.get(SecHeadingId);
                int temp = 0;
                for (String topicName : topicNameList) {
                    ExpChild e = new ExpChild();
                    if (topicCompleteMap.get(topicList.get(temp)) == 1) {
                        e.setSelectedChild(1);
                    } else {
                        e.setSelectedChild(0);
                    }
                    e.setText(topicName);
                    ch_list.add(e);
                    temp += 1;
                }
            }
            ExpGroup eg = new ExpGroup();
            if (i == 0) {
                eg.setImage1(img1[1]);
                eg.setText1(h[child[0]]);
            } else if (i == 1) {
                eg.setImage1(img1[1]);
                eg.setText1("Term " + termList.get(child[1]));
            } else if (i == 2) {
                eg.setImage1(img1[1]);
                eg.setText1(secNameList.get(childPosArr[2]));
            } else {
                eg.setImage1(img1[0]);
                eg.setText1(header[i]);
            }
            eg.setItems(ch_list);
            ExpListItems.add(eg);
        }
        ExpAdapter = new ExpandListAdapter(context, ExpListItems);
        ExpandList.setAdapter(ExpAdapter);
        //	ExpAdapter.notifyDataSetChanged();
    }

    private void groupFourth() {
        TopicId = topicList.get(childPosArr[3]);
        child[3] = childPosArr[3];
        ExpListItems.clear();
        ExpListItems = new ArrayList<>();
        int[] img1 = {R.drawable.cross, R.drawable.tick};
        String[] header = {"Select Evaluation", "Select Term", "Select Section Heading", "Select Topic", "Select Aspect"};
        String[] h = {"Select Evaluation", "Term wise", "Exam wise"};
        ArrayList<ExpChild> ch_list;
        for (int i = 0; i < 5; i++) {
            ch_list = new ArrayList<>();
            if (i == 0) {
                ExpChild ec0 = new ExpChild();
                ec0.setText(h[0]);
                ch_list.add(ec0);
                ExpChild ec1 = new ExpChild();
                ec1.setText(h[1]);
                ch_list.add(ec1);
                ExpChild ec2 = new ExpChild();
                ec2.setText(h[2]);
                ch_list.add(ec2);
            } else if (i == 1) {
                int temp = 0;
                for (Integer term : termList) {
                    ExpChild e = new ExpChild();
                    if (termCompleteMap.get(termList.get(temp)) == 1) {
                        e.setSelectedChild(1);
                    } else {
                        e.setSelectedChild(0);
                    }
                    e.setText("Term " + term);
                    ch_list.add(e);
                }
            } else if (i == 2) {
                secHeadingList = termMap.get(Term);
                secNameList = termMaps.get(Term);
                int temp = 0;
                for (String secName : secNameList) {
                    ExpChild e = new ExpChild();
                    if (secCompleteMap.get(secHeadingList.get(temp)) == 1) {
                        e.setSelectedChild(1);
                    } else {
                        e.setSelectedChild(0);
                    }
                    e.setText(secName);
                    ch_list.add(e);
                    temp += 1;
                }
            } else if (i == 3) {
				/*for(int a = 0, nsize = topicCompleteMap.size(); a < nsize; a++) {
					Log.d("iterate", topicCompleteMap.valueAt(a)+"");
				}*/
                topicList = secMap.get(SecHeadingId);
                topicNameList = secMaps.get(SecHeadingId);
                int temp = 0;
                for (String topicName : topicNameList) {
                    ExpChild e = new ExpChild();
                    if (topicCompleteMap.get(topicList.get(temp)) == 1) {
                        e.setSelectedChild(1);
                    } else {
                        e.setSelectedChild(0);
                    }
                    e.setText(topicName);
                    ch_list.add(e);
                    temp += 1;
                }
            } else if (i == 4) {
                Cursor c = sqliteDatabase.rawQuery("select * from cceaspectprimary where TopicId=" + TopicId +" ORDER BY CAST(strftime('%s', DateTimeRecordInserted) AS INT)", null);
                c.moveToFirst();
                aspectList.clear();
                aspectNameList.clear();
                while (!c.isAfterLast()) {
                    aspectList.add(c.getInt(c.getColumnIndex("AspectId")));
                    aspectNameList.add(c.getString(c.getColumnIndex("AspectName")));
                    c.moveToNext();
                }
                c.close();
                int temp = 0;
                for (String aspectName : aspectNameList) {
                    ExpChild e = new ExpChild();
                    if (CceCoScholasticGradeDao.isThereCoSchGrade(aspectList.get(temp), Term, sectionId, sqliteDatabase) == 1) {
                        e.setSelectedChild(1);
                    } else {
                        e.setSelectedChild(0);
                    }
                    e.setText(aspectName);
                    ch_list.add(e);
                    temp += 1;
                }
            }
            ExpGroup eg = new ExpGroup();
            if (i == 0) {
                eg.setImage1(img1[1]);
                eg.setText1(h[child[0]]);
            } else if (i == 1) {
                eg.setImage1(img1[1]);
                eg.setText1("Term " + termList.get(child[1]));
            } else if (i == 2) {
                eg.setImage1(img1[1]);
                eg.setText1(secNameList.get(child[2]));
            } else if (i == 3) {
                eg.setImage1(img1[1]);
                eg.setText1(topicNameList.get(childPosArr[3]));
            } else {
                eg.setImage1(img1[0]);
                eg.setText1(header[i]);
            }
            eg.setItems(ch_list);
            ExpListItems.add(eg);
        }
        ExpAdapter = new ExpandListAdapter(context, ExpListItems);
        ExpandList.setAdapter(ExpAdapter);
        //	ExpAdapter.notifyDataSetChanged();
    }

    private void groupFifth() {
        AspectId = aspectList.get(childPosArr[4]);
        int isPresent = CceCoScholasticGradeDao.isThereCoSchGrade(AspectId, Term, sectionId, sqliteDatabase);
        if (isPresent == 1) {
            Bundle b = new Bundle();
            b.putInt("Term", Term);
            b.putInt("TopicId", TopicId);
            b.putInt("AspectId", AspectId);

            Fragment fragment = new UpdateCCSGrade();
            fragment.setArguments(b);
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(animator.fade_in, animator.fade_out)
                    .replace(R.id.content_frame, fragment).addToBackStack(null).commit();
        } else {
            Bundle b = new Bundle();
            b.putInt("Term", Term);
            b.putInt("TopicId", TopicId);
            b.putInt("AspectId", AspectId);

            Fragment fragment = new InsertCoSchGrade();
            fragment.setArguments(b);
            getFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(animator.fade_in, animator.fade_out)
                    .replace(R.id.content_frame, fragment).addToBackStack(null).commit();
        }
    }

    private void setCoScholasticId() {
        boolean found = false;
        Cursor c = sqliteDatabase.rawQuery("select CoScholasticId, ClassIDs from ccecoscholastic", null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            String[] sArray = c.getString(c.getColumnIndex("ClassIDs")).split(",");
            for (String str : sArray) {
                if (str.equals(classId + "")) {
                    CoScholasticId = c.getInt(c.getColumnIndex("CoScholasticId"));
                    found = true;
                    break;
                }
            }
            if (found) {
                c.moveToLast();
            }
            c.moveToNext();
        }
        c.close();
    }

    private ArrayList<ExpGroup> SetStandardGroups() {
        int[] img1 = {R.drawable.cross, R.drawable.tick};
        String[] header = {"Select Evaluation", "Select Term", "Select Section Heading", "Select Topic", "Select Aspect"};
        ArrayList<ExpGroup> grp_list = new ArrayList<>();
        ArrayList<ExpChild> ch_list;
        for (int i = 0; i < 5; i++) {
            ch_list = new ArrayList<>();
            if (i == 0) {
                ExpChild ec0 = new ExpChild();
                ec0.setText("Select Evaluation");
                ch_list.add(ec0);
                ExpChild ec1 = new ExpChild();
                ec1.setText("Term wise");
                ch_list.add(ec1);
                ExpChild ec2 = new ExpChild();
                ec2.setText("Exam wise");
                ch_list.add(ec2);
            }
            ExpGroup eg = new ExpGroup();
            eg.setImage1(img1[0]);
            eg.setText1(header[i]);
            eg.setItems(ch_list);
            grp_list.add(eg);
        }
        return grp_list;
    }

    public class ExpandListAdapter extends BaseExpandableListAdapter {
        private ArrayList<ExpGroup> groups;
        private LayoutInflater inflater;

        public ExpandListAdapter(Context context, ArrayList<ExpGroup> groups) {
            this.groups = groups;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            ArrayList<ExpChild> chList = groups.get(groupPosition).getItems();
            return chList.get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            ExpChild child = (ExpChild) getChild(groupPosition, childPosition);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.child_item, null);
            }
            TextView tv = (TextView) convertView.findViewById(R.id.childText1);
            tv.setText(child.getText().toString());

            if (child.getSelectedChild() == 1) {
                //	tv.setSelected(true);
                tv.setTextColor(getResources().getColor(R.color.green));
                //	tv.setBackgroundColor(getResources().getColor(R.color.green));
            }

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            ArrayList<ExpChild> chList = groups.get(groupPosition).getItems();
            return chList.size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groups.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return groups.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            ExpGroup group = (ExpGroup) getGroup(groupPosition);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.group_item, null);
            }

            ImageView iv1 = (ImageView) convertView.findViewById(R.id.img1);
            iv1.setImageResource(group.getImage1());
            TextView tv = (TextView) convertView.findViewById(R.id.text1);
            tv.setText(group.getText1());
            ImageView iv2 = (ImageView) convertView.findViewById(R.id.img2);

            if (isExpanded && getChildrenCount(groupPosition) > 0) {
                convertView.setPadding(0, 0, 0, 0);
                //	iv2.setImageResource(R.drawable.tick);
            } else {
                convertView.setPadding(0, 0, 0, 20);
                //	iv2.setImageResource(R.drawable.cross);
            }

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }


    private void doInBackground() {

		/*Cursor c1 = sqliteDatabase.rawQuery("select distinct Term from exams where ClassId="+classId, null);
			c1.moveToFirst();
			termList.clear();
			while(!c1.isAfterLast()){
				termList.add(c1.getInt(c1.getColumnIndex("Term")));
				c1.moveToNext();
			}
			c1.close();*/

        //	for(Integer t: termList){
        Cursor c2 = sqliteDatabase.rawQuery("select * from ccesectionheading where CoScholasticId=" + CoScholasticId + " ORDER BY CAST(strftime('%s', DateTimeRecordInserted) AS INT)", null);
        c2.moveToFirst();
        secHeadingList.clear();
        secNameList.clear();
        while (!c2.isAfterLast()) {
            secHeadingList.add(c2.getInt(c2.getColumnIndex("SectionHeadingId")));
            secNameList.add(c2.getString(c2.getColumnIndex("SectionName")));
            c2.moveToNext();
        }
        c2.close();

        for (Integer s : secHeadingList) {
            Cursor c3 = sqliteDatabase.rawQuery("select * from ccetopicprimary where SectionHeadingId=" + s + " ORDER BY CAST(strftime('%s', DateTimeRecordInserted) AS INT)", null);
            c3.moveToFirst();
            topicList.clear();
            topicNameList.clear();
            while (!c3.isAfterLast()) {
                topicList.add(c3.getInt(c3.getColumnIndex("TopicId")));
                topicNameList.add(c3.getString(c3.getColumnIndex("TopicName")));
                c3.moveToNext();
            }
            c3.close();

            for (Integer top : topicList) {
                Cursor c4 = sqliteDatabase.rawQuery("select * from cceaspectprimary where TopicId=" + top + " ORDER BY CAST(strftime('%s', DateTimeRecordInserted) AS INT)", null);
                c4.moveToFirst();
                aspectList.clear();
                aspectNameList.clear();
                while (!c4.isAfterLast()) {
                    aspectList.add(c4.getInt(c4.getColumnIndex("AspectId")));
                    aspectNameList.add(c4.getString(c4.getColumnIndex("AspectName")));
                    c4.moveToNext();
                }
                c4.close();

                int topComp = 0;
                for (Integer a : aspectList) {
                    if (CceCoScholasticGradeDao.isThereCoSchGrade(a, Term, sectionId, sqliteDatabase) == 1) {
                        topComp = 1;
                    } else {
                        topComp = 0;
                    }
                }
                topicMap.put(top, new ArrayList<>(aspectList));
                topicMaps.put(top, new ArrayList<>(aspectNameList));

				/*for (int loo=0; loo<topicMap.size(); loo++)
						{
							int key = topicMap.keyAt(loo);
							Log.d("topic_key", key+"");
							Log.d("topic_value", topicMap.get(key)+"");
						}*/
                topicCompleteMap.put(top, topComp);
                topicComplete.add(topComp);
            }
            int secComp = 0;
            secMap.put(s, new ArrayList<>(topicList));
            secMaps.put(s, new ArrayList<>(topicNameList));

            if (topicComplete.contains(0)) {
                secComplete.add(0);
            } else {
                secComp = 1;
                secComplete.add(1);
            }
            secCompleteMap.put(s, secComp);
            secComplete.add(secComp);
        }
        int termComp = 0;
        termMap.put(Term, new ArrayList<>(secHeadingList));
        //	termMaps.put(Term, new ArrayList<String>(secNameList));
        termMaps.put(Term, secNameList);

        if (secComplete.contains(0)) {
            termComplete.add(0);
        } else {
            termComp = 1;
            termComplete.add(1);
        }
        termCompleteMap.put(Term, termComp);
        termComplete.add(termComp);
        //	}
    }
}
