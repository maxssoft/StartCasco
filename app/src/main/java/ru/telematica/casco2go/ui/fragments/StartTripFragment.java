package ru.telematica.casco2go.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.telematica.casco2go.R;
import ru.telematica.casco2go.model.eventbus.StartTripEvent;
import ru.telematica.casco2go.utils.Animations;

public class StartTripFragment extends BaseFragment {


    @BindView(R.id.carPriceEdit)
    EditText carPriceEdit;

    @BindView(R.id.start_button)
    View startButton;

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

        startButton.setOnClickListener(v -> {
            Animations.startPressedTripAnim(v, () -> startClick());
        });
    }

    private void startClick(){
        try {
            int price = PriceValidator.validate(getContext(), carPriceEdit.getEditableText().toString());
            EventBus.getDefault().post(new StartTripEvent(price));
        } catch (Exception e){
            showError(e.getMessage());
        }
    }

    private static class PriceValidator {

        public static int validate(Context context, String value) throws Exception {
            if (value.isEmpty()){
                throw new Exception(context.getString(R.string.error_price_is_empty));
            }
            try {
                return Integer.parseInt(value.replace(" ", ""));
            } catch (Exception e){
                throw new Exception(context.getString(R.string.error_price_is_invalid));
            }
       }
    }

}
