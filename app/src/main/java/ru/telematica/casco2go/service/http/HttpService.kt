package ru.telematica.casco2go.service.http

import android.util.Log
import io.reactivex.Single
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Query
import ru.telematica.casco2go.App
import ru.telematica.casco2go.R
import ru.telematica.casco2go.model.request.CreateTokenRequest
import ru.telematica.casco2go.model.response.*
import ru.telematica.casco2go.utils.isNull

/**
 * Created by m.sidorov on 29.04.2018.
 */
class HttpService(val apiService: TelematicaApi) {

    private val TAG = HttpService::class.java.simpleName

    private val AccessCompanyID = 1095
    private val AccessCompanySecret = "0a744d8afe0c912f8c77d710a2cb0e51"
    private var authHeader: String = ""

    fun createAccessToken(): CreateAccessTokenResponse? {
        val response = apiService.createToken(CreateTokenRequest(AccessCompanyID, AccessCompanySecret, true)).execute()
        if (!response.isSuccessful || response.body() == null){
            Log.e(TAG, "error of createAccessToken(), errorCode = ${response.code()}")
            throw RuntimeException("Server auth response error, error code [${response.code()}]")
        }
        authHeader = "Bearer " + response.body()?.accesstoken?.access_token.isNull("")
        return response.body()
    }

    fun getTimeZone(latitude: Double, longtitude: Double): StatusResponse? {
        val response = apiService.getTimeZone(authHeader, latitude, longtitude).execute()
        if (!response.isSuccessful || response.body() == null || response.body()?.code != 1) {
            if (response.body() != null && response.body()?.code != 1) {
                Log.e(TAG, "error of getTimeZone(), code = ${response.body()?.code}")
                throw RuntimeException("Server getTimeZone response error, error code [${response.body()?.code}]")
            } else {
                Log.e(TAG, "error of getTimeZone(), errorCode = ${response.code()}")
                throw RuntimeException("Server getTimeZone response error, error code [${response.code()}]")
            }
        }

        //val text = response.body()?.string()
        //return null
        return response.body();
    }

    fun startTrip(latitude: Double, longtitude: Double, maxDuraton: Int, timeZone: String, sessionId: Long): Single<StartTripResponse> {
        return Single.fromCallable {
            val response = apiService.startTrip(authHeader, latitude, longtitude, maxDuraton, timeZone, sessionId).execute()
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

    fun finishTrip(): Single<FinishTripResponse> {
        return Single.fromCallable {
            val response = apiService.finishTrip(authHeader).execute()
            if (!response.isSuccessful || response.body() == null || response.body()?.code != 1){
                if (response.body() != null && response.body()?.code != 1) {
                    Log.e(TAG, "error of startTrip(), code = ${response.body()?.code}")
                    throw RuntimeException("Server finishTrip response error, error code [${response.body()?.code}]")
                } else {
                    Log.e(TAG, "error of startTrip(), errorCode = ${response.code()}")
                    throw RuntimeException("Server finishTrip response error, error code [${response.code()}]")
                }
            }
            response.body()
        }
    }
}