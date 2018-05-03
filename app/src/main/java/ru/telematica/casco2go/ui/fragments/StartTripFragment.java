package ru.telematica.casco2go.ui.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.telematica.casco2go.R;
import ru.telematica.casco2go.model.eventbus.StartTripEvent;
import ru.telematica.casco2go.service.ScoringService;
import ru.telematica.casco2go.utils.Animations;
import ru.telematica.casco2go.utils.ConfigPreferences;
import ru.telematica.casco2go.utils.Permissions;

public class StartTripFragment extends BaseFragment {


    @BindView(R.id.carPriceEdit)
    EditText carPriceEdit;

    @BindView(R.id.start_button)
    View startButton;

    @BindView(R.id.progressImage)
    ImageView progressImage;

    public StartTripFragment() {
        // Required empty public constructor
    }

    public static StartTripFragment newInstance() {
        return new StartTripFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        int carPrice = ConfigPreferences.INSTANCE.readCarPrice(getContext());
        if (carPrice > 0){
            carPriceEdit.setText(String.valueOf(carPrice));
        }

        startButton.setOnClickListener(v -> {
            Animations.startPressedTripAnim(v, () -> startClick());
        });
    }

    private void startClick(){
        if (!Permissions.INSTANCE.checkLocation()) {
            Permissions.INSTANCE.requestLocation(getActivity());
            return;
        }
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Location service disabled");
            builder.setMessage("The app needs location service. Please enable gps location to continue using the features of the app.");
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            builder.setNegativeButton(android.R.string.no, null);
            builder.show();
            return;
        }

        try {
            int carPrice = PriceValidator.validate(getContext(), carPriceEdit.getEditableText().toString());

            ConfigPreferences.INSTANCE.writeCarPrice(getContext(), carPrice);

            progressImage.setVisibility(View.VISIBLE);
            Animations.startRotateAnimation(progressImage);

            EventBus.getDefault().post(new StartTripEvent(carPrice));
        } catch (Exception e){
            showError(e.getMessage());
        }
    }

    private static class PriceValidator {

        public static int validate(Context context, String value) throws Exception {
            if (value.isEmpty()){
                throw new Exception(context.getString(R.string.error_price_is_empty));
            }
            int price = 0;
            try {
                price = Integer.parseInt(value.replace(" ", ""));
            } catch (Exception e){
                throw new Exception(context.getString(R.string.error_price_is_invalid));
            }
            if (price < 5000){
                throw new Exception(context.getString(R.string.error_price_is_low));
            }
            return price;
       }
    }

}
