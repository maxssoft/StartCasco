package ru.telematica.casco2go.service.http

import android.util.Log
import io.reactivex.Single
import ru.telematica.casco2go.App
import ru.telematica.casco2go.model.AuthData
import ru.telematica.casco2go.model.ScoringData
import ru.telematica.casco2go.model.request.CreateTokenRequest
import ru.telematica.casco2go.model.request.RefreshTokenRequest
import ru.telematica.casco2go.model.response.*
import ru.telematica.casco2go.repository.ConfigRepository
import ru.telematica.casco2go.service.ScoringService
import ru.telematica.casco2go.utils.DateUtils
import ru.telematica.casco2go.utils.isNull
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by m.sidorov on 29.04.2018.
 */
class HttpService(val apiService: TelematicaApi) {

    private val TAG = HttpService::class.java.simpleName

    fun createAccessToken(request: CreateTokenRequest): CreateAccessTokenResponse {
        val response = apiService.createToken(request).execute()
        if (!response.isSuccessful || response.body() == null){
            Log.e(TAG, "error of createAccessToken(), errorCode = ${response.code()}")
            throw RuntimeException("Server auth response error, error code [${response.code()}]")
        }
        return response.body().isNull(CreateAccessTokenResponse())
    }

    fun refreshAccessToken(request: RefreshTokenRequest): CreateAccessTokenResponse {
        val response = apiService.refreshToken(request).execute()
        if (!response.isSuccessful || response.body() == null){
            Log.e(TAG, "error of createAccessToken(), errorCode = ${response.code()}")
            throw RuntimeException("Server auth response error, error code [${response.code()}]")
        }
        return response.body().isNull(CreateAccessTokenResponse())
    }

    private fun getAuthHeader(): String{
        if (!ConfigRepository.authData.isExpired()) {
            return ConfigRepository.authData.getAuthHeader()
        } else {
            return ""
        }
    }

    fun getTimeZone(latitude: Double, longtitude: Double): String {
        val response = apiService.getTimeZone(getAuthHeader(), latitude, longtitude).execute()
        if (!response.isSuccessful || response.body() == null || response.body()?.code != 1) {
            if (response.body() != null && response.body()?.code != 1) {
                Log.e(TAG, "error of getTimeZone(), code = ${response.body()?.code}")
                throw RuntimeException("Server getTimeZone response error, error code [${response.body()?.code}]")
            } else {
                Log.e(TAG, "error of getTimeZone(), errorCode = ${response.code()}")
                throw RuntimeException("Server getTimeZone response error, error code [${response.code()}]")
            }
        }

        val timeZone = response.body()?.timeZone.isNull("")
        return timeZone
    }

    fun startTrip(latitude: Double, longtitude: Double, maxDuraton: Int, timeZone: String): Single<StartTripResponse> {
        return Single.fromCallable {
            val response = apiService.startTrip(getAuthHeader(), latitude, longtitude, maxDuraton, timeZone, ConfigRepository.authData.sessionID).execute()
            if (!response.isSuccessful || response.body() == null || response.body()?.code != 1) {
                if (response.body() != null && response.body()?.code != 1) {
                    Log.e(TAG, "error of startTrip(), code = ${response.body()?.code}")
                    throw RuntimeException("Server startTrip response error, error code [${response.body()?.code}]")
                } else {
                    Log.e(TAG, "error of startTrip(), errorCode = ${response.code()}")
                    throw RuntimeException("Server startTrip response error, error code [${response.code()}]")
                }
            }

            response.body()
        }
    }

    fun finishTrip(): Single<ScoringData> {
        return Single.fromCallable {
            val response = apiService.finishTrip(getAuthHeader()).execute()
            if (!response.isSuccessful || response.body() == null || response.body()?.code != 1){
                if (response.body() != null && response.body()?.code != 1) {
                    Log.e(TAG, "error of startTrip(), code = ${response.body()?.code}")
                    throw RuntimeException("Server finishTrip response error, error code [${response.body()?.code}]")
                } else {
                    Log.e(TAG, "error of startTrip(), errorCode = ${response.code()}")
                    throw RuntimeException("Server finishTrip response error, error code [${response.code()}]")
                }
            }
            journeyToScoring(response.body()?.journey.isNull(Journey()))
        }
    }

    fun loadHistory(lastScoringData: ScoringData?): Single<List<ScoringData>>{
        var paramDateTime : String = lastScoringData?.startTimeS.isNull("")
        if (paramDateTime.isBlank()){
            paramDateTime = DateUtils.serverFormatter.formatDate(Date())
        }

        return Single.fromCallable{
            val response = apiService.loadHistory(getAuthHeader(), paramDateTime).execute()
            if (!response.isSuccessful || response.body() == null){
                Log.e(TAG, "error of loadHistory(), errorCode = ${response.code()}")
                throw RuntimeException("Server loadHistory response error, error code [${response.code()}]")
            }

            val result = ArrayList<ScoringData>()
            val list = response.body()?.data.isNull(ArrayList<Journey>())
            for (journey in list){
                result.add(journeyToScoring(journey))
            }
            result.apply {
                sortWith( compareBy { it.startTime } )
            }
        }
    }

    private fun journeyToScoring(journey: Journey): ScoringData {
        return ScoringData().apply {
            timeTripSec = journey.duration.isNull(0)
            timeInTrafficSec = journey.trafficJamDuration.isNull(0)
            timeInTravelSec = timeTripSec - timeInTrafficSec
            drivingLevel = journey.scorePercentKm.isNull(0f).toInt()
            val cost = ((timeTripSec / 60) * getOneMinuteCost()).toFloat()
            tripCost = cost - cost * getDiscount() / 100f

            startTimeS =journey.startTime
            if (!startTimeS.isNull("").isBlank()) {
                startTime = DateUtils.serverFormatter.parseDate(startTimeS.isNull(""))
            }
            if (!journey.finishTime.isNull("").isBlank()) {
                finishTime = DateUtils.serverFormatter.parseDate(journey.finishTime.isNull(""))
            }
            if (timeTripSec < ScoringService.MIN_TRIP_TIME_SEC) {
                drivingLevel = 0
            }
        }
    }

}