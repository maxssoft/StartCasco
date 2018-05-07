package ru.telematica.casco2go.model.response

import com.google.gson.annotations.SerializedName

/**
 * Created by m.sidorov on 05.05.2018.
 */
class HistoryResponse {

    @SerializedName("Total")
    val total: Int = 0
    @SerializedName("Data")
    val data: List<Journey> = ArrayList<Journey>()

}