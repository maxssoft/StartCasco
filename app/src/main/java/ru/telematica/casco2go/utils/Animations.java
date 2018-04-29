package ru.telematica.casco2go.utils;

import android.os.Build;
import android.view.View;

/**
 * Created by m.sidorov on 29.04.2018.
 */

public class Animations {

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
            final float offset = 8;
            view.animate().scaleYBy(offset).scaleYBy(offset).setDuration(150).withEndAction(new Runnable() {
                @Override
                public void run() {
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        view.animate().scaleY(offset).scaleYBy(offset).setDuration(50).withEndAction(finishRunnable).start();
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
