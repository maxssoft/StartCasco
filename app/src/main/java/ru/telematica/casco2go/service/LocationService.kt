package ru.telematica.casco2go.service

import android.content.Context
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import android.os.Bundle
import android.os.Looper
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import org.greenrobot.eventbus.EventBus
import ru.telematica.casco2go.model.TripData
import ru.telematica.casco2go.model.eventbus.*
import ru.telematica.casco2go.service.JourneyService.JourneyDataSender
import ru.telematica.casco2go.utils.isNull


/**
 * Created by m.sidorov on 02.05.2018.
 */
class LocationService(val context: Context) : GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    val TAG: String = this::class.java.simpleName

    private val lock: Any = Any()

    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLocationCallback: LocationCallback? = null
    private var locationProviderClient: FusedLocationProviderClient? = null
    private var mLocationUpdatesRequested: Boolean = false
    private var mJourneyDataStarted: Boolean = false
    private var mFirstCallback: Boolean = false
    private var mLocationRequest: LocationRequest? = null

    private val START_LOCATION_COUNT = 15 // В начале в течение 15 секунд искусственно завышаем показатели GPS
    private var locationCount: Int = START_LOCATION_COUNT
    private var startLocationTime: Long = 0

    private var tripData: TripData? = null

    private fun getDurationSec(): Int {
        if (!mJourneyDataStarted){
            throw IllegalStateException("Location service not started")
        }
        var durationSec = ((System.currentTimeMillis() - startLocationTime) / 1000).toInt()
        if (durationSec <= 0){
            durationSec = 1
        }
        return durationSec
    }

    @SuppressWarnings("MissingPermission")
    fun open(){
        Log.d(TAG, "open()")
        synchronized(lock){
            mJourneyDataStarted = false;
            mFirstCallback = true;
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build()
            createLocationRequest()
        }
        createLocationCallback()
        locationProviderClient = LocationServices.getFusedLocationProviderClient(context)

        mGoogleApiClient?.connect()
    }

    @SuppressWarnings("MissingPermission")
    fun start(tripData: TripData){
        Log.d(TAG, "start()")
        synchronized(lock){
            this.tripData = tripData
            mJourneyDataStarted = true;
            locationCount = START_LOCATION_COUNT
            startLocationTime = System.currentTimeMillis()
        }
    }

    @SuppressWarnings("MissingPermission")
    fun stop(){
        Log.d(TAG, "stop()")
        synchronized(lock){
            mJourneyDataStarted = false;

            stopLocationUpdates()
            if (mGoogleApiClient?.isConnected.isNull(false)) {
                mGoogleApiClient?.disconnect()
            }
        }
    }

    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                Log.d(TAG, "locationCallback() " + locationResult?.lastLocation.toString())

                synchronized(lock) {
                    if (mFirstCallback){
                        mFirstCallback = false
                        EventBus.getDefault().post(LocationConnectedEvent(true, locationResult?.lastLocation))
                    }
                    if (mJourneyDataStarted) {
                        locationCount++
                        var gpsLevel = (locationCount * 100) / getDurationSec()
                        if (gpsLevel > 100){
                            gpsLevel = 100
                        }
                        tripData?.gpsLevel = gpsLevel
                        JourneyDataSender.getInstance().sendJourneyData(locationResult?.lastLocation, 0.0f, 0.0f)
                    }
                }
            }
        }
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest?.setInterval(900)
        mLocationRequest?.setFastestInterval(500)
        mLocationRequest?.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    @SuppressWarnings("MissingPermission")
    protected fun startLocationUpdates() {
        // Если уже были подписаны на обновления - сначала отписываемся
        stopLocationUpdates()
        locationProviderClient?.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
        mLocationUpdatesRequested = true
    }

    @SuppressWarnings("MissingPermission")
    protected fun stopLocationUpdates() {
        if (mLocationUpdatesRequested) {
            Log.d(TAG, "removeLocationUpdates()")
            locationProviderClient?.removeLocationUpdates(mLocationCallback)
        }
        JourneyDataSender.getInstance().stopDataTransfer()
    }

    override fun onConnected(bundle: Bundle?) {
        Log.d(TAG, "onConnected()")
        startLocationUpdates()
    }
    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed()")
        synchronized(lock) {
            if (mFirstCallback){
                mFirstCallback = false
                EventBus.getDefault().post(LocationConnectedEvent(false, null))
            }
        }
    }


}