package ru.telematica.casco2go.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

import ru.telematica.casco2go.R;
import ru.telematica.casco2go.model.eventbus.*;
import ru.telematica.casco2go.model.TripData;
import ru.telematica.casco2go.utils.Animations;
import ru.telematica.casco2go.service.ScoringService;
import ru.telematica.casco2go.utils.DateUtils;


public class ProcessTripFragment extends BaseFragment {


    @BindView(R.id.tripTimerText)
    TextView tripTimerText;

    @BindView(R.id.finish_button)
    View finishButton;

    @BindView(R.id.progressImage)
    ImageView progressImage;


    private Handler handler = new Handler();

    public ProcessTripFragment() {
        // Required empty public constructor
    }

    public static ProcessTripFragment newInstance() {
        return new ProcessTripFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trip, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        finishButton.setOnClickListener(v -> {
            finishClick();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        startHandler();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopHandler();
    }

    private static final int UPDATE_INTERVAL = 1000;
    private boolean handlerStopped = false;

    private void startHandler(){
        handlerStopped = false;
        handler.post(updateTripInfo);
    }

    private void stopHandler(){
        handlerStopped = true;
        handler.removeCallbacks(updateTripInfo);
    }


    private Runnable updateTripInfo = new Runnable() {
        @Override
        public void run() {
            if (isDetached() || handlerStopped){
                return;
            }

            TripData tripData = ScoringService.getTripData();

            Date timeDate = new Date(tripData.getTripTime());
            String timeS = DateUtils.INSTANCE.getTimerFormatter().format(timeDate);
            tripTimerText.setText(timeS);

/*
            if (tripData.getGpsLevel() < 90){
                stopHandler();
                EventBus.getDefault().post(new FirstScreenEvent());
                EventBus.getDefault().post(new ErrorEvent(getString(R.string.error_low_gps), null));
                return;
            }

*/
            if (tripData.getTripTime() > ScoringService.Companion.getMAX_TRIP_TIME()){
                stopHandler();
                finishClick();
                return;
            }

            handler.postDelayed(updateTripInfo, UPDATE_INTERVAL);
        }
    };

    private void finishClick(){
        Animations.startPressedTripAnim(finishButton, () -> finishTrip());
    }

    private void finishTrip(){
        stopHandler();

        progressImage.setVisibility(View.VISIBLE);
        Animations.startRotateAnimation(progressImage);

        EventBus.getDefault().post(new FinishTripEvent());
    }

}
