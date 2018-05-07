package ru.telematica.casco2go.model.request

/**
 * Created by m.sidorov on 02.05.2018.
 */
class CreateTokenRequest(val CompanyID: Int = 0, val CompanySecret: String = "", val silentMode: Boolean = true) {
}

class RefreshTokenRequest(val CompanyID: Int = 0, val CompanySecret: String = "", val code: Int = 0, val phone: String = "") {
}