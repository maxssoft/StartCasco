package ru.telematica.casco2go.utils;

import android.os.Build;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/**
 * Created by m.sidorov on 29.04.2018.
 */

public class Animations {

    public static void startRotateAnimation(ImageView view){
        RotateAnimation anim = new RotateAnimation(0.0f, 360.0f ,
                Animation.RELATIVE_TO_SELF, .5f, Animation.RELATIVE_TO_SELF, .5f);
        anim.setInterpolator(new LinearInterpolator());
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(1000);
        view.setAnimation(anim);
        view.startAnimation(anim);
    }

    public static void startPressedElevationAnim(final View view, final Runnable finishRunnable){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            final float z = view.getZ();
            view.animate().translationZ(-z).setDuration(150).withEndAction(new Runnable() {
                @Override
                public void run() {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        view.animate().translationZ(z).setDuration(50).withEndAction(finishRunnable).start();
                    } else {
                        finishRunnable.run();
                    }
                }
            }).start();
        } else {
            finishRunnable.run();
        }
    }

    public static void startPressedScaleAnim(final View view, final Runnable finishRunnable){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            final float offset = 0.05f;
            view.animate().scaleYBy(-offset).scaleXBy(-offset).setDuration(150).withEndAction(new Runnable() {
                @Override
                public void run() {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        view.animate().scaleYBy(offset).scaleXBy(offset).setDuration(50).withEndAction(finishRunnable).start();
                    } else {
                        finishRunnable.run();
                    }
                }
            }).start();
        } else {
            finishRunnable.run();
        }
    }

    public static void startPressedTripAnim(final View view, final Runnable finishRunnable){
        startPressedScaleAnim(view, finishRunnable);
    }

}
