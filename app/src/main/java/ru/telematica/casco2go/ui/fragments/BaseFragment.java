package ru.telematica.casco2go.ui.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import ru.telematica.casco2go.ui.base.BaseView;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by alexander on 9/25/17.
 */

public abstract class BaseFragment extends Fragment implements BaseView {

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void showError(String message, Throwable error) {
        if (getActivity() == null) {
            return;
        }
        if (getActivity() instanceof BaseView){
            ((BaseView) getActivity()).showError(message, error);
        } else {
            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void hideKeyboard() {
        if (getActivity() == null) {
            return;
        }
        if (getActivity() instanceof BaseView){
            ((BaseView) getActivity()).hideKeyboard();
        } else {
            View view = getActivity().getCurrentFocus();
            if (view == null)
                return;
            InputMethodManager mImm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            mImm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected void showKeyboard(View view) {
        if (getActivity() == null){
            return;
        }
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    public void closeFragment() {
        getActivity().getSupportFragmentManager().popBackStack();
    }
}
