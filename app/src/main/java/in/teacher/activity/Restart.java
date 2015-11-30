package in.teacher.activity;

import in.teacher.sync.UploadError;
import in.teacher.util.NetworkUtils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by vinkrish.
 */
public class Restart extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restart);

        Intent i = getIntent();
        String s = i.getStringExtra("error");
        Log.d("error message", s);

        SharedPreferences sharedPref = this.getSharedPreferences("db_access", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("is_sync", 0);
        editor.putInt("first_sync", 0);
        editor.putInt("sleep_sync", 0);
        editor.apply();

        if (NetworkUtils.isNetworkConnected(this)) {
            new UploadError(this, s).upError();
        } else {
            Intent intent = new Intent(this, in.teacher.activity.LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
    }
}
