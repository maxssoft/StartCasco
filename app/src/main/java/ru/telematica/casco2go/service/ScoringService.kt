package ru.telematica.casco2go.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import ru.telematica.casco2go.App
import ru.telematica.casco2go.R
import ru.telematica.casco2go.model.AuthData
import ru.telematica.casco2go.model.TripData
import ru.telematica.casco2go.model.eventbus.ErrorEvent
import ru.telematica.casco2go.model.eventbus.LocationConnectedEvent
import ru.telematica.casco2go.model.eventbus.OpenFragmentEvent
import ru.telematica.casco2go.ui.activity.MainActivity
import ru.telematica.casco2go.ui.fragments.FragmentTypes
import ru.telematica.casco2go.utils.getErrorMessage
import ru.telematica.casco2go.utils.isNull

/**
 * Фоновый сервис, который запускается при старте поездки, собирает gps данные и хранит данные поездки
 * Created by m.sidorov on 29.04.2018.
 */
class ScoringService : Service() {

    private val TAG = ScoringService::class.java.simpleName

    val NOTIFICATION_ID = 18624
    val NOTIFICATION_CHANEL = "ru.telematica.scoring.service"

    private var foregroundStarted = false
    private var scoringStarted = false

    private var subscriptions: CompositeDisposable = CompositeDisposable()

    companion object {

        val ACTION_CLEAR_TRIP = "ru.telematica.scoring.service.trip.clear"
        val ACTION_START_TRIP = "ru.telematica.scoring.service.trip.start"
        val ACTION_STOP_TRIP = "ru.telematica.scoring.service.trip.stop"
        val ACTION_RELEASE = "ru.telematica.scoring.service.release"

        val MIN_TRIP_TIME: Long = 5 * 60 * 1000
        val MAX_TRIP_TIME: Int = 30 * 60 * 1000

        val EXTRA_CAR_PRICE = "trip.start.car_price"

        @JvmStatic
        private var _tripData: TripData = TripData(5000)

        @JvmStatic
        @Synchronized
        fun getTripData(): TripData {
            return _tripData
        }

        @JvmStatic
        @Synchronized
        private fun setTripData(data: TripData) {
            _tripData = data
        }

        @JvmStatic
        val authData: AuthData = AuthData()
    }

    private lateinit var locationService: LocationService

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationService = LocationService(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action.isNull("");
            when (action) {
                ACTION_CLEAR_TRIP -> clearScoring()
                ACTION_START_TRIP -> {
                    val carPrice = intent.getIntExtra(EXTRA_CAR_PRICE, 5000)
                    startScoring(TripData(carPrice))
                }
                ACTION_STOP_TRIP -> stopScoring()
                ACTION_RELEASE -> releaseService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startScoring(newTrip: TripData) {
        disposeSubscriptions()

        if (!foregroundStarted) {
            startForeground(NOTIFICATION_ID, createNotification())
            foregroundStarted = true
        }

        EventBus.getDefault().register(this)
        locationService.open()
        setTripData(newTrip)
    }

    private fun stopScoring() {
        val tripData = getTripData()
        if (!tripData.started) {
            processErrorAndBreak(error = IllegalStateException("trip has not been started"))
            return
        }
        if (tripData.finished) {
            processErrorAndBreak(error = IllegalStateException("trip already finished"))
            return
        }

        subscriptions.add(
                App.instance.httpService.finishTrip()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (it.code != 1){
                        processErrorAndBreak(getString(R.string.error_server_unknown))
                    } else {
                        val journey = it.journey
                        val data = getTripData()
                        data.finish()
                        data.scoringData.apply {
                            timeTripSec = journey?.duration.isNull(0)
                            timeInTrafficSec = journey?.trafficJamDuration.isNull(0)
                            timeInTravelSec = timeTripSec - timeInTrafficSec
                            drivingLevel = journey?.scorePercentKm.isNull(0f).toInt()
                            val cost = ((timeTripSec / 60) * getOneMinuteCost()).toFloat()
                            tripCost = cost - cost * getDiscount() / 100f

                            // Обрабатываем граничные условия (мин.время поездки и минимальный ровень GPS)
                            if ((timeTripSec * 1000 < MIN_TRIP_TIME) || (data.gpsLevel < 90)) {
                                drivingLevel = 0
                                tripCost = 0.0f
                            }
                        }

                        stopService()
                        EventBus.getDefault().post(OpenFragmentEvent(FragmentTypes.FINISH_TRIP_FRAGMENT))
                    }
                }, { processErrorAndBreak(getString(R.string.error_server_unknown))})
        )
    }

    private fun clearScoring(){
        App.instance.httpService.finishTrip()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    stopService()
                    EventBus.getDefault().post(OpenFragmentEvent(FragmentTypes.START_TRIP_FRAGMENT))
                }, {
                    stopService()
                    EventBus.getDefault().post(OpenFragmentEvent(FragmentTypes.START_TRIP_FRAGMENT))
                })
    }

    private fun processErrorAndBreak(message: String = "", error: Throwable? = null) {
        clearScoring()
        EventBus.getDefault().post(ErrorEvent(message + "\n" + error.getErrorMessage(), error))
    }

    @Synchronized
    private fun disposeSubscriptions(){
        subscriptions.dispose()
        subscriptions = CompositeDisposable()
    }

    @Subscribe
    fun onEvent(connectedEvent: LocationConnectedEvent) {
        EventBus.getDefault().unregister(this)

        if (!connectedEvent.connected || connectedEvent.location == null){
            processErrorAndBreak(getString(R.string.error_location_connect))
            return
        }

        val location = connectedEvent.location
        subscriptions.add(
                Single.just(authData)
                        .map {
                            if (!it.hasAuth()) {
                                val response = App.instance.httpService.createAccessToken()
                                authData.apply {
                                    sessionID = response?.session.isNull(0)
                                    token = response?.accesstoken?.access_token.isNull("")
                                    userId = response?.accesstoken?.user_id_long.isNull(0)

                                    TimeZone = App.instance.httpService.getTimeZone(location.latitude, location.longitude)?.timeZone.isNull("")
                                }
                            } else {
                                authData
                            }
                        }
                        .flatMap {
                            App.instance.httpService.startTrip(location.latitude, location.longitude, MAX_TRIP_TIME, authData.TimeZone, authData.sessionID)
                        }
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            if (it.code != 1){
                                processErrorAndBreak(getString(R.string.error_server_unknown))
                            } else {
                                getTripData().start()
                                locationService.start(getTripData())
                                scoringStarted = true
                                EventBus.getDefault().post(OpenFragmentEvent(FragmentTypes.PROCESS_TRIP_FRAGMENT))
                            }
                        }, {
                            processErrorAndBreak(getString(R.string.error_server_unknown), it)
                        })
        )
    }

    @Synchronized
    private fun releaseService() {
        stopService()
        stopSelf()
        disposeSubscriptions()
    }

    @Synchronized
    private fun stopService(){
        EventBus.getDefault().unregister(this)
        if (foregroundStarted) {
            stopForeground(true)
            foregroundStarted = false
        }

        locationService.stop()
        scoringStarted = false
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        notificationIntent.action = Intent.ACTION_MAIN
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        val contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        //int iconId = Build.VERSION.SDK_INT >= 21 ? R.drawable.notification_icon_lolipop : R.drawable.ic_indicator;
        val iconId = R.drawable.ic_notification
        return NotificationCompat.Builder(this, NOTIFICATION_CHANEL)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.ticker_text))
                .setSmallIcon(iconId)
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .build()
    }

}