package in.teacher.util;

import in.teacher.activity.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;

/**
 * Created by vinkrish.
 */

public class AnimationUtils {

    public static void activityExit(Activity activity) {
        activity.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    public static void activityEnter(Activity activity) {
        activity.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    public static void activityExitVertical(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_top_middle_full, R.anim.slide_middle_bottom_full);
    }

    public static void activityEnterVertical(Activity activity) {
        activity.overridePendingTransition(R.anim.slide_bottom_middle_full, R.anim.slide_middle_top_full);
    }

    public static void alphaTranslate(View view, Context context) {
        AnimationSet animationSet = new AnimationSet(context , null);
        TranslateAnimation translateAnimation = new TranslateAnimation(0, 0, view.getHeight(), 0);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0f, 1f);
        animationSet.addAnimation(translateAnimation);
        animationSet.addAnimation(alphaAnimation);
        animationSet.setDuration(700);
        view.startAnimation(animationSet);
    }

    public static void animateViewFly(View viewToAnimate, float fromX, float toX,
                                           float fromY, float toY, int typeRelative, int animationDuration,
                                           int animationRepeatCount) {
        Animation animation = new TranslateAnimation(typeRelative, fromX, typeRelative, toX, typeRelative, fromY, typeRelative, toY);
        animation.setDuration(animationDuration);
        animation.setRepeatCount(animationRepeatCount);
        animation.setInterpolator(new LinearInterpolator());
        viewToAnimate.startAnimation(animation);
    }

    // convert dp to px
    public static int dpToPx(Resources resources, int dp) {
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

}
