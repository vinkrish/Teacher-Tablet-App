package in.teacher.util;

import in.teacher.activity.R;
import in.teacher.activity.R.animator;
import android.app.Fragment;
import android.app.FragmentManager;

public class ReplaceFragment {

	public static void replace(Fragment f, FragmentManager fm){
		fm
		.beginTransaction()
		.setCustomAnimations(animator.fade_in,animator.fade_out)
		.replace(R.id.content_frame, f).addToBackStack(null).commit();
	}
	
	public static void replaceNoBackStack(Fragment f, FragmentManager fm){
		fm
		.beginTransaction()
		.setCustomAnimations(animator.fade_in,animator.fade_out)
		.replace(R.id.content_frame, f).commit();
	}
	
	public static void clearBackStack(FragmentManager fm) {
	    if (fm.getBackStackEntryCount() > 0) {
	        FragmentManager.BackStackEntry first = fm.getBackStackEntryAt(0);
	         fm.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
	    }
	}
}
