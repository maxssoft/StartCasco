package ru.telematica.casco2go.ui.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

import ru.telematica.casco2go.R;
import ru.telematica.casco2go.model.eventbus.*;
import ru.telematica.casco2go.model.TripData;
import ru.telematica.casco2go.utils.Animations;
import ru.telematica.casco2go.service.ScoringService;
import ru.telematica.casco2go.utils.DateUtils;


public class ProcessTripFragment extends BaseFragment {


    @BindView(R.id.gpsLevelText)
    TextView gpsLevelText;

    @BindView(R.id.tripTimerText)
    TextView tripTimerText;

    @BindView(R.id.finish_button)
    View finishButton;

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
            Animations.startPressedTripAnim(v, () -> finishClick());
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.post(updateTripInfo);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(updateTripInfo);
    }

    private static final int UPDATE_INTERVAL = 5000;
    private Runnable updateTripInfo = new Runnable() {
        @Override
        public void run() {
            if (isDetached()){
                return;
            }

            TripData tripData = ScoringService.getTripData();
            gpsLevelText.setText(tripData.getGpsLevel() + "%");
            gpsLevelText.setTextColor(tripData.getGpsLevel() > 90 ? getResources().getColor(R.color.green) : getResources().getColor(R.color.error));

            tripTimerText.setText(DateUtils.timerFormatter.format(tripData.getTripTime()));

            handler.postDelayed(updateTripInfo, UPDATE_INTERVAL);
        }
    };

    private void finishClick(){
        EventBus.getDefault().post(new FinishTripEvent());
    }

}
