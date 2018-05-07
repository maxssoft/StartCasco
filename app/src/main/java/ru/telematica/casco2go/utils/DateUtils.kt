package ru.telematica.casco2go.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit;

/**
 * Created by m.sidorov on 29.04.2018.
 */
object DateUtils {

    val DATETIME_VIEW_FORMAT = "dd.MM.yy, HH:mm"
    val DATETIME_SERVER_FORMAT = "yyyy-MM-dd HH:mm:ss"
    val DATE_VIEW_FORMAT = "dd.MMMM.yyyy"
    val TIME_VIEW_FORMAT = "mm:ss"

    val timerFormatter = SimpleFormatter(TIME_VIEW_FORMAT).apply { timeZone = TimeZone.getTimeZone("GMT+0") }

    val serverFormatter = SeverDateFormatter()
}

interface DateFormatter {

    fun formatDate(date: Date): String
    fun parseDate(value: String): Date
}

class SimpleFormatter(template: String) : SimpleDateFormat(template), DateFormatter {

    override fun formatDate(date: Date): String {
        return format(date)
    }

    override fun parseDate(value: String): Date {
        return parse(value)
    }
}

class SeverDateFormatter : DateFormatter {

    private val formatter = SimpleDateFormat(DateUtils.DATETIME_SERVER_FORMAT)

    override fun formatDate(date: Date): String {
        val s = formatter.format(date)
        return s.replace(" ", "T") + "Z"
    }

    override fun parseDate(value: String): Date {
        var dateS = value.replace("T", " ")
        dateS = dateS.substringBefore("Z", dateS)
        return formatter.parse(dateS)
    }
}