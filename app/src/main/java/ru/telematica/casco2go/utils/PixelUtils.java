package ru.telematica.casco2go.utils;


import android.util.DisplayMetrics;

import ru.telematica.casco2go.App;

public class PixelUtils {
    public static int dpToPx(int dp) {
        return Math.round(dp * getPixelScaleFactor());
    }

    public static int getScreenWidth() {
        return App.getInstance().getResources().getDisplayMetrics().widthPixels;
    }

    public static int getPixelSize(int dimenId) {
        if (dimenId <= 0) {
            return 0;
        }
        return App.getInstance().getResources().getDimensionPixelSize(dimenId);
    }

    private static float getPixelScaleFactor() {
        DisplayMetrics displayMetrics = App.getInstance().getResources().getDisplayMetrics();
        return (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static int getColumnWidth(int colCount, int horzOffset, int colSpace) {
        return (getScreenWidth() - (horzOffset * 2) - (colSpace * (colCount - 1))) / colCount;
    }

}
