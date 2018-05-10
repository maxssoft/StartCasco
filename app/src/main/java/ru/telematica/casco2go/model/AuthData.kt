package ru.telematica.casco2go.model

/**
 * Created by m.sidorov on 02.05.2018.
 */
class AuthData {

    var userId: Long = 0
    var sessionID: Long = 0
    var token: String = ""
    var refreshToken: Long = 0
    var expiredDate: Long = 0

    fun getAuthHeader(): String {
        return "Bearer " + token
    }

    fun isExpired(): Boolean{
        return !token.isBlank() && System.currentTimeMillis() >= expiredDate - AuthDataConst.EXPIRE_OFFSET
    }

    fun hasAuth(): Boolean{
        return !token.isBlank()
    }

}

object AuthDataConst {
    private val ONE_HOUR: Long = 60 * 60 * 1000
    val MIN_EXPIRE_PERIOD: Long = 2 * ONE_HOUR
    val EXPIRE_OFFSET = ONE_HOUR
}