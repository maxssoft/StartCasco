package ru.telematica.casco2go.model.response

import com.google.gson.annotations.SerializedName

/**
 * Created by m.sidorov on 05.05.2018.
 */
class Journey {
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
    @SerializedName("StartTime")
    val startTime: String? = null // "2018-05-06T10:20:18Z"
    @SerializedName("FinishTime")
    val finishTime: String? = null
    @SerializedName("UTSFrom")
    val utsFromSec: Long? = null
    @SerializedName("UTSTo")
    val utsToSec: Long? = null

}