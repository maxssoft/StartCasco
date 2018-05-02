package ru.telematica.casco2go.utils

/**
 * Created by m.sidorov on 29.04.2018.
 */

fun <T> T?.isNull(block: () -> T): T {
    return this?: block()
}

fun <T> T?.isNull(emptyValue: T): T {
    return this?: emptyValue
}

fun <T : Throwable> T?.getErrorMessage(): String {
    if (this == null)
        return ""
    else
        return this.message.isNull("")
}

