package ru.telematica.casco2go.model.response

import com.google.gson.annotations.SerializedName

/**
 * Created by m.sidorov on 01.05.2018.
 */
class StartTripResponse {

    @SerializedName("UTSNow")
    val utsNow: Long = 0;
    @SerializedName("ActiveJourney")
    val activeJourney: ActiveJourneyClass? = null
    @SerializedName("ActionCode")
    val actionCode: Int = 0
    @SerializedName("Code")
    val code: Int = 0

    inner class ActiveJourneyClass {
        @SerializedName("UnclosedID")
        val unclosedID: String? = null
        @SerializedName("UTSFrom")
        val utsFrom: Long = 0
        @SerializedName("UTSTo")
        val utsTo: Long = 0
    }

}