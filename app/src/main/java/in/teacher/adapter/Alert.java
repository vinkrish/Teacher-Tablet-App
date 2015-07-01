package in.teacher.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class Alert {
	Activity activity;

	public Alert(Activity activity){
		this.activity = activity;
	}

	public void showAlert(String message){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
		alertDialog.setTitle("Notification");
		alertDialog.setMessage(message);
		alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				dialog.dismiss();
			}
		});
		alertDialog.show();
	}

}
