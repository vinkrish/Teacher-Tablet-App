package in.teacher.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

public class UpdateApk extends BaseActivity {
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_apk);

        sharedPref = this.getSharedPreferences("db_access", Context.MODE_PRIVATE);
        int apkUpdate = sharedPref.getInt("apk_update", 0);
        String apkName = sharedPref.getString("apk_name", "teacher");

        if(apkUpdate == 2){

        }

    }

    public void updateClicked(View v){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("apk_update", 1);
        editor.apply();
    }



}
