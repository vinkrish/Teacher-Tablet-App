package in.teacher.activity;

import in.teacher.sync.FirstTimeSync;
import in.teacher.util.NetworkUtils;
import in.teacher.util.SharedPreferenceUtil;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class ServerBlock extends BaseActivity {
	private Button butResolve;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_server_block);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		butResolve = (Button)findViewById(R.id.butResolve);
		resolveClicked(butResolve);
	}
	
	public void resolveClicked(View view){
		SharedPreferenceUtil.updateFirstSync(this, 1);
		if(NetworkUtils.isNetworkConnected(ServerBlock.this)){
			new FirstTimeSync().callFirstTimeSync();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.server_block, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		SharedPreferenceUtil.updateFirstSync(this, 0);
	}
	
	@Override
	public void onBackPressed(){}
}
