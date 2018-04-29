package ru.telematica.casco2go.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.drm.DrmErrorEvent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import org.greenrobot.eventbus.EventBus;

import ru.telematica.casco2go.R;
import ru.telematica.casco2go.model.eventbus.*;
import ru.telematica.casco2go.model.TripData;
import ru.telematica.casco2go.ui.activity.MainActivity;
import ru.telematica.casco2go.ui.fragments.FragmentTypes;

/**
 * Created by m.sidorov on 29.04.2018.
 */

public class ScoringService extends Service {

    private static final String TAG = ScoringService.class.getSimpleName();

    public static final int NOTIFICATION_ID = 18624;
    public static final String NOTIFICATION_CHANEL = "ru.telematica.scoring.service";

    private static boolean foregroundStarted = false;
    private static boolean scoringStarted = false;

    public static final String ACTION_START_TRIP = "ru.telematica.scoring.service.trip.start";
    public static final String ACTION_STOP_TRIP = "ru.telematica.scoring.service.trip.stop";
    public static final String ACTION_RELEASE = "ru.telematica.scoring.service.release";

    public static final String EXTRA_CAR_PRICE = "trip.start.car_price";

    private static TripData tripData = new TripData(0);
    private static synchronized void setTripData(TripData newData){
        tripData = newData;
    }

    public static synchronized TripData getTripData(){
        return tripData;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            String action = intent.getAction();
            if (action.equals(ACTION_START_TRIP)) {
                TripData newData = new TripData(intent.getIntExtra(EXTRA_CAR_PRICE, 5000));
                startScoring(newData);
            } else
            if (action.equals(ACTION_STOP_TRIP)) {
                stopScoring();
            } else
            if (action.equals(ACTION_RELEASE)) {
                releaseService();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void releaseService(){
        if (foregroundStarted){
            stopForeground(true);
            foregroundStarted = false;
        }
        scoringStarted = false;
        setTripData(new TripData(tripData.getCarPrice()));

        stopSelf();
    }

    private Notification createNotification(){
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //int iconId = Build.VERSION.SDK_INT >= 21 ? R.drawable.notification_icon_lolipop : R.drawable.ic_indicator;
        int iconId = R.drawable.ic_notification;
        return new NotificationCompat.Builder(this, NOTIFICATION_CHANEL)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.ticker_text))
                .setSmallIcon(iconId)
                .setContentIntent(contentIntent)
                .setWhen(System.currentTimeMillis())
                .build();
    }

    private void startScoring(TripData newTrip){
        if (!foregroundStarted) {
            startForeground(NOTIFICATION_ID, createNotification());
            foregroundStarted = true;
        }

        if (getTripData().isStarted()){
            processError(new IllegalStateException("trip already is started"));
            return;
        }
        setTripData(newTrip.start());
        scoringStarted = true;
        EventBus.getDefault().post(new OpenFragmentEvent(FragmentTypes.PROCESS_TRIP_FRAGMENT, false));
    }

    private void stopScoring(){
        if (foregroundStarted){
            stopForeground(true);
            foregroundStarted = false;
        }

        if (!getTripData().isStarted()){
            throw new IllegalStateException("trip has not been started");
        }
        if (getTripData().isFinished()){
            throw new IllegalStateException("trip already finished");
        }
        getTripData().finish();
        scoringStarted = false;
        EventBus.getDefault().post(new OpenFragmentEvent(FragmentTypes.FINISH_TRIP_FRAGMENT, false));
    }

    private void processError(Exception e){
        processError(e.getMessage(), e);
    }

    private void processError(String message, Exception e){
        releaseService();
        EventBus.getDefault().post(new OpenFragmentEvent(FragmentTypes.START_TRIP_FRAGMENT, false));
        EventBus.getDefault().post(new ErrorEvent(message, e));
    }


}
