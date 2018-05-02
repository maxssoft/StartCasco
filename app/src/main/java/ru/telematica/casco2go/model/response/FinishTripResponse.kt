package ru.telematica.casco2go.model.response

import com.google.gson.annotations.SerializedName

/**
 * Created by m.sidorov on 01.05.2018.
 */
class FinishTripResponse {

    @SerializedName("ActionCode")
    val actionCode: Int = 0
    @SerializedName("Code")
    val code: Int = 0
    @SerializedName("Journey")
    val journey: Journey? = null

    inner class Journey {
        @SerializedName("ID")
        val id: String? = null
        @SerializedName("ScorePercentTotal")
        val scorePercentTotal: Float? = null
        @SerializedName("ScorePercentKm")
        val scorePercentKm: Float? = null
        @SerializedName("Duration")
        val duration: Int? = null
        @SerializedName("TrafficJamDuration")
        val trafficJamDuration: Int? = null
    }
}