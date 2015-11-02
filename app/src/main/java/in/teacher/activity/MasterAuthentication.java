package in.teacher.activity;

import in.teacher.sync.FirstTimeSync;
import in.teacher.util.AnimationUtils;
import in.teacher.util.AppGlobal;
import in.teacher.util.CommonDialogUtils;
import in.teacher.util.ExceptionHandler;
import in.teacher.util.NetworkUtils;
import in.teacher.util.SharedPreferenceUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by vinkrish.
 */

public class MasterAuthentication extends BaseActivity {
    private TextView adminUser, adminPass, deviceId;
    private boolean tvflag, authflag;
    private Context context;
    private String passwordText;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = AppGlobal.getContext();

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_master_authentication);
        sharedPref = this.getSharedPreferences("db_access", Context.MODE_PRIVATE);

        init();

        int newlyUpdated = sharedPref.getInt("newly_updated", 0);
        if (newlyUpdated == 1) {
            authSuccess();
        }
    }

    private void init() {
        adminUser = (TextView) findViewById(R.id.adminUserName);
        adminPass = (TextView) findViewById(R.id.adminPassword);
        deviceId = (TextView) findViewById(R.id.deviceId);

        String android_id = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
        deviceId.setText(android_id);

        initializeButton();
    }

    private void initializeButton() {
        int[] buttonIds = {R.id.num1, R.id.num2, R.id.num3, R.id.num4, R.id.num5, R.id.num6, R.id.num7, R.id.num8, R.id.num9, R.id.num0};
        for (int i = 0; i < 10; i++) {
            Button b = (Button) findViewById(buttonIds[i]);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (adminUser.getText().toString().equals("Username")) {
                        adminUser.setText("");
                    } else if (adminPass.getText().toString().equals("Password") && tvflag) {
                        adminPass.setText("");
                    }
                    if (adminUser.getText().toString().equalsIgnoreCase("|")) {
                        adminUser.setText("");
                        adminPass.setHint("Password");
                    } else if (adminPass.getText().toString().equalsIgnoreCase("|")) {
                        adminPass.setText("");
                        passwordText = "";
                    }

                    if (v.getId() == R.id.num1) {
                        updateFields("1");
                    }
                    if (v.getId() == R.id.num2) {
                        updateFields("2");
                    }
                    if (v.getId() == R.id.num3) {
                        updateFields("3");
                    }
                    if (v.getId() == R.id.num4) {
                        updateFields("4");
                    }
                    if (v.getId() == R.id.num5) {
                        updateFields("5");
                    }
                    if (v.getId() == R.id.num6) {
                        updateFields("6");
                    }
                    if (v.getId() == R.id.num7) {
                        updateFields("7");
                    }
                    if (v.getId() == R.id.num8) {
                        updateFields("8");
                    }
                    if (v.getId() == R.id.num9) {
                        updateFields("9");
                    }
                    if (v.getId() == R.id.num0) {
                        updateFields("0");
                    }
                }
            });
        }
    }

    public void nameClicked(View view) {
        if (adminPass.getText().toString().equalsIgnoreCase("|")) {
            adminPass.setText("");
            adminPass.setHint("Password");
        }
        adminUser.setText("|");
        tvflag = false;
    }

    public void passwordClicked(View view) {
        if (adminUser.getText().toString().equalsIgnoreCase("|")) {
            adminUser.setText("");
            adminUser.setHint("Username");
        }
        adminPass.setText("|");
        tvflag = true;
    }

    public void clearClicked(View view) {
        if (tvflag) {
            adminPass.setText("|");
            if (adminUser.getText().toString().equalsIgnoreCase("|")) {
                adminUser.setText("");
                adminUser.setHint("Username");
            }
        } else {
            adminUser.setText("|");
            if (adminPass.getText().toString().equalsIgnoreCase("|")) {
                adminPass.setText("");
                adminPass.setHint("Password");
            }
        }
    }

    private void updateFields(String value) {
        if (tvflag) {
            adminPass.setText(new StringBuffer(adminPass.getText()).append("*"));
            String sb = passwordText + value;
            passwordText = sb;
            if (passwordText.length() == 5)
                authenticate();
        } else {
            String s = adminUser.getText().toString();
            StringBuffer sb = new StringBuffer(s);
            sb.append(value);
            adminUser.setText(sb);
            String s2 = adminUser.getText().toString();
            if (s2.length() == 5)
                preauthenticate();
        }
    }

    private void preauthenticate() {
        adminPass.setText("|");
        tvflag = true;
    }

    private void authenticate() {
        String s = adminUser.getText().toString();
        if (s.isEmpty()) {
            authflag = false;
        } else {
            String enteredId = adminUser.getText().toString();
            if (enteredId.equals("11111") && passwordText.equals("11111")) {
                authflag = true;
                authSuccess();
            }
        }
        if (!authflag) {
            CommonDialogUtils.displayAlertWhiteDialog(this, "Admin is not Authenticated");
        }

        adminUser.setText("Username");
        adminPass.setText("Password");
        tvflag = false;
    }

    private void authSuccess() {
        if (NetworkUtils.isNetworkConnected(context)) {
            setContentView(R.layout.sync);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt("first_sync", 1);
            editor.apply();
            new FirstTimeSync().callFirstTimeSync();
        } else {
            CommonDialogUtils.displayAlertWhiteDialog(this, "Check Wifi");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferenceUtil.updateFirstSync(this, 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AnimationUtils.activityExit(MasterAuthentication.this);
    }

}
