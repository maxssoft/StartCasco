package ru.telematica.casco2go.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by m.sidorov on 29.04.2018.
 */

public class DateUtils {

    public static final String DATETIME_VIEW_FORMAT = "dd.MM.yy, HH:mm";
    public static final String DATE_VIEW_FORMAT = "dd.MMMM.yyyy";
    public static final String TIME_VIEW_FORMAT = "HH:mm";

    public static final SimpleDateFormat timerFormatter = new SimpleDateFormat(TIME_VIEW_FORMAT);

}
