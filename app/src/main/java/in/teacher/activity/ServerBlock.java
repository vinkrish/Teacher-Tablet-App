package in.teacher.activity;

import in.teacher.sync.FirstTimeSync;
import in.teacher.util.NetworkUtils;
import in.teacher.util.SharedPreferenceUtil;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

/**
 * Created by vinkrish.
 */
public class ServerBlock extends BaseActivity {
    private Button butResolve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_block);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        butResolve = (Button) findViewById(R.id.butResolve);
        resolveClicked(butResolve);
    }

    public void resolveClicked(View view) {
        SharedPreferenceUtil.updateFirstSync(this, 1);
        if (NetworkUtils.isNetworkConnected(ServerBlock.this)) {
            new FirstTimeSync().callFirstTimeSync();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferenceUtil.updateFirstSync(this, 0);
    }

    @Override
    public void onBackPressed() {
    }
}
