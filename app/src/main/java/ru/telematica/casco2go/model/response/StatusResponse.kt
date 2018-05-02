package ru.telematica.casco2go.model.response

import com.google.gson.annotations.SerializedName

/**
 * Created by m.sidorov on 02.05.2018.
 */
class StatusResponse {

    @SerializedName("TimeZone")
    val timeZone: String? = null

    @SerializedName("ActionCode")
    val actionCode: Int? = null

    @SerializedName("Code")
    val code: Int? = null

    @SerializedName("Info")
    val info: String? = null

}