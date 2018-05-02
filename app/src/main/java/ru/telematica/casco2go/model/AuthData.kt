package ru.telematica.casco2go.model

/**
 * Created by m.sidorov on 02.05.2018.
 */
class AuthData {

    var userId: Long = 0
    var sessionID: Long = 0
    var token: String = ""
    var TimeZone: String = ""

    fun hasAuth(): Boolean{
        return sessionID > 0
    }
}