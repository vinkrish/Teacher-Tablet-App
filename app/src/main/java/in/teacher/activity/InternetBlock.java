package in.teacher.activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import in.teacher.util.AppGlobal;
import in.teacher.util.SharedPreferenceUtil;

/**
 * Created by vinkrish.
 */
public class InternetBlock extends BaseActivity {
    private int ignoreCount;
    private LinearLayout ignoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_block);

        ignoreText = (LinearLayout) findViewById(R.id.ignore_text);

        Calendar calendar1 = Calendar.getInstance();
        int hour1 = calendar1.get(Calendar.HOUR_OF_DAY);
        if (hour1 >= 9 && hour1 <= 18) {
            ignoreCount = SharedPreferenceUtil.getIgnoreCount(this);
            if (ignoreCount >= 2) {
                findViewById(R.id.ignore).setVisibility(View.INVISIBLE);
                findViewById(R.id.steps_but).setVisibility(View.GONE);
                ignoreText.setVisibility(View.VISIBLE);
            }
        }
    }

    public void stepsClicked(View view) {
        ignoreText.setVisibility(view.VISIBLE);
    }

    public void ignoreClicked(View view) {
        ignoreCount++;
        SharedPreferenceUtil.updateStatusCountIgnore(this, 0, 0, ignoreCount);
        Intent intent = new Intent(this, in.teacher.activity.LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
    }
}
