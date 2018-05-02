package ru.telematica.casco2go.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit;

/**
 * Created by m.sidorov on 29.04.2018.
 */
object DateUtils {

    val DATETIME_VIEW_FORMAT = "dd.MM.yy, HH:mm"
    val DATE_VIEW_FORMAT = "dd.MMMM.yyyy"
    val TIME_VIEW_FORMAT = "mm:ss"

    val timerFormatter = SimpleDateFormat(TIME_VIEW_FORMAT).apply { timeZone = TimeZone.getTimeZone("GMT+0") }
}