package ru.telematica.casco2go.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import io.reactivex.Single
import io.reactivex.Completable
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
import ru.telematica.casco2go.utils.isNull
import android.content.Context.NOTIFICATION_SERVICE
import android.app.NotificationManager
import android.content.Context


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

        val MIN_TRIP_TIME_SEC: Long = 5 * 60
        val MAX_TRIP_TIME_SEC: Int = 30 * 60

        @JvmStatic
        private var _tripData: TripData = TripData()

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
        private var timeZone: String = ""
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
                    startScoring()
                }
                ACTION_STOP_TRIP -> stopScoring()
                ACTION_RELEASE -> releaseService()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startScoring() {
        disposeSubscriptions()

        if (!foregroundStarted) {
            startForeground(NOTIFICATION_ID, createNotification())
            foregroundStarted = true
        }

        EventBus.getDefault().register(this)
        setTripData(TripData())
        locationService.open()
    }

    private fun stopScoring() {
        val tripData = getTripData()
        if (!tripData.started) {
            processErrorAndBreak("trip has not been started")
            return
        }
        if (tripData.finished) {
            processErrorAndBreak("trip already finished")
            return
        }

        subscriptions.add(
                App.instance.httpService.finishTrip()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val data = getTripData()
                    // Обрабатываем граничные условия (минимальный ровень GPS)
                    if (data.gpsLevel < 90) {
                        it.drivingLevel = 0
                    }
                    data.finish(it)

                    stopService()
                    EventBus.getDefault().post(OpenFragmentEvent(FragmentTypes.FINISH_TRIP_FRAGMENT))
                }, { processErrorAndBreak(getString(R.string.error_server_unknown), it)})
        )
    }

    private fun clearScoring(){
        App.instance.httpService.finishTrip()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    setTripData(TripData())
                    stopService()
                    EventBus.getDefault().post(OpenFragmentEvent(FragmentTypes.START_TRIP_FRAGMENT))
                }, {
                    setTripData(TripData())
                    stopService()
                    EventBus.getDefault().post(OpenFragmentEvent(FragmentTypes.START_TRIP_FRAGMENT))
                })
    }

    private fun processErrorAndBreak(message: String, error: Throwable? = null) {
        clearScoring()
        EventBus.getDefault().post(ErrorEvent(message, error))
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
                Completable
                        .fromAction {
                            if (timeZone.isBlank()) {
                                timeZone = App.instance.httpService.getTimeZone(location.latitude, location.longitude)
                            }
                        }
                        .andThen( App.instance.httpService.startTrip(location.latitude, location.longitude, MAX_TRIP_TIME_SEC, timeZone) )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            getTripData().start()
                            locationService.start(getTripData())
                            scoringStarted = true
                            EventBus.getDefault().post(OpenFragmentEvent(FragmentTypes.PROCESS_TRIP_FRAGMENT))
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

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val mChannel = NotificationChannel(
                    NOTIFICATION_CHANEL, getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(mChannel)
        }
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