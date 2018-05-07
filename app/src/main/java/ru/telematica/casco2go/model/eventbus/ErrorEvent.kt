package ru.telematica.casco2go.model.eventbus

import ru.telematica.casco2go.utils.isNull

/**
 * Created by m.sidorov on 29.04.2018.
 */
class ErrorEvent(val message: String, val error: Throwable?) {

    constructor(message: String) : this(message, error = null)
    constructor(error: Throwable) : this(message = "", error = error)

}