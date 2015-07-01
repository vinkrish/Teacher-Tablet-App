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


public class InternetBlock extends BaseActivity {
    private SharedPreferences sharedPref;
    private int ignoreCount;
    private LinearLayout ignoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_internet_block);
        ignoreText = (LinearLayout)findViewById(R.id.ignore_text);
        sharedPref = this.getSharedPreferences("internet_access", Context.MODE_PRIVATE);

        Calendar calendar1 = Calendar.getInstance();
        int hour1 = calendar1.get(Calendar.HOUR_OF_DAY);
        if(hour1>=9 && hour1<=18) {
            ignoreCount = sharedPref.getInt("ignore_count", 0);
            if (ignoreCount >= 2) {
                findViewById(R.id.ignore).setVisibility(View.INVISIBLE);
                findViewById(R.id.steps_but).setVisibility(View.GONE);
                ignoreText.setVisibility(View.VISIBLE);
            }
        }
    }

    public void stepsClicked(View view){
        ignoreText.setVisibility(view.VISIBLE);
    }

    public void ignoreClicked(View view){
        SharedPreferences.Editor editor = sharedPref.edit();
        Calendar calendar2 = Calendar.getInstance();
        int hour2 = calendar2.get(Calendar.HOUR_OF_DAY);
        if(hour2>=9 && hour2<=18){
            ignoreCount++;
            editor.putInt("ignore_count", ignoreCount);
            editor.putInt("ignore_status", 1);
            editor.apply();
            Intent intent = new Intent(this, in.teacher.activity.LoginActivity.class);
            startActivity(intent);
        }else{
            editor.putInt("ignore_count", 0);
            editor.putInt("ignore_status", 1);
            editor.putInt("i_failed_status", 0);
            editor.putInt("i_failed_count", 0);
            editor.apply();
            Intent intent = new Intent();
            intent.setClassName("in.teacher.activity", "in.teacher.activity.LoginActivity");
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_internet_block, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed(){
    }
}
